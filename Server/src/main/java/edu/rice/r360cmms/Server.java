package edu.rice.r360cmms;


import static spark.Spark.staticFileLocation;
import org.json.JSONObject;
import spark.Spark;

public class Server {
    private static final String TAG = "CMMSServer";
    private static JSONObject database = new JSONObject();


    public static void main(String[] args) {
        staticFileLocation("/WebPublic");
        Spark.get(//Returns JSON object
            "/",
            (request, response) -> {
                //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                return database.toString();
            });
        Spark.put( //Replaces item in JSON object
            "/",
            (request, response) -> {
                //response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                return database.toString();
            });
        Spark.post( //adds new item to JSON object
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
