package edu.rice.r360cmms;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;
import org.json.JSONObject;

public class Server {
    private static final String TAG = "CMMSServer";
    private static JSONObject database = new JSONObject();


    public static void main(String[] args) {
        staticFileLocation("/WebPublic");
        get(
                "/",
                (request, response) -> {
                    response.redirect("/week11images.html", 301); // you can find this file in /WebPublic
                    return "Next page!";
                });

    }
}
