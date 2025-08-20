package com.factseekerbackend.global.email;

public interface EmailService {

  void sendVerificationCodeEmail(String email, String verificationCode);

  void sendWelcomeEmail(String email, String username);
}
