package rs.ac.bg.etf.contacttracing;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

public class Security {
    public class RollingProximityIdentifier implements Serializable {
        String rpi;
        Date date;
        Key key;

        public RollingProximityIdentifier(String rpi, Date date, Key key) {
            this.rpi = rpi;
            this.date = date;
            this.key=key;
        }
    }
    private KeyGenerator keyGen;
    public Security(){
        try {
            keyGen = KeyGenerator.getInstance("DES");
            SecureRandom secRandom = new SecureRandom();
            keyGen.init(secRandom);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String generateTracingKey(){
        Random generator=new Random();
        int key=generator.nextInt();
        return Integer.toUnsignedString(key);
        //ovo ce se obavljati na serveru
    }

    public String generateDailyKey(String UUID) throws NoSuchAlgorithmException, InvalidKeyException {
        Key key = keyGen.generateKey();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        long day= new Date().getTime()/(1000*60*60*24);
        byte[] bytes = (UUID+day).getBytes();
        byte[] macResult = mac.doFinal(bytes);
        return new String(macResult);
    }

    public RollingProximityIdentifier generateRPI(String dailyKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Key key = keyGen.generateKey();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        Date d=new Date();
        long time= d.getTime();
        byte[] bytes = (dailyKey+time).getBytes();
        byte[] macResult = mac.doFinal(bytes);
        return new RollingProximityIdentifier(new String(macResult),d, key);
    }
}
