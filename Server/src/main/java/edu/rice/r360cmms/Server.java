package edu.rice.r360cmms;


import static spark.Spark.staticFileLocation;

import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Request;
import spark.Spark;

import java.io.*;
import java.security.UnresolvedPermission;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    private static JSONObject database = new JSONObject();
    private static JSONObject idArray = new JSONObject();

    public static void main(String[] args) {
        String DB_File = "DB.json";
        String ID_File = "ID.json";
        File initialFile_DB = new File(DB_File);
        File initialFile_ID = new File(ID_File);
        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(initialFile_DB);
        } catch (FileNotFoundException ignored) {
        }
        try {
            is2 = new FileInputStream(initialFile_ID);
        } catch (FileNotFoundException ignored) {
        }

        if (is != null) {
            JSONTokener tokenizer = new JSONTokener(is);
            database = new JSONObject(tokenizer);
        }
        else {
            database = new JSONObject();
        }
        if (is2 != null) {
            JSONTokener tokenizer = new JSONTokener(is2);
            idArray = new JSONObject(tokenizer);
        }
        else {
            idArray = new JSONObject();
        }
        AtomicReference<ShutdownHandler> shutdownListener = new AtomicReference<>(new ShutdownHandler(database, DB_File));
        AtomicReference<Boolean> Update = new AtomicReference<>(true);
        AtomicReference<Boolean> Update2 = new AtomicReference<>(true);
        //shutdownListener.run();
        Runtime.getRuntime().addShutdownHook(shutdownListener.get());

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            if (Update.get()) {
                shutdownListener.get().Database = database;
                shutdownListener.get().start();
                shutdownListener.set(new ShutdownHandler(database, DB_File));
                Update.set(false);
            }
        }, 0, 5, TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(() -> {
            if (Update2.get()) {
                ShutdownHandler saver = new ShutdownHandler(idArray, ID_File);
                saver.start();
                Update2.set(false);
            }
        }, 0, 5, TimeUnit.SECONDS);

        System.out.println("Shutdown Handler Initialized");
        staticFileLocation("/Webpage");


        Spark.get(//Returns JSON object
                "/",
                (request, response) -> database.toString(5));
        Spark.get(//Returns JSON object
                "/DB/",
                (request, response) -> database.toString(5));
        Spark.get(//Returns JSON object
                "/DB/:category/",
                (request, response) -> getCategory(database,request).toString(5));
        Spark.get(//Returns JSON object
                "/DB/:category/:object/",
                (request, response) -> getObject(database,request).toString(5));
        Spark.get(//Returns JSON object
                "/DB/:category/:object/:field/",
                (request, response) -> getField(database,request));

        Spark.get(//Returns JSON object
                "/Save/",
                (request, response) -> {
                    System.out.println(request.toString());
                    System.out.println(request.attributes().toString());
                    System.out.println(request.body());
                    System.out.println(request.params().toString());
                    shutdownListener.get().Database = database;
                    shutdownListener.get().start();
                    shutdownListener.set(new ShutdownHandler(database, "DB2.json"));
                    return database.toString();
                });

        Spark.put( //Replaces JSON object in a specific category
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    JSONObject newObject = new JSONObject(); // need to replace this with the object that gets past to the function.
                    Update.set(true);
                    return getObject(database,request).put(request.params().get(":field"), newObject);
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    database = newObject;
                    Update.set(true);
                    LogChange("Replace Database With:",newObject);
                    return database;
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    Update.set(true);
                    LogChange("Replace "+request.params().get(":category")+" With:",newObject);
                    return database.put(request.params().get(":category"), newObject);
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/:object/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    Update.set(true);
                    LogChange("Replace "+request.params().get(":category")+"/"+request.params().get(":object")+" With:",newObject);
                    return getCategory(database,request).put(request.params().get(":object"), newObject);
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    Update.set(true);
                    LogChange("Replace "+request.params().get(":category")+"/"+request.params().get(":object")+"/"+request.params().get(":field")+" With:",newObject);
                    return getObject(database,request).put(request.params().get(":field"), newObject);
                });
        Spark.delete( //Deletes an entire category
                "/DB/:category/",
                (request, response) -> {
                    LogChange("Remove "+request.params().get(":category"),null);
                    return database.remove(request.params().get(":category")).toString();
                });
        Spark.delete( //Deletes JSON object in a specific category
                "/DB/:category/:object/",
                (request, response) -> {
                    LogChange("Remove "+request.params().get(":category")+"/"+request.params().get(":object"),null);
                    return getCategory(database,request).remove(request.params(":object")).toString();

                });
        Spark.delete(//Delete a specific field
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    LogChange("Remove "+request.params().get(":category")+"/"+request.params().get(":object")+"/"+request.params().get(":field"),null);
                    return getObject(database,request).remove(request.params().get(":field")).toString();
                });


        Spark.post( //Adds a new JSON object to a specific category
                "/ID/:tag/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    Update2.set(true);
                    LogChange("ID Replace "+request.params().get(":tag")+" With:",newObject);
                    return idArray.put(request.params().get(":tag"), newObject);
                });
        Spark.get(//Returns JSON object
                "/ID/",
                (request, response) -> idArray.toString(5));
        Spark.get(//Returns JSON object
                "/ID/:tag/",
                (request, response) -> getTag(idArray,request).toString(5));

    }

    /**
     * Helper function used to check if a field exists and return it
     * @param DB database object
     * @param Input index of the requested object, e.g. "category1"
     * @return the requested Object, or null if the object doesn't exist
     */
    private static Object getHandler(JSONObject DB, String Input) {
        if (DB.has(Input)) {
            return DB.get(Input);
        } else if (Input.contains("_")){
            return DB.get(Input.replaceAll("_", " "));
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

    private static JSONObject getField(JSONObject DB, String category, String object, String field) {
        JSONObject OField = getObject(DB, category, object);
        if (OField == null) {
            return null;
        } else {
            return (JSONObject) getHandler(OField, field);
        }
    }

    private static JSONObject getField(JSONObject DB, Request request) {
        return getField(DB, request.params().get(":category"),request.params().get(":object"),request.params().get(":field"));
    }

    private static JSONObject getObject(JSONObject DB, Request request) {
        return getObject(DB, request.params().get(":category"),request.params().get(":object"));
    }

    private static JSONObject getCategory(JSONObject DB, Request request) {
        return getCategory(DB, request.params().get(":category"));
    }

    private static JSONObject getTag(JSONObject DB, Request request) {
        return getCategory(DB, request.params().get(":tag"));
    }

    private static void LogChange(String text, JSONObject Data) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String out = dtf.format(now);
        out += ":"+text;
        if (Data != null) {
            out += Data.toString();
        }
        try {
            FileWriter fw = new FileWriter("Log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(out);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
