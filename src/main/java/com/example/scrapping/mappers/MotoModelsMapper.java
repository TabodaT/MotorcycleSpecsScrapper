package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.example.scrapping.dto.MotoModelDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MotoModelsMapper {
    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private Manufacturer manufacturer;
    private String modelName;
    private String modelProductionYears;
    private String endYear = "0";

    public MotoModelDTO mapMotoModel(List<String> listOfSpecName, List<String> listOfSpecValue, Manufacturer manufacturer, Model model, String imageFile) {
        this.listOfSpecName = listOfSpecName;
        this.listOfSpecValue = listOfSpecValue;
        this.manufacturer = manufacturer;
        this.modelName = model.getName();
        this.modelProductionYears = model.getProductionYears();

        MotoModelDTO motoModelDTO = new MotoModelDTO();

        //   make
        motoModelDTO.setMake(manufacturer.getName());
        //   model
        motoModelDTO.setModel(model.getName());
        //   year
        motoModelDTO.setYear(Integer.parseInt(getSpecValue("year")));
        //   engine
        motoModelDTO.setEngine(getSpecValue("engine"));
        //   capacity
        motoModelDTO.setCapacity(Integer.parseInt(getSpecValue("capacity")));
        //   power
        motoModelDTO.setPower(Integer.parseInt(getSpecValue("power")));
        //   clutch
        motoModelDTO.setClutch(getSpecValue("clutch"));
        //   torque
        motoModelDTO.setTorque(Integer.parseInt(getSpecValue("torque")));
        //   abs
        motoModelDTO.setAbs(Boolean.parseBoolean(getSpecValue("abs")));
        //   transmission
        motoModelDTO.setTransmission(getSpecValue("transmission"));
        //   final_drive
        motoModelDTO.setFinalDrive(getSpecValue("drive"));
        //   seat_height
        motoModelDTO.setSeatHeight(Integer.parseInt(getSpecValue("seat")));
        //   dry_weight
        motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("dry")));
        //   wet_weight
        motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("wet")));
        if (motoModelDTO.getDryWeight() == 0 && motoModelDTO.getWetWeight() == 0) {
            motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("weight")));
        }
        //   fuel_capacity
        motoModelDTO.setFuelCapacity(Integer.parseInt(getSpecValue("fuel capacity")));
        if (motoModelDTO.getFuelCapacity() == 0) {
            motoModelDTO.setFuelCapacity(Integer.parseInt(getSpecValue("load capacity")));
        }
        //   reserve
        motoModelDTO.setReserve(Integer.parseInt(getSpecValue("reserve")));
        //   consumption
        motoModelDTO.setConsumption(Double.parseDouble(getSpecValue("consumption")));
        //   cooling_system
        motoModelDTO.setCoolingSystem(getSpecValue("cooling"));
        //   top_speed
        motoModelDTO.setTop_speed(Integer.parseInt(getSpecValue("speed")));
        //   url
        motoModelDTO.setUrl(model.getUrl());
        //   image
        motoModelDTO.setImage(imageFile);


        return motoModelDTO;
    }

    private String getSpecValue(String specName) {
        String result = "0";
        for (int i = 0; i < listOfSpecName.size(); i++) {
            String nameOfSpecAtI = listOfSpecName.get(i).toLowerCase();
            String valueOfSpecAtI = listOfSpecValue.get(i);
            if (nameOfSpecAtI.equals(specName)) {                          // EQUALS
                if (specName.equals("year")) {
                    result = formatYear(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("capacity")) {
                    result = formatMetricUnit(valueOfSpecAtI, "cc");
                    break;
                }
                if (specName.equals("clutch")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("weight")) {
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
                result = valueOfSpecAtI;
            } else if (nameOfSpecAtI.contains(specName)) {                   // CONTAINS
                if (nameOfSpecAtI.contains("power") && !nameOfSpecAtI.contains("weight")) {
                    result = formatMetricUnit(valueOfSpecAtI, "kw");
                    break;
                }
                if (specName.equals("drive") || specName.equals("cooling")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("torque")) {
                    result = formatMetricUnit(valueOfSpecAtI, "nm");
                    break;
                }
                if (nameOfSpecAtI.contains("dry") || nameOfSpecAtI.contains("wet")) {
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
                if (specName.equals("seat")) {
                    result = formatMetricUnit(valueOfSpecAtI, "mm");
                    break;
                }
                if (specName.equals("fuel capacity") || specName.equals("load capacity")) {
                    result = formatMetricUnit(valueOfSpecAtI, "litre");
                    break;
                }
                if (specName.equals("reserve")) {
                    result = formatMetricUnit(valueOfSpecAtI, "l");
                    break;
                }
                if (specName.equals("speed")) {
                    result = formatMetricUnit(valueOfSpecAtI, "km");
                    break;
                }
                if (specName.equals("consumption")) {
                    result = getConsumption(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("transmission")) {
                    result = formatMetricUnit(valueOfSpecAtI, "speed");
                    break;
                }

                result = valueOfSpecAtI;
            }

            if (nameOfSpecAtI.contains("brake") && specName.equals("abs")) {
                if (valueOfSpecAtI.toLowerCase().contains(specName)) {
                    result = "true";
                    return result;
                } else {
                    result = "false";
                }
            }

        }
        return result;
    }

    private String getConsumption(String rawSpecValue) {
        String result = "";
        String preFormat = "";
        if (rawSpecValue.toLowerCase().contains("km")) {
            preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("km")).trim().replaceAll(" ", "").substring(0, rawSpecValue.toLowerCase().indexOf("l"));
            for (int i = 0; i < preFormat.length(); i++) {
                char c = preFormat.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    result = result + c;
                }
            }
        } else if (rawSpecValue.toLowerCase().contains("mpg")) {
            preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("mpg")).trim().replaceAll(" ", "");
            result = getNumberFromEndOfString(preFormat);
            DecimalFormat df = new DecimalFormat("#.#");
            double lPer100km = 235.21 / Double.parseDouble(result);
            result = df.format(lPer100km);
        }
        return result;
    }

    private String formatMetricUnit(String rawSpecValue, String kwOrNmOrLitre) {
        String result = "";
        if (rawSpecValue.toLowerCase().contains(kwOrNmOrLitre)) {
            String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf(kwOrNmOrLitre)).trim().replaceAll(" ", "");
            result = getNumberFromEndOfString(preFormat);
        }
        return result.isEmpty() ? "0" : result;
    }

    private String getNumberFromEndOfString(String preFormat) {
        String result = "";
        if (preFormat.length() > 1) {
            for (int i = preFormat.length() - 1; i >= 0; i--) {
                char c = preFormat.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    result = c + result;
                } else break;
            }
        } else if (preFormat.length() == 1) {
            if (Character.isDigit(preFormat.charAt(0))) {
                result = preFormat;
            }
        }
        return result;
    }

    private String formatYear(String year) {
        String result = "0";
        String yearsToBeFormatted = year.isEmpty() ? modelProductionYears : year;
        String[] years = yearsToBeFormatted.trim().split("-");
        result = years[0];
        if (years.length == 1){
            endYear = years[0];
        } else {
            endYear = years[1];
        }
        return result;
    }

}
