package com.example.scrapping.service;

import com.example.scrapping.DAO.MotoModelsRepository;
import com.example.scrapping.DAO.UserRepository;
import com.example.scrapping.dto.Manufacturer;
import com.example.scrapping.dto.Model;
import com.example.scrapping.dto.MotoModelDTO;
import com.example.scrapping.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StoreModelsToDataBaseService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MotoModelsRepository motoModelsRepository;

    public void insertMoto(){
        MotoModelDTO motoModelDTO1 = new MotoModelDTO();
        motoModelDTO1.setMake("test");
        motoModelDTO1.setModel("test");
        motoModelDTO1.setYear(1);
        motoModelDTO1.setEndYear(1);
        motoModelDTO1.setEngine("test");
        motoModelDTO1.setCapacity(1);
        motoModelDTO1.setPower(1);
        motoModelDTO1.setClutch("test");
        motoModelDTO1.setTorque(1);
        motoModelDTO1.setAbs(true);
        motoModelDTO1.setTransmission(1);
        motoModelDTO1.setFinalDrive("test");
        motoModelDTO1.setSeatHeight(1);
        motoModelDTO1.setDryWeight(1);
        motoModelDTO1.setWetWeight(1);
        motoModelDTO1.setFuelCapacity(1);
        motoModelDTO1.setReserve(1);
        motoModelDTO1.setConsumption(1);
        motoModelDTO1.setCoolingSystem("test");
        motoModelDTO1.setTopSpeed(1);
        motoModelDTO1.setUrl("test");
        motoModelDTO1.setImage("test");

        motoModelsRepository.insertMoto(motoModelDTO1);
    }





















    public void getAllUserNames(Manufacturer manufacturer, Model model){
        System.out.println(userRepository.getAllUserNames());
    }

    public void getUserID1(){
        userRepository.getUserIDs();
    }

    public void updateUserId(){
        userRepository.updateUserId();
    }

    public void insertUser(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(2);
        userDTO.setFullName("name2");
        userDTO.setEmail("email2");
        userDTO.setPassword("pass2");
        userRepository.insertUser(userDTO);
    }

    public void deleteUser(){
        userRepository.deleteUser(2);
    }
}
