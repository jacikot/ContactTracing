package rs.ac.bg.etf.contacttracing;

import android.bluetooth.BluetoothAdapter;
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

public class MainMenyFragment extends Fragment {
    private FragmentMainMenyBinding amb;
    private MainActivity activity;
    private NavController controller;
    private MyBluetoothDevice device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity= (MainActivity) requireActivity();
    }

    public void setText(String s){
        amb.text.setText(s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        amb=FragmentMainMenyBinding.inflate(inflater,container,false);
        device=new MyBluetoothDevice(activity, this);
        getViewLifecycleOwner().getLifecycle().addObserver(device); //ne treba da bude povezano sa fragentom nego sa servisom sredi!!!
        return amb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller= Navigation.findNavController(view);
    }
}