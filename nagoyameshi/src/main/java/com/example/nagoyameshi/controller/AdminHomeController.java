package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.UserService;

@Controller
public class AdminHomeController {
	private final UserService userService;
	private final RestaurantService restaurantService;
	private final ReservationService reservationService;
	
	public AdminHomeController(UserService userService, RestaurantService restaurantService, ReservationService reservationService) {
		this.userService = userService;
		this.restaurantService = restaurantService;
		this.reservationService = reservationService;
	}
	
	@GetMapping("/admin/home")
	public String index(Model model) {
		
		long totalFreeMembers = userService.countUserByRole_Name("ROLE_FREE_MEMBER");
		long totalPaidMembers = userService.countUserByRole_Name("ROLE_PAID_MEMBER");
		long totalMembers = totalFreeMembers + totalPaidMembers;
		long totalRestaurants = restaurantService.countRestaurant();
		long totalReservations = reservationService.countReservations();
		long salesForThisMonth = 300 * totalPaidMembers;
		
		model.addAttribute("totalFreeMembers", totalFreeMembers);
		model.addAttribute("totalPaidMembers", totalPaidMembers);
		model.addAttribute("totalMembers", totalMembers);
		model.addAttribute("totalRestaurants", totalRestaurants);
		model.addAttribute("totalReservations", totalReservations);
		model.addAttribute("salesForThisMonth", salesForThisMonth);
		
		return "admin/index";
	}
}
