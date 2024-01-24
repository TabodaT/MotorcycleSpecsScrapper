package com.example.scrapping.DAO;

import com.example.scrapping.Constants.Constants;
import com.example.scrapping.dto.MotoModelDTO;
import com.google.common.io.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@Repository
public class MotoModelsRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // Create
    public int insertMoto(MotoModelDTO motoModelDTO) {
        String sql =  String.format( getSqlText(Constants.INSERT_MOTO_SCRIPT), motoModelDTO.getMake() , motoModelDTO.getModel(), motoModelDTO.getStartYear(),
                motoModelDTO.getEndYear(), motoModelDTO.getEngine(), motoModelDTO.getCapacity(), motoModelDTO.getPower(),
                motoModelDTO.getClutch(), motoModelDTO.getTorque(), motoModelDTO.isAbs(), motoModelDTO.getTransmission(),
                motoModelDTO.getFinalDrive(), motoModelDTO.getSeatHeight(), motoModelDTO.getDryWeight(), motoModelDTO.getWetWeight(),
                motoModelDTO.getFuelCapacity(), motoModelDTO.getReserve(), motoModelDTO.getConsumption(), motoModelDTO.getCoolingSystem(),
                motoModelDTO.getTopSpeed(), motoModelDTO.getUrl(), motoModelDTO.getImage());

        return jdbcTemplate.update(sql);
    }

    public int queryCountByUrl(String url) {
        String sql = String.format(getSqlText(Constants.COUNT_MOTO_BY_URL), url);
//        String sql = getSqlText(Constants.COUNT_MOTO_BY_URL) + "'" + url + "';";
        int motoModelCount = 0;
        try {
            motoModelCount = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            System.out.println(e);
        }
        System.out.println(motoModelCount == 0 ? "Not in DB" : "Exists in DB");
        return motoModelCount;
    }

    // Update
    // Delete
    // Auxiliary
    private String getSqlText(String filename) {
        URL url = Resources.getResource(filename);
        String text = "";
        try {
            text = Resources.toString(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(e);
        }
        return text;
    }

    // Other // to be deleted

//    private static final String INSERT_USER_SCRIPT = "Statements/insert_user.txt";
//    private static final String DELETE_USER_SCRIPT = "Statements/delete_user.txt";

//    public List<String> getAllUserNames(){
//        return jdbcTemplate.queryForList("SELECT fullname from moto_models;", String.class);
//    }
//
//    public void deleteUser(int id){
//        String sql = getSqlText(DELETE_USER_SCRIPT);
//        System.out.println(sql); // to be deleted
//        jdbcTemplate.update(sql+id+");");
//    }

}
