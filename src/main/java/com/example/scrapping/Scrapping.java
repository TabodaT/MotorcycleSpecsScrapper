package com.example.scrapping;

import com.example.scrapping.config.MyDataBaseService;
import com.example.scrapping.service.PageListService;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Scrapping {
//	@Autowired
//	PageListService pageListService;
	public static void main(String[] args) throws Exception {
		PageListService pageListService = new PageListService();
		MyDataBaseService myDataBaseService = new MyDataBaseService();

		SpringApplication.run(Scrapping.class, args);
//		pageListService.getListOfPostsFromPage();
//		pageListService.run();
		myDataBaseService.run();

	}

}
