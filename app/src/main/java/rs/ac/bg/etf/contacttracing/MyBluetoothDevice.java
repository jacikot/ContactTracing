package rs.ac.bg.etf.contacttracing;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import rs.ac.bg.etf.contacttracing.db.ContactTracingDatabase;
import rs.ac.bg.etf.contacttracing.db.RPIKey;


public class MyBluetoothDevice implements DefaultLifecycleObserver {

    private BluetoothService context;

//    private ActivityResultLauncher<Intent> activityResultLauncher;
    private BluetoothLeScanner bluetoothLeScanner;
    private static BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeAdvertiser advertiser;
    private boolean scanning;
    private boolean advertising;
    private static boolean bluetoothActivated;
    private boolean started=false;
    private static final long SCAN_PERIOD = 10000;
    private static final long REST_PERIOD = 1000*60;
    private static final String APP_UUID="CDB7950D-73F1-4D4D-8E47-C090502DBD63";
    private Timer timer;

    public void start(){
        if(!bluetoothActivated)
            Log.d("bluetooth:","Bluetooth not activated");
        else{
            if(!started){
                scheduleScan();
                scheduleAdd();
                started=true;
            }
        }
    }

    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Toast.makeText(context, (errorCode==AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE)+"", Toast.LENGTH_SHORT).show();

        }
    };
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result.getDevice()!=null){
                        try{
                            byte[]res=result.getScanRecord().getServiceData(new ParcelUuid(UUID.fromString(APP_UUID)));
                            String key=new String(Arrays.copyOfRange(res,0,8),StandardCharsets.ISO_8859_1);
                            String mac=new String(Arrays.copyOfRange(res,8,13),StandardCharsets.ISO_8859_1);
                            RPIKey rpikey=new RPIKey(key,mac,new Date());
                            Log.d("lifecycle-aware", Arrays.toString(res) +"");
                            ContactTracingDatabase.getInstance(context).getRPIDao().getExisting(key,mac,new Date(new Date().getTime()-1000*60*60*24*5)).observe(context,(rpik)->{
                                if(rpik==null){
                                    Toast.makeText(context, new String(res,StandardCharsets.ISO_8859_1), Toast.LENGTH_SHORT).show();
                                    ContactTracingDatabase.getInstance(context).getRPIDao().insert(rpikey);
                                }
                            });
                        }
                        catch (Exception e){ }
                    }


                }
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Toast.makeText(context, "Discovery onScanFailed: "+errorCode, Toast.LENGTH_SHORT).show();
                    super.onScanFailed(errorCode);
                }

            };

    private void scheduleScan(){
//        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scan();
            }
        },0,1000*5);

    }

    private void scheduleAdd(){
//        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                add();
            }
        },0,REST_PERIOD);

    }

    private byte[] getRPI(){
        SharedPreferences sp=context.getSharedPreferences(MyKeyGenerator.shared_NAME, Context.MODE_PRIVATE);
        String rpi=sp.getString(MyKeyGenerator.RPI_NAME,null);
        if(rpi==null) return new byte[]{};
        return new Security().createRPIMSSG(rpi,sp.getString(MyKeyGenerator.RPI_KEY,null));
    }

    private void add(){
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();
        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( APP_UUID ) );
        byte[] rpi=getRPI();
        AdvertiseData data = new AdvertiseData.Builder()
//                .addServiceUuid(pUuid)
                .addServiceData( pUuid, rpi )
                .build();
        if(!advertising){
            Handler handler=new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                advertising = false;
                advertiser.stopAdvertising(advertisingCallback);
            }, REST_PERIOD-10000);
            advertising = true;
            advertiser.startAdvertising( settings, data, advertisingCallback );
        }

    }

    private void scan(){
        bluetoothLeScanner= bluetoothAdapter.getBluetoothLeScanner();
        ScanFilter filter = new ScanFilter.Builder()
//                .setServiceUuid(new ParcelUuid(UUID.fromString( APP_UUID )),ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")).build();
                .setServiceData(new ParcelUuid(UUID.fromString( APP_UUID )),new byte[13],new byte[13]).build();
//                ;
        List<ScanFilter> filters=new ArrayList<>();
        filters.add( filter );
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
        if(!scanning){
            scanning = true;
            bluetoothLeScanner.startScan(filters,settings,leScanCallback);
        } else {
//            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            bluetoothLeScanner.startScan(filters,settings,leScanCallback);
        }
    }
    public MyBluetoothDevice(BluetoothService context) {
        this.context = context;
        timer=new Timer();
    }

    public static void permissionRequest(MainActivity activity){
        ActivityResultLauncher<String[]> locationPermissionRequest =
                activity.registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
//                            Boolean LocationGranted = result.getOrDefault(
//                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,false);
//                            if(!LocationGranted) System.out.println("bg location failed");
                            requestBackgroundLocationPermission(activity);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                System.out.println("fine location ok");
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                System.out.println("corse location ok");
                            } else {
                                System.err.println("no access");
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private static void requestBackgroundLocationPermission(MainActivity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                66
        );
    }

    public static void enableBluetooth(MainActivity activity){
        ActivityResultLauncher<Intent> activityResultLauncher= activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        System.out.println("Bluetooth activated");
//                        scheduleScan();
                        bluetoothActivated=true;
                    }
                });
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(activity, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
        }
        else {
            bluetoothActivated=true;
        }
    }
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        bluetoothLeScanner.stopScan(leScanCallback);
        advertiser.stopAdvertising(advertisingCallback);
        timer.cancel();
    }
}
