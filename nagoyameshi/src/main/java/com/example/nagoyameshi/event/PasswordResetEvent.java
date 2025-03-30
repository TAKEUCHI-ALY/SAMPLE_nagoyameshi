package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEvent;

import com.example.nagoyameshi.entity.User;

import lombok.Getter;

@Getter
public class PasswordResetEvent extends ApplicationEvent {
	private User user;
	private String requestUrl;
	private final String requestEmail;
	
	public PasswordResetEvent(Object source, User user, String requestUrl, String requestEmail) {
		super(source);
		
		this.user = user;
		this.requestUrl = requestUrl;
		this.requestEmail = requestEmail;
	}

}
