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
public class ModelsToDataBaseService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MotoModelsRepository motoModelsRepository;

    // Create
    public int insertMoto(MotoModelDTO motoModelDTO){
        return motoModelsRepository.insertMoto(motoModelDTO);
    }

    // Read
    public boolean existsInDB(String url){
        return motoModelsRepository.queryCountByUrl(url) != 0;
    }
    // Update
    // Delete
    // Auxiliary



















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
