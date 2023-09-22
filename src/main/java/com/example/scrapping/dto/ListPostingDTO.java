package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class ListPostingDTO {
    private String title;
    private String url;
    private String id;
    private int price;
    private String currency;
    private String location;
    private Date date;
    private boolean reactualizat;
    private boolean negociabil;

    public boolean getNegociabil(){
        return this.negociabil;
    }
    public boolean getReactualizat(){
        return this.reactualizat;
    }
}


