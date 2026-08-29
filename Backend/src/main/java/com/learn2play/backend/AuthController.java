package com.learn2play.backend;

import com.learn2play.backend.entity.AppUser;
import com.learn2play.backend.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.learn2play.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://learn2play-final-1.onrender.com"
})

public class AuthController {
    private final AppUserRepository appUserRepository;
    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AuthController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody AppUser user) {

        if (appUserRepository.findByEmail(
                user.getEmail()).isPresent()) {

            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        user.setPassword(
                encoder.encode(user.getPassword())
        );

        appUserRepository.save(user);

        return ResponseEntity.ok(
                "Account created successfully"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AppUser loginUser) {

        Optional<AppUser> existingUser =
                appUserRepository.findByEmail(
                        loginUser.getEmail());

        if (existingUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Invalid email");
        }

        AppUser user = existingUser.get();

        if (!encoder.matches(
                loginUser.getPassword(),
                user.getPassword())) {

            return ResponseEntity.badRequest()
                    .body("Invalid password");
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        Optional<AppUser> userOptional =
                appUserRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Email not found");
        }

        AppUser user = userOptional.get();

        String token =
                UUID.randomUUID().toString();

        user.setResetToken(token);

        user.setResetTokenExpiry(
                LocalDateTime.now().plusMinutes(30)
        );

        appUserRepository.save(user);
String resetLink =
        "https://learn2play-final-1.onrender.com/reset-password?token="
                + token;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink
        );

        return ResponseEntity.ok(
                "Password reset email sent."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("newPassword");

        Optional<AppUser> userOptional =
                appUserRepository.findByResetToken(token);

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid or expired token");
        }

        AppUser user = userOptional.get();

        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry()
                        .isBefore(LocalDateTime.now())) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid or expired token");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        appUserRepository.save(user);

        return ResponseEntity.ok(
                "Password has been reset successfully."
        );
    }
}