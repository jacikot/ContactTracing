package rs.ac.bg.etf.contacttracing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;

import rs.ac.bg.etf.contacttracing.databinding.ActivityMainBinding;
import rs.ac.bg.etf.contacttracing.db.DailyKey;
import rs.ac.bg.etf.contacttracing.db.RPIKey;
import rs.ac.bg.etf.contacttracing.rest.CentralServiceApi;
import rs.ac.bg.etf.contacttracing.rest.DiagnosedPositive;
import rs.ac.bg.etf.contacttracing.rest.RegisteredInfectedKey;
import rs.ac.bg.etf.contacttracing.rest.RestService;

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
//        DailyKey daily=security.generateDailyKey(tracingKey);
//        Security.RollingProximityIdentifier rpi=security.generateRPI(daily.getDailyKey());
//        byte[] a=rpi.key.getEncoded();
//        byte[]t= new String(rpi.key.getEncoded(), StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1);
//        byte[] b=security.createRPIMSSG(rpi.rpi, new String(rpi.key.getEncoded(), StandardCharsets.ISO_8859_1));
//        ArrayList<DailyKey> keys=new ArrayList<>();
//        keys.add(daily);
//        new RestService().registerInfected(this,keys);
//        SystemClock.sleep(2000);
//        new RestService().getInfected(this,1000*60*60*24).observe(this,list->{
//            list.forEach(e->{
//                security.validateRPI(new RPIKey(new String(new byte[]{b[0],b[1],b[2],b[3],b[4],b[5],b[6],b[7]},StandardCharsets.ISO_8859_1),new String(new byte[]{b[8],b[9],b[10],b[11],b[12]},StandardCharsets.ISO_8859_1),null), e);
//            });
//        });


//        RegisteredInfectedKey rik=new RegisteredInfectedKey();
//        rik.dailyKey=daily.getDailyKey();
//        security.validateRPI(new RPIKey(new String(new byte[]{b[0],b[1],b[2],b[3],b[4],b[5],b[6],b[7]},StandardCharsets.ISO_8859_1),new String(new byte[]{b[8],b[9],b[10],b[11],b[12]},StandardCharsets.ISO_8859_1),null), rik);
//

//        RestService service=new RestService();
//        service.getInfected(this,1000*60*60*24).observe(this,list->{
//            byte []b2=list.get(0).dailyKey.getBytes(StandardCharsets.ISO_8859_1);
//            Log.d("jana","x");
//            byte []b="jana".getBytes(StandardCharsets.ISO_8859_1);
//            ArrayList<DailyKey>keys=new ArrayList<>();
//            DailyKey d=new DailyKey("jana",new Date());
//            keys.add(d);
//            service.registerInfected(this,keys);
//        });

        if(tracingKey==null){
            //ovde treba ciljati na server i dohvatiti key koji je unique
            tracingKey=security.generateTracingKey();
            sp.edit().putString("KEY",tracingKey).apply();
        }

        //Toast.makeText(this, tracingKey, Toast.LENGTH_SHORT).show();
        amb=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(amb.getRoot());
    }
}