package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ModelDetailsService {
    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private String imageLink = "";

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

        for (Model model : manufacturer.getModelsList()) {
            System.out.println(modelsCounter+"/"+nrOfModels + ": " + model.getName());
            modelsCounter++;
//            if (!model.getName().equals("C 400GT")) continue;    // to be deleted

            HtmlPage page = client.getPage(model.getUrl());

//            HtmlElement anchor = page.getFirstByXPath(".//font/a");
            HtmlElement table24;
            try {
                table24 = page.getHtmlElementById("table24");
            } catch (Exception e){
                continue;
            }
            getImageOfTheModel(table24, model.getName());
            printScrapedTable();
            savePicturesOfModels(imageLink,manufacturer.getName(),model.getName());
            getDataFromTable(table24);

            listOfSpecName.clear();
            listOfSpecValue.clear();
            imageLink = "";
        }
        listOfSpecName.forEach(System.out::println);
    }

    private void getImageOfTheModel(HtmlElement table24, String modelName) {
        List<HtmlElement> images = table24.getByXPath(".//img");
        String[] wordsInModelName = modelName.split(" ");
        String modelNameNoSpaces = modelName.replaceAll(" ","");
        for (int i =0; i<wordsInModelName.length; i++) {
            for (HtmlElement img : images) {
                String imgLink = img.getAttribute("src");
                System.out.println(imgLink); // to be deleted
                String formattedImgLink = imgLink.replace("%20", "");
                if (formattedImgLink.contains(modelNameNoSpaces)) {
                    imageLink = imgLink.replaceAll("../../", Constants.MOTORCYCLESPECS_CO_ZA);
                    System.out.println(imageLink); // to be deleted
                    break;
                }
            }
            if (!imageLink.equals("")) break;
            modelNameNoSpaces = "";
            for (int j = 0; j< wordsInModelName.length-i-1;j++){
                modelNameNoSpaces = modelNameNoSpaces + wordsInModelName[j];
            }
        }

    }

    private void savePicturesOfModels(String imageLink, String manufacturer, String model) throws IOException {
        String picturesPathBase = "D:/Learning/Projects/Scrapping Projects/MotorcycleSpecsScrapper/Scrapped Images DB/" + manufacturer + "/";
        createManufacturerFolder(picturesPathBase);
        String picturesPath = picturesPathBase + model.replaceAll(" ","") + ".jpg";
//        Path path = Paths.get(imageLink);

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
//            Path destination = Paths.get(destinationPath);
            Path destination = Paths.get(destinationPath);
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void getDataFromTable(HtmlElement table24){
        List<HtmlElement> rows = table24.getByXPath(".//tr");
        for (HtmlElement row : rows) {
            List<HtmlTableCell> cells = row.getByXPath(".//td");
            if (cells.size() == 2) {
                int cellNr = 1;
                for (HtmlElement cell : cells) {
                    if (cell.getTextContent().contains("Lubrication")){
                        System.out.println("Here!!");
                    }
                    String cellText = cell.getTextContent().replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ","").trim();
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
    
    private void printScrapedTable(){
        System.out.println("total rows: "+listOfSpecValue.size());
        for (int i=0; i<listOfSpecValue.size(); i++){
            System.out.println((i+1)+".\t"+listOfSpecName.get(i)+"\t"+listOfSpecValue.get(i));
        }
        System.out.println("--------------------------");
    }
}
