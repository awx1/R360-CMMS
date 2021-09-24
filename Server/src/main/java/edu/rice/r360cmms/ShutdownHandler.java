package edu.rice.r360cmms;

import org.json.JSONObject;

import java.io.File;

public class ShutdownHandler extends Thread {
    JSONObject Database;
    File DBFile;
    public ShutdownHandler(JSONObject Databasein, File DB_File) {
        Database = Databasein;
        DBFile = DB_File;
    }
    public void run() {
        System.out.println("=== my shutdown hook activated");

    }
}
