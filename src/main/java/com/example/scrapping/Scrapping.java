package com.example.scrapping;

import com.example.scrapping.DAO.DAO;
import com.example.scrapping.config.MyDataBaseService;
import com.example.scrapping.model.User;
import com.example.scrapping.service.PageListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;


@SpringBootApplication
public class Scrapping {

	private static DAO<User> dao;

	public Scrapping(DAO<User> dao){
		this.dao = dao;
	}

//	@Autowired
//	PageListService pageListService;
	public static void main(String[] args) throws Exception {
		PageListService pageListService = new PageListService();
		MyDataBaseService myDataBaseService = new MyDataBaseService();

		SpringApplication.run(Scrapping.class, args);
//		pageListService.getListOfPostsFromPage();
//		pageListService.run();
//		myDataBaseService.run();

		System.out.println("All users --------------------");
		List<User> users = dao.list();
		users.forEach(System.out::println);

	}

}
