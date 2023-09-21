package com.example.scrapping;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Scrapping {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(Scrapping.class, args);
	}

}
