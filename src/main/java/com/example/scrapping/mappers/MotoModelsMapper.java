package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.example.scrapping.dto.MotoModelDTO;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.List;

@Slf4j
public class MotoModelsMapper {
    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private Manufacturer manufacturer;
    private String modelName;
    private String modelProductionYears;
    private String endYear = "0";

    private boolean containsSpec;

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
        if (containsSpec && motoModelDTO.getYear() == 0)
            log.error("year " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
        //   end_year
        motoModelDTO.setYear(Integer.parseInt(endYear));
        //   url
        motoModelDTO.setUrl(model.getUrl());
        //   image
        motoModelDTO.setImage(imageFile);

        if (listOfSpecName.size()>0) {
            //   engine
            motoModelDTO.setEngine(getSpecValue("engine"));
            if (containsSpec && motoModelDTO.getEngine().equals("0"))
                log.error("engine " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   capacity
            motoModelDTO.setCapacity(Integer.parseInt(getSpecValue("capacity")));
            if (containsSpec && motoModelDTO.getCapacity() == 0)
                log.error("capacity " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   power
            motoModelDTO.setPower(Integer.parseInt(getSpecValue("power")));
            if (containsSpec && motoModelDTO.getPower() == 0)
                log.error("power " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   clutch
            motoModelDTO.setClutch(getSpecValue("clutch"));
            if (containsSpec && motoModelDTO.getClutch().equals("0"))
                log.error("clutch " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   torque
            motoModelDTO.setTorque(Integer.parseInt(getSpecValue("torque")));
            if (containsSpec && motoModelDTO.getTorque() == 0)
                log.error("torque " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   abs
            motoModelDTO.setAbs(Boolean.parseBoolean(getSpecValue("abs")));
            //   transmission
            motoModelDTO.setTransmission(Integer.parseInt(getSpecValue("transmission")));
            if (containsSpec && motoModelDTO.getTransmission() == 0)
                log.error("transmission " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   final_drive
            motoModelDTO.setFinalDrive(getSpecValue("drive"));
            if (containsSpec && motoModelDTO.getFinalDrive().equals("0"))
                log.error("drive " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   seat_height
            motoModelDTO.setSeatHeight(Integer.parseInt(getSpecValue("seat")));
            if (containsSpec && motoModelDTO.getSeatHeight() == 0)
                log.error("seat " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   dry_weight
            motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("dry")));
            if (containsSpec && motoModelDTO.getDryWeight() == 0)
                log.error("dry " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   wet_weight
            motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("wet")));
            if (motoModelDTO.getDryWeight() == 0 && motoModelDTO.getWetWeight() == 0) {
                motoModelDTO.setDryWeight(Integer.parseInt(getSpecValue("weight")));
            }
            if (containsSpec && motoModelDTO.getWetWeight() == 0 && motoModelDTO.getDryWeight() == 0)
                log.error("wet " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   fuel_capacity
            motoModelDTO.setFuelCapacity(Double.parseDouble(getSpecValue("fuel capacity")));
            if (motoModelDTO.getFuelCapacity() == 0) {
                motoModelDTO.setFuelCapacity(Double.parseDouble(getSpecValue("load capacity")));
            }
            if (containsSpec && motoModelDTO.getFuelCapacity() == 0)
                log.error("fuel capacity " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   reserve
            motoModelDTO.setReserve(Integer.parseInt(getSpecValue("reserve")));
            if (containsSpec && motoModelDTO.getReserve() == 0)
                log.error("reserve " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   consumption
            motoModelDTO.setConsumption(Double.parseDouble(getSpecValue("consumption")));
            if (containsSpec && motoModelDTO.getConsumption() == 0)
                log.error("consumption " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   cooling_system
            motoModelDTO.setCoolingSystem(getSpecValue("cooling"));
            if (containsSpec && motoModelDTO.getCoolingSystem().equals("0"))
                log.error("cooling " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
            //   top_speed
            motoModelDTO.setTopSpeed(Integer.parseInt(getSpecValue("speed")));
            if (containsSpec && motoModelDTO.getTopSpeed() == 0)
                log.error("speed " + modelName + " is missing spec" + " " + model.getUrl()); // to be deleted
        }
        return motoModelDTO;
    }

    private String getSpecValue(String specName) {
        containsSpec = false;
        String result = "0";
        for (int i = 0; i < listOfSpecName.size(); i++) {
            String nameOfSpecAtI = listOfSpecName.get(i).toLowerCase();
            String valueOfSpecAtI = listOfSpecValue.get(i);
            if (nameOfSpecAtI.equals(specName)) {                          // EQUALS
                if (specName.equals("year")) {
                    containsSpec = true;
                    result = formatYear(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("capacity")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "cc");
                    break;
                }
                if (specName.equals("clutch")) {
                    containsSpec = true;
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("weight")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
                if (specName.equals("fuel capacity") || specName.equals("load capacity")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "litre");
                    if (result.equals("0")){
                        result = formatMetricUnit(valueOfSpecAtI, "l");
                    }
                    break;
                }
                if (specName.equals("drive")) {
                    containsSpec = true;
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("reserve")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "l");
                    if (result.equals("0")){
                        result = formatMetricUnit(valueOfSpecAtI, "litre");
                    }
                    break;
                }
                if (specName.equals("consumption")) {
                    containsSpec = true;
                    result = getConsumption(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("transmission")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "speed");
                    break;
                }
            } else if (nameOfSpecAtI.contains(specName)) {                   // CONTAINS
                if (nameOfSpecAtI.contains("power") && !nameOfSpecAtI.contains("weight")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "kw");
                    break;
                }
                if ((specName.equals("drive") && nameOfSpecAtI.contains("final")) || specName.equals("cooling")) {
                    containsSpec = true;
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("torque")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "nm");
                    break;
                }
                if (nameOfSpecAtI.contains("dry") || nameOfSpecAtI.contains("wet")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
                if (specName.equals("seat")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "mm");
                    break;
                }
                if (specName.equals("fuel capacity") || specName.equals("load capacity")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "litre");
                    if (result.equals("0")){
                        result = formatMetricUnit(valueOfSpecAtI, "l");
                    }
                    break;
                }
                if (specName.equals("reserve")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "l");
                    if (result.equals("0")){
                        result = formatMetricUnit(valueOfSpecAtI, "litre");
                    }
                    break;
                }
                if (specName.equals("speed")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "km");
                    break;
                }
                if (specName.equals("consumption")) {
                    containsSpec = true;
                    result = getConsumption(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("transmission")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "speed");
                    break;
                }
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
            boolean foundDigit = false;
            for (int i = preFormat.length() - 1; i >= 0; i--) {
                char c = preFormat.charAt(i);
                if (!foundDigit) {
                    if (Character.isDigit(c) || c == '.') {
                        foundDigit = true;
                    }
                    if (!foundDigit) continue;
                }
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
        String result= "0";
        String yearsToBeFormatted = year.isEmpty() ? modelProductionYears : year;
        String[] years = yearsToBeFormatted.trim().replaceAll(" ", "").split("-");
        if (years.length>0) result = years[0];
        if (years.length == 1) {
            endYear = years[0];
        } else {
            String endYearUnchecked = years[1];
            if (endYearUnchecked.length() == 2) {
                endYear = "20" + endYearUnchecked;
            } else {
                endYear = endYearUnchecked;
            }
        }
        return result;
    }
}
