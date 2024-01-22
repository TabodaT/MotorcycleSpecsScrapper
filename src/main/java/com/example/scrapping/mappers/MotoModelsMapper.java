package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.ModelOfManuf;
import com.example.scrapping.dto.MotoModelDTO;
import com.example.scrapping.service.LogsWriterSingletonService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Getter
public class MotoModelsMapper {
    public MotoModelsMapper() {
        this.hasErrors = false;
    }

    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private Manufacturer manufacturer;
    private String modelProductionYears;
    private String endYear = "0";
    private boolean containsSpec;
    private ModelOfManuf modelOfManuf;
    public boolean hasErrors;

    public MotoModelDTO mapMotoModel(List<String> listOfSpecName, List<String> listOfSpecValue, Manufacturer manufacturer, ModelOfManuf modelOfManuf, String imageFile) {
        this.listOfSpecName = listOfSpecName;
        this.listOfSpecValue = listOfSpecValue;
        this.manufacturer = manufacturer;
        this.modelOfManuf = modelOfManuf;
        this.modelProductionYears = modelOfManuf.getProductionYears();

        MotoModelDTO motoModelDTO = new MotoModelDTO();
        String modelName = modelOfManuf.getName();

        //   make
        motoModelDTO.setMake(manufacturer.getName());
        //   model
        motoModelDTO.setModel(modelOfManuf.getName());
        //   year
        motoModelDTO.setYear(Integer.parseInt(getSpecValue("year")));
        motoModelDTO.setEndYear(Integer.parseInt(this.endYear));
        if (containsSpec && motoModelDTO.getYear() == 0)
            log.error("year " + modelName + " is missing spec" + " " + modelOfManuf.getUrl()); // to be deleted
        //   end_year
        motoModelDTO.setYear(Integer.parseInt(endYear));
        //   url
        motoModelDTO.setUrl(modelOfManuf.getUrl());
        //   image
        motoModelDTO.setImage(imageFile);

        if (listOfSpecName.size() > 0) {
            //   engine
            motoModelDTO.setEngine(getSpecValue("engine"));
            if (containsSpec && motoModelDTO.getEngine().equals("0"))
                logError("engine");
            //   capacity
            motoModelDTO.setCapacity(Double.parseDouble(getSpecValue("capacity")));
            if (containsSpec && motoModelDTO.getCapacity() == 0)
                logError("capacity");
            //   power
            motoModelDTO.setPower(Double.parseDouble(getSpecValue("power")));
            if (containsSpec && motoModelDTO.getPower() == 0)
                logError("power");
            //   clutch
            motoModelDTO.setClutch(getSpecValue("clutch"));
            if (containsSpec && motoModelDTO.getClutch().equals("0"))
                logError("clutch");
            //   torque
            motoModelDTO.setTorque(Double.parseDouble(getSpecValue("torque")));
            if (containsSpec && motoModelDTO.getTorque() == 0)
                logError("torque");
            //   abs
            motoModelDTO.setAbs(Boolean.parseBoolean(getSpecValue("abs")));
            //   transmission
            motoModelDTO.setTransmission(Integer.parseInt(getSpecValue("transmission")));
            if (containsSpec && motoModelDTO.getTransmission() == 0)
                logError("transmission");
            //   final_drive
            motoModelDTO.setFinalDrive(getSpecValue("drive"));
            if (containsSpec && motoModelDTO.getFinalDrive().equals("0"))
                logError("drive");
            //   seat_height
            motoModelDTO.setSeatHeight(Integer.parseInt(getSpecValue("seat")));
            if (containsSpec && motoModelDTO.getSeatHeight() == 0)
                logError("seat");
            //   dry_weight
            motoModelDTO.setDryWeight(Double.parseDouble(getSpecValue("dry")));
            if (containsSpec && motoModelDTO.getDryWeight() == 0)
                logError("dry");
            //   wet_weight
            motoModelDTO.setDryWeight(Double.parseDouble(getSpecValue("wet")));
            if (motoModelDTO.getDryWeight() == 0 && motoModelDTO.getWetWeight() == 0) {
                motoModelDTO.setDryWeight(Double.parseDouble(getSpecValue("weight")));
            }
            if (containsSpec && motoModelDTO.getWetWeight() == 0 && motoModelDTO.getDryWeight() == 0)
                logError("wet");
            //   fuel_capacity
            motoModelDTO.setFuelCapacity(Double.parseDouble(getSpecValue("fuel capacity")));
            if (motoModelDTO.getFuelCapacity() == 0) {
                motoModelDTO.setFuelCapacity(Double.parseDouble(getSpecValue("load capacity")));
            }
            if (containsSpec && motoModelDTO.getFuelCapacity() == 0)
                logError("fuel capacity");
            //   reserve
            motoModelDTO.setReserve(Double.parseDouble(getSpecValue("reserve")));
            if (containsSpec && motoModelDTO.getReserve() == 0)
                logError("reserve");
            //   consumption
            motoModelDTO.setConsumption(Double.parseDouble(getSpecValue("consumption")));
            if (containsSpec && motoModelDTO.getConsumption() == 0)
                logError("consumption");
            //   cooling_system
            motoModelDTO.setCoolingSystem(getSpecValue("cooling"));
            if (containsSpec && motoModelDTO.getCoolingSystem().equals("0"))
                logError("cooling");
            //   top_speed
            motoModelDTO.setTopSpeed(Double.parseDouble(getSpecValue("speed")));
            if (containsSpec && motoModelDTO.getTopSpeed() == 0)
                logError("speed");
        }
        return motoModelDTO;
    }

    private void logError(String specValue) {
        hasErrors = true;
        LogsWriterSingletonService logsWriterSingletonService = LogsWriterSingletonService.getInstance();
        String getNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss|dd/MM/yy"));
        String errorString = specValue + " " + manufacturer.getName() + " " + modelOfManuf.getName() + " is missing spec" + " " + modelOfManuf.getUrl();
        log.error(errorString);
        try {
            logsWriterSingletonService.logError(getNow + ": " + errorString);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private String getSpecValue(String specName) {
        containsSpec = false;
        String result = "0";
        for (int i = 0; i < listOfSpecName.size(); i++) {
            String nameOfSpecAtI = listOfSpecName.get(i).toLowerCase();
            String valueOfSpecAtI = listOfSpecValue.get(i);
            if (nameOfSpecAtI.equals(specName)) {                          // EQUALS
                containsSpec = true;
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
                if (specName.equals("fuel capacity") || specName.equals("load capacity")) {
                    result = formatMetricUnit(valueOfSpecAtI, "litre");
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "l");
                    }
                    break;
                }
                if (specName.equals("drive")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("reserve")) {
                    result = formatMetricUnit(valueOfSpecAtI, "l");
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "litre");
                    }
                    break;
                }
                if (specName.equals("consumption")) {
                    result = getConsumption(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("transmission")) {
                    result = formatMetricUnit(valueOfSpecAtI, "speed");
                    result = result.equals("0") ? "1" : result;
                    break;
                }
                if (specName.equals("engine")) {
                    containsSpec = true;
                    result = valueOfSpecAtI;
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
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "l");
                    }
                    break;
                }
                if (specName.equals("reserve")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "l");
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "litre");
                    }
                    break;
                }
                if (specName.equals("speed")) {
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "km");
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "kph");
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
        String preFormat;
        try {
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
        } catch (Exception e) {
            System.out.println("Consumption err: " + e);
            result = "1";
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
        String result = "0";
        String yearsToBeFormatted = year.isEmpty() ? modelProductionYears : year;
        String[] years = yearsToBeFormatted.trim().replaceAll(" ", "").split("-");
        if (years.length > 0) result = years[0];

        if (years.length == 1 && result.length() == 8) {
            endYear = result.substring(4);
            result = result.substring(0, 4);
            return result;
        } else if (years.length == 1 && result.length() > 8) {
            result = result.substring(0, 4);
            endYear = result;
            return result;
        }

        if (years.length == 1) {
            endYear = years[0];
        } else {
            String endYearUnchecked = years[1];
            if (endYearUnchecked.length() == 2) {
                int yearLast2Num = Integer.parseInt(years[0].substring(2));
                int endInteger = Integer.parseInt(endYearUnchecked);
                String startYearPrefix = years[0].substring(0, 2);
                if (Math.max(yearLast2Num, endInteger) == endInteger) {
                    endYear = startYearPrefix + endYearUnchecked;
                } else {
                    endYear = (Integer.parseInt(startYearPrefix) + 1) + endYearUnchecked;
                }
            } else {
                endYear = endYearUnchecked;
            }
        }
        return result;
    }
}
