package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class ListPostingDTO {
    private String title;
    private String url;
    private int id;
    private int price;
    private String currency;
    private String location;
    private Date date;
    private boolean reactualizat;
    private boolean negociabil;

    @Override
    public String toString() {
        String dateFormatted = "";
        if (this.date!=null) {
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            dateFormatted = simpleDateFormat.format(this.date);
        }

        return
                "Title:        " + this.title + "\n" +
                "URL:          " + this.url + "\n" +
                "id:           " + this.id + "\n" +
                "Price:        " + this.price + this.currency + "\n" +
                "Negociabil:   " + this.negociabil + "\n" +
                "Location:     " + this.location + "\n" +
//                "Date:         " + this.date.getDay() + "-" + this.date.getMonth() + "-" + this.date.getYear() + "\n" +
                "Date:         " + dateFormatted + "\n" +
                "Reactualizat: " + this.reactualizat;
    }
}


