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
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MyBluetoothDevice implements DefaultLifecycleObserver {

    private MainActivity context;
    private MainMenyFragment fragment;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private boolean advertising;
    private static final long SCAN_PERIOD = 10000;
    private static final long REST_PERIOD = 1000*60;
    private Timer timer;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(context, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
            Toast.makeText(context, "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
        }
        else{
            scheduleScan();
            scheduleAdd();
        }
    }
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
                                fragment.setText(mssg);
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
        },0,REST_PERIOD);

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
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();
        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( "CDB7950D-73F1-4D4D-8E47-C090502DBD63" ) );
        AdvertiseData data = new AdvertiseData.Builder()
//                .addServiceUuid(pUuid)
                .addServiceData( pUuid, "DataH".getBytes( Charset.forName( "UTF-8" ) ) )
                .build();
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
    ActivityResultLauncher<String[]> locationPermissionRequest;

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
            Handler handler=new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
            }, REST_PERIOD-10000);
            scanning = true;
            bluetoothLeScanner.startScan(filters,settings,leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }
    public MyBluetoothDevice(MainActivity context, MainMenyFragment fragment) {
        this.context = context;
        this.fragment=fragment;
        timer=new Timer();
        activityResultLauncher= context.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        System.out.println("Bluetooth activated");
                        scheduleScan();
                    }
                });
        locationPermissionRequest =
                context.registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
}
