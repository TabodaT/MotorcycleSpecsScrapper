package com.example.scrapping.controller;

import com.example.scrapping.DAO.UserRepository;
import com.example.scrapping.service.PageListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/scrape")
@Slf4j
public class ScraperController {

    @Autowired
    PageListService pageListService;

    @Autowired
    UserRepository userRepository;

//    @GetMapping
//    public List<String> testApi() {
//        return pageListService.getAllUserNames();
//    }

    @GetMapping("/getusernames")
    public List<String> getAllUserNames() {
        return userRepository.getAllUserNames();
    }

    @GetMapping("/scrape-a-page")
    public ResponseEntity<String> scrapeAPage() {
        pageListService.startScrapping();
        return new ResponseEntity<>("WORKED!", HttpStatus.ACCEPTED);
    }
}
