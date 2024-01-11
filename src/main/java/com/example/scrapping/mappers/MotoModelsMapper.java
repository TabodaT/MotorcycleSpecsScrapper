package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.example.scrapping.dto.MotoModelDTO;
import lombok.extern.slf4j.Slf4j;

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
        //   fuel_capacity
        //   reserve
        //   consumption
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
        for (int i = 0; i < listOfSpecName.size(); i++){
            String nameOfSpecAtI = listOfSpecName.get(i).toLowerCase();
            String[] wordsInSpecName = nameOfSpecAtI.split("_");
            String valueOfSpecAtI = listOfSpecValue.get(i);
            if (nameOfSpecAtI.equals(specName)){                          // EQUALS
                if (specName.equals("year")){
                    result = formatYear(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("capacity")){
                    result = formatCapacityCC(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("clutch")){
                    result = valueOfSpecAtI;
                    break;
                }
                result = valueOfSpecAtI;
            }else if(nameOfSpecAtI.contains(specName)){                   // CONTAINS
                if (nameOfSpecAtI.contains("power") && !nameOfSpecAtI.contains("weight")){
                    result = formatPower(valueOfSpecAtI);
                    break;
                }
                if (specName.equals("torque") || specName.equals("transmission") || specName.equals("drive")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (nameOfSpecAtI.contains("weigh")){
                    result = formatWeightKg(valueOfSpecAtI);
                }
                if (specName.equals("seat")){
                    result =
                }

                result = valueOfSpecAtI;
            }

            if (nameOfSpecAtI.contains("brake") && specName.equals("abs")){
                if (valueOfSpecAtI.toLowerCase().contains(specName)) {
                    result = "true";
                    return result;
                }else {
                    result = "false";
                }
            }

        }
        return result;
    }

    private String formatPower(String power){
        String formatPower = "";
        String preFormat = power.substring(0,power.toLowerCase().indexOf("kw")).trim().replaceAll(" ","");
        if (preFormat.contains("/") || preFormat.contains("hp")){
            int locOfSlash = preFormat.indexOf("/");
            int locOfHP = preFormat.indexOf("hp");
            int cutFromLoc = Math.max(locOfHP,locOfSlash);
            if (preFormat.contains("hp") && locOfHP == cutFromLoc){
                cutFromLoc+=2;
            } else if (preFormat.contains("/") && locOfSlash == cutFromLoc){
                cutFromLoc++;
            }
            formatPower = preFormat.substring(cutFromLoc);
        } else {
            formatPower = preFormat;
        }
        checkIfValueIsExtracted(formatPower,power);
        return formatPower;
    }

    private String formatWeightKg(String weight){
        String formatWeight = weight.substring(0,weight.toLowerCase().indexOf("kg")).trim();
        checkIfValueIsExtracted(formatWeight,weight);
        return formatWeight;
    }

    private String formatCapacityCC(String capacity){
        String formatCapacity = capacity.trim().substring(0,capacity.indexOf("cc"));
        checkIfValueIsExtracted(formatCapacity,capacity);
        return formatCapacity;
    }

    private String formatYear(String year){
        String formattedYear = year.trim().replaceAll("-","");

        return formattedYear;
    }

    private void checkIfValueIsExtracted(String value, String field){
        try{
            int convert = Integer.parseInt(value);
        }catch (Exception e){
            log.error("Failed to convert "+field+" ("+value+"): " +
                    manufacturer.getName()+" "+modelName+" "+manufacturer.getUrl() + e);
        }
    }

}
