package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantService {
	private final RestaurantRepository restaurantRepository;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	
	public RestaurantService(RestaurantRepository restaurantRepository, CategoryRestaurantService categoryRestaurantService, RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}
	
//	指定したIDを持つ店舗を取得する
	public Optional<Restaurant> findRestaurantById(Integer id) {
		return restaurantRepository.findById(id);
	}
//	店舗のレコード数を取得する
	public long countRestaurant() {
		return restaurantRepository.count();
	}
	
//	店舗登録機能
	@Transactional
	public void create(RestaurantRegisterForm restaurantRegisterForm) {
		Restaurant restaurant = new Restaurant();
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		List<Integer> categoryIds = restaurantRegisterForm.getCategoryIds();
		List<Integer> regularHolidayIds = restaurantRegisterForm.getRegularHolidayIds();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImageName(hashedImageName);
		}
		
		restaurant.setName(restaurantRegisterForm.getName());
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
		restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
		restaurant.setAddress(restaurantRegisterForm.getAddress());
		restaurant.setOpeningTime(restaurantRegisterForm.getOpeningTime());
		restaurant.setClosingTime(restaurantRegisterForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantRegisterForm.getSeatingCapacity());
		
		restaurantRepository.save(restaurant);
		
		if(categoryIds != null) {
			categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);
		}
		
		if (regularHolidayIds != null) {
			regularHolidayRestaurantService.createRegularHolidaysRestaurants(regularHolidayIds, restaurant);
		}
	}
	
//	店舗更新機能
	@Transactional
	public void update(RestaurantEditForm restaurantEditForm) {
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantEditForm.getId());
		MultipartFile imageFile = restaurantEditForm.getImageFile();
		List<Integer> categoryIds = restaurantEditForm.getCategoryIds();
		List<Integer> regularHolidayIds = restaurantEditForm.getRegularHolidayIds();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImageName(hashedImageName);
		}
		
		restaurant.setName(restaurantEditForm.getName());
		restaurant.setDescription(restaurantEditForm.getDescription());
		restaurant.setLowestPrice(restaurant.getLowestPrice());
		restaurant.setHighestPrice(restaurant.getHighestPrice());
		restaurant.setPostalCode(restaurantEditForm.getPostalCode());
		restaurant.setAddress(restaurantEditForm.getAddress());
		restaurant.setOpeningTime(restaurantEditForm.getOpeningTime());
		restaurant.setClosingTime(restaurantEditForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantEditForm.getSeatingCapacity());
		
		restaurantRepository.save(restaurant);
		
		categoryRestaurantService.syncCategoriesRestaurants(categoryIds, restaurant);
		regularHolidayRestaurantService.syncRegularHolidaysRestaurants(regularHolidayIds, restaurant);
	}
	
//	UUIDを使って生成したファイル名を返す
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length -1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}
	
//	画像ファイルを指定したファイルにコピーする
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	価格が正しく設定されているかチェック
	public boolean isValidPrices(Integer lowestPrice, Integer highestPrice) {
		return highestPrice >= lowestPrice;
	}
	
//	営業時間が正しく入力されているかチェック
	public boolean isValidOpeningHours(LocalTime openingTime, LocalTime closingTime) {
		return openingTime.isBefore(closingTime);
	}
	
//	全ての店舗をページングされた状態で取得
	public Page<Restaurant> findAllOrderByCreatedAtDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);
	}

//  全ての店舗を最低価格が安い順に並べる。
	public Page<Restaurant> findAllRestaurantsByOrderByLowestPriceAsc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
	}
	
//	全ての店舗を平均評価が高い順に並び替える。
	public Page<Restaurant> findAllRestaurantsByOrderByAverageScoreDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByAverageScoreDesc(pageable);
	}
	
//	全ての店舗を予約数が多い順に並び替える
	public Page<Restaurant> findAllRestaurantsByOrderByReservationCountDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByReservationCountDesc(pageable);
	}
	
//	指定されたキーワード名を含む店舗を作成日時が新しい順に並び替える
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(nameKeyword, addressKeyword, categoryNameKeyword, pageable);
	}
	
//	指定されたキーワードを含む店舗を最低価格が安い順に並べる
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(nameKeyword, addressKeyword, categoryNameKeyword, pageable);
	}
	
//	指定されたキーワードを含む店舗を平均評価が高い順に並べる
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable){
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(nameKeyword, addressKeyword, categoryNameKeyword, pageable);
		
	}
	
//	指定されたキーワードを含む店舗を予約数が多い順に並び替える
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(nameKeyword, addressKeyword, categoryNameKeyword, pageable);
	}
	
//	指定されたカテゴリIDの店舗を作成日時が新しい順に並べる
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByCreatedAtDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
	}
	
//	指定されたカテゴリIDの店舗を最低価格が安い順に並べる
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByLowestPriceAsc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
	}
	
//	指定されたカテゴリIDの店舗を平均評価が高い順に並べる
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByAverageScoreDesc(Integer categoryId, Pageable pageable){
		return restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(categoryId, pageable);
	}
	
//	指定されたカテゴリIDの店舗を予約数が多い順に並び替える
	public Page<Restaurant> findRestaurantsByCategoryIdOrderByReservationCountDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByReservationCountDesc(categoryId, pageable);
	}
	
//	指定された最低価格以下の店舗を作成日時が新しい順に並べる
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
	}
	
//	指定された最低価格以下の店舗を最低価格が安い順に並べる
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
	}
	
//	指定された最低価格以下の店舗を平均評価が高い順に並べる
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByAverageScoreDesc(Integer price, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(price, pageable);
	}
	
//	指定された最低価格以下の店舗を予約数が多い順に並び替える
	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByReservationCountDesc(Integer price, Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByReservationCountDesc(price, pageable);
	}
	
//	指定された店舗の定休日のday_indexフィールドの値をリストで取得する。
	public List<Integer> findDayIndexesByRestaurantId(Integer restaurantId) {
		return restaurantRepository.findDayIndexByRestaurantId(restaurantId);
	}
}
