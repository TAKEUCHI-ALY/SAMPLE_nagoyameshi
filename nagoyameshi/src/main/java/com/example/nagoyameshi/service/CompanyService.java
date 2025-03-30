package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.repository.CompanyRepository;

@Service
public class CompanyService {
	private final CompanyRepository companyRepository;
	
	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
//	最もIDの大きい会社概要を取得する
	public Company findFirstCompanyByOrderByIdDesc() {
		return companyRepository.findFirstByOrderByIdDesc();
	}
	
//	EditFormに登録された内容をcompaniesテーブルに上書きする
	@Transactional
	public void update(CompanyEditForm companyEditForm, Company company) {
		
		company.setName(companyEditForm.getName());
		company.setPostalCode(company.getPostalCode());
		company.setAddress(companyEditForm.getAddress());
		company.setRepresentative(companyEditForm.getRepresentative());
		company.setCapital(companyEditForm.getCapital());
		company.setBusiness(companyEditForm.getBusiness());
		company.setNumberOfEmployees(companyEditForm.getNumberOfEmployees());
		
		companyRepository.save(company);
	}

}
