package com.example.scrapping.service;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ModelDetailsService {
    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;

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
            printScrapedTable();
        }
        listOfSpecName.forEach(System.out::println);
    }

    private void printScrapedTable(){
        System.out.println("total rows: "+listOfSpecValue.size());
        for (int i=0; i<listOfSpecValue.size(); i++){
            System.out.println((i+1)+".\t"+listOfSpecName.get(i)+"\t"+listOfSpecValue.get(i));
        }
        System.out.println("--------------------------");
    }
}
