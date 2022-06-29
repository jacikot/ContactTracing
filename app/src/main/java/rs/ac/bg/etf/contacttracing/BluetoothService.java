package rs.ac.bg.etf.contacttracing;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;

public class BluetoothService extends LifecycleService {
    private static final String NOTIFICATION_CHANNEL_ID = "workout-notification-channel";
    private static final int NOTIFICATION_ID = 1;
    private MyBluetoothDevice device;
    public BluetoothService() {
        device=new MyBluetoothDevice(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("lifecycle-aware","Service->onCreate");
        getLifecycle().addObserver(device);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("lifecycle-aware", "Service->onStartCommand");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification()); //pokreni foreground service
        device.start();
        return START_STICKY;

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