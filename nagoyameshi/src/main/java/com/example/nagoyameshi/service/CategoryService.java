package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
//	指定したIDの持つカテゴリを取得する
	public Optional<Category> findCategoryById(Integer id) {
		return categoryRepository.findById(id);
	}
	
//	全てのカテゴリを取得する
	public List<Category> findAllCategories(){
		return categoryRepository.findAll();
	}
	
//	カテゴリ登録機能
	@Transactional
	public void create(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();
		
		category.setName(categoryRegisterForm.getName());
		
		categoryRepository.save(category);
	}
	
//	カテゴリ編集機能
	@Transactional
	public void update(CategoryEditForm categoryEditForm) {
		Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
		
		category.setName(categoryEditForm.getName());
		
		categoryRepository.save(category);
	}
	
//	指定したカテゴリ名を持つ最初のカテゴリを取得する
	public Category findFirstCategoryByName(String name) {
		return categoryRepository.findFirstByName(name);
	}

}
