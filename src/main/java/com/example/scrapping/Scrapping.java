package com.example.scrapping;

import com.example.scrapping.service.PageListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Scrapping {
//	@Autowired
//	PageListService pageListService;
	public static void main(String[] args) throws Exception {
		PageListService pageListService = new PageListService();

		SpringApplication.run(Scrapping.class, args);
//		pageListService.getListOfPostsFromPage();
//		myDataBaseService.run();

	}

}
