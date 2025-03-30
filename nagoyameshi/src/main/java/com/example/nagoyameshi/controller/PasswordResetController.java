package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.event.PasswordResetEventPublisher;
import com.example.nagoyameshi.form.PasswordEditForm;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.PasswordResetService;
import com.example.nagoyameshi.service.PasswordResetTokenService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/passwordreset")
public class PasswordResetController {
	private final PasswordResetEventPublisher passwordResetEventPublisher;
	private final PasswordResetTokenService passwordResetTokenService;
	private final UserRepository userRepository;
	private final PasswordResetService passwordResetService;
	private final PasswordEncoder passwordEncoder;
	
	public PasswordResetController(PasswordResetEventPublisher passwordResetEventPublisher, PasswordResetTokenService passwordResetTokenService, UserRepository userRepository, PasswordResetService passwordResetService,  PasswordEncoder passwordEncoder) {
		this.passwordResetEventPublisher = passwordResetEventPublisher;
		this.passwordResetTokenService = passwordResetTokenService;
		this.userRepository = userRepository;
		this.passwordResetService = passwordResetService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@GetMapping
	public String passwordreset(Model model) {
		model.addAttribute("passwordResetForm", new PasswordResetForm());
		
		return "password/reset";
	}
	
	@PostMapping
	public String postMail(PasswordResetForm passwordResetForm, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
		String requestUrl = new String(httpServletRequest.getRequestURL());
		String email = passwordResetForm.getEmail();
	    User user = userRepository.findByEmail(email);
		
	    if(user != null) {
	    	passwordResetEventPublisher.publishPasswordResetEvent(user, requestUrl, email);
			redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスにパスワード再設定用URLを送信しました。メールに記載されているリンクをクリックし、パスワードを再設定してください"); 
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "入力されたメールアドレスに関連付けられたアカウントが見つかりませんでした。");
	    }
	    
		
		return "redirect:/";
	}
	
	@GetMapping("/edit")
	public String edit (@RequestParam(name = "token") String token, Model model) {
		PasswordResetToken passwordResetToken = passwordResetTokenService.getPasswordResetToken(token);
		
		
		if(passwordResetToken != null) {
			String successMessage = "パスワードを再設定してください。";
			model.addAttribute("successMessage", successMessage);
			PasswordEditForm form = new PasswordEditForm();
	        form.setToken(token); // トークンをフォームに設定
	        model.addAttribute("passwordEditForm", form);
		} else {

			String errorMessage = "トークンが無効です。";
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:/";
		}
		
		return "password/edit";
	}
	
	@PostMapping("/update")
    public String updatePassword(@ModelAttribute @Validated PasswordEditForm passwordEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes,  Model model) {
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("passwordEditForm", passwordEditForm);
            return "password/edit";
        }
		
		User user = passwordResetService.getUserByPasswordResetToken(passwordEditForm.getToken());
		
		passwordResetService.updatePassword(passwordEditForm, passwordEncoder, user);
		redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました。");
        
        return "redirect:/login";
    }

}
