package com.example.scrapping.service;

import com.example.scrapping.DAO.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StoreModelsToDataBaseService {
    @Autowired
    UserRepository userRepository;

    public void getAllUserNames(){
        System.out.println(userRepository.getAllUserNames());
    }

    public void getUserID1(){
        userRepository.getUserIDs();
    }

    public void updateUserId(){
        userRepository.updateUserId();
    }
}
