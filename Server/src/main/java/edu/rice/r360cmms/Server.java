package edu.rice.r360cmms;


import static spark.Spark.staticFileLocation;
import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Request;
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
        //shutdownListener.run();
        Runtime.getRuntime().addShutdownHook(shutdownListener);

        System.out.println("Shutdown Handler Initialized");
        staticFileLocation("/WebPublic");
        Spark.get(//Returns JSON object
                "/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return database.toString(5);
                });
        Spark.get(//Returns JSON object
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return getField(database,request);
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
                "/Save/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    shutdownListener.run();
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
                    return getCategory(database,request);
                });
        Spark.get(//Returns JSON object
                "/DB/:category/:object/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return getObject(database,request);
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
                    return getObject(database,request).put(request.params().get(":field"), newObject);
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/",
                (request, response) -> {
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    JSONObject newObject = new JSONObject(); // need to replace this with the object that gets passed in
                    return database.put(request.params().get(":category"), newObject);
                });
        Spark.delete( //Deletes an entire category
                "/DB/:category/",
                (request, response) -> {
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    return database.remove(request.params().get(":category")).toString();
                });
        Spark.delete( //Deletes JSON object in a specific category
                "/DB/:category/:object/",
                (request, response) -> {
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    return getCategory(database,request).remove(request.params(":object")).toString();
                });
        Spark.delete(//Delete a specific field
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body().toString());
                    System.out.println(request.params().toString());
                    //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return getObject(database,request).remove(request.params().get(":field")).toString();
                });

    }

    /**
     * Helper function used to check if a field exists and return it
     * @param DB database object
     * @param Input index of the requested object, e.g. "category1"
     * @return the requested Object, or null if the object doesn't exist
     */
    private static Object getHandler(JSONObject DB, String Input) {
        if (DB.has(Input)) {
            Object out = DB.get(Input);
            return out;
        } else {
            return null;
        }

    }

    private static JSONObject getCategory(JSONObject DB, String category) {
        return (JSONObject) getHandler(DB,category);
    }

    private static JSONObject getObject(JSONObject DB, String category, String object) {
        JSONObject cat =  getCategory(DB, category);
        if (cat == null) {
            return null;
        } else {
            return (JSONObject) getHandler(cat, object);
        }
    }

    private static String getField(JSONObject DB, String category, String object, String field) {
        JSONObject fiel = getObject(DB, category, object);
        if (fiel == null) {
            return null;
        } else {
            return (String) getHandler(fiel, field);
        }
    }

    private static String getField(JSONObject DB, Request request) {
        return getField(DB, request.params().get(":category"),request.params().get(":object"),request.params().get(":field"));
    }

    private static JSONObject getObject(JSONObject DB, Request request) {
        return getObject(DB, request.params().get(":category"),request.params().get(":object"));
    }

    private static JSONObject getCategory(JSONObject DB, Request request) {
        return getCategory(DB, request.params().get(":category"));
    }
}
