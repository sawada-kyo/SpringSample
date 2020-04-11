package com.example.demo.login.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Component
public class GlobalControllAdvice {

	@ExceptionHandler(DataAccessException.class)
    public String dataAccessExceptionHandler(DataAccessException e, Model model) {

    	// 例外クラスのメッセージをmodelに登録
    	model.addAttribute("error", "内部サーバエラー（DB）：GlobalControllAdvice");
    	model.addAttribute("message", "DataAccessExceptionが発生しました。");

    	// HTTPのエラーコード（500）をmodelに登録
    	model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR);

    	return "error";
    }

	@ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e, Model model) {

    	// 例外クラスのメッセージをmodelに登録
    	model.addAttribute("error", "内部サーバエラー：GlobalControllAdvice");
    	model.addAttribute("message", "Exceptionが発生しました。");

    	// HTTPのエラーコード（500）をmodelに登録
    	model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR);

    	return "error";
    }

}
