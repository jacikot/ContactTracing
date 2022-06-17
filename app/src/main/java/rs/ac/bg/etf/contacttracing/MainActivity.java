package rs.ac.bg.etf.contacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

import rs.ac.bg.etf.contacttracing.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding amb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        amb=ActivityMainBinding.inflate(getLayoutInflater());
        Security security=new Security();
        String s=security.generateTracingKey();
        try {
            String ss=security.generateDailyKey(s);
            Security.RollingProximityIdentifier id=security.generateRPI(ss);
            System.out.println(s);
            System.out.println(ss);
            System.out.println(id.rpi);
            System.out.println(id.key);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(id.key);
            System.out.println(new String(mac.doFinal((ss+id.date.getTime()).getBytes(StandardCharsets.UTF_8))).equals(id.rpi) );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}