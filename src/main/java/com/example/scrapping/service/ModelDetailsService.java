package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.ModelOfManuf;
import com.example.scrapping.dto.MotoModelDTO;
import com.example.scrapping.mappers.MotoModelsMapper;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
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
    private final LogsWriterSingletonService logsWriterSingletonService = LogsWriterSingletonService.getInstance();
    private final MotoModelsMapper motoModelsMapper = new MotoModelsMapper();
    private HtmlElement specsTable;
    private HtmlPage page;
    private MotoModelDTO motoModelDTO;

    private List<String> ignoreURLs = new ArrayList<>(Arrays.asList(
                "https://www.motorcyclespecs.co.za/H-D.htm",
                "https://www.motorcyclespecs.co.za/model/Honda/honda_rc51_sp2_nicky_hayden.html"
            ));
    private List<String> tablesToLookFor = new ArrayList<>(Arrays.asList(
                "table24","table28","table5","table47","table33","table30","table36"
            ));

    public ModelDetailsService() {
        this.listOfSpecName = new ArrayList<>();
        this.listOfSpecValue = new ArrayList<>();
    }

    public void getModelDetails(Manufacturer manufacturer) throws IOException {
        int modelsCounter = 1;
        int nrOfModels = manufacturer.getModelsList().size();

        for (ModelOfManuf modelOfManuf : manufacturer.getModelsList()) {
            if (ignoreURLs.contains(modelOfManuf.getUrl())) continue; // to be deleted todo
            printModelBeingScrapped(modelsCounter,nrOfModels,modelOfManuf);
            modelsCounter++;
            boolean hasSpecsTable = true;

            try(WebClient client = new WebClient()) {
                client.getOptions().setCssEnabled(false);
                client.getOptions().setJavaScriptEnabled(false);
                page = client.getPage(modelOfManuf.getUrl());
            } catch (FailingHttpStatusCodeException e) {
                logErrorInGetModel(manufacturer, modelOfManuf, "404 model page not found: page=", e);
                continue;
            }

            for (String tableName : tablesToLookFor){
                try {
                    specsTable = page.getHtmlElementById(tableName);
                    if (checkIfIsCorrectTable(specsTable)){
                        getDataFromTable(specsTable);
                    }
                } catch (Exception e) {
//                    logErrorInGetModel(manufacturer, modelOfManuf, "table24 not found: page=", e); // todo uncomment this and add motorcycles
                    hasSpecsTable = false;
                }

            }


            if (hasSpecsTable) {
                getImageOfTheModel(specsTable, manufacturer.getName(), modelOfManuf.getName());
                savePicturesOfModels(imageLink, manufacturer.getName(), modelOfManuf.getName());
//                getDataFromTable(specsTable);

                printScrapedTable(); // to be deleted

                try {
                    motoModelDTO = motoModelsMapper.mapMotoModel(listOfSpecName, listOfSpecValue, manufacturer, modelOfManuf, imageFile);
                } catch (Exception e) {
                    logErrorInGetModel(manufacturer, modelOfManuf, "Error mapping model: page=", e);
                    continue;
                }
            } else {
                try {
                    motoModelDTO = motoModelsMapper.mapMotoModelWithoutTable24(manufacturer,modelOfManuf);
                } catch (Exception e) {
                    logErrorInGetModel(manufacturer, modelOfManuf, "Error mapping (table24) model: page=", e);
                    continue;
                }
            }

            if (!motoModelsMapper.isErroneous() && !motoModelDTO.getModel().isEmpty()) {
                boolean wasInserted = false;
                try {
                    wasInserted = modelsToDataBaseService.insertMoto(motoModelDTO) == 1;
                } catch (Exception e) {
                    StringBuilder manufModelURLSB = new StringBuilder();
                    manufModelURLSB.append(modelOfManuf.getPage()).append(". ")
                            .append(manufacturer.getName()).append(" ")
                            .append(modelOfManuf.getName()).append(" ")
                            .append(modelOfManuf.getProductionYears()).append(" ")
                            .append(modelOfManuf.getUrl());
                    StringBuilder errorSB = new StringBuilder();
                    errorSB.append("Error inserting in DB for ")
                            .append(manufModelURLSB).append(" ")
                            .append(e);
                    logsWriterSingletonService.logError(errorSB.toString());
                    System.out.println(errorSB);
                }
                System.out.println((wasInserted ? "Inserted " : "Not inserted "));
                modelOfManuf.setInserted(wasInserted);
            }
            clearData();
        }
    }

    private boolean checkIfIsCorrectTable(HtmlElement table) {
        boolean result = false;
//        boolean hasMakeModel = false;
//        boolean hasCapacity = false;
        List<HtmlElement> rows = table.getByXPath(".//tr");
        for (HtmlElement row : rows) {
            List<HtmlTableCell> cells = row.getByXPath(".//td");
            if (cells.size() == 2) result = true;
//            if (cells.size() == 2) {
//                int cellNr = 1;
//                for (HtmlElement cell : cells) {
//                    String cellText = cell.getTextContent().replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "").trim();
//                    if (cellNr == 1 && cellText.toLowerCase().contains("make model")) {
//                        hasMakeModel = true;
//                        continue;
//                    }
//                    if (cellNr == 1 && cellText.toLowerCase().contains("capacity")) {
//                        hasCapacity = true;
//                        continue;
//                    }
//                    if (hasMakeModel && hasCapacity){
//                        result = true;
//                        break;
//                    }
//                    cellNr++;
//                }
//            }
        }
        return result;
    }

    private void printModelBeingScrapped(int modelsCounter, int nrOfModels, ModelOfManuf modelOfManuf) {
        StringBuilder sb = new StringBuilder();
        sb.append(modelsCounter).append("/").append(nrOfModels).append(": ")
                        .append(modelOfManuf.getName())
                        .append(" (").append(modelOfManuf.getProductionYears()).append(") ")
                        .append(modelOfManuf.getUrl());
        System.out.println(sb);
    }

    private void logErrorInGetModel(Manufacturer manufacturer, ModelOfManuf model, String errorText, Exception e) {
        StringBuilder sbError = new StringBuilder();
        sbError.append(errorText).append(" ")
                .append(model.getPage()).append(". ")
                .append(manufacturer.getName()).append(" ")
                .append(model.getName()).append(" ")
                .append(model.getProductionYears()).append(" ")
                .append(model.getUrl()).append(" ")
                .append(e);
//        String error = errorText + " " + model.getPage() + ". " + manufacturer.getName() + " " +
//                model.getName() + " " + model.getProductionYears() + " " + model.getUrl() + " " + e;
        logsWriterSingletonService.logError(sbError.toString());
        System.out.println(sbError);
        clearData();
    }

    private void clearData() {
        listOfSpecName.clear();
        listOfSpecValue.clear();
        imageLink = "";
        imageFile = "";
        specsTable = null;
        page = null;
        motoModelDTO = null;
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
        String imageFile1 = formattedImgLink.substring(formattedImgLink.lastIndexOf("/") + 1).replaceAll("'", "");
        if (formattedImgLink.toLowerCase().contains(manufName)) {
            imageFile = imageFile1;
            return true;
        }
        String[] modelNameWords = removeAllNotLettersNumbersOrSpace(modelName).split(" ");
        String imageJPG = formattedImgLink.substring(formattedImgLink.lastIndexOf("/"));
        for (String wordOfModelName : modelNameWords) {
            if (wordOfModelName.length() > 2 && imageJPG.toLowerCase().contains(wordOfModelName.toLowerCase())){
                imageFile = imageFile1;
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
        String picturesPathBase = "";
        if (manufacturer.contains("/")){
            String firstManuf = manufacturer.split("/")[0].trim();
            picturesPathBase = picturesPathBase = allManufacturersFolder + "/" + firstManuf;
        } else {
            picturesPathBase = allManufacturersFolder + "/" + manufacturer;
        }
        createManufacturerFolder(picturesPathBase);
        String picturesPath = picturesPathBase + "/" + imageFile;

        File directoryPath = new File(allManufacturersFolder);
        List<String> manufacturersFolders = Arrays.asList(Objects.requireNonNull(directoryPath.list()));
        if (manufacturersFolders.contains(manufacturer)) {
            File filesPath = new File(picturesPathBase);
            List<String> images = Arrays.asList(Objects.requireNonNull(filesPath.list()));
            if (images.contains(imageFile)) {
                System.out.println("Image exists!");  // to be deleted
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
                    String cellText = cell.getTextContent().replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "").trim();
                    if (cellNr == 1) {
                        listOfSpecName.add(cellText);
                    } else {
                        listOfSpecValue.add(cellText);
                    }
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

    public List<String> getIgnoreURLs() {
        return ignoreURLs;
    }
}
