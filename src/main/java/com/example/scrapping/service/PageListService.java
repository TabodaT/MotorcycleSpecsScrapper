package com.example.scrapping.service;

import com.example.scrapping.dto.ListPostingDTO;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PageListService {
    private static final String MOTORCYCLESPECS_CO_ZA = "https://www.motorcyclespecs.co.za/";

    private static final String BIKES = "bikes/";
    private List<ListPostingDTO> listPostingDTOS = new ArrayList<>(); // to be deleted
    private List<Manufacturer> listOfManufacturers = new ArrayList<>();
    //    Map<String,String> listOfManufacturers = new TreeMap<>();
    private static final List<String> listOfMotorcyclesWithDifferentUrlFormat = new ArrayList<>((Arrays.asList("aprilia")));
    private static final List<String> listOfLinksToIgnore = new ArrayList<>((Arrays.asList("Home","Manufacturer","Contact","Previous","Classic Bikes")));

    @Autowired
    ModelDetailsService modelDetailsService;
    public void startScrapping() {
        try {
            getListOfManufacturers();
            getModels();
        } catch (Exception e) {
            log.error("Something is wrong: " + e);
        }

    }

    private void getModels() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        for (Manufacturer manufacturer : listOfManufacturers) {
            if (!manufacturer.getName().equals("BMW")) continue;    // to be deleted

            HtmlPage page = client.getPage(manufacturer.getUrl());
            String nextButtonUrl = getNextButtonUrl(page);
            boolean canScrapPage = true;
            int pageCounter = 1; // to be deleted
            while (canScrapPage) {
                System.out.println("--------------------- Page: "+pageCounter+" ---------------------"); // to be deleted
                pageCounter++; // to be deleted
                List<HtmlElement> items = page.getByXPath("//td");
                int modelPerPageCounter = 1;
                for (HtmlElement item : items) {
                    HtmlElement anchor = item.getFirstByXPath(".//font/a");

                    if (anchor == null) anchor = item.getFirstByXPath(".//p/a");
                    if (anchor == null) anchor = item.getFirstByXPath(".//span/a");
                    if (anchor == null) anchor = item.getFirstByXPath(".//a");

                    if (anchor != null) {
                        String modelName = anchor.getTextContent();
                        modelName = modelName.replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ","").trim();
                        if (listOfLinksToIgnore.contains(modelName)) continue;
                        String semiLink = anchor.getAttribute("href");
                        String url = composeUrlOfModel(manufacturer, semiLink);
                        Model model = new Model(modelName, url);
                        manufacturer.addModel(model);
                        if (model.getUrl().isEmpty() || model.getName().isEmpty()) { // to be deleted
                            continue;
                        }
                        System.out.println(modelPerPageCounter + ". " + model); // to be deleted
                        modelPerPageCounter++; // to be deleted
                    }
                }
                canScrapPage = !nextButtonUrl.isEmpty();
                if (canScrapPage) {
                    page = client.getPage(nextButtonUrl);
                    nextButtonUrl = getNextButtonUrl(page);
                }
//                manufacturer.printModels();
                System.out.println("HERE ---------------------------------"); // to be deleted
            }
            modelDetailsService.getModelDetails(manufacturer);

            System.out.println("Here1");// to be deleted
        }
        System.out.println("Here2");// to be deleted
    }

    private String getNextButtonUrl(HtmlPage page) {
        List<HtmlElement> items = page.getByXPath("//a");
        String nextButtonUrl = "";
        for (HtmlElement htmlElement : items) {
            String textContent = htmlElement.getTextContent();
            if (!textContent.equals("Next")) continue;
            String semiLink = htmlElement.getAttribute("href");
            nextButtonUrl = MOTORCYCLESPECS_CO_ZA + BIKES + semiLink;
            break;
        }
        return nextButtonUrl;
    }

    private void getListOfManufacturers() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        HtmlPage page = client.getPage(MOTORCYCLESPECS_CO_ZA);

        HtmlElement theDivWithManuf = page.getFirstByXPath("//div[@class='subMenu']");

        Iterable<DomElement> linksOfManuf = theDivWithManuf.getChildElements();
        boolean startRecordingManufacturersLinks = false;
        for (DomElement element : linksOfManuf) {
            String semiLink = element.getAttribute("href");
            if (semiLink.contains("AJP.htm")) {
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

    private String composeUrlOfModel(Manufacturer manufacturer, String semiLink) {
        String manufUrl = manufacturer.getUrl();
        String url = semiLink.substring(semiLink.lastIndexOf("/") + 1);
        String prefix = manufUrl.substring(0, manufUrl.lastIndexOf(".")) + "/";
        if (listOfMotorcyclesWithDifferentUrlFormat.contains(manufacturer.getName().toLowerCase())) {
            prefix = prefix.toLowerCase();
        }
        return prefix.replaceFirst("bikes", "model") + url;
    }

    private String composeUrlOfManuf(String semiLink) {
        String manuf = semiLink.substring(semiLink.lastIndexOf("/") + 1);
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
