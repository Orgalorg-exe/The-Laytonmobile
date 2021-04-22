package Pinguino.thelaytonmobile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class VinculatedDevices extends AppCompatActivity {

    //Depuración de LOGCAT
    private static final String TAG = "VinculatedDevices";
    //Declaración de ListView
    ListView listView_DevicesList;
    //String que se enviará a la actividad principal
    public static String EXTRA__DEVICE_ADDRESS = "device_address";

    //Declaración de campos
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vinculated_devices);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    public void onResume(){
        super.onResume();

        verificarEstadoBT();
        //Inicia el array que contendra la lista de los dispositivos bluetooth vinculados
        arrayAdapter = new ArrayAdapter(this, R.layout.found_devices);
        //Presenta los dispositivos vinculados en el ListView
        listView_DevicesList = (ListView)findViewById(R.id.ListView_DevicesList);
        listView_DevicesList.setAdapter(arrayAdapter);
        listView_DevicesList.setOnItemClickListener(mDeviceClickListener);
        //Obtiene el adaptador local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        //Adiciona los dispositivos emparejados al array
        if(pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices){
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    // Configura un (on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            // Obtener la dirección MAC del dispositivo, que son los últimos 17 caracteres en la vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            finishAffinity();

            // Realiza un intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
            Intent intend = new Intent(VinculatedDevices.this, MainActivity.class);
            intend.putExtra(EXTRA__DEVICE_ADDRESS, address);
            startActivity(intend);
        }
    };

    private void verificarEstadoBT(){
        //Comprueba que el dispositivo tiene Bluetooth y que está encendido
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Blueatooth", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Solicita al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}