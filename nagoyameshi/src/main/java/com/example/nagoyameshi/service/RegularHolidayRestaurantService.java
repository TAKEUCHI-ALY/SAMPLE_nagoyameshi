package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@Service
public class RegularHolidayRestaurantService {
	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final RegularHolidayService regularHolidayService;
	
	public RegularHolidayRestaurantService(RegularHolidayRestaurantRepository regularHolidayRestaurantRepository, RegularHolidayService regularHolidayService) {
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.regularHolidayService = regularHolidayService;
	}
	
//	指定した店舗の定休日IDを取得
	public List<Integer> findRegularHolidayIdsByRestaurant(Restaurant restaurant){
		return regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(restaurant);
	}
	
//	店舗登録フォームから送信されたデータをregular_holiday_restaurantテーブルに登録する
	@Transactional
	public void createRegularHolidaysRestaurants(List<Integer> regularHolidayIds, Restaurant restaurant) {
		for(Integer regularHolidayId : regularHolidayIds) {
			if(regularHolidayId != null) {
				Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(regularHolidayId);
				
				if (optionalRegularHoliday.isPresent()) {
					RegularHoliday regularHoliday = optionalRegularHoliday.get();
					
					Optional<RegularHolidayRestaurant> optionalCurrentRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRegularHolidayAndRestaurant(regularHoliday, restaurant);
					
					if (optionalCurrentRegularHolidayRestaurant.isEmpty()) {
						RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
						regularHolidayRestaurant.setRegularHoliday(regularHoliday);
						regularHolidayRestaurant.setRestaurant(restaurant);
						
						regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
					}
				}
			}
		}
	}
	
//	店舗編集フォームから送信されたデータをregular_holiday_restaurantテーブルへ上書きする
	@Transactional
	public void syncRegularHolidaysRestaurants(List<Integer> newRegularHolidayIds, Restaurant resutaurant) {
		List<RegularHolidayRestaurant> currentRegularHolidaysRestaurants = regularHolidayRestaurantRepository.findByRestaurant(resutaurant);
		
		if (newRegularHolidayIds == null) {
			for(RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidaysRestaurants) {
				regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
			}
		} else {
			for (RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidaysRestaurants) {
				if (!newRegularHolidayIds.contains(currentRegularHolidayRestaurant.getRegularHoliday().getId())) {
					regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
				}
			}
			
			for (Integer newRegularHolidayId : newRegularHolidayIds) {
				if (newRegularHolidayId != null) {
					Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(newRegularHolidayId);
					
					if (optionalRegularHoliday.isPresent()) {
						RegularHoliday regularHoliday = optionalRegularHoliday.get();
						
						Optional<RegularHolidayRestaurant> optionalCurrentRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRegularHolidayAndRestaurant(regularHoliday, resutaurant);
						
						if(optionalCurrentRegularHolidayRestaurant.isEmpty()) {
							RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
							regularHolidayRestaurant.setRestaurant(resutaurant);
							regularHolidayRestaurant.setRegularHoliday(regularHoliday);
							
							regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
	
						}
					}
				}
			}
		}
	}

}
