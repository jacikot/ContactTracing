package rs.ac.bg.etf.contacttracing;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.bg.etf.contacttracing.databinding.FragmentMainMenyBinding;
import rs.ac.bg.etf.contacttracing.db.ContactTracingDatabase;
import rs.ac.bg.etf.contacttracing.rest.RestService;

public class MainMenyFragment extends Fragment {
    private FragmentMainMenyBinding amb;
    private MainActivity activity;
    private NavController controller;
    private MyBluetoothDevice device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity= (MainActivity) requireActivity();
        Intent intent = new Intent();
        intent.setClass(activity, BluetoothService.class);
        //postavljanje akcije
        intent.setAction("START");
        activity.startService(intent);
    }

    public void setText(String s){
//        amb.text.setText(s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        amb=FragmentMainMenyBinding.inflate(inflater,container,false);
        amb.alert.setOnClickListener(e->{

            Intent intent = new Intent();
            intent.setClass(activity, BluetoothService.class);
            //postavljanje akcije
            intent.setAction("REGISTER");
            activity.startService(intent);
//            ContactTracingDatabase.getInstance()
//            new RestService().registerInfected(activity);
        });

        amb.check.setOnClickListener(e->{
            Intent intent = new Intent();
            intent.setClass(activity, BluetoothService.class);
            //postavljanje akcije
            intent.setAction("GET");
            activity.startService(intent);
        });
//        device=new MyBluetoothDevice(activity, this);
//        getViewLifecycleOwner().getLifecycle().addObserver(device); //ne treba da bude povezano sa fragentom nego sa servisom sredi!!!
        return amb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller= Navigation.findNavController(view);
    }
}