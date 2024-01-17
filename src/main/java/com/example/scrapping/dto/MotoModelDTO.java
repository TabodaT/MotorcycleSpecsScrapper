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
    private int endYear;
    private String engine;
    private int capacity;   // cc
    private int power;  // kw
    private String clutch;
    private int torque; // nm
    private boolean abs;
    private int transmission;   // speed
    private String finalDrive;
    private int seatHeight; // mm
    private int dryWeight;  // kg
    private int wetWeight;  // kg
    private double fuelCapacity; // Litres
    private int reserve;    // l
    private double consumption; // l/100km
    private String coolingSystem;
    private int topSpeed; // km/h
    private String url;
    private String image;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Class<?> c = this.getClass();
        Field[] fields = c.getDeclaredFields();
        Map<String, Object> temp = new HashMap<>();

        for (Field field : fields) {
            try {
                temp.put(field.getName(), field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                System.out.println(e1);
            }
        }
        List<String> listOfNotNullFields = new ArrayList<>();
        for (Map.Entry<String, Object> entry : temp.entrySet()) {
            Object value = entry.getValue();

            if ((value instanceof String && !((String) value).isEmpty() && !value.equals("0")) ||
                    (value instanceof Integer && (Integer) value != 0) ||
                    value instanceof Double && (Double) value != 0) {
                listOfNotNullFields.add(entry.getKey());
            }
        }

        Collections.sort(listOfNotNullFields);
        for (String field : listOfNotNullFields) {
            sb.append(field).append(":\t").append(temp.get(field));
            switch (field) {
                case "seatHeight" -> sb.append("mm");
                case "dryWeight", "wetWeight" -> sb.append("kg");
                case "fuelCapacity" -> sb.append("Litres");
                case "capacity" -> sb.append("cc");
                case "power" -> sb.append("kw");
                case "top_speed" -> sb.append("km/h");
                case "torque" -> sb.append("nm");
                case "transmission" -> sb.append(" speed");
                case "consumption" -> sb.append("l/100km");
                case "reserve" -> sb.append("l");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
