package com.example.scrapping.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Manufacturer extends MotoListing {
    private HashSet<ModelOfManuf> modelsList;

    public Manufacturer(String name, String url) {
        super(name, url);
        this.modelsList = new HashSet<>();
    }

    public HashSet<ModelOfManuf> getModelsList(){
        return new HashSet<>(this.modelsList);
    }

    public void addModel(ModelOfManuf modelOfManuf) {
        if (modelOfManuf.getUrl().isEmpty() || modelOfManuf.getName().isEmpty()) {
            return;
        }
        this.modelsList.add(modelOfManuf);
    }

    public void printModels() {
        Set<ModelOfManuf> treeSet = new TreeSet<>(this.modelsList);
        Iterator<ModelOfManuf> namesIterator = treeSet.iterator();
        int i = 1;
        while (namesIterator.hasNext()) {
            System.out.println(i + ". " + namesIterator.next());
            i++;
        }
    }
}
