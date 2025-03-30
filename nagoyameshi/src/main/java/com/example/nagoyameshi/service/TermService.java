package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.repository.TermRepository;

@Service
public class TermService {
	private final TermRepository termRepository;
	
	public TermService(TermRepository termRepository) {
		this.termRepository = termRepository;
	}
	
//	最もIDの大きい利用規約を取得する
	public Term findFirstTermByOrderByIdDesc() {
		return termRepository.findFirstByOrderByIdDesc();
	}
	
//	EditFormに入力された内容をtermsテーブルに上書きする
	public void update(TermEditForm termEditForm, Term term) {
		
		term.setContent(termEditForm.getContent());
		
		termRepository.save(term);
	}

}
