package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
	public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
		this.passwordResetTokenRepository = passwordResetTokenRepository;
	}
	
	@Transactional
	public void create(User user, String token) {
		PasswordResetToken passwordResetToken = new PasswordResetToken();
		
		passwordResetToken.setUser(user);
		passwordResetToken.setToken(token);
		
		passwordResetTokenRepository.save(passwordResetToken);
	}
	
	public PasswordResetToken getPasswordResetToken(String token) {
		return passwordResetTokenRepository.findByToken(token);
	}

}
