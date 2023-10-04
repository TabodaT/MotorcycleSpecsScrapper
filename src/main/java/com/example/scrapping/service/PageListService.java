package com.example.scrapping.service;

import com.example.scrapping.dto.ListPostingDTO;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PageListService {
    private static final String MOTORCYCLESPECS_CO_ZA = "https://www.motorcyclespecs.co.za/";

    private static final String BIKES = "bikes/";
    List<ListPostingDTO> listPostingDTOS = new ArrayList<>(); // to be deleted
    List<Manufacturer> listOfManufacturers = new ArrayList<>();
//    Map<String,String> listOfManufacturers = new TreeMap<>();

    public void startScrapping() {
        try {
            getListOfManufacturers();
            getModels();
        } catch (Exception e){
            log.error("Something is wrong: " + e);
        }

    }

    private void getModels() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        for (Manufacturer manufacturer : listOfManufacturers){
            HtmlPage page = client.getPage(manufacturer.getUrl());

//            List<HtmlElement> items = page.getByXPath("//td[@class='auto-style4']");
            List<HtmlElement> items = page.getByXPath("//a[@target='_self']");
            boolean startRecordingModels = false;
            for (HtmlElement htmlElement : items){
//                HtmlElement anchor = htmlElement.getFirstByXPath("//a");

                String modelName = htmlElement.getTextContent();
                if (modelName.contains("Home")) {
                    startRecordingModels = true;
                    continue;
                }
                if (!startRecordingModels) continue;

                String semiLink = htmlElement.getAttribute("href");
                String url = composeUrlOfModel(manufacturer.getUrl(),semiLink);
                Model model = new Model(modelName, url);
                manufacturer.getModelsList().add(model);

                System.out.println(semiLink);
            }
        }


    }

    private void getListOfManufacturers() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        HtmlPage page = client.getPage(MOTORCYCLESPECS_CO_ZA);

        HtmlElement theDivWithManuf = page.getFirstByXPath("//div[@class='subMenu']");

        Iterable<DomElement> linksOfManuf = theDivWithManuf.getChildElements();
        boolean startRecordingManufacturersLinks = false;
        for (DomElement element : linksOfManuf){
            String semiLink = element.getAttribute("href");
            if (semiLink.contains("AJP.htm")){
                startRecordingManufacturersLinks = true;
            }

            if (!startRecordingManufacturersLinks) continue;

            String manufName = element.getTextContent();
            if (manufName.isBlank() || manufName.contains("Complete Manufacturer")) continue;

            Manufacturer manufacturer = new Manufacturer(manufName, composeUrlOfManuf(semiLink));
            listOfManufacturers.add(manufacturer);
        }
//        listOfManufacturers.forEach(System.out::println);
//        System.out.println("Here");

        /* ------------------------------------------------------- */
        /* ------------------------------------------------------- */ // to be deleted
        if (false) {
            List<HtmlElement> items = page.getByXPath("//div[@class='subMenu']");

            boolean firstCard = true;
            int it = 0;
            for (HtmlElement htmlItem : items) {
                ListPostingDTO listPostingDTO = new ListPostingDTO();
                String href = htmlItem.getAttribute("href");
                System.out.println(href);
                HtmlElement divCard = (HtmlElement) htmlItem.getFirstChild();

                // Title
//            listPostingDTO.setTitle(titles.get(it).asNormalizedText());

                // URL
                if (firstCard) {
                    HtmlElement urlFirst = htmlItem.getFirstByXPath("//a[@class='css-rc5s2u']");
                    listPostingDTO.setUrl(MOTORCYCLESPECS_CO_ZA + urlFirst.getAttribute("href"));
                    firstCard = false;
                } else {
                    listPostingDTO.setUrl(MOTORCYCLESPECS_CO_ZA + divCard.getAttribute("href"));
                }

                // id
                listPostingDTO.setId(Integer.parseInt(htmlItem.getAttribute("id")));

                // price & currency
//            setPriceAndCurrency(listPostingDTO, prices, it);

                // location and date
                try {
//                setLocationAndDate(listPostingDTO, locationsAndDates, it);
                } catch (Exception e) {
                    log.warn("Date parse exception: " + e);
                }

                listPostingDTOS.add(listPostingDTO);
                it++;
                System.out.println(listPostingDTO + "\n");
            }
            System.out.println("DONE");
        }
    }

    private String composeUrlOfModel(String manufUrl, String semiLink){
        String url = semiLink.substring(semiLink.lastIndexOf("/")+1);
        String prefix = manufUrl.substring(0,manufUrl.lastIndexOf(".")) + "/";
        return prefix.replaceFirst("bikes", "model") + url;
    }
    private String composeUrlOfManuf(String semiLink){
        String manuf = semiLink.substring(semiLink.lastIndexOf("/")+1);
        return MOTORCYCLESPECS_CO_ZA + BIKES + manuf;
    }


//    public List<String> getAllUserNames(){
//        List<String> usernameList = new ArrayList<>();
//
//        usernameList.addAll(jdbcTemplate.queryForList("SELECT fullname from user;", String.class));
//
//        usernameList.forEach(System.out::println);
//        return usernameList;
//    }

}
