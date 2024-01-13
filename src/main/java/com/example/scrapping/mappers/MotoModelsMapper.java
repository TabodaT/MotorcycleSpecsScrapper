package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.example.scrapping.dto.MotoModelDTO;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MotoModelsMapper {
    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private Manufacturer manufacturer;
    private String modelName;

    public MotoModelDTO mapMotoModel(List<String> listOfSpecName, List<String> listOfSpecValue, Manufacturer manufacturer, Model model, String imageFile) {
        this.listOfSpecName = listOfSpecName;
        this.listOfSpecValue = listOfSpecValue;
        this.manufacturer = manufacturer;
        this.modelName = model.getName();

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
        motoModelDTO.setCapacity(getSpecValue("capacity"));
        //   power
        motoModelDTO.setPower(getSpecValue("power"));
        //   clutch
        motoModelDTO.setClutch(getSpecValue("clutch"));
        //   torque
        motoModelDTO.setTorque(getSpecValue("torque"));
        //   abs
        motoModelDTO.setAbs(Boolean.parseBoolean(getSpecValue("abs")));
        //   front_brakes
        //   rear_brakes
        //   transmission
        motoModelDTO.setTransmission(getSpecValue("transmission"));
        //   final_drive
        motoModelDTO.setFinalDrive(getSpecValue("drive"));
        //   seat_height
        motoModelDTO.setSeatHeight(getSpecValue("seat"));
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
        //   range
        //   cooling_system
        //   ignition
        //   oil_capacity
        //   power_to_weight_ratio
        //   top_speed
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
            List<String> wordsInSpecName = getWords(nameOfSpecAtI);
            String valueOfSpecAtI = listOfSpecValue.get(i);
            if (nameOfSpecAtI.equals(specName)) {                          // EQUALS
                if (specName.equals("year")) {
                    result = formatYear(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("capacity")) {
                    result = formatCapacityCC(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("clutch")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("weight")) {
                    result = formatWeightKg(valueOfSpecAtI);
                    break;
                }
                result = valueOfSpecAtI;
            } else if (nameOfSpecAtI.contains(specName)) {                   // CONTAINS
                if (nameOfSpecAtI.contains("power") && !nameOfSpecAtI.contains("weight")) {
                    result = formatKwOrNmOrLitre(valueOfSpecAtI, "kw");
                    break;
                }
                if (specName.equals("transmission") || specName.equals("drive")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("torque")) {
                    result = formatKwOrNmOrLitre(valueOfSpecAtI, "nm");
                    break;
                }
                if (nameOfSpecAtI.contains("dry") || nameOfSpecAtI.contains("wet")) {
                    result = formatWeightKg(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("seat")) {
                    result = getSeatMMHeight(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("fuel capacity") || specName.equals("load capacity")) {
                    result = formatKwOrNmOrLitre(valueOfSpecAtI, "litre");
                    break;
                }
                if (specName.equals("reserve")) {
                    result = formatKwOrNmOrLitre(valueOfSpecAtI, "l");
                    break;
                }
                if (specName.equals("consumption")) {
                    result = getConsumption(valueOfSpecAtI);
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
            preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("km")).trim().replaceAll(" ", "").substring(0,rawSpecValue.toLowerCase().indexOf("l"));
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
            double lPer100km = 235.21/Double.parseDouble(result);
            result = df.format(lPer100km);
        }
        return result;
    }

    private String getSeatMMHeight(String seat) {
        return seat.toLowerCase().substring(0, seat.indexOf("mm"));
    }

    private String formatKwOrNmOrLitre(String rawSpecValue, String kwOrNmOrLitre) {
        String result = "";
        String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf(kwOrNmOrLitre)).trim().replaceAll(" ", "");
        result = getNumberFromEndOfString(preFormat);
        return result;
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

    private String formatWeightKg(String weight) {
        String formatWeight = weight.substring(0, weight.toLowerCase().indexOf("kg")).trim();
        checkIfValueIsExtracted(formatWeight, weight);
        return formatWeight;
    }

    private String formatCapacityCC(String capacity) {
        String formatCapacity = capacity.trim().substring(0, capacity.indexOf("cc"));
        checkIfValueIsExtracted(formatCapacity, capacity);
        return formatCapacity;
    }

    private String formatYear(String year) {
        String formattedYear = year.trim().replaceAll("-", "");

        return formattedYear;
    }

    private List<String> getWords(String specName) {
        List<String> result = Arrays.asList(specName.split(" "));
        if (result.size() == 1) {
            for (int i = 0; i < specName.length(); i++) {
                char c = specName.charAt(i);
                if (!Character.isLetter(c)) {
                    result = Arrays.asList(specName.split(String.valueOf(c)));
                }
            }
        }
        return result;
    }

    private void checkIfValueIsExtracted(String value, String field) {
        try {
            int convert = Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Failed to convert " + field + " (" + value + "): " +
                    manufacturer.getName() + " " + modelName + " " + manufacturer.getUrl() + e);
        }
    }

}
