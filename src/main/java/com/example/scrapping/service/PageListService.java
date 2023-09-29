package com.example.scrapping.service;

import com.example.scrapping.dto.ListPostingDTO;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class PageListService {
    private static final String OLX_RO = "https://www.olx.ro";
    private static final String REACTUALIZAT = "reactualizat";
    private static final String AZI = "azi";
    private static final String DATE_FORMATTER = "dd-MM-yyyy";
    List<ListPostingDTO> listPostingDTOS = new ArrayList<>();

//    private UserDaoImpl userDAO = new UserDaoImpl();

    public void getListOfPostsFromPage() throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
//        String searchUrl = OLX_RO + "/oferte/q-klr-650/?currency=EUR";
        String searchUrl = OLX_RO + "/auto-masini-moto-ambarcatiuni/motociclete/piatra-neamt/q-honda/?currency=EUR";

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

            // URL
            if (firstCard) {
                HtmlElement urlFirst = htmlItem.getFirstByXPath("//a[@class='css-rc5s2u']");
                listPostingDTO.setUrl(OLX_RO + urlFirst.getAttribute("href"));
                firstCard = false;
            } else {
                listPostingDTO.setUrl(OLX_RO + divCard.getAttribute("href"));
            }

            // id
            listPostingDTO.setId(Integer.parseInt(htmlItem.getAttribute("id")));

            // price & currency
            setPriceAndCurrency(listPostingDTO, prices, it);

            // location and date
            try {
                setLocationAndDate(listPostingDTO, locationsAndDates, it);
            } catch (Exception e) {
                log.warn("Date parse exception: " + e);
            }

            listPostingDTOS.add(listPostingDTO);
            it++;
            System.out.println(listPostingDTO + "\n");
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
        String location = getLocation(locationDateReactualizat);

        String date;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMATTER);
        if (locationDateReactualizat.toLowerCase().contains(AZI)) {
            LocalDateTime now = LocalDateTime.now();
            date = dtf.format(now);
        } else {
            date = getDateString(locationDateReactualizat);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        listPostingDTO.setDate(formatter.parse(date));
        listPostingDTO.setLocation(location);
        listPostingDTO.setReactualizat(locationDateReactualizat.toLowerCase().contains(REACTUALIZAT));
    }

    private String getDateString(String locationDateReactualizat) {
        String dateString = "";
        int iterSect = 0;
        for (Character c : locationDateReactualizat.toCharArray()) {
            if (Character.isDigit(c)) {
                if (locationDateReactualizat.toLowerCase().contains("sectorul") && (iterSect == 0)) {
                    iterSect++;
                    continue;
                }
                dateString = locationDateReactualizat.substring(locationDateReactualizat.indexOf(c));
                break;
            }
        }
        String[] dayMonthYear = dateString.split(" ");
        String day = dayMonthYear[0];
        String monthLiteral = dayMonthYear[1];
        String year = dayMonthYear[2];
        String month = switch (monthLiteral) {
            case "ianuarie" -> "01";
            case "februarie" -> "02";
            case "martie" -> "03";
            case "aprile" -> "04";
            case "mai" -> "05";
            case "iunie" -> "06";
            case "iulie" -> "07";
            case "august" -> "08";
            case "septembrie" -> "09";
            case "octombrie" -> "10";
            case "noiembrie" -> "11";
            case "decembrie" -> "12";
            default -> "";
        };

        return day + "-" + month + "-" + year;
    }

    private String getLocation(String locationDateReactualizat) {
        String location;
        if (locationDateReactualizat.toLowerCase().contains("bucuresti")) {
            return "Bucuresti";
        }
        location = locationDateReactualizat.substring(0, locationDateReactualizat.indexOf(" - ")).trim();
        return location;
    }



    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<String> getAllUserNames(){
        List<String> usernameList = new ArrayList<>();

        usernameList.addAll(jdbcTemplate.queryForList("SELECT fullname from user;", String.class));
        return usernameList;
    }

}
