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
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;


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
    private ArrayList<String> lemssgs=new ArrayList<>();
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result.getDevice()!=null){
                        String mssg=null;
                        try{
                            mssg=new String(result.getScanRecord().getServiceData(new ParcelUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"))));
//                            Toast.makeText(context, mssg, Toast.LENGTH_SHORT).show();
                            if(!lemssgs.contains(mssg)) {
                                lemssgs.add(mssg);
//                                fragment.setText(mssg);
                                Toast.makeText(context, mssg, Toast.LENGTH_SHORT).show();
                            }
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

    private void add(){
        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();
        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( "CDB7950D-73F1-4D4D-8E47-C090502DBD63" ) );
        AdvertiseData data = new AdvertiseData.Builder()
//                .addServiceUuid(pUuid)
                .addServiceData( pUuid, "DataJA".getBytes( Charset.forName( "UTF-8" ) ) )
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
                .setServiceData(new ParcelUuid(UUID.fromString( "CDB7950D-73F1-4D4D-8E47-C090502DBD63" )),new byte[]{0},new byte[]{0})
                .build();
        List<ScanFilter> filters=new ArrayList<>();
        filters.add( filter );
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
        if(!scanning){
//            Handler handler=new Handler(Looper.getMainLooper());
//            handler.postDelayed(() -> {
//                scanning = false;
//                bluetoothLeScanner.stopScan(leScanCallback);
//            }, REST_PERIOD-10000);
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
                Manifest.permission.ACCESS_COARSE_LOCATION,
        });
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
