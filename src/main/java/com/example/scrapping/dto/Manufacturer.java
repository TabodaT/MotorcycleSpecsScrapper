package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Manufacturer extends MotoListing{
    public Manufacturer(String name, String url) {
        super(name, url);
    }
}
