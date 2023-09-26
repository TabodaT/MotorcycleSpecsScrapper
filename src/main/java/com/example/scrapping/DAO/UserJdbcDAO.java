package com.example.scrapping.DAO;

import com.example.scrapping.mappers.UserRowMapper;
import com.example.scrapping.model.User;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserJdbcDAO implements DAO<User>{

    private JdbcTemplate jdbcTemplate;

    public UserJdbcDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> list() {
        String sql = "SELECT id, fullname, email, password from user";
        UserRowMapper userRowMapper = new UserRowMapper();
        return jdbcTemplate.query(sql,userRowMapper);
    }

    @Override
    public void create(User user) {

    }

    @Override
    public Optional<User> get(int id) {
        return Optional.empty();
    }

    @Override
    public void update(User user, int id) {

    }

    @Override
    public void delete(int id) {

    }
}
