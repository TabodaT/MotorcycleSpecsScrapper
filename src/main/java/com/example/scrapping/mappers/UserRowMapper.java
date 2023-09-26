package com.example.scrapping.mappers;

import com.example.scrapping.DAO.UserJdbcDAO;
import com.example.scrapping.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        try {
            user.setEmail(rs.getString("email"));
            user.setId(rs.getInt("id"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
        } catch (SQLException e){
            log.error(String.valueOf(e));
        }

        return user;
    }
}
