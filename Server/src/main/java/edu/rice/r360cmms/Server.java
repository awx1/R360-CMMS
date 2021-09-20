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
