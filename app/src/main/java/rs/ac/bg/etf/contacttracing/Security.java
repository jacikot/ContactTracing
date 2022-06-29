package rs.ac.bg.etf.contacttracing;

import android.util.Log;

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

import rs.ac.bg.etf.contacttracing.db.DailyKey;

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

    public DailyKey generateDailyKey(String UUID)  {
        Key key = keyGen.generateKey();
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            Date date=new Date();
            byte[] bytes = (UUID).getBytes();
            byte[] macResult = mac.doFinal(bytes);
            Log.d("length",""+macResult.length);
            return new DailyKey(new String(macResult),date);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
       return null;
    }

    public RollingProximityIdentifier generateRPI(String dailyKey)  {
        Key key = keyGen.generateKey();
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            Date d=new Date();
            byte[] bytes = dailyKey.getBytes();
            byte[] macResult = mac.doFinal(bytes);
            return new RollingProximityIdentifier(new String(macResult),d, key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
