package com.example.scrapping.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class MotoModelsRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // Get
    public List<String> getAllUserNames(){
        return jdbcTemplate.queryForList("SELECT fullname from moto_models;", String.class);
    }
    // Update
    // Insert
    // Delete
}
