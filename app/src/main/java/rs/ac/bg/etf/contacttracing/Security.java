package rs.ac.bg.etf.contacttracing;

import android.util.Log;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import rs.ac.bg.etf.contacttracing.db.DailyKey;
import rs.ac.bg.etf.contacttracing.db.RPIKey;
import rs.ac.bg.etf.contacttracing.rest.RegisteredInfectedKey;

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
            byte[] bytes = (UUID).getBytes(StandardCharsets.ISO_8859_1);
            byte[] macResult = mac.doFinal(bytes);
            Log.d("length",""+macResult.length);
            return new DailyKey(new String(macResult, StandardCharsets.ISO_8859_1),date);
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
            byte[] bytes = dailyKey.getBytes(StandardCharsets.ISO_8859_1);
            byte[] macResult = mac.doFinal(bytes);
            return new RollingProximityIdentifier(new String(macResult, StandardCharsets.ISO_8859_1),d, key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] createRPIMSSG(String rpi,String rpikey){
        byte[] decodedKey= rpikey.getBytes(StandardCharsets.ISO_8859_1);
        byte[] MAC=rpi.getBytes(StandardCharsets.ISO_8859_1);
        byte[]output=new byte[13]; //maksimum koji moze da prihvati
        for(int i=0;i<8;i++){
            output[i]=decodedKey[i];
            if(i<5)output[i+8]=MAC[i];
        }
        return output;
    }

    public boolean validateRPI(RPIKey key, RegisteredInfectedKey dailyKey){
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            byte[] decoded=key.getKey().getBytes(StandardCharsets.ISO_8859_1);
            byte[]b=new SecretKeySpec(decoded,0,decoded.length,"DES").getEncoded();
            mac.init(new SecretKeySpec(decoded,0,decoded.length,"DES"));
            Date d=key.getDate();
            byte[] bytes = dailyKey.dailyKey.getBytes(StandardCharsets.ISO_8859_1);
            byte[] macResult = mac.doFinal(bytes);
            byte[] macCheck=key.getMac().getBytes(StandardCharsets.ISO_8859_1);
            for(int i=0;i<5;i++){
                if(macCheck[i]!=macResult[i]) break;
                if(i==4) {
                    return true;
                }
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;

    }
}
