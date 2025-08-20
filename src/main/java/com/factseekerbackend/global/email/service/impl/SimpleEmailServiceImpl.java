package com.factseekerbackend.global.email.service.impl;

import com.factseekerbackend.global.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleEmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.mail.from}")
  private String fromEmail;

  @Async
  @Override
  public void sendVerificationCodeEmail(String email, String verificationCode) {
    try {
      String subject = "비밀번호 재설정 인증번호";
      String content = String.format(
          "안녕하세요, 회원님.\n\n" +
              "비밀번호 재설정을 위한 인증번호입니다.\n\n" +
              "인증번호: %s\n\n" +
              "이 인증번호는 5분간 유효합니다.\n" +
              "요청하지 않았다면 이 이메일을 무시해주세요.\n\n" +
              "감사합니다.\n" +
              "FactSeeker 팀", verificationCode
      );

      sendSimpleEmail(email, subject, content);
      log.info("인증코드가 보내진 이메일: {}", email);
    } catch (Exception e) {
      log.error("인증코드를 보내는 데 실패했습니다: {}", email, e);
    }
  }

  @Async
  @Override
  public void sendWelcomeEmail(String email, String username) {
    try {
      String subject = "FactSeeker에 오신 것을 환영합니다!";
      String content = String.format(
          "%s님, 안녕하세요!\n\n" +
              "FactSeeker에 가입해 주셔서 감사합니다.\n" +
              "이제 다양한 서비스를 이용하실 수 있습니다.\n\n" +
              "궁금한 사항이 있으시면 언제든 문의해주세요.\n\n" +
              "감사합니다.\n" +
              "FactSeeker 팀", username
      );

      sendSimpleEmail(email, subject, content);
      log.info("웰컴 이메일 전송: {}", email);
    } catch (Exception e) {
      log.error("웰컴 이메일 전송에 실패했습니다: {}", email, e);
    }
  }

  private void sendSimpleEmail(String to, String subject, String content) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, false);

      mailSender.send(message);
    } catch (MessagingException e) {
      log.error("이메일 발송에 실패했습니다: {}", to, e);
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }

}
