package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MotoListing {
    private final String name;
    private final String url;

    protected MotoListing(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return this.name + " " + this.url;
    }
}
