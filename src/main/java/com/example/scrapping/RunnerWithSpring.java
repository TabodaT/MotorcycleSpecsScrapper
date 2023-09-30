package com.example.scrapping;

import com.example.scrapping.service.PageListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class RunnerWithSpring implements CommandLineRunner {

    @Autowired
    PageListService pageListService;

    @Override
    public void run(String... args) throws Exception {
		pageListService.getAllUserNames();
    }
}
