package com.example.scrapping.controller;

import com.example.scrapping.Scrapping;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("scrape")
public class Scrape {

    @GetMapping("/standardPage")
    public ResponseEntity<String> scrapeAPage() throws IOException {

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        String searchUrl = "https://olx.ro/oferte/q-klr-650/";

        HtmlPage page = client.getPage(searchUrl);
        List<HtmlElement> items = page.getByXPath("//div[@data-cy='l-card']");

        for (HtmlElement htmlItem : items) {

            HtmlElement spanPrice = ((HtmlElement) htmlItem.getFirstByXPath(".//a[@class='css-rc5s2u']"));
//            String itemName = itemAnchor.asNormalizedText();
            String itemUrl = spanPrice.getAttribute("href");


            System.out.println("HELLO WORLD");
        }


        System.out.println("HELLO WORLD");
        return new ResponseEntity<>("This is the response", HttpStatus.ACCEPTED);
    }
}
