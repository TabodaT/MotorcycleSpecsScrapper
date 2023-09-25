package com.example.scrapping.controller;

import com.example.scrapping.service.PageListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("scrape")
@Slf4j
public class ScraperController {

    @Autowired
    PageListService pageListService;

    @GetMapping("/standardPage")
    public ResponseEntity<String> scrapeAPage() {
        try {
            pageListService.getListOfPostsFromPage();
        } catch (IOException e){
            log.warn("Couldn't get page " + e);
            return new ResponseEntity<>("Couldn't get page", HttpStatus.FAILED_DEPENDENCY);
        }
        return new ResponseEntity<>("WORKED!", HttpStatus.ACCEPTED);
    }
}
