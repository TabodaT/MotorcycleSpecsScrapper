package com.example.scrapping.service;

import com.example.scrapping.dto.ListPostingDTO;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.time.LocalTime.now;

@Service
@Slf4j
public class PageListService {
    private static final String OLX_RO = "https://www.olx.ro";
    private static final String REACTUALIZAT = "reactualizat";
    private static final String AZI = "azi";
    private static final String DATE_FORMATTER = "dd-MM-yyyy";
    List<ListPostingDTO> listPostingDTOS = new ArrayList<>();

    public void getListOfPostsFromPage() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        String searchUrl = OLX_RO + "/oferte/q-klr-650/?currency=EUR";

        HtmlPage page = client.getPage(searchUrl);
        List<HtmlElement> items = page.getByXPath("//div[@data-cy='l-card']");
        List<HtmlElement> titles = page.getByXPath("//h6[@class='css-16v5mdi er34gjf0']");
        List<HtmlElement> prices = page.getByXPath("//p[@class='css-10b0gli er34gjf0']");
        List<HtmlElement> locationsAndDates = page.getByXPath("//p[@class='css-veheph er34gjf0']");

        boolean firstCard = true;
        int it = 0;
        for (HtmlElement htmlItem : items) {
            ListPostingDTO listPostingDTO = new ListPostingDTO();
            HtmlElement divCard = (HtmlElement) htmlItem.getFirstChild();

            // Title
            listPostingDTO.setTitle(titles.get(it).asNormalizedText());
            System.out.println("Title:        " + listPostingDTO.getTitle());
            // URL
            if (firstCard) {
                HtmlElement urlFirst = htmlItem.getFirstByXPath("//a[@class='css-rc5s2u']");
                listPostingDTO.setUrl(OLX_RO + urlFirst.getAttribute("href"));
                firstCard = false;
            } else {
                listPostingDTO.setUrl(OLX_RO + divCard.getAttribute("href"));
            }
            System.out.println("URL:          " + listPostingDTO.getUrl());
            // id
            listPostingDTO.setId(htmlItem.getAttribute("id"));
            System.out.println("id:           " + htmlItem.getAttribute("id"));
            // price & currency
            setPriceAndCurrency(listPostingDTO, prices, it);
            System.out.println("Price:        " + listPostingDTO.getPrice() + listPostingDTO.getCurrency());
            System.out.println("Negociabil:   " + listPostingDTO.getNegociabil());
            // location and date
            try {
                setLocationAndDate(listPostingDTO, locationsAndDates, it);

            } catch (Exception e){
                log.warn("Date parse exception: "+ e);
            }
            System.out.println("Location:     " + listPostingDTO.getLocation());
            System.out.println("Date:         " + listPostingDTO.getDate());
            System.out.println("Reactualizat: " + listPostingDTO.getReactualizat());


            System.out.println("\n");

//            Iterable<DomNode> nodes = htmlItem.getChildren();
//            for (DomNode node : nodes) {
//
//                HtmlElement titleElement = node.get;
//                System.out.printf(titleElement.asNormalizedText());
//
//
////                Iterable<HtmlElement> elements = node.getHtmlElementDescendants();
////                System.out.println("ELEMENTS:\n");
////                for (HtmlElement el : elements) {
////                    System.out.println(el);
////                }
//                System.out.println("\n");
//
//            }


//            HtmlElement titleElement = htmlItem.getFirstByXPath("//h6");
//            listPostingDTO.setTitle(titleElement.asNormalizedText());
            // id


            listPostingDTOS.add(listPostingDTO);
            it++;
        }
        System.out.println("DONE");
    }

    private void setPriceAndCurrency(ListPostingDTO listPostingDTO, List<HtmlElement> prices, int it) {
        String pricePosting = prices.get(it).asNormalizedText().replace(" ", "");
        StringBuilder price = new StringBuilder();
        StringBuilder currency = new StringBuilder();
        int currencyIter = 0;
        for (char c : pricePosting.toCharArray()) {
            if (Character.isDigit(c)) {
                price.append(c);
            } else {
                if (c == '€') {
                    currency.append(c);
                    break;
                } else {
                    while (currencyIter <= 2) {
                        currency.append(c);
                        currencyIter++;
                    }
                }
            }
        }
        listPostingDTO.setNegociabil(pricePosting.toLowerCase().contains("nego"));
        listPostingDTO.setPrice(Integer.parseInt(price.toString()));
        listPostingDTO.setCurrency(currency.toString());
    }

    private void setLocationAndDate(ListPostingDTO listPostingDTO, List<HtmlElement> locationsAndDates, int it) throws ParseException {
        String locationDateReactualizat = locationsAndDates.get(it).asNormalizedText();
        String location = locationDateReactualizat.substring(0,locationDateReactualizat.indexOf(" "));

        String date = "";
        if (locationDateReactualizat.contains(AZI)){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            date = dtf.format(now);
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            date = dtf.format(now);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        listPostingDTO.setDate(formatter.parse(date));



        listPostingDTO.setLocation(location);
        listPostingDTO.setReactualizat(locationDateReactualizat.toLowerCase().contains(REACTUALIZAT));

        System.out.println(location);

    }

}
