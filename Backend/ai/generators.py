import re
import json
import sys

import requests

from config import OLLAMA_URL, OLLAMA_MODEL

# function to check that ollama is running
def check_ollama():
    try:
        resp = requests.get(f"{OLLAMA_URL}/api/tags", timeout=5)
        resp.raise_for_status()
    except requests.ConnectionError:
        sys.exit(
            "\n[ERROR] Ollama is not running.\n"
            "Start it with: ollama serve"
        )
    except requests.HTTPError as exc:
        sys.exit(f"\n[ERROR] Ollama responded with an error: {exc}")

    available = [m["name"] for m in resp.json().get("models", [])]

    # Exact-tag match, not a family-name substring match — "qwen2.5" being
    # a substring of an installed "qwen2.5:3b" used to be treated as proof
    # that "qwen2.5:1.5b" was available, which isn't true and led to every
    # generation call silently 404-ing instead of failing here with a
    # clear message.
    if OLLAMA_MODEL in available:
        print(f"Ollama is running. Model '{OLLAMA_MODEL}' is available.")
        return

    family = OLLAMA_MODEL.split(":")[0]
    related = [m for m in available if m.split(":")[0] == family]

    hint = ""
    if related:
        hint = (
            f"\nYou do have other '{family}' tags installed ({', '.join(related)}), "
            f"but not the exact tag '{OLLAMA_MODEL}' this script is configured to use."
        )

    sys.exit(
        f"\n[ERROR] Model '{OLLAMA_MODEL}' not found.{hint}\n"
        f"Run: ollama pull {OLLAMA_MODEL}"
    )

# function used to call ai model for question generation
def run_inference(messages, max_tokens=512):
    payload = {
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
        "think": False,
        # keep the model resident in memory between calls instead of the
        # Ollama default (unload after 5 min idle) — a single quiz run
        # makes many back-to-back calls, so avoiding reload overhead
        # between them matters more than freeing that memory quickly.
        "keep_alive": "10m",
        "options": {
            "num_predict": max_tokens,
            "temperature": 0.25,
            "top_p": 0.8,
            "top_k": 20,
        }
    }
    response = requests.post(
        f"{OLLAMA_URL}/api/chat",
        json=payload,
        timeout=600,
    )
    response.raise_for_status()
    return response.json()["message"]["content"].strip()

# converts ai output into proper json structure
# If the model's response got cut off mid-generation (ran out of the
# token budget before finishing the JSON array), the whole thing fails
# to parse as valid JSON even though the first several question objects
# in it were likely complete and fine. This scans for individual
# balanced {...} objects and parses each independently, so a truncated
# tail costs us just the one unfinished question instead of the entire
# batch.
def _extract_complete_objects(raw):
    objects = []
    depth = 0
    start = None
    for i, ch in enumerate(raw):
        if ch == '{':
            if depth == 0:
                start = i
            depth += 1
        elif ch == '}':
            if depth > 0:
                depth -= 1
                if depth == 0 and start is not None:
                    candidate = raw[start:i + 1]
                    try:
                        obj = json.loads(candidate)
                        if isinstance(obj, dict):
                            objects.append(obj)
                    except json.JSONDecodeError:
                        pass
                    start = None
    return objects


def _parse_json_array(raw):
    raw = re.sub(r"^```(?:json)?\s*", "", raw)
    raw = re.sub(r"\s*```$", "", raw).strip()

    bracket_start = raw.find("[")
    if bracket_start > 0:
        raw = raw[bracket_start:]

    try:
        parsed = json.loads(raw)
        return parsed if isinstance(parsed, list) else None
    except json.JSONDecodeError:
        pass

    match = re.search(r"\[.*\]", raw, re.DOTALL)
    if match:
        try:
            parsed = json.loads(match.group(0))
            return parsed if isinstance(parsed, list) else None
        except json.JSONDecodeError:
            pass

    # Last resort: salvage whatever complete question objects did finish
    # generating, rather than discarding a truncated batch entirely.
    salvaged = _extract_complete_objects(raw)
    if salvaged:
        return salvaged

    return None

################################################################################################################
# Difficulty handling — restored so prompts scale with the difficulty picked
# on the Upload page (Easy / Intermediate / Hard) instead of always writing
# at one fixed level.
DIFFICULTY_LEVELS = ("easy", "intermediate", "hard")

DIFFICULTY_INSTRUCTIONS = {
    "easy": (
        "- Difficulty: EASY. Ask about clearly stated, surface-level facts "
        "(definitions, names, simple details) that can be found almost verbatim "
        "in the text. Avoid multi-step reasoning or tricky wording."
    ),
    "intermediate": (
        "- Difficulty: INTERMEDIATE. Ask about facts that require understanding "
        "how a couple of details in the text relate to each other, not just "
        "spotting a single sentence. Avoid pure trivia and avoid overly obscure details."
    ),
    "hard": (
        "- Difficulty: HARD. Ask about nuanced, less obvious details, or require "
        "connecting multiple pieces of information from the text to answer correctly. "
        "Distractors/wrong answers should be subtle and easy to confuse with the correct one."
    ),
}

def _difficulty_instruction(difficulty: str) -> str:
    return DIFFICULTY_INSTRUCTIONS.get(difficulty, DIFFICULTY_INSTRUCTIONS["intermediate"])

# Used on top-up rounds (when the first pass came up short of the requested
# total) so the model doesn't just regenerate near-duplicates of questions
# it already wrote for this chunk.
def _avoid_instruction(avoid) -> str:
    if not avoid:
        return ""
    listed = "\n".join(f"  - {q}" for q in avoid[:20])
    return (
        "- Do not repeat, or closely rephrase, any of these already-used "
        f"questions:\n{listed}\n"
    )

################################################################################################################

# remove generated questions that don't fit the format for each question type
def _validate_question(item, question_type):
    if not isinstance(item, dict):
        return False
    if not isinstance(item.get("question"), str) or len(item["question"].strip()) < 4:
        return False
    if question_type == "mcq":
        options = item.get("options")
        # A small model occasionally returns "options" as a list or even
        # a plain string instead of the requested {"A": ..., "B": ...}
        # shape. Reject just this one malformed question instead of
        # crashing .values() and losing the rest of the chunk's batch.
        if not isinstance(options, dict):
            return False
        option_values = [v.lower().strip() for v in options.values()]
        if len(set(option_values)) < 4:   # fewer than 4 unique options
            return False
        answer_letter = item.get("answer", "").upper()
        return (
            set(options.keys()) >= {"A", "B", "C", "D"}
            and isinstance(item.get("answer"), str)
            and item["answer"].upper() in options
        )
    if question_type == "truefalse":
        return (
            isinstance(item.get("answer"), str)
            and item["answer"].strip().lower() in ("true", "false")
        )
    if question_type == "fill":
        return (
            isinstance(item.get("answer"), str)
            and len(item["answer"].strip()) > 0
            and "_____" in item["question"]
        )
    return (
        isinstance(item.get("answer"), str)
        and len(item["answer"].strip()) > 0
    )

# sends outputted questions to ai to check for vague or low quality questions based on the text
def verify_questions(questions, chunk):
    messages = [{
        "role": "user",
        "content": (
            "You are a strict quiz reviewer. Given the source text and a list "
            "of questions, remove any questions that:\n"
            "- Cannot be answered from the source text\n"
            "- Are too vague or ambiguous\n"
            "- Test only surface-level recall of a single word\n"
            "- Have an incorrect or missing answer\n\n"
            "Return only the valid questions as a JSON array in the same format "
            "as the input. No markdown, no explanation.\n\n"
            f"Source text:\n{chunk}\n\n"
            f"Questions:\n{json.dumps(questions)}"
        )
    }]

    raw = run_inference(messages, max_tokens=max(1024, len(questions) * 220))
    return _parse_json_array(raw)


# messages text is passed into run_inference which is sent to the ai model with each chunk of text
# the resulting output is converted into proper json and then validated to ensure correctness of the questions
def generate_mcq(chunk, n, difficulty="intermediate", avoid=None, verify=True):
    messages = [{
        "role": "system",
        "content": (
            "You are an expert educator and quiz writer. "
            "You write clear, specific questions that test genuine understanding, "
            "not just surface recall. You always respond in valid JSON."
            "Do not generate questions using 'NOT' or 'EXCEPT' phrasing as they cause ambiguity.\n"
        )},
        {"role": "user",
        "content": (
            f"You are a quiz question writer. Based ONLY on the text below, "
            f"generate {n} multiple choice questions.\n"
            "Rules:\n"
            "- Each question must have exactly 4 options labelled A, B, C, D.\n"
            "- \"options\" MUST be a JSON OBJECT with keys \"A\", \"B\", \"C\", \"D\" — "
            "for example {\"A\": \"first option\", \"B\": \"second option\", ...}. "
            "Never output \"options\" as a list/array or as a single string.\n"
            "- Only one option is correct.\n"
            "- Make the incorrect options plausible but clearly wrong to an expert.\n"
            "- Do not reference 'the text' or 'the passage' in the questions.\n"
            f"{_difficulty_instruction(difficulty)}\n"
            f"{_avoid_instruction(avoid)}"
            "- Respond ONLY with a valid JSON array. No markdown, no explanation.\n\n"
            "Format:\n"
            "[\n"
            "  {\n"
            '    "question": "...",\n'
            '    "options": {"A": "...", "B": "...", "C": "...", "D": "..."},\n'
            '    "answer": "..."\n'
            "  }\n"
            "]\n\n"
            f"Text:\n{chunk}"
        )
    }]

    raw = run_inference(messages, max_tokens=max(1024, n * 260))
    parsed = _parse_json_array(raw)
    if parsed is None:
        print(f"MCQ parse failed. Snippet: {raw[:300]}")
        return [], {"proposed": 0, "accepted": 0}

    proposed = len(parsed)

    if verify:
        verified = verify_questions(parsed, chunk)
        if verified is None:
            print("Verification failed, using unverified questions")
        else:
            parsed = verified

    validated = []
    for item in parsed:
        try:
            if not _validate_question(item, "mcq"):
                continue
            validated.append({
                "type": "Multiple Choice",
                "question": item["question"].strip(),
                "options": item["options"],
                "answer_letter": item["answer"].upper(),
                "answer_text": item["options"][item["answer"].upper()]
            })
        except (KeyError, AttributeError, TypeError) as exc:
            print(f"Skipping one malformed question: {exc}")
    return validated, {"proposed": proposed, "accepted": len(validated)}


def generate_short_answer(chunk, n, difficulty="intermediate", avoid=None, verify=True):
    messages = [{
        "role": "system",
        "content": (
            "You are an expert educator and quiz writer. "
            "You write clear, specific questions that test genuine understanding, "
            "not just surface recall. You always respond in valid JSON."
            "Do not generate questions using 'NOT' or 'EXCEPT' phrasing as they cause ambiguity.\n"
        )},
        {"role": "user",
        "content": (
            f"You are a quiz question writer. Based ONLY on the text below, "
            f"generate {n} short-answer questions with answers.\n"
            "Rules:\n"
            "- Each answer must be answerable in one sentence or less.\n"
            "- Do not reference 'the text' or 'the passage' in the questions.\n"
            f"{_difficulty_instruction(difficulty)}\n"
            f"{_avoid_instruction(avoid)}"
            "- Respond ONLY with a valid JSON array. No markdown, no explanation.\n\n"
            "Format:\n"
            "[\n"
            "  {\n"
            '    "question": "...",\n'
            '    "answer": "..."\n'
            "  }\n"
            "]\n\n"
            f"Text:\n{chunk}"
        )
    }]

    raw = run_inference(messages, max_tokens=max(640, n * 150))
    parsed = _parse_json_array(raw)
    if parsed is None:
        print(f"Short-answer parse failed. Snippet: {raw[:300]}")
        return [], {"proposed": 0, "accepted": 0}

    proposed = len(parsed)

    if verify:
        verified = verify_questions(parsed, chunk)
        if verified is None:
            print("Verification failed, using unverified questions")
        else:
            parsed = verified

    validated = []
    for item in parsed:
        try:
            if not _validate_question(item, "short"):
                continue
            validated.append({
                "type": "Short Answer",
                "question": item["question"].strip(),
                "answer": item["answer"].strip()
            })
        except (KeyError, AttributeError, TypeError) as exc:
            print(f"Skipping one malformed question: {exc}")
    return validated, {"proposed": proposed, "accepted": len(validated)}


def generate_fill_blank(chunk, n, difficulty="intermediate", avoid=None, verify=True):
    messages = [{
        "role": "system",
        "content": (
            "You are an expert educator and quiz writer. "
            "You write clear, specific questions that test genuine understanding, "
            "not just surface recall. You always respond in valid JSON."
            "Do not generate questions using 'NOT' or 'EXCEPT' phrasing as they cause ambiguity.\n"
        )},
        {"role": "user",
        "content": (
            f"You are a quiz question writer. Based ONLY on the text below, "
            f"generate {n} fill-in-the-blank questions.\n"
            "Rules:\n"
            "- Replace one key word or phrase in a sentence with _____.\n"
            "- The blank must come directly from the text below, not from any examples.\n"
            "- Include the correct answer for each blank.\n"
            "- Do not reference 'the text' or 'the passage' in the questions.\n"
            f"{_difficulty_instruction(difficulty)}\n"
            f"{_avoid_instruction(avoid)}"
            "- Respond ONLY with a valid JSON array. No markdown, no explanation.\n\n"
            "Format:\n"
            "[\n"
            "  {\n"
            '    "question": "...",\n'
            '    "answer": "..."\n'
            "  }\n"
            "]\n\n"
            f"Text:\n{chunk}"
        )
    }]

    raw = run_inference(messages, max_tokens=max(640, n * 150))
    parsed = _parse_json_array(raw)
    if parsed is None:
        print(f"Fill-blank parse failed. Snippet: {raw[:300]}")
        return [], {"proposed": 0, "accepted": 0}

    proposed = len(parsed)

    if verify:
        verified = verify_questions(parsed, chunk)
        if verified is None:
            print("Verification failed, using unverified questions")
        else:
            parsed = verified

    validated = []
    for item in parsed:
        try:
            if not _validate_question(item, "fill"):
                continue
            validated.append({
                "type": "Fill in the Blank",
                "question": item["question"].strip(),
                "answer": item["answer"].strip()
            })
        except (KeyError, AttributeError, TypeError) as exc:
            print(f"Skipping one malformed question: {exc}")
    return validated, {"proposed": proposed, "accepted": len(validated)}


def generate_true_false(chunk, n, difficulty="intermediate", avoid=None, verify=True):
    messages = [{
        "role": "system",
        "content": (
            "You are an expert educator and quiz writer. "
            "You write clear, specific questions that test genuine understanding, "
            "not just surface recall. You always respond in valid JSON."
            "Do not generate questions using 'NOT' or 'EXCEPT' phrasing as they cause ambiguity.\n"
        )},
        {"role": "user",
        "content": (
            f"You are a quiz question writer. Based ONLY on the text below, "
            f"generate {n} true or false questions.\n"
            "Rules:\n"
            "- Each statement must be clearly true or false based on the text below.\n"
            "- The statements must come directly from the text below, not from any examples.\n"
            "- Mix true and false statements — do not make them all true or all false.\n"
            "- Keep each statement short and unambiguous.\n"
            "- Do not reference 'the text' or 'the passage' in the statements.\n"
            f"{_difficulty_instruction(difficulty)}\n"
            f"{_avoid_instruction(avoid)}"
            "- Respond ONLY with a valid JSON array. No markdown, no explanation.\n\n"
            "Format:\n"
            "[\n"
            "  {\n"
            '    "question": "The Eiffel Tower is located in London.",\n'
            '    "answer": "False"\n'
            "  }\n"
            "]\n\n"
            f"Text:\n{chunk}"
        )
    }]

    raw = run_inference(messages, max_tokens=max(640, n * 150))
    parsed = _parse_json_array(raw)
    if parsed is None:
        print(f"True/false parse failed. Snippet: {raw[:300]}")
        return [], {"proposed": 0, "accepted": 0}

    proposed = len(parsed)

    if verify:
        verified = verify_questions(parsed, chunk)
        if verified is None:
            print("Verification failed, using unverified questions")
        else:
            parsed = verified

    validated = []
    for item in parsed:
        try:
            if not _validate_question(item, "truefalse"):
                continue
            validated.append({
                "type": "True/False",
                "question": item["question"].strip(),
                "answer": item["answer"].strip()
            })
        except (KeyError, AttributeError, TypeError) as exc:
            print(f"Skipping one malformed question: {exc}")
    return validated, {"proposed": proposed, "accepted": len(validated)}
