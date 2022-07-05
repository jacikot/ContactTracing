package rs.ac.bg.etf.contacttracing;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;

import rs.ac.bg.etf.contacttracing.db.ContactTracingDatabase;
import rs.ac.bg.etf.contacttracing.db.DailyKey;
import rs.ac.bg.etf.contacttracing.db.RPIKey;
import rs.ac.bg.etf.contacttracing.rest.RegisteredInfectedKey;
import rs.ac.bg.etf.contacttracing.rest.RestService;

public class BluetoothService extends LifecycleService {
    private static final String NOTIFICATION_CHANNEL_ID = "workout-notification-channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long period=1000*60*60*24*5;
    private MyBluetoothDevice device;
    private MyKeyGenerator keyGenerator;
    public BluetoothService() {
        device=new MyBluetoothDevice(this);
        keyGenerator=new MyKeyGenerator();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("lifecycle-aware","Service->onCreate");
        getLifecycle().addObserver(device);
        getLifecycle().addObserver(keyGenerator);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("lifecycle-aware", "Service->onStartCommand");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification()); //pokreni foreground service
        switch (intent.getAction()){
            case "START":
                device.start();
                keyGenerator.start(this);
                break;
            case "REGISTER":
                ContactTracingDatabase.getInstance(this).getDao().getLastNDays(5).observe(this,list->{
                    new RestService().registerInfected(this, list);
                });
                break;
            case "GET":
                new RestService().getInfected(this,period).observe(this,list->{
                    checkExposure(list).observe(this,e->{
                        Toast.makeText(this, "EXPOSED", Toast.LENGTH_SHORT).show();
                    });
                    //promeni ovo

                });
                break;

        }

        return START_STICKY;

    }

    private LiveData<Boolean> checkExposure(List<RegisteredInfectedKey> list){
        MutableLiveData<Boolean> b=new MutableLiveData<>();
        list.forEach(infected->{
            ContactTracingDatabase.getInstance(this).getRPIDao().getAllBetween(new Date(infected.date), new Date(infected.date+1000*60*60*24)).observe(this, rpis->{
                for (RPIKey rpi:rpis){
                    if(new Security().validateRPI(rpi,infected) && b.getValue()==null) b.postValue(true);
                }
            });
        });
        return b;
    }

    private Notification getNotification() {

        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setAction(MainActivity.INTENT_ACTION_NOTIFICATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // pravimo novu aktivnost ali se stara brise

        //omotac oko intenta koji nosi informaciju koju akciju treba izvrsiti nad intentu
        PendingIntent pendingIntent = PendingIntent
                //pravi se aktivnost sa intentom
                .getActivity(this, 0, intent, 0);

        //notifikacioni kanal se prosledjuje


        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_connect_without_contact_24)
                .setContentTitle("ContactTracing")
                .setContentText("ContactTracing")
                //content intent - za notifikaciju definise se ono sto se izvrsava pri kliku na aplikaciju
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.teal_200))
                .build();
    }

    private void createNotificationChannel() {
        //notification importance definise kako ce notifikacija iskociti
        //ovaj compat radi proveru kompatibilnosti verzija -> treba nam zavisnost od androidx.core
        NotificationChannelCompat notificationChannel = new NotificationChannelCompat
                .Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("ContactTracing")
                .build();
        NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel);
    }
}