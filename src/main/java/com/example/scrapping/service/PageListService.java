package com.example.scrapping.service;

import com.example.scrapping.dto.ListPostingDTO;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PageListService {
    List<ListPostingDTO> listPostingDTOS = new ArrayList<>();

    public void getListOfPostsFromPage () throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        String searchUrl = "https://olx.ro/oferte/q-klr-650/";

        HtmlPage page = client.getPage(searchUrl);
        List<HtmlElement> items = page.getByXPath("//div[@data-cy='l-card']");

        for (HtmlElement htmlItem : items) {
            ListPostingDTO listPostingDTO = new ListPostingDTO();


//            Iterable<HtmlElement> elements = htmlItem.getHtmlElementDescendants();
            HtmlElement titleElement = htmlItem.getFirstByXPath("//h6");
            String title = titleElement.asNormalizedText();
//            for (HtmlElement el : elements){
//                System.out.println(el);
//            }


            HtmlElement spanPrice = htmlItem.getFirstByXPath(".//a[@class='css-rc5s2u']");
            listPostingDTO.setUrl(spanPrice.getAttribute("href"));
            listPostingDTOS.add(listPostingDTO);
        }
        System.out.println("DONE");
    }

}
