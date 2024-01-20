package com.example.scrapping.service;

import com.example.scrapping.Constants.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    public void logError(String error) throws IOException {
        addToLog(error, Constants.ERRORS_LOG_TXT_FILE);
    }

    public void logInsertedMotos(String motos) throws IOException {
        addToLog(motos, Constants.INSERTED_MODELS_JSON_LOG_FILE);
    }

    private void addToLog(String errors, String file) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            pw.println(errors);
            pw.flush();
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
            } catch (IOException io) {
                System.out.println(io);
            }
        }
    }
}
