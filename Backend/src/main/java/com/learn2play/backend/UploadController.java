package com.learn2play.backend;

import com.learn2play.backend.dto.LeaderboardResponse;
import com.learn2play.backend.dto.ProfileResponse;
import com.learn2play.backend.dto.QuizAttemptRequest;
import com.learn2play.backend.dto.QuizAttemptResponse;
import com.learn2play.backend.dto.QuizHistoryResponse;
import com.learn2play.backend.dto.QuizResponse;
import com.learn2play.backend.dto.StatisticsResponse;
import com.learn2play.backend.entity.Quiz;
import com.learn2play.backend.entity.UploadedDocument;
import com.learn2play.backend.repository.UploadedDocumentRepository;
import com.learn2play.backend.service.LeaderboardService;
import com.learn2play.backend.service.ProfileService;
import com.learn2play.backend.service.QuizPersistenceService;
import com.learn2play.backend.service.StatisticsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RestController
public class UploadController {

    private static final long MAX_FILE_SIZE_BYTES =
            100L * 1024L * 1024L;

    private final QuizGenerator quizGenerator;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final QuizPersistenceService quizPersistenceService;
    private final LeaderboardService leaderboardService;
    private final ProfileService profileService;
    private final StatisticsService statisticsService;

    public UploadController(
            QuizGenerator quizGenerator,
            UploadedDocumentRepository uploadedDocumentRepository,
            QuizPersistenceService quizPersistenceService,
            LeaderboardService leaderboardService,
            ProfileService profileService,
            StatisticsService statisticsService
    ) {
        this.quizGenerator = quizGenerator;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.quizPersistenceService = quizPersistenceService;
        this.leaderboardService = leaderboardService;
        this.profileService = profileService;
        this.statisticsService = statisticsService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "questionType", defaultValue = "short")
            String questionType,
            @RequestParam(value = "difficulty", defaultValue = "intermediate")
            String difficulty,
            @RequestParam(value = "questionCount", defaultValue = "10")
            int questionCount,
            @RequestParam("email")
            String email
    ) {

        UploadedDocument document = null;

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new UploadResponse(
                                null, null, "File is empty"
                        ));
            }

            String originalName = file.getOriginalFilename();

            if (originalName == null || !originalName.contains(".")) {
                return ResponseEntity.badRequest()
                        .body(new UploadResponse(
                                null, null,
                                "File must have an extension"
                        ));
            }

            String safeOriginalName =
                    Paths.get(originalName).getFileName().toString();

            String ext = safeOriginalName
                    .substring(safeOriginalName.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);

            if (!isAllowedExtension(ext)) {
                return ResponseEntity.badRequest()
                        .body(new UploadResponse(
                                null,
                                safeOriginalName,
                                "Only supported document files are allowed"
                        ));
            }

            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                return ResponseEntity.badRequest()
                        .body(new UploadResponse(
                                null,
                                safeOriginalName,
                                "File is too large. Maximum size is 100 MB"
                        ));
            }

            int clampedQuestionCount =
                    Math.max(1, Math.min(50, questionCount));

            Path uploadDir = Paths.get("uploads");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileId = UUID.randomUUID().toString();
            String storedFileName =
                    fileId + "-" + safeOriginalName;

            Path filePath =
                    uploadDir.resolve(storedFileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            document = new UploadedDocument(
                    fileId,
                    safeOriginalName,
                    storedFileName,
                    filePath.toString(),
                    file.getContentType(),
                    file.getSize(),
                    "UPLOADED",
                    "File uploaded successfully"
            );

            document.setUserEmail(email);
            document = uploadedDocumentRepository.save(document);

            List<QuizItem> generatedQuestions =
                    quizGenerator.generateQuizForFile(
                            filePath.toString(),
                            questionType,
                            difficulty,
                            clampedQuestionCount
                    );

            Quiz savedQuiz =
                    quizPersistenceService.saveGeneratedQuiz(
                            document,
                            generatedQuestions,
                            difficulty
                    );

            document.setStatus("QUIZ_GENERATED");
            document.setMessage(
                    "File uploaded and quiz generated successfully"
            );

            uploadedDocumentRepository.save(document);

            return ResponseEntity.ok(
                    new UploadResponse(
                            fileId,
                            safeOriginalName,
                            "File uploaded and quiz generated successfully",
                            document.getId(),
                            savedQuiz.getId(),
                            quizGenerator.getLastRelevanceAccuracy()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            if (document != null) {
                document.setStatus("FAILED");
                document.setMessage(
                        "Upload saved but quiz generation failed: "
                                + e.getMessage()
                );

                uploadedDocumentRepository.save(document);
            }

            return ResponseEntity.status(500)
                    .body(new UploadResponse(
                            null,
                            null,
                            "Error uploading file or generating quiz"
                    ));
        }
    }

    @GetMapping("/quiz")
    public ResponseEntity<List<QuizItem>> getLatestQuiz() {

        try {
            List<QuizItem> questions =
                    quizPersistenceService.getLatestQuizItems();

            if (questions == null || questions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(questions);

        } catch (Exception e) {

            List<QuizItem> fallbackQuestions =
                    quizGenerator.getLastQuiz();

            if (fallbackQuestions == null ||
                    fallbackQuestions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(fallbackQuestions);
        }
    }

    @GetMapping("/quiz/latest")
    public ResponseEntity<QuizResponse> getLatestQuizWithId() {
        return ResponseEntity.ok(
                quizPersistenceService.getLatestQuizResponse()
        );
    }

    // THIS FIXES THE REACT REQUEST:
    // GET http://localhost:8080/quiz/{quizId}
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable String quizId
    ) {
        return ResponseEntity.ok(
                quizPersistenceService.getQuizById(quizId)
        );
    }

    @PostMapping("/quiz/{quizId}/attempts")
    public ResponseEntity<QuizAttemptResponse> submitQuizAttempt(
            @PathVariable String quizId,
            @RequestBody QuizAttemptRequest request
    ) {
        return ResponseEntity.ok(
                quizPersistenceService.submitAttempt(
                        quizId,
                        request
                )
        );
    }

    @GetMapping("/statistics/{email}")
    public ResponseEntity<StatisticsResponse> getStatistics(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(
                statisticsService.getStatistics(email)
        );
    }

    @GetMapping("/quiz/history/{email}")
    public ResponseEntity<List<QuizHistoryResponse>> getQuizHistory(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(
                quizPersistenceService.getQuizHistory(email)
        );
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {
        return ResponseEntity.ok(
                leaderboardService.getLeaderboard()
        );
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(
                profileService.getProfile(email)
        );
    }

    private boolean isAllowedExtension(String ext) {
        return ext.equals("pdf")
                || ext.equals("docx")
                || ext.equals("txt")
                || ext.equals("md")
                || ext.equals("rtf")
                || ext.equals("pptx")
                || ext.equals("csv")
                || ext.equals("html")
                || ext.equals("htm");
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }
}