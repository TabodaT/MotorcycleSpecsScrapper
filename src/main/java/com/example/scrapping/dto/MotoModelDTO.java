package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotoModelDTO {
    private int id;
    private String make;
    private String model;
    private int year;
    private String engine;
    private String capacity;
    private String power;
    private String clutch;
    private String torque;
    private boolean abs;
    private String frontBrakes;
    private String rearBrakes;
    private String transmission;
    private String finalDrive;
    private String seatHeight;
    private int dryWeight;
    private int wetWeight;
    private int fuelCapacity;
    private int reserve;
    private double consumption;
    private String range;
    private String coolingSystem;
    private String ignition;
    private String oil_capacity;
    private String power_to_weight_ratio;
    private String top_speed;
    private String url;
    private String image;

    @Override
    public String toString() {
        return "MotoModelDTO{" +
                "id=" + id +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", engine='" + engine + '\'' +
                ", capacity='" + capacity + '\'' +
                ", power='" + power + '\'' +
                ", clutch='" + clutch + '\'' +
                ", torque='" + torque + '\'' +
                ", abs=" + abs +
                ", frontBrakes='" + frontBrakes + '\'' +
                ", rearBrakes='" + rearBrakes + '\'' +
                ", transmission='" + transmission + '\'' +
                ", finalDrive='" + finalDrive + '\'' +
                ", seatHeight='" + seatHeight + '\'' +
                ", dryWeight='" + dryWeight + '\'' +
                ", wetWeight='" + wetWeight + '\'' +
                ", fuelCapacity='" + fuelCapacity + '\'' +
                ", reserve='" + reserve + '\'' +
                ", consumption='" + consumption + '\'' +
                ", range='" + range + '\'' +
                ", coolingSystem='" + coolingSystem + '\'' +
                ", ignition='" + ignition + '\'' +
                ", oil_capacity='" + oil_capacity + '\'' +
                ", power_to_weight_ratio='" + power_to_weight_ratio + '\'' +
                ", top_speed='" + top_speed + '\'' +
                ", url='" + url + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
