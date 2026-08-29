package com.learn2play.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class QuizGenerator {

    private final ObjectMapper objectMapper;
    private List<QuizItem> lastQuiz = new ArrayList<>();
    private double lastRelevanceAccuracy = 0.0;

    public QuizGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<QuizItem> generateQuizForFile(
            String relativePath,
            String questionType,
            String difficulty,
            int questionCount) {

        try {

            // Normalize difficulty for Python
            String resolvedDifficulty =
                    (difficulty == null || difficulty.isBlank())
                            ? "intermediate"
                            : difficulty.toLowerCase().trim();

            // Normalize question type for Python
            String resolvedQuestionType =
                    (questionType == null || questionType.isBlank())
                            ? "mcq"
                            : questionType.toLowerCase().trim();

            // Convert possible frontend values to values accepted by quizgen.py
            switch (resolvedQuestionType) {
                case "multiple choice":
                case "multiple-choice":
                    resolvedQuestionType = "mcq";
                    break;

                case "short answer":
                case "short-answer":
                    resolvedQuestionType = "short";
                    break;

                case "fill in the blank":
                case "fill-in-the-blank":
                    resolvedQuestionType = "fill";
                    break;

                case "true/false":
                case "true false":
                case "true-false":
                    resolvedQuestionType = "truefalse";
                    break;

                default:
                    break;
            }

            // Validate question type before starting Python
            List<String> validQuestionTypes = Arrays.asList(
                    "mcq",
                    "short",
                    "fill",
                    "truefalse"
            );

            if (!validQuestionTypes.contains(resolvedQuestionType)) {
                throw new IllegalArgumentException(
                        "Invalid question type: " + questionType
                                + ". Allowed values are: "
                                + "mcq, short, fill, truefalse"
                );
            }

            Path projectRootPath =
                    Paths.get(System.getProperty("user.dir"));

            Path windowsPython = projectRootPath
                    .resolve("venv-quiz")
                    .resolve("Scripts")
                    .resolve("python.exe");

            String pythonCommand =
                    Files.exists(windowsPython)
                            ? windowsPython.toString()
                            : "python";

            Path quizgenScript =
                    projectRootPath
                            .resolve("ai")
                            .resolve("quizgen.py");

            Path sourceFilePath =
                    projectRootPath.resolve(relativePath);

            System.out.println(
                    "[quizgen] Question Type = "
                            + resolvedQuestionType
            );

            System.out.println(
                    "[quizgen] Difficulty = "
                            + resolvedDifficulty
            );

            System.out.println(
                    "[quizgen] Using Python: "
                            + pythonCommand
            );

            System.out.println(
                    "[quizgen] Using script: "
                            + quizgenScript
            );

            System.out.println(
                    "[quizgen] Using source file: "
                            + sourceFilePath
            );

            System.out.println(
                    "[quizgen] Question Count = "
                            + questionCount
            );

            ProcessBuilder pb = new ProcessBuilder(
                    pythonCommand,
                    quizgenScript.toString(),
                    sourceFilePath.toString(),
                    "--type",
                    resolvedQuestionType,
                    "--difficulty",
                    resolvedDifficulty,
                    "--questions-per-chunk",
                    "3",
                    "--num-questions",
                    String.valueOf(questionCount)
            );

            pb.directory(
                    new File(projectRootPath.toString())
            );

            // Show Python output immediately
            pb.environment().put(
                    "PYTHONUNBUFFERED",
                    "1"
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream()
                                    )
                            )
            ) {
                String line;

                while ((line = reader.readLine()) != null) {
                    System.out.println(
                            "[quizgen] " + line
                    );
                }
            }

            // Maximum generation time: 20 minutes
            boolean finished =
                    process.waitFor(
                            20,
                            TimeUnit.MINUTES
                    );

            if (!finished) {

                process.destroyForcibly();

                System.out.println(
                        "[quizgen] Timed out after 20 minutes"
                );

                throw new RuntimeException(
                        "quizgen.py timed out after 20 minutes "
                                + "and was terminated."
                );
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {

                throw new RuntimeException(
                        "quizgen.py failed with exit code "
                                + exitCode
                );
            }

            Path jsonPath =
                    projectRootPath.resolve(
                            "generated_questions.json"
                    );

            if (!Files.exists(jsonPath)) {

                throw new RuntimeException(
                        "generated_questions.json not found at: "
                                + jsonPath.toAbsolutePath()
                );
            }

            String json =
                    Files.readString(jsonPath);

            QuizItem[] arr =
                    objectMapper.readValue(
                            json,
                            QuizItem[].class
                    );

            lastQuiz = new ArrayList<>(
                    Arrays.asList(arr)
            );

            System.out.println(
                    "[quizgen] Loaded "
                            + lastQuiz.size()
                            + " questions (requested "
                            + questionCount
                            + ")"
            );

            // Make sure the frontend receives no more
            // questions than requested
            if (questionCount > 0
                    && lastQuiz.size() > questionCount) {

                lastQuiz = new ArrayList<>(
                        lastQuiz.subList(
                                0,
                                questionCount
                        )
                );

                System.out.println(
                        "[quizgen] Trimmed to "
                                + lastQuiz.size()
                                + " questions"
                );
            }

            // Default value if generation_stats.json does not exist
            lastRelevanceAccuracy = 0.0;

            Path statsPath =
                    projectRootPath.resolve(
                            "generation_stats.json"
                    );

            if (Files.exists(statsPath)) {

                try {

                    String statsJson =
                            Files.readString(statsPath);

                    var statsNode =
                            objectMapper.readTree(
                                    statsJson
                            );

                    if (statsNode.has(
                            "relevance_accuracy"
                    )) {

                        lastRelevanceAccuracy =
                                statsNode
                                        .get(
                                                "relevance_accuracy"
                                        )
                                        .asDouble();
                    }

                    System.out.println(
                            "[quizgen] AI relevance accuracy: "
                                    + lastRelevanceAccuracy
                                    + "%"
                    );

                } catch (Exception statsEx) {

                    System.out.println(
                            "[quizgen] Could not read "
                                    + "generation_stats.json: "
                                    + statsEx.getMessage()
                    );
                }

            } else {

                System.out.println(
                        "[quizgen] No generation_stats.json found "
                                + "(older script version?)"
                );
            }

            return lastQuiz;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to generate quiz",
                    e
            );
        }
    }

    public List<QuizItem> getLastQuiz() {
        return lastQuiz;
    }

    public double getLastRelevanceAccuracy() {
        return lastRelevanceAccuracy;
    }
}