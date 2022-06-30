package rs.ac.bg.etf.contacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

import rs.ac.bg.etf.contacttracing.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding amb;
    public static final String INTENT_ACTION_NOTIFICATION = "NOTIFICATION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        MyBluetoothDevice.permissionRequest(this);
        MyBluetoothDevice.enableBluetooth(this);
        Security security=new Security();

        SharedPreferences sp=getSharedPreferences(MyKeyGenerator.shared_NAME, Context.MODE_PRIVATE);
        String tracingKey=sp.getString("KEY",null);
        if(tracingKey==null){
            //ovde treba ciljati na server i dohvatiti key koji je unique
            tracingKey=security.generateTracingKey();
            sp.edit().putString("KEY",tracingKey).apply();
        }

        //Toast.makeText(this, tracingKey, Toast.LENGTH_SHORT).show();
        amb=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(amb.getRoot());

        String s=security.generateTracingKey();
        try {
            String ss=security.generateDailyKey(s).getDailyKey();
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