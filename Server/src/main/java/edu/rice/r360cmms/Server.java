package edu.rice.r360cmms;


import static spark.Spark.staticFileLocation;
import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Server {
    private static final String TAG = "CMMSServer";
    private static JSONObject database = new JSONObject();


    public static void main(String[] args) {
        String DB_File = "DB.json";
        File initialFile = new File(DB_File);
        InputStream is = null;
        try {
            is = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
        }


        if (is != null) {
            JSONTokener tokener = new JSONTokener(is);
            database = new JSONObject(tokener);
        }
        else {
            database = new JSONObject();
        }
        var shutdownListener = new ShutdownHandler(database, "DB2.json");
        Runtime.getRuntime().addShutdownHook(shutdownListener);
        staticFileLocation("/WebPublic");
        Spark.get(//Returns JSON object
                "/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.toString();
                });
        Spark.get(//Returns JSON object
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return ((JSONObject)((JSONObject)database.get(request.params().get(":category"))).get(request.params().get(":object"))).get(request.params().get(":field")).toString();
                });
        Spark.get(//Returns JSON object
                "/DB/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.toString();
                });
        Spark.get(//Returns JSON object
                "/DB/:category/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.get(request.params().get(":category")).toString();
                });
        Spark.get(//Returns JSON object
                "/DB/:category/:object/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return ((JSONObject)database.get(request.params().get(":category"))).get(request.params().get(":object")).toString();
                });
        Spark.put( //Replaces JSON object in a specific category
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    JSONObject newObject = new JSONObject(); // need to replace this with the object that gets passed in
                    return ((JSONObject)((JSONObject)database.get(request.params().get(":category"))).get(request.params().get(":object"))).put(request.params().get(":field"), newObject).toString();
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/",
                (request, response) -> {
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.toString();
                });
        Spark.delete( //Deletes item
                "/",
                (request, response) -> {
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.toString();
                });

    }
}
