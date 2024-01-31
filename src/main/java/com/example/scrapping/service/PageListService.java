package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.ModelOfManuf;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PageListService {
    private static final String BIKES = "bikes/";
    private List<Manufacturer> listOfManufacturers = new ArrayList<>();
    private static final List<String> listOfManufWUrlModel = new ArrayList<>((Arrays.asList("aprilia", "ajp")));
    private static final List<String> listOfManufWUrlRacers = new ArrayList<>((Arrays.asList("brough superior")));
    private static final List<String> listOfLinksToIgnore = new ArrayList<>((Arrays.asList("Home", "Manufacturer", "Contact", "Previous", "Classic Bikes")));

    @Autowired
    ModelDetailsService modelDetailsService;
    @Autowired
    ModelsToDataBaseService modelsToDataBaseService;
    LogsWriterSingletonService logsWriterSingletonService;
    private List<String> ignoreManufacturersList = new ArrayList<>(Arrays.asList("AJP","AJS","Aprilia"));

    // START HERE
    public void startScrapping() {
        try {

//            scrapeOneModelByUrl("BMW","F 750GS ","https://www.motorcyclespecs.co.za/model/bmw/bmw-f750gs-20.html","2020");
//            modelsToDataBaseService.existsInDB("test");

            listOfManufacturers = getListOfManufacturers();
            for (Manufacturer manufacturer : listOfManufacturers) {
                getModelsOfManuf(manufacturer);
                getModelsDetailsAndAddToDB(manufacturer);
//                logInsertedMotos(newManuf);
            }
        } catch (Exception e) {
            log.error("Something is wrong: " + e);
        }

    }

    private void scrapeOneModelByUrl(String manufName, String modelName, String url, String productionYears){
        Manufacturer manuf = new Manufacturer(manufName,"ScrapingOne");
        ModelOfManuf model = new ModelOfManuf(modelName,url,productionYears,1);
        if (!modelsToDataBaseService.existsInDB(url)) {
            manuf.addModel(model);
            getModelsDetailsAndAddToDB(manuf);
        }
    }

    private void logInsertedMotos(Manufacturer manufacturer) {
        StringBuilder sbModels = new StringBuilder();
        for (ModelOfManuf model : manufacturer.getModelsList()) {
            if (model.isInserted()) {
                sbModels.append("\"").append(manufacturer.getName()).append(" ")
                        .append(model.getName()).append(" ")
                        .append(model.getUrl()).append("\"\n");
            }
        }
        if (!sbModels.isEmpty()) {
            LogsWriterSingletonService logsWriterSingletonService = LogsWriterSingletonService.getInstance();
            String getNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy|HH:mm:ss"));
            StringBuilder sb = new StringBuilder();
            sb.append("\"").append(getNow).append("\"")
                    .append("\n{\n")
                    .append(sbModels)
                    .append("\n}");
            try {
                logsWriterSingletonService.logInsertedMotos(sb.toString());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void getModelsDetailsAndAddToDB(Manufacturer manufacturer) {
        try {
            modelDetailsService.getModelDetails(manufacturer);
        } catch (Exception e) {
            String error = "Some error while getting models details " + manufacturer.getName() + " " + e;
            try {
                logsWriterSingletonService.logError(error);
            } catch (Exception ignored){}
            System.out.println(error);
        }
    }

    private void getModelsOfManuf(Manufacturer manufacturer) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        HtmlPage page = client.getPage(manufacturer.getUrl());
        String nextButtonUrl = getNextButtonUrl(page);

        boolean canScrapPage = true;
        int pageCounter = 1;
        while (canScrapPage) {
//            System.out.println("--------------------- Page: " + pageCounter + " ---------------------"); // to be deleted
            pageCounter++;
//            int modelPerPageCounter = 1;
            List<HtmlElement> items = page.getByXPath("//tr");
            for (int i = 0; i < items.size(); i++) {
                HtmlElement item = items.get(i);
                HtmlElement anchor = item.getFirstByXPath(".//font/a");

                if (anchor == null) anchor = item.getFirstByXPath(".//p/a");
                if (anchor == null) anchor = item.getFirstByXPath(".//span/a");
                if (anchor == null) anchor = item.getFirstByXPath(".//a");

                if (anchor != null) {
                    String modelName = anchor.getTextContent();
                    modelName = modelName.replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "").trim();
                    if (modelName.isEmpty()) continue;
//                    if (listOfLinksToIgnore.contains(modelName)) continue; // TODO remove

                    String productionYears = "";
                    String semiLink = anchor.getAttribute("href");
                    String url = composeUrlOfModel(manufacturer, semiLink);

                    // check if model already exists in DB
                    if (modelsToDataBaseService.existsInDB(url)) continue;

                    List<HtmlTableCell> cells = item.getByXPath(".//td");
                    int cellNr = 1;
                    for (HtmlElement cell : cells) {
                        if (cellNr == 2) {
                            productionYears = cell.getTextContent().replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll(" ", "");
                            if (productionYears.length() > 10) {
                                productionYears = "";
                            }
                        }
                        if (cellNr > 2) break;
                        cellNr++;
                    }
                    if (productionYears.isEmpty()) continue;

                    ModelOfManuf modelOfManuf = new ModelOfManuf(modelName, url, productionYears, pageCounter);
                    manufacturer.addModel(modelOfManuf);

//                    if (modelOfManuf.getUrl().isEmpty()) continue;
//                    System.out.println(modelPerPageCounter + ". " + modelOfManuf); // to be deleted
//                    modelPerPageCounter++; // to be deleted
                }
            }
            canScrapPage = !nextButtonUrl.isEmpty();
            if (canScrapPage) {
                page = client.getPage(nextButtonUrl);
                nextButtonUrl = getNextButtonUrl(page);
            }
//                manufacturer.printModels();
        }
//            modelDetailsService.getModelDetails(manufacturer);
    }

    private String getNextButtonUrl(HtmlPage page) {
        List<HtmlElement> items = page.getByXPath("//a");
        String nextButtonUrl = "";
        for (HtmlElement htmlElement : items) {
            String textContent = htmlElement.getTextContent();
            if (!textContent.equals("Next")) continue;
            String semiLink = htmlElement.getAttribute("href");
            nextButtonUrl = Constants.MOTORCYCLESPECS_CO_ZA + BIKES + semiLink;
            break;
        }
        return nextButtonUrl;
    }

    private List<Manufacturer> getListOfManufacturers() throws IOException {
        List<Manufacturer> resultListOfManufacturers = new ArrayList<>();
//        WebClient client = new WebClient();
//        client.getOptions().setCssEnabled(false);
//        client.getOptions().setJavaScriptEnabled(false);
        HtmlPage page;

        try(WebClient client = new WebClient()) {
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            page = client.getPage(Constants.MOTORCYCLESPECS_CO_ZA);
        } catch (FailingHttpStatusCodeException e) {
            log.error(String.valueOf(e));
            return null;
        }
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
            if (manufName.isBlank() || manufName.contains("Complete Manufacturer") ||
                    ignoreManufacturersList.contains(manufName)) continue;

            Manufacturer manufacturer = new Manufacturer(manufName, composeUrlOfManuf(semiLink));
            resultListOfManufacturers.add(manufacturer);
        }
        return resultListOfManufacturers;
    }

    private String composeUrlOfModel(Manufacturer manufacturer, String semiLink) {
        String result;
        String manufUrl;
        String linkHalf2;
        String linkHalf1;
//        if (listOfManufWUrlRacers.contains(manufacturer.getName().toLowerCase())) {
        linkHalf1 = Constants.MOTORCYCLESPECS_CO_ZA;
        linkHalf2 = semiLink.substring(semiLink.indexOf("/") + 1);
//            result = linkHalf1 + linkHalf2;
//            return result;
//        }
//        manufUrl = manufacturer.getUrl();
//        linkHalf2 = semiLink.substring(semiLink.lastIndexOf("/") + 1);
//        linkHalf1 = manufUrl.substring(0, manufUrl.lastIndexOf(".")) + "/";

//        if (listOfManufWUrlModel.contains(manufacturer.getName().toLowerCase())) {
//            manufUrl = manufacturer.getUrl(); // weren't here
//            linkHalf2 = semiLink.substring(semiLink.lastIndexOf("/") + 1); // weren't here
//            linkHalf1 = manufUrl.substring(0, manufUrl.lastIndexOf(".")) + "/"; // weren't here
//            linkHalf1 = linkHalf1.replaceFirst("/bikes/", "/model/");
//        }
        result = linkHalf1 + linkHalf2;
        return result;
    }

    private String composeUrlOfManuf(String semiLink) {
        String manuf = semiLink.substring(semiLink.lastIndexOf("/") + 1);
        return Constants.MOTORCYCLESPECS_CO_ZA + BIKES + manuf;
    }

}
