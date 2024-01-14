package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.*;

@Getter
@Setter
public class MotoModelDTO {
    private int id;
    private String make;
    private String model;
    private int year;
    private int end_year;
    private String engine;
    private int capacity;   // cc
    private int power;  // kw
    private String clutch;
    private int torque;
    private boolean abs;
    private String transmission;
    private String finalDrive;
    private int seatHeight; // mm
    private int dryWeight;  // kg
    private int wetWeight;  // kg
    private int fuelCapacity; // Litres
    private int reserve;
    private double consumption;
    private String coolingSystem;
    private int top_speed; // km/h
    private String url;
    private String image;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Class<?> c = this.getClass();
        Field[] fields = c.getDeclaredFields();
        Map<String, Object> temp = new HashMap<>();

        for( Field field : fields ){
            try {
                temp.put(field.getName(), field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                System.out.println(e1);
            }
        }
        List<String> listOfNotNullFields = new ArrayList<>();
        for (Map.Entry<String, Object> entry : temp.entrySet()) {
            Object value = entry.getValue();

            if ((value instanceof String && !((String) value).isEmpty()) || (value instanceof Integer && (Integer) value != 0)) {
                listOfNotNullFields.add(entry.getKey());
            }
        }

        Collections.sort(listOfNotNullFields);
        for (String field : listOfNotNullFields){
            sb.append(field).append(":\t").append(temp.get(field)).append("\n");
        }

        return sb.toString();
    }
}
