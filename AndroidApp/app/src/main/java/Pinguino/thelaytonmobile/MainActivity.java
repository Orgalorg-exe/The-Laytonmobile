package Pinguino.thelaytonmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //---------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //---------------------------------------

    private TextView txtVel, txtMsgFromBT;
    private SeekBar seekBar;
    private ImageButton ImgBtn_Arriba, ImgBtn_Abajo, ImgBtn_Izq, ImgBtn_Der, ImgBtn_PlayPause;
    private Button Btn_Luces;

    //################################################################################################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        //---------------------------------------
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    char MyCaracter = (char) msg.obj;

                    if(MyCaracter == 'u'){
                        txtMsgFromBT.setText("ADELANTE");
                    }
                    if(MyCaracter == 'd'){
                        txtMsgFromBT.setText("ATRÁS");
                    }
                    if(MyCaracter == 'l'){
                        txtMsgFromBT.setText("GIRO IZQUIERDA");
                    }
                    if(MyCaracter == 'r'){
                        txtMsgFromBT.setText("GIRO DERECHA");
                    }
                    if(MyCaracter == 'P'){
                        txtMsgFromBT.setText("PLAY");
                    }
                    if(MyCaracter == 'p'){
                        txtMsgFromBT.setText("PAUSE");
                    }
                    if(MyCaracter == 'O'){
                        txtMsgFromBT.setText("LUCES ON");
                    }
                    if(MyCaracter == 'o'){
                        txtMsgFromBT.setText("LUCES OFF");
                    }
                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();
        //---------------------------------------

        txtMsgFromBT = (TextView)findViewById(R.id.txtMsgFromBT);

        ImgBtn_Abajo = (ImageButton)findViewById(R.id.ImgButton_GoBack);
        ImgBtn_Arriba = (ImageButton)findViewById(R.id.ImgButton_GoFront);
        ImgBtn_Izq = (ImageButton)findViewById(R.id.ImgButton_TurnLeft);
        ImgBtn_Der = (ImageButton)findViewById(R.id.ImgButton_TurnRigth);
        ImgBtn_PlayPause = (ImageButton)findViewById(R.id.ImgButton_PlayPause);

        Btn_Luces = (Button)findViewById(R.id.Button_Lights);

        txtVel = (TextView)findViewById(R.id.txtVel);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        ImgBtn_Arriba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {MyConexionBT.write("U."); // Up
            }
        });

        ImgBtn_Abajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {MyConexionBT.write("D."); //Down
            }
        });

        ImgBtn_Izq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("L."); //Left
            }
        });

        ImgBtn_Der.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("R."); //Right
            }
        });

        ImgBtn_PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { MyConexionBT.write("P."); //Play-Pause
            }
        });

        Btn_Luces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("O."); //On-Off
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int vel = progress * 135 / 100;
                txtVel.setText(vel + "km/h");
                String strVel = "vel" + String.valueOf(vel) + ".";
                MyConexionBT.write(strVel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(VinculatedDevices.EXTRA__DEVICE_ADDRESS);
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] byte_in = new byte[1];
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Envio de trama
        public void write(String input) {

            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión falló", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    //################################################################################################################

    //Método para mostrar el actionbar
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    //Método para agrear las acciones de los botones
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.actionbar_searchButton){
            Intent siguiente = new Intent(this, VinculatedDevices.class);
            startActivity(siguiente);
            Toast.makeText(this, "Dispositivos enlazados", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.actionbar_offButton){
            if( btSocket != null){
                try {
                    btSocket.close();
                    Toast.makeText(this, "Dispositivo desvinculado", Toast.LENGTH_LONG).show();
                } catch (IOException e){
                    Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //################################################################################################################

}