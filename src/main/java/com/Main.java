package com;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Main {

    public static String AbsPath;
    public static Properties sensorProp;

    public static int max;
    public static int min;
    public static String uid;
    public static int send_interval;
    public static String security_key;

    public static void main(String[] args) {
        try {
            //Socket socket = new Socket("localhost", 3777);

            String path = Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");

            //System.out.println("decodedPath : " + decodedPath);

            AbsPath = decodedPath
                    .replace("sensorEmu-1.0.jar", "").replace("classes/","");

            sensorProp = new Properties();


            System.out.println("application location : " + AbsPath);
            FileInputStream input = new FileInputStream(AbsPath + "sensorConfig.properties");

            sensorProp.load(input);

            max = Integer.parseInt(Main.sensorProp.getProperty("MAX_VALUE"));
            min = Integer.parseInt(Main.sensorProp.getProperty("MIN_VALUE"));
            uid = Main.sensorProp.getProperty("UID");
            send_interval = Integer.parseInt(Main.sensorProp.getProperty("SEND_INTERVAL"));
            security_key = Main.sensorProp.getProperty("SECURITY_KEY");

            while (true) {

                Thread.currentThread().sleep(send_interval*1000);

                try {
                    Socket socket = new Socket("localhost", 3777);

                    if (!security_key.equals("")) {
                        socket.getOutputStream().write(generateMessage().getBytes());
                    } else {
                        socket.getOutputStream().write(uid.getBytes());
                    }
                    // читаем ответ
                    byte buf[] = new byte[256 * 1024];
                    int r = socket.getInputStream().read(buf);
                    String data = new String(buf, 0, r);

                    if (data.contains("key")){
                        security_key = data.replace("key : ","");
                        Main.sensorProp.setProperty("SECURITY_KEY",security_key);
                        FileOutputStream output = new FileOutputStream(Main.AbsPath + "sensorConfig.properties");
                        Main.sensorProp.store(output,"sensor linked");
                    }
                    socket.close();
                    socket = null;
                    System.gc();

                    // выводим ответ в консоль
                    System.out.println(data);
                } catch (Exception e1){
                    System.out.println("Bridge is not available");
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateMessage(){
        //UID:TIME(unix):VALUES:STATE
        //SNS-123123:42141231341:34.3:5
        String message = null;
        try {

            int rval = new Random().nextInt(max - min + 1) + min;
            String unixTime = String.valueOf(new Long(new Date().getTime() / 1000));

            message =  uid + ":" + unixTime + ":" + rval + ":" + "7";
            System.out.println("message sent: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}
