package edu.rice.r360cmms;


import net.glxn.qrgen.QRCode;
import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Request;
import spark.Spark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static spark.Spark.staticFileLocation;

public class Server {
    private static JSONObject database = new JSONObject();
    private static JSONObject idArray = new JSONObject();
    private static ReentrantLock countLock = new ReentrantLock();
    private static int count = 0;
    private static String[] keys = new String[0];
    public static void main(String[] args) {
        String DB_File = "DB.json";
        String ID_File = "ID.json";
        String values_File = "values.val";
        File initialFile_DB = new File(DB_File);
        File initialFile_ID = new File(ID_File);
        File valuesFile = new File(values_File);

        try {
            FileInputStream fi = new FileInputStream(valuesFile);
            ObjectInputStream oi = new ObjectInputStream(fi);
            count =(int) oi.readObject();
            keys =(String[]) oi.readObject();
            oi.close();
            fi.close();
        } catch (FileNotFoundException e) {
            System.out.println("Did not find a a value file to import");
            count = 0;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        AtomicReference<Boolean> Update3 = new AtomicReference<>(true);
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
        exec.scheduleAtFixedRate(() -> {
            if (Update3.get()) {
                try {
                    FileOutputStream f = new FileOutputStream(valuesFile);
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    countLock.lock();
                    try {
                        o.writeObject(count);
                        o.writeObject(keys);
                    } finally {
                        countLock.unlock();
                    }
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Update3.set(false);
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
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    database = newObject;
                    JSONObject data = (JSONObject) newObject.get("Data");
                    if (checkAuthKey(newObject.get("Key").toString())) {
                        Update.set(true);
                        LogChange("Replace Database With:",newObject);
                        return database;
                    }
                    return "Bad auth Key";
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    JSONObject data = (JSONObject) newObject.get("Data");
                    if (checkAuthKey(newObject.get("Key").toString())) {
                        Update.set(true);
                        LogChange("Replace "+request.params().get(":category")+" With:",newObject);
                        return database.put(request.params().get(":category"), newObject);
                    }
                    return "Bad auth Key";
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/:object/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    JSONObject data = (JSONObject) newObject.get("Data");
                    if (checkAuthKey(newObject.get("Key").toString())) {
                        Update.set(true);
                        LogChange("Replace "+request.params().get(":category")+"/"+request.params().get(":object")+" With:",data);
                        return getCategory(database,request).put(request.params().get(":object"), data);
                    }
                    return "Bad auth Key";
                });
        Spark.post( //Adds a new JSON object to a specific category
                "/DB/:category/:object/:field/",
                (request, response) -> {
                    System.out.println("Post:"+request.body());
                    JSONTokener tokenizer = new JSONTokener(request.body());
                    JSONObject newObject = new JSONObject(tokenizer);
                    JSONObject data = (JSONObject) newObject.get("Data");
                    if (checkAuthKey(newObject.get("Key").toString())) {
                        Update.set(true);
                        LogChange("Replace "+request.params().get(":category")+"/"+request.params().get(":object")+"/"+request.params().get(":field")+" With:",data);
                        return getObject(database,request).put(request.params().get(":field"), data);
                    }
                    return "Bad auth Key";
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
        Spark.get(//Returns JSON object
                "/QR/:string/",
                (request, response) -> {
                    BufferedImage image = generateQRCodeImage(request.params().get(":string"));
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR1/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR2/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR3/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR4/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR5/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR6/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR7/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
                    });
        Spark.get(//Returns JSON object
                "/QR8/",
                (request, response) -> {
                    BufferedImage image = getQRCodeNext(Update3);
                    response.type("image/png");
                    return imageToPng(image);
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

    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        int MaxLength = 2953; //This is the max size the qr code can reach.
        System.out.println(barcodeText.length());
        String textTrimmed = barcodeText.substring(0, Math.min(MaxLength, barcodeText.length()));
        int size = 500;
        ByteArrayOutputStream stream = QRCode
                .from(textTrimmed)
                .withSize(size, size)
                .stream();
        ByteArrayInputStream bis = new ByteArrayInputStream(stream.toByteArray());
        BufferedImage image = ImageIO.read(bis);
        Font font = new Font("Arial", Font.BOLD, 18);

        BufferedImage imageout = new BufferedImage(image.getWidth(),image.getHeight()+40,BufferedImage.TYPE_INT_ARGB);
        Graphics g = imageout.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,imageout.getWidth(),imageout.getHeight());
        g.setFont(font);
        g.setColor(Color.BLACK);
        FontMetrics metrics = g.getFontMetrics(font);
        String hospitalName = "Testing Hospital";
        int positionXCode = (imageout.getWidth() - metrics.stringWidth(barcodeText)) / 2;
        int positionXName = (imageout.getWidth() - metrics.stringWidth(hospitalName)) / 2;
        if (positionXCode < 0) {
            positionXCode = 0;
        }
        if (positionXName < 0) {
            positionXName = 0;
        }
        int offsetTop = 15;
        int offsetBottom = 5;
        g.drawImage(image,0,20,null);
        g.drawString(barcodeText, positionXCode, imageout.getHeight() - offsetBottom);

        g.drawString(hospitalName, positionXName, offsetTop);
        return imageout;
    }

    private static BufferedImage getQRCodeNext(AtomicReference<Boolean> Update3) throws Exception {
        countLock.lock();
        BufferedImage image = null;
        try {
            image = generateQRCodeImage("" + count);
            count += 1;
        } finally {
            countLock.unlock();
        }
        Update3.set(true);
        return image;
    }

    private static boolean checkAuthKey(String key){
        for (int x = 0; x <= keys.length; x++) {
            if (Objects.equals(keys[x], key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a BufferedImage, convert it to PNG format.
     *
     * @return the raw PNG bytes
     */
    public static byte[] imageToPng(BufferedImage image) throws IOException {
        var os = new ByteArrayOutputStream();
        var success = ImageIO.write(image, "png", os);
        if (!success) {
            throw new RuntimeException(
                    "ImageIO internal failure"); // useless feedback, but it's all we have
        } else {
            return os.toByteArray();
        }
    }
}
