package com.example.scrapping.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Manufacturer extends MotoListing {
    private HashSet<Model> modelsList;

    public Manufacturer(String name, String url) {
        super(name, url);
        this.modelsList = new HashSet<>();
    }

    public HashSet<Model> getModelsList(){
        return new HashSet<>(this.modelsList);
    }

    public void addModel(Model model) {
        if (model.getUrl().isEmpty() || model.getName().isEmpty()) {
            return;
        }
        this.modelsList.add(model);
    }

    public void printModels() {
        Set<Model> treeSet = new TreeSet<>(this.modelsList);
        Iterator<Model> namesIterator = treeSet.iterator();
        int i = 1;
        while (namesIterator.hasNext()) {
            System.out.println(i + ". " + namesIterator.next());
            i++;
        }
    }
}
