package rs.ac.bg.etf.contacttracing;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import java.util.ArrayList;
import java.util.List;


public class MyBluetoothDevice implements DefaultLifecycleObserver {

    private MainActivity context;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
        }
        else{
            scan();
        }
    }
    private ArrayList<BluetoothDevice> leDevices;
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    leDevices.add(result.getDevice());
                }
            };

    private void scan(){
        bluetoothLeScanner= bluetoothAdapter.getBluetoothLeScanner();
        if(!scanning){
            Handler handler=new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);
            }, SCAN_PERIOD);
        }

    }
    public MyBluetoothDevice(MainActivity context) {
        this.context = context;
        activityResultLauncher= context.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        System.out.println("Bluetooth activated");
                        scan();
                    }
                });
    }
}
