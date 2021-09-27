package edu.rice.r360cmms;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShutdownHandler extends Thread {
    JSONObject Database;
    FileWriter DBFile;
    public ShutdownHandler(JSONObject Databasein, String File) {
        Database = Databasein;
        try {
            DBFile = new FileWriter(File);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        System.out.println("=== my shutdown hook activated");
        try {
            DBFile.write(Database.toString(5));
            DBFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
