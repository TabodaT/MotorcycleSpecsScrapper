package com.example.scrapping.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteErrorsLogSingletonService {
    public WriteErrorsLogSingletonService() {}
    private static WriteErrorsLogSingletonService instance;

    public static WriteErrorsLogSingletonService getInstance(){
        if (instance == null){
            instance = new WriteErrorsLogSingletonService();
        }
        return instance;
    }

    private static final String writeTo = "src/main/resources/errors_log.txt";

    public void addToErrorsLog(String errors) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(writeTo, true);
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
