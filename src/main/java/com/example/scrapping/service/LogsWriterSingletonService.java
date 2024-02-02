package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LogsWriterSingletonService {
    public LogsWriterSingletonService() {}
    private static LogsWriterSingletonService instance;

    public static LogsWriterSingletonService getInstance(){
        if (instance == null){
            instance = new LogsWriterSingletonService();
        }
        return instance;
    }

    public void logError(String error) {
        addToLog(error, Constants.ERRORS_LOG_TXT_FILE);
    }

    public void logInsertedMotos(String motos)  {
        addToLog(motos, Constants.NOT_INSERTED_MODELS_JSON_LOG_FILE);
    }

    private void addToLog(String errors, String file) {
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw)) {
            pw.println(errors);
            pw.flush();
        }  catch (Exception e){
            System.out.println(e);
        }
    }
}
