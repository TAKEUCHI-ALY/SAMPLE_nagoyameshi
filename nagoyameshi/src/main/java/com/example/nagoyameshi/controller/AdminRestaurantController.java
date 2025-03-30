package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.service.CategoryRestaurantService;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RegularHolidayRestaurantService;
import com.example.nagoyameshi.service.RegularHolidayService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	private final RestaurantRepository restaurantRepository;
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayService regularHolidayService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	
	public AdminRestaurantController(RestaurantRepository restaurantRepository, RestaurantService restaurantService, CategoryService categoryService, CategoryRestaurantService categoryRestaurantService, RegularHolidayService regularHolidayService, RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayService = regularHolidayService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}
	
//	店舗一覧を検索条件に合わせて表示
	
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		Page<Restaurant> restaurantPage;
		
		if(keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			restaurantPage = restaurantRepository.findAll(pageable);
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		
		return "admin/restaurants/index";
	}
	
//	店舗詳細ページを表示
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		
		model.addAttribute("restaurant", restaurant);
		
		return "admin/restaurants/show";
	}
	
//	店舗登録フォームを表示
	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryService.findAllCategories();
		List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		model.addAttribute("categories", categories);
		model.addAttribute("regularHolidays", regularHolidays);
		return "admin/restaurants/register";
	}

//	登録フォームの内容を保存する
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes , Model model) {
		
		Integer lowestPrice = restaurantRegisterForm.getLowestPrice();
		Integer highestPrice = restaurantRegisterForm.getHighestPrice();
		LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
		LocalTime closingTime = restaurantRegisterForm.getClosingTime();
		
		if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格以下に設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格以上に設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);
		}
		
		if (openingTime != null && closingTime != null && !restaurantService.isValidOpeningHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間より前に設定してください");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間より前に設定してください");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);
		}
		
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllCategories();
			List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();
			model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
			model.addAttribute("categories", categories);
			model.addAttribute("regularHolidays", regularHolidays);
			
			return "admin/restaurants/register";
		}
		
		restaurantService.create(restaurantRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");
		return "redirect:/admin/restaurants";
	}
	
//	店舗編集ページを表示
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		String imageName = restaurant.getImageName();
		List<Integer> categoryIds = categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
		List<Integer> regularHolidayIds = regularHolidayRestaurantService.findRegularHolidayIdsByRestaurant(restaurant);
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getId(), restaurant.getName(), null, restaurant.getDescription(), restaurant.getLowestPrice(), restaurant.getHighestPrice(), restaurant.getPostalCode(), restaurant.getAddress(), restaurant.getOpeningTime(), restaurant.getClosingTime(), restaurant.getSeatingCapacity(), categoryIds, regularHolidayIds);
		
		List<Category> categories = categoryService.findAllCategories();
		List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();
		model.addAttribute("imageName", imageName);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("categories", categories);
		model.addAttribute("regularHolidays", regularHolidays);
		
		return "admin/restaurants/edit";
	}
	
//	編集内容を保存する
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		
		Integer lowestPrice = restaurantEditForm.getLowestPrice();
		Integer highestPrice = restaurantEditForm.getHighestPrice();
		LocalTime openingTime = restaurantEditForm.getOpeningTime();
		LocalTime closingTime = restaurantEditForm.getClosingTime();
		

		if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格以下に設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格以上に設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);
		}
		
		if (openingTime != null && closingTime != null && !restaurantService.isValidOpeningHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間より前に設定してください");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間より前に設定してください");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);
		}
		
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllCategories();
			List<RegularHoliday> regularHolidays = regularHolidayService.findAllRegularHolidays();
			model.addAttribute("restaurantRegisterForm", restaurantEditForm);
			model.addAttribute("categories", categories);
			model.addAttribute("regularHolidays", regularHolidays);

			return "admin/restaurants/edit";
		}
			
		restaurantService.update(restaurantEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");
		return "redirect:/admin/restaurants/{id}";		
	}
	
//	店舗情報を削除する
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id")Integer id, RedirectAttributes redirectAttributes) {
		restaurantRepository.deleteById(id);
		
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
		
		return "redirect:/admin/restaurants";
	}
}
