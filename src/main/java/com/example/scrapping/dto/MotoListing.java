package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class MotoListing {
    private final String name;
    private final String url;
    private List<Model> modelsList;

    protected MotoListing(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
