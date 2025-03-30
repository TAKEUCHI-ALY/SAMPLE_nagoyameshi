package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	@Value("${stripe.premium-plan-price-id}")
	private String premiumPlanPricedId;
	
	private final UserService userService;
	private final StripeService stripeService;
	
	public SubscriptionController(UserService userService, StripeService stripeService) {
		this.userService = userService;
		this.stripeService = stripeService;
	}
	
	
//	有料プランの登録ページを表示
	@GetMapping("/register")
	public String register() {
		return "subscription/register";
	}
	
//	顧客の作成
	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId, RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		
//		ユーザーのstripeCustomerIdがnullの場合
		if (user.getStripeCustomerId() == null) {
			try {
				Customer customer = stripeService.createCustomer(user);
				
				userService.saveStripeCustomerId(user, customer.getId());
			} catch (StripeException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランの登録に失敗しました。再度お試しください。");
				
				return "redirect:/";
			}
			
		}
		
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			
			stripeService.createSubscription(stripeCustomerId, premiumPlanPricedId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの登録に失敗しました。再度お試しください。");
			
			return "redirect:/";
		}
		
//		ユーザーのロールを変更する
		userService.updateRole(user, "ROLE_PAID_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_PAID_MEMBER");
		
		redirectAttributes.addFlashAttribute("successMessage", "有料プランの登録が完了しました。");
		
		return "redirect:/";
		
	}
	
	
//	支払方法変更ページの表示
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, Model model) {
		User user = userDetailsImpl.getUser();
		
		try {
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(user.getStripeCustomerId());
			
			
			model.addAttribute("card", paymentMethod.getCard());
			model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払方法を取得できませんでした。再度お試しください。");
			
			return "redirect:/";
		}
		
		return "subscription/edit";
	}
	
//	支払方法の変更と登録
	@PostMapping("/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId, RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			String currentDefaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(stripeCustomerId);
			
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			
			stripeService.detachPaymentMethodFromCustomer(currentDefaultPaymentMethodId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払方法の変更に失敗しました。再度お試しください。");
			
			return "redirect:/";
		}
		
		redirectAttributes.addFlashAttribute("successMessage", "お支払方法を変更しました。");
		
		return "redirect:/";
		
	}
	
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}
	
	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		
		try {
			List<Subscription> subscriptions = stripeService.getSubscriptions(user.getStripeCustomerId());
			
			stripeService.cancelSubscriptions(subscriptions);
			
			String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			
			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");
			
			return "redirect:/";
		}
		
		userService.updateRole(user, "ROLE_FREE_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_FREE_MEMBER");
		
		redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");
		
		return "redirect:/";
		
	}

}
