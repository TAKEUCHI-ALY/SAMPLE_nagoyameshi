package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.PasswordResetTokenService;

@Component
public class PasswordResetEventListener {
	private final PasswordResetTokenService passwordResetTokenService;
	private final JavaMailSender javaMailSender;
	
	public PasswordResetEventListener(PasswordResetTokenService passwordResetTokenService, JavaMailSender javaMailSender) {
		this.passwordResetTokenService = passwordResetTokenService;
		this.javaMailSender = javaMailSender;
	}
	
	@EventListener
	private void onPasswordResetEvent(PasswordResetEvent passwordResetEvent) {
		User user = passwordResetEvent.getUser();
		String token = UUID.randomUUID().toString();
		passwordResetTokenService.create(user, token);
		
		String recipientAddress = passwordResetEvent.getRequestEmail();
		String subject = "パスワード再設定";
		String confirmationUrl = passwordResetEvent.getRequestUrl() + "/edit?token=" + token;
		String message = "以下のリンクをクリックしてパスワードを再設定してください。";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}

}
