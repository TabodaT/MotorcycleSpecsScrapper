package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ListPostingDTO {
    private String url;
    private String title;
    private String price;
    private String location;
    private Date date;
}
