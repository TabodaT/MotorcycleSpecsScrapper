package com.example.scrapping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
//public class MyDataBaseService implements CommandLineRunner {
public class MyDataBaseService {

//    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Override
    public void run(String... args) throws Exception {
        String sql = "INSERT INTO user (id, fullname, email, password)  VALUES (?, ?, ?, ?)";

        int result = jdbcTemplate.update(sql,"1", "name1", "email1", "passw1");

//        jdbcTemplate.execute();

        if (result>0){
            System.out.println("A new row has been inserted");
        } else {
            System.out.println("Didn't insert");
        }


    }
}
