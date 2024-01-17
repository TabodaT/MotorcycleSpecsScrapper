package com.example.scrapping.DAO;

import com.example.scrapping.dto.UserDTO;
import com.google.common.io.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private static final String INSERT_USER_SCRIPT = "Statements/insert_user.txt";
    private static final String DELETE_USER_SCRIPT = "Statements/delete_user.txt";
    // Insert
    public void insertUser(UserDTO userDTO){
        String sql = getSqlText(INSERT_USER_SCRIPT);
        System.out.println(sql); // to be deleted

        jdbcTemplate.update(sql+userDTO.getId()+","+
                "'"+userDTO.getFullName()+"',"+
                "'"+userDTO.getEmail()+"',"+
                "'"+userDTO.getPassword()+"');");
    }

    private String getSqlText(String filename){
        URL url = Resources.getResource(filename);
        String text = "";
        try {
            text = Resources.toString(url, StandardCharsets.UTF_8);
        }catch (Exception e){
            System.out.println(e);
        }
        return text;
    }

    //Delete
    public void deleteUser(int id){
        String sql = getSqlText(DELETE_USER_SCRIPT);
        System.out.println(sql); // to be deleted
        jdbcTemplate.update(sql+id+");");
    }
}
