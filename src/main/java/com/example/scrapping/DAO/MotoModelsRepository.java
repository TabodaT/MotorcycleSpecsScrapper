package com.example.scrapping.DAO;

import com.example.scrapping.dto.MotoModelDTO;
import com.google.common.io.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Repository
public class MotoModelsRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;
    private static final String INSERT_USER_SCRIPT = "Statements/insert_user.txt";
    private static final String INSERT_MOTO_SCRIPT = "Statements/insert_moto.txt";
    private static final String DELETE_USER_SCRIPT = "Statements/delete_user.txt";
    // Insert
    public void insertMoto(MotoModelDTO motoModelDTO){
        String sql = getSqlText(INSERT_MOTO_SCRIPT);
        System.out.println(sql); // to be deleted

        jdbcTemplate.update(sql+
                "'"+motoModelDTO.getMake()+"',"+
                "'"+motoModelDTO.getModel()+"',"+
                motoModelDTO.getYear()+","+
                motoModelDTO.getEndYear()+","+
                "'"+motoModelDTO.getEngine()+"',"+
                motoModelDTO.getCapacity()+","+
                motoModelDTO.getPower()+","+
                "'"+motoModelDTO.getClutch()+"',"+
                motoModelDTO.getTorque()+","+
                motoModelDTO.isAbs()+","+
                motoModelDTO.getTransmission()+","+
                "'"+motoModelDTO.getFinalDrive()+"',"+
                motoModelDTO.getSeatHeight()+","+
                motoModelDTO.getDryWeight()+","+
                motoModelDTO.getWetWeight()+","+
                motoModelDTO.getFuelCapacity()+","+
                motoModelDTO.getReserve()+","+
                motoModelDTO.getConsumption()+","+
                "'"+motoModelDTO.getCoolingSystem()+"',"+
                motoModelDTO.getTopSpeed()+","+
                "'"+motoModelDTO.getUrl()+"',"+
                "'"+motoModelDTO.getImage()+"')");
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
    // Get
    public List<String> getAllUserNames(){
        return jdbcTemplate.queryForList("SELECT fullname from moto_models;", String.class);
    }
    // Update
}
