"""
Uses the Gemini API. Set GEMINI_API_KEY before running.

Usage:
  python quizgen.py notes.pdf
  python quizgen.py notes.pdf --type mcq/short/fill/truefalse
  python quizgen.py notes.pdf --difficulty easy/intermediate/hard

  (for testing)
  python quizgen.py notes.pdf --questions-per-chunk 2  # fewer questions
  python quizgen.py notes.pdf --max-chars 1500         # smaller chunks (faster)
"""
from extractor import extract_text, chunk_text, remove_acknowledgement
from generators import (
    generate_mcq,
    generate_short_answer,
    generate_fill_blank,
    generate_true_false,
    DIFFICULTY_LEVELS,
)

import csv
import json
import argparse
import concurrent.futures

import pandas as pd

# removes questions that ended up with the exact same answer as an
# earlier question — usually a sign the model re-asked the same fact
# from a different chunk.
def remove_duplicate(questions):
    seen = set()
    unique = []
    for q in questions:
        # Dedupe by the question text itself, not the answer. Answer-based
        # dedup silently collapsed anything sharing an answer value — for
        # True/False (only "true"/"false" possible) this meant every
        # question after the first true and first false got thrown away
        # regardless of what it actually asked.
        question_key = q.get("question", "").lower().strip()
        if not question_key or question_key in seen:
            continue
        seen.add(question_key)
        unique.append(q)
    return unique

# function to convert the generated text into csv and json
def save_outputs(questions: list[dict]):
    df = pd.DataFrame(questions)

    if "options" in df.columns:
        options_expanded = df["options"].apply(
            lambda x: pd.Series(x) if isinstance(x, dict) else pd.Series()
        )
        options_expanded.columns = [f"option_{c}" for c in options_expanded.columns]
        df = pd.concat([df.drop(columns=["options"]), options_expanded], axis=1)

    # conversion to csv
    df.to_csv(
        "generated_questions.csv",
        index=False,
        encoding="utf-8-sig",
        quotechar='"',
        quoting=csv.QUOTE_ALL
    )
    # json
    with open("generated_questions.json", "w", encoding="utf-8") as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)

def generate_questions(
    path: str,
    question_type: str,
    max_chars: int = 1500,
    questions_per_chunk: int = 3,
    difficulty: str = "intermediate",
    num_questions: int | None = None,
    max_workers: int = 1,
    verify: bool = True
):
    if difficulty not in DIFFICULTY_LEVELS:
        difficulty = "intermediate"

    # extract text from document, then filter out acknowledgement/publisher
    # boilerplate so questions aren't generated from front-matter, then
    # split into chunks to be processed
    print(f"Extracting text from: {path}")
    text = extract_text(path)
    text = remove_acknowledgement(text)
    chunks = chunk_text(text, max_chars)

    # If the document doesn't have enough chunks to hit the target at the
    # default rate (e.g. a short doc split into 2 chunks x 3/chunk = 6,
    # but the user asked for 15), ask for more per chunk instead of
    # silently under-delivering. Capped so a single prompt never asks for
    # an unreasonable number of questions at once.
    MAX_PER_CHUNK = 8
    if num_questions is not None and chunks:

        # If even the capped per-chunk rate can't reach the target with the
        # chunks we have, there simply aren't enough chunks — split the
        # document more finely so there's enough capacity, as long as
        # there's enough underlying text to do so.
        if len(chunks) * MAX_PER_CHUNK < num_questions:
            target_chunk_count = -(-num_questions // MAX_PER_CHUNK)  # ceil
            smaller_max_chars = max(400, len(text) // target_chunk_count)
            if smaller_max_chars < max_chars:
                print(
                    f"Document too short for {len(chunks)} chunk(s) to reach "
                    f"{num_questions} question(s) even at max per-chunk rate; "
                    f"splitting more finely (max_chars {max_chars} -> {smaller_max_chars})."
                )
                chunks = chunk_text(text, smaller_max_chars)

        required_per_chunk = -(-num_questions // len(chunks))  # ceil division
        if required_per_chunk > questions_per_chunk:
            boosted = min(required_per_chunk, MAX_PER_CHUNK)
            print(
                f"Only {len(chunks)} chunk(s) available for a target of "
                f"{num_questions}; raising questions-per-chunk from "
                f"{questions_per_chunk} to {boosted}."
            )
            questions_per_chunk = boosted

    print(
        f"{len(chunks)} chunk(s) | type: {question_type} | "
        f"difficulty: {difficulty} | "
        f"{questions_per_chunk} questions per chunk"
        + (f" | target: {num_questions} question(s)" if num_questions else "")
    )

    def dispatch(chunk, n, avoid):
        if question_type == "mcq":
            return generate_mcq(chunk, n, difficulty, avoid, verify)
        elif question_type == "short":
            return generate_short_answer(chunk, n, difficulty, avoid, verify)
        elif question_type == "fill":
            return generate_fill_blank(chunk, n, difficulty, avoid, verify)
        elif question_type == "truefalse":
            return generate_true_false(chunk, n, difficulty, avoid, verify)
        return [], {"proposed": 0, "accepted": 0}

    # question generation based on selected arguments. When a target count
    # is given, this keeps making extra passes over the chunks — telling
    # the model what's already been used so it doesn't just re-ask the
    # same thing — until it hits the exact number or gives up after
    # MAX_ROUNDS (a genuinely too-short/thin document may not have enough
    # distinct content to reach an arbitrarily high target).
    #
    # Chunks within a round are dispatched concurrently (up to
    # max_workers at once) instead of one blocking Ollama call at a time —
    # this is the single biggest speed lever available at the app level.
    MAX_ROUNDS = 4
    all_questions: list[dict] = []
    round_num = 1

    # Tracks how much of what the model *proposed* actually survived
    # AI-verification + format-validation — this is the "how relevant/
    # correct is the generated content" signal, distinct from a user's
    # quiz-taking accuracy. Computed from the existing generation calls.
    total_proposed = 0
    total_accepted = 0

    while True:
        remaining = None
        if num_questions is not None:
            remaining = num_questions - len(all_questions)
            if remaining <= 0:
                break

        # On the first pass, only touch as many chunks as are likely
        # needed to hit the target — no point firing every chunk in a
        # 40-page document when 3 chunks would already cover the count.
        # Top-up rounds use every chunk, since round 1 already showed
        # a subset wasn't enough.
        if remaining is not None and round_num == 1:
            chunks_needed = min(len(chunks), max(1, -(-remaining // questions_per_chunk)))
            round_chunks = list(enumerate(chunks))[:chunks_needed]
        else:
            round_chunks = list(enumerate(chunks))

        label = f" (round {round_num})" if round_num > 1 else ""
        print(
            f"\n=== Generating{label}: {len(round_chunks)} chunk(s), "
            f"up to {max_workers} at a time ==="
        )

        avoid = [q["question"] for q in all_questions] if round_num > 1 else None

        with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
            future_to_idx = {
                executor.submit(dispatch, chunk, questions_per_chunk, avoid): idx
                for idx, chunk in round_chunks
            }
            for future in concurrent.futures.as_completed(future_to_idx):
                idx = future_to_idx[future]
                try:
                    results, stats = future.result()
                except Exception as exc:
                    print(f"Chunk {idx + 1} failed: {exc}")
                    results, stats = [], {"proposed": 0, "accepted": 0}

                total_proposed += stats["proposed"]
                total_accepted += stats["accepted"]

                print(
                    f"Chunk {idx + 1}/{len(chunks)}: got {len(results)} question(s) "
                    f"({stats['accepted']}/{stats['proposed']} proposed questions kept)"
                )
                all_questions.extend(results)

        all_questions = remove_duplicate(all_questions)
        print(f"Round {round_num} complete: {len(all_questions)} unique question(s) so far")

        if num_questions is None:
            break  # no target set — single pass, same as before

        if len(all_questions) >= num_questions:
            break

        if round_num >= MAX_ROUNDS:
            print(
                f"\nWARNING: could only generate {len(all_questions)} of "
                f"{num_questions} requested unique, valid question(s) after "
                f"{round_num} round(s). The source document may not have "
                f"enough distinct content for more."
            )
            break

        round_num += 1

    # a chunk in progress when the target was reached can still overshoot
    # by up to `questions_per_chunk - 1` questions — trim to the exact
    # count the user asked for.
    if num_questions is not None and len(all_questions) > num_questions:
        all_questions = all_questions[:num_questions]

    relevance_accuracy = (
        round(100 * total_accepted / total_proposed, 1)
        if total_proposed > 0 else 0.0
    )
    print(
        f"\nAI relevance accuracy: {relevance_accuracy}% "
        f"({total_accepted}/{total_proposed} proposed questions were "
        f"relevant/valid and kept)"
    )

    # generated_questions.json stays a plain array (Java's QuizGenerator
    # deserializes it directly as QuizItem[]) — the relevance metric is
    # saved separately so it doesn't break that existing contract.
    with open("generation_stats.json", "w", encoding="utf-8") as f:
        json.dump({
            "total_proposed": total_proposed,
            "total_accepted": total_accepted,
            "relevance_accuracy": relevance_accuracy,
            "final_question_count": len(all_questions)
        }, f, ensure_ascii=False, indent=2)

    save_outputs(all_questions)
    return all_questions

def parse_args():
    # kept as a single value (not nargs="+") and using the same choice
    # names ("truefalse", not "tf") so this stays a drop-in match for
    # what UploadController.java already sends via ProcessBuilder.
    parser = argparse.ArgumentParser()
    parser.add_argument("file")
    parser.add_argument(
        "--type",
        choices=["mcq", "short", "fill", "truefalse"],
        default="short"
    )
    parser.add_argument(
        "--difficulty",
        choices=["easy", "intermediate", "hard"],
        default="intermediate"
    )

    # for testing
    parser.add_argument("--max-chars", type=int, default=1500)
    parser.add_argument("--questions-per-chunk", type=int, default=3)

    # total question count the caller wants — optional, defaults to
    # "unlimited" (old behaviour: however many the chunks produce) so this
    # stays backward compatible with anything that doesn't pass it.
    parser.add_argument("--num-questions", type=int, default=None)

    # how many chunks to send to Gemini at once. This only helps if Ollama
    # can actually run inference concurrently (a GPU, or enough spare CPU
    # cores) — on a CPU-only machine, one model is doing all the work
    # regardless of how many requests are in flight, so raising this just
    # makes them compete for the same cores instead of speeding anything
    # up. Defaults to 1 (sequential) for that reason; if you have a GPU,
    # try 3-4 and compare.
    parser.add_argument("--max-workers", type=int, default=1)

    # verification runs a second Gemini API call per chunk to review question
    # quality — real value, but it roughly doubles generation time. Skip
    # it for faster (rougher) output, especially useful on CPU-only setups.
    parser.add_argument("--skip-verification", action="store_true")
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    result = generate_questions(
        path=args.file,
        question_type=args.type,
        max_chars=args.max_chars,
        questions_per_chunk=args.questions_per_chunk,
        difficulty=args.difficulty,
        num_questions=args.num_questions,
        max_workers=args.max_workers,
        verify=not args.skip_verification
    )
    print(f"\n{len(result)} question(s) saved.")
    print("  CSV  -> generated_questions.csv")
    print("  JSON -> generated_questions.json")
