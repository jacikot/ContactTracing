package rs.ac.bg.etf.contacttracing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;


import rs.ac.bg.etf.contacttracing.db.ContactTracingDatabase;
import rs.ac.bg.etf.contacttracing.db.DailyKey;

public class MyKeyGenerator implements DefaultLifecycleObserver {
    private Timer timerDaily;
    private Timer timerRPI;
    private BluetoothService service;
    public static final String shared_NAME="TracingKey";
    public static final String RPI_NAME="RPI";
    public static final String RPI_KEY="RPI_key";
    Security security=new Security();
    private static final long daily=1000*60*60; //promeni ovo na 24h
    private static final long rpi=1000*60*15;
    private boolean started=false;
    private SharedPreferences sp;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        timerRPI=new Timer();
        timerDaily=new Timer();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        timerDaily.cancel();
        timerRPI.cancel();
    }

    private String getTracingKey(){
        sp=service.getSharedPreferences(shared_NAME, Context.MODE_PRIVATE);
        String tracingKey=sp.getString("KEY",null);
        if(tracingKey==null){
            //ovde treba ciljati na server i dohvatiti key koji je unique
            tracingKey=security.generateTracingKey();
            sp.edit().putString("KEY",tracingKey).apply();
        }
        return tracingKey;
    }
    public void start(BluetoothService service){
        if(started) return;
        started=true;
        this.service=service;
        String tracingKey=getTracingKey();
        timerDaily.schedule(new TimerTask() {
            @Override
            public void run() {
                ContactTracingDatabase.getInstance(service).getDao().insert(security.generateDailyKey(tracingKey));
            }
        },0,daily);
        timerRPI.schedule(new TimerTask() {
            @Override
            public void run() {
                Handler h= new Handler(Looper.getMainLooper());
                h.post(()->{
                    ContactTracingDatabase.getInstance(service).getDao().getLatest().observe(service,dailyKey -> {
                        Security.RollingProximityIdentifier rpi=security.generateRPI(dailyKey.getDailyKey());
                        sp.edit().putString(RPI_NAME,rpi.rpi)
                                .putString(RPI_KEY, Base64.getEncoder().encodeToString(rpi.key.getEncoded()))
                                .apply();
                    });
                });

            }
        },1000*10,rpi);
    }
}
