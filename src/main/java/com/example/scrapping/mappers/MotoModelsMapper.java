package com.example.scrapping.mappers;

import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.ModelOfManuf;
import com.example.scrapping.dto.MotoModelDTO;
import com.example.scrapping.service.LogsWriterSingletonService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Getter
public class MotoModelsMapper {
    public MotoModelsMapper() {
        this.erroneous = false;
    }

    private List<String> listOfSpecName;
    private List<String> listOfSpecValue;
    private Manufacturer manufacturer;
    private String modelProductionYears;
    private String endYear = "0";
    private boolean containsSpec;
    private ModelOfManuf modelOfManuf;
    private boolean erroneous;
    private final LogsWriterSingletonService logsWriterSingletonService = LogsWriterSingletonService.getInstance();

    public MotoModelDTO mapMotoModel(List<String> listOfSpecName, List<String> listOfSpecValue,
                                     Manufacturer manufacturer, ModelOfManuf modelOfManuf, String imageFile) {
        this.listOfSpecName = listOfSpecName;
        this.listOfSpecValue = listOfSpecValue;
        this.manufacturer = manufacturer;
        this.modelOfManuf = modelOfManuf;
        this.modelProductionYears = modelOfManuf.getProductionYears();
        this.erroneous = false;

        MotoModelDTO motoModelDTO = new MotoModelDTO();
//        String modelName = modelOfManuf.getName();

        //   make
        motoModelDTO.setMake(manufacturer.getName());
        //   model
        motoModelDTO.setModel(modelOfManuf.getName().replaceAll("'", "\\\\'"));
        //   year
        motoModelDTO.setStartYear(Integer.parseInt(getSpecValue("year")));
        if (motoModelDTO.getStartYear() == 0) {
            motoModelDTO.setStartYear(Integer.parseInt(formatYear(modelOfManuf.getProductionYears())));
        }
        //   end_year
        motoModelDTO.setEndYear(Integer.parseInt(endYear));
        if (motoModelDTO.getStartYear() == 0 || motoModelDTO.getEndYear() == 0) {
            logError("year");
        }
        //   url
        motoModelDTO.setUrl(modelOfManuf.getUrl());
        //   image
        motoModelDTO.setImage(imageFile);

        if (listOfSpecName.size() > 0) {
            //   engine
            motoModelDTO.setEngine(getSpecValue("engine").replaceAll("'","\\\\'"));
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
            motoModelDTO.setSeatHeight(Double.parseDouble(getSpecValue("seat")));
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
        resetMapper();
        return motoModelDTO;
    }

    private void logError(String specValue) {
        erroneous = true;
        String getNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss|dd/MM/yy"));
        StringBuilder errorStringSB = new StringBuilder();
        errorStringSB.append(specValue).append(", spec is missing from: ")
                .append("page= ").append(modelOfManuf.getPage()).append(". ")
                .append(manufacturer.getName()).append(" ")
                .append(modelOfManuf.getName()).append(" ")
                .append(modelOfManuf.getUrl());
        log.error(errorStringSB.toString());
        try {
            logsWriterSingletonService.logError(errorStringSB.insert(0, ": ").insert(0, getNow).toString());
        } catch (Exception e) {
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
                    result = formatMetricUnit(valueOfSpecAtI, "litre");
                    if (result.equals("0")) {
                        result = formatMetricUnit(valueOfSpecAtI, "l");
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
                    result = valueOfSpecAtI;
                    break;
                }
                if (specName.equals("cooling")) {
                    result = valueOfSpecAtI;
                    break;
                }
                if (nameOfSpecAtI.equals("dry") || nameOfSpecAtI.equals("wet")) {
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
            } else if (nameOfSpecAtI.contains(specName)) {                   // CONTAINS
                boolean itExistsButItsEmpty = valueOfSpecAtI.trim().toLowerCase().contains("n/a")
                        || valueOfSpecAtI.trim().toLowerCase().contains("na")
                        || valueOfSpecAtI.trim().isEmpty();
                if (nameOfSpecAtI.contains("power") && !nameOfSpecAtI.contains("weight") && !nameOfSpecAtI.contains("rear") && !nameOfSpecAtI.contains("transmission")) {
                    if (itExistsButItsEmpty) break;
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "kw");
                    break;
                }
                if ((specName.equals("drive") && nameOfSpecAtI.contains("final")) || specName.equals("cooling")) {
                    containsSpec = true;
                    result = valueOfSpecAtI;
                    if (specName.equals("drive") && result.length() > 100) {
                        containsSpec = true;
                        result = "0";
                    }
                    break;
                }
                if (specName.equals("torque")) {
                    if (itExistsButItsEmpty) break;
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "nm");
                    break;
                }
                if (nameOfSpecAtI.contains("dry") || nameOfSpecAtI.contains("wet")) {
                    if (itExistsButItsEmpty) break;
                    containsSpec = true;
                    result = formatMetricUnit(valueOfSpecAtI, "kg");
                    break;
                }
                if (specName.equals("seat")) {
                    if (itExistsButItsEmpty) break;
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
                    result = result.equals("0") ? "1" : result;
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

    private void resetMapper() {
        this.listOfSpecName.clear();
        this.listOfSpecValue.clear();
        this.manufacturer = null;
        this.modelProductionYears = null;
        this.endYear = "0";
        this.containsSpec = false;
        this.modelOfManuf = null;
    }

    private String getConsumption(String rawSpecValue) {
        String result = "";
        String preFormat;
        try {
            if (rawSpecValue.toLowerCase().contains("km")) {
                preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("km")).trim().replaceAll(" ", "").substring(0, rawSpecValue.toLowerCase().indexOf("l"));
                result = getNumberFromBeginningOrEndOfString(preFormat, true);
            } else if (rawSpecValue.toLowerCase().contains("mpg")) {
                preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("mpg")).trim().replaceAll(" ", "");
                result = getNumberFromBeginningOrEndOfString(preFormat, false);
                DecimalFormat df = new DecimalFormat("#.#");
                double lPer100km = 235.21 / Double.parseDouble(result);
                result = df.format(lPer100km);
            } else if (rawSpecValue.toLowerCase().contains("kwh")) {
                result = getNumberFromBeginningOrEndOfString(rawSpecValue, false);
            }
        } catch (Exception e) {
            System.out.println("Consumption err: " + e);
            result = "1";
        }
        return result;
    }

    private String formatMetricUnit(String rawSpecValue, String unit) {
        String result = "";
        if (rawSpecValue.toLowerCase().contains(unit)) {
            String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf(unit)).trim().replaceAll(" ", "");
            result = getNumberFromBeginningOrEndOfString(preFormat, false);
        } else {
            String preFormat2 = (rawSpecValue.toLowerCase().trim()).replaceAll(" ", "");
            if (unit.equals("cc")) {
                result = getNumberFromBeginningOrEndOfString(preFormat2, true);
                if (result.isEmpty())
                    result = getNumberFromBeginningOrEndOfString(modelOfManuf.getName(), true);
            } else if (unit.equals("nm") && rawSpecValue.toLowerCase().contains("kg")) {
                String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("kg")).trim().replaceAll(" ", "");
                double kgM = Double.parseDouble(getNumberFromBeginningOrEndOfString(preFormat, false));
                DecimalFormat df = new DecimalFormat("#");
                result = String.valueOf(df.format(9.80665 * kgM));
            } else if (unit.equals("kw") && rawSpecValue.toLowerCase().contains("hp")) {
                String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("hp")).trim().replaceAll(" ", "");
                result = getNumberFromBeginningOrEndOfString(preFormat, false);
            } else if (unit.equals("mm")) {
                if (preFormat2.contains("in")) {
                    String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("in")).trim().replaceAll(" ", "");
                    double in = Double.parseDouble(getNumberFromBeginningOrEndOfString(preFormat, false));
                    DecimalFormat df = new DecimalFormat("#");
                    result = String.valueOf(df.format(25.4 * in));
                } else
                    result = getNumberFromBeginningOrEndOfString(preFormat2, true);
            } else if (unit.equals("kg") && rawSpecValue.contains("lb")) {
                String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("lb")).trim().replaceAll(" ", "");
                double lb = Double.parseDouble(getNumberFromBeginningOrEndOfString(preFormat, false));
                DecimalFormat df = new DecimalFormat("#");
                result = String.valueOf(df.format(0.453592 * lb));
            } else if (unit.equals("kph") && rawSpecValue.contains("mph")) {
                String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("mph")).trim().replaceAll(" ", "");
                double mph = Double.parseDouble(getNumberFromBeginningOrEndOfString(preFormat, false));
                DecimalFormat df = new DecimalFormat("#");
                result = String.valueOf(df.format(1.609 * mph));
            } else if (unit.equals("nm") && result == "" && rawSpecValue.contains("ft")) {
                String preFormat = rawSpecValue.substring(0, rawSpecValue.toLowerCase().indexOf("ft")).trim().replaceAll(" ", "");
                double ftLb = Double.parseDouble(getNumberFromBeginningOrEndOfString(preFormat, false));
                DecimalFormat df = new DecimalFormat("#");
                result = String.valueOf(df.format(ftLb * 1.3558));
            }
        }
        return result.isEmpty() ? "0" : result;
    }

    private String getNumberFromBeginningOrEndOfString(String preFormat, Boolean startFromBeginningOfString) {
        StringBuilder resultSB = new StringBuilder();
        if (preFormat.length() > 1) {
            boolean foundDigit = false;
            boolean foundDot = false;
            for (int i = preFormat.length() - 1; i >= 0; i--) {
                int k = i;
                if (startFromBeginningOfString) k = preFormat.length() - 1 - i;
                char c = preFormat.charAt(k);
                if (!foundDigit) {
                    if (Character.isDigit(c)) {
                        foundDigit = true;
                    }
                    if (!foundDigit) continue;
                }
                if (Character.isDigit(c) || c == '.') {
                    if (foundDot && c == '.') break;
                    if (startFromBeginningOfString)
                        resultSB.append(c);
                    else
                        resultSB.insert(0, c);
                    if (c == '.') foundDot = true;
                } else break;
            }
        } else if (preFormat.length() == 1) {
            if (Character.isDigit(preFormat.charAt(0))) {
                resultSB.append(preFormat);
            }
        }
        return resultSB.toString();
    }

    private String formatYear(String year) {
        String result = "0";
        String yearsToBeFormatted = year.isEmpty() ? modelProductionYears : year;
        for (char c : year.toCharArray()) {
            if (Character.isLetter(c) || c == '/' || c == ':' ) {
                yearsToBeFormatted = modelProductionYears;
                break;
            }
        }

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
