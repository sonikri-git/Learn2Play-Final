package com.learn2play.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("kritishsoni2004@gmail.com"); // Replace with your verified sender

        message.setTo(to);

        message.setSubject("Reset your Learn2Play Password");

        message.setText(
                "Hello,\n\n"
                + "Click the link below to reset your password:\n\n"
                + resetLink
                + "\n\n"
                + "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
    }}