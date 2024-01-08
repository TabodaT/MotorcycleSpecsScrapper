package com.example.scrapping.DAO;

import com.example.scrapping.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository { // to be deleted
    @Autowired
    JdbcTemplate jdbcTemplate;


    // Get
    public List<String> getAllUserNames(){
        return jdbcTemplate.queryForList("SELECT fullname from user;", String.class);
    }

    public void getUserIDs(){
        String ids = "1,2";
        String sql = "select * from user where id in ("+ids+")";
        List<UserDTO> userDTO = jdbcTemplate.query(sql, new BeanPropertyRowMapper(UserDTO.class));
        System.out.println(userDTO);
    }

    // Update
    public void updateUserId(){
        System.out.println("Before:");
        getUserIDs();
        jdbcTemplate.update("update user set fullname = 'updated1' where id = 1 ");
        System.out.println("After:");
        getUserIDs();
    }

    // Insert


    //Delete
}
