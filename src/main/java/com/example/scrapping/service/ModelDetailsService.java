package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.ModelOfManuf;
import com.example.scrapping.dto.MotoModelDTO;
import com.example.scrapping.mappers.MotoModelsMapper;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class ModelDetailsService {

    @Autowired
    ModelsToDataBaseService modelsToDataBaseService;

    private final List<String> listOfSpecName;
    private final List<String> listOfSpecValue;
    private String imageLink = "";
    private String imageFile = "";
    private LogsWriterSingletonService logsWriterSingletonService = LogsWriterSingletonService.getInstance();

    public ModelDetailsService() {
        this.listOfSpecName = new ArrayList<>();
        this.listOfSpecValue = new ArrayList<>();
    }

    public void getModelDetails(Manufacturer manufacturer) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        int modelsCounter = 1;
        int nrOfModels = manufacturer.getModelsList().size();

        for (ModelOfManuf modelOfManuf : manufacturer.getModelsList()) {
            System.out.println(modelsCounter + "/" + nrOfModels + ": " + modelOfManuf.getName() +
                    " (" + modelOfManuf.getProductionYears() + ") " +
                    modelOfManuf.getUrl());
            modelsCounter++;
            HtmlPage page;

            try {
                page = client.getPage(modelOfManuf.getUrl());
            } catch (FailingHttpStatusCodeException e){
                logsWriterSingletonService.logError("404 model page not found: " + manufacturer.getName()+" "+
                        modelOfManuf.getName()+" "+modelOfManuf.getUrl());
                System.out.println(e);
                continue;
            }

            HtmlElement table24;
            try {
                table24 = page.getHtmlElementById("table24");
            } catch (Exception e) {
                continue;
            }
            getImageOfTheModel(table24, manufacturer.getName(), modelOfManuf.getName());
            savePicturesOfModels(imageLink, manufacturer.getName(), modelOfManuf.getName());
            getDataFromTable(table24);

            printScrapedTable(); // to be deleted

            MotoModelsMapper motoModelsMapper = new MotoModelsMapper();
            MotoModelDTO motoModelDTO = new MotoModelDTO();

            try {
                motoModelDTO = motoModelsMapper.mapMotoModel(listOfSpecName, listOfSpecValue, manufacturer, modelOfManuf, imageFile);
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                System.out.println(manufacturer.getName() + " " + modelOfManuf.getName());
            }
            System.out.println(motoModelDTO); // to be deleted

            if (!motoModelsMapper.hasErrors && !motoModelDTO.getModel().isEmpty()) {
                boolean wasInserted = false;
                String manufModelURL = manufacturer.getUrl() + " " + modelOfManuf.getName()+ " " + modelOfManuf.getProductionYears() + " " + modelOfManuf.getUrl();
                try {
                    wasInserted = modelsToDataBaseService.insertMoto(motoModelDTO) == 1;
                } catch (Exception e) {
                    System.out.println("Error inserting in DB for " + manufModelURL + " " + e);
                }
                System.out.println((wasInserted ? "Inserted " : "Not inserted "));
                modelOfManuf.setInserted(wasInserted);
            }

            listOfSpecName.clear();
            listOfSpecValue.clear();
            imageLink = "";
            imageFile = "";
        }
//        listOfSpecName.forEach(System.out::println); // to be deleted
    }

    private void getImageOfTheModel(HtmlElement table24, String manufacturer, String modelName) {
        List<HtmlElement> images = table24.getByXPath(".//img");

        String manufacturerNameNoSpaces = manufacturer.replaceAll(" ", "").toLowerCase();
        for (HtmlElement img : images) {
            String imgLink = img.getAttribute("src");
//            System.out.println(imgLink); // to be deleted
            boolean foundImage = findModelOrManufNameInImageLink(imgLink, modelName, manufacturerNameNoSpaces);

            if (foundImage) {
                imageLink = imgLink.replaceAll("../../", Constants.MOTORCYCLESPECS_CO_ZA);
//                System.out.println("imageLink:        " + imageLink); // to be deleted
//                System.out.println("imageFile:        " + imageFile); // to be deleted
                break;
            }
        }
    }

    private boolean findModelOrManufNameInImageLink(String link, String modelName, String manufName) {
        String formattedImgLink = link.replace("%20", "").replace("-", "");
        if (formattedImgLink.toLowerCase().contains(manufName)) {
            imageFile = formattedImgLink.substring(formattedImgLink.lastIndexOf("/") + 1);
//            System.out.println("formattedImgLink: " + formattedImgLink); // to be deleted
            return true;
        }
        String[] modelNameWords = removeAllNotLettersNumbersOrSpace(modelName).split(" ");
        String imageJPG = formattedImgLink.substring(formattedImgLink.lastIndexOf("/"));
        for (String wordOfModelName : modelNameWords) {
            if (wordOfModelName.length() > 2 && imageJPG.toLowerCase().contains(wordOfModelName.toLowerCase())) ;
            {
                imageFile = formattedImgLink.substring(formattedImgLink.lastIndexOf("/") + 1);
                System.out.println("formattedImgLink: " + formattedImgLink); // to be deleted
                return true;
            }
        }
        return false;
    }

    private String removeAllNotLettersNumbersOrSpace(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i)) || Character.isLetter(text.charAt(i)) || text.charAt(i) == ' ') {
                result.append(text.charAt(i));
            }
        }
        return result.toString();
    }

    private void savePicturesOfModels(String imageLink, String manufacturer, String model) throws IOException {
        String allManufacturersFolder = "D:/Learning/Projects/Scrapping Projects/MotorcycleSpecsScrapper/Scrapped Images DB";
        String picturesPathBase = allManufacturersFolder + "/" + manufacturer;
        createManufacturerFolder(picturesPathBase);
        String picturesPath = picturesPathBase + "/" + imageFile;

        File directoryPath = new File(allManufacturersFolder);
        List<String> manufacturersFolders = Arrays.asList(Objects.requireNonNull(directoryPath.list()));
        if (manufacturersFolders.contains(manufacturer)) {
            File filesPath = new File(picturesPathBase);
            List<String> images = Arrays.asList(Objects.requireNonNull(filesPath.list()));
            if (images.contains(imageFile)) {
//                System.out.println("Image exists!");  // to be deleted
                return;
            }
        }

        try {
            downloadImage(imageLink, picturesPath);
            System.out.println("Image downloaded successfully!");
        } catch (IOException e) {
            System.err.println("Error downloading image: " + e.getMessage());
        }

    }

    private void createManufacturerFolder(String pathBase) throws IOException {
        Path dirPath = Paths.get(pathBase);
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath);
        }
    }

    private void downloadImage(String imageUrl, String destinationPath) throws IOException {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();

        try (InputStream inputStream = connection.getInputStream()) {
            // Using java.nio.file for copying the input stream to a local file
            Path destination = Paths.get(destinationPath);
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void getDataFromTable(HtmlElement table24) {
        List<HtmlElement> rows = table24.getByXPath(".//tr");
        for (HtmlElement row : rows) {
            List<HtmlTableCell> cells = row.getByXPath(".//td");
            if (cells.size() == 2) {
                int cellNr = 1;
                for (HtmlElement cell : cells) {
                    if (cell.getTextContent().contains("Lubrication")) {
                        System.out.println("Here!!");
                    }
                    String cellText = cell.getTextContent().replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "").trim();
                    if (cellNr == 1) {
                        listOfSpecName.add(cellText);
                    } else {
                        listOfSpecValue.add(cellText);
                    }
//                        System.out.println(cell.getTextContent());
                    cellNr++;
                }
            }
        }
    }

    private void printScrapedTable() {
        System.out.println("total rows: " + listOfSpecValue.size());
//        for (int i = 0; i < listOfSpecValue.size(); i++) {
//            System.out.println((i + 1) + ".\t" + listOfSpecName.get(i) + "\t" + listOfSpecValue.get(i));
//        }
//        System.out.println("--------------------------");
    }
}
