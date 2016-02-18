package com.example.kamillysy.platformcontroller;

import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.view.View;
import java.util.Set;
import java.util.ArrayList;
import android.widget.TextView;
import java.io.IOException;
import java.util.UUID;
import android.widget.SeekBar.OnSeekBarChangeListener;



public class PlatformController extends AppCompatActivity {
    BluetoothAdapter btAdapter;
    Button btlist;
    ImageButton imageButtons[] = new ImageButton[8];
    Set<BluetoothDevice> pairedDevices;
    BluetoothSocket socket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    SeekBar seekBar;
    TextView textSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_controller);
        btIniter();
        buttonsIniter();
        pwmControl();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
        }
    }

    private void btIniter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            if (!btAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
            }

        }
    }


    public void btList(View view) {
        pairedDevices = btAdapter.getBondedDevices();
        final ArrayList list = new ArrayList();
        final ArrayList addresses = new ArrayList();

        CharSequence[] ls = new CharSequence[pairedDevices.size()];

        if (pairedDevices.size() > 0) {
            int it = 0;
            for (BluetoothDevice bt : pairedDevices) {
                addresses.add(bt.getAddress());
                list.add(bt.getName() + "\n" + bt.getAddress());
                ls[it] = list.get(it).toString();
                it++;
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices", Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Choose paired BT device");
        builder.setItems(ls, new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, final int which) {
                connect(addresses.get(which).toString());
            }
        });

        builder.create();
        builder.show();
    }

    private void connect(String deviceadress) {
        final String adress = deviceadress;
        BluetoothDevice platform = btAdapter.getRemoteDevice(adress);
        try {
            socket = platform.createInsecureRfcommSocketToServiceRecord(myUUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket.isConnected()) {
            btlist.setText("Connected");
        } else {
            socket = null;
            btlist.setText("Error, check the platform!");
        }

    }

    private void buttonsIniter() {
        btlist = (Button) findViewById(R.id.ButtonList);
        final int[] idlist = {R.id.ButtonF, R.id.ButtonLF, R.id.ButtonRF,
                R.id.ButtonL, R.id.ButtonR,
                R.id.ButtonB, R.id.ButtonLB, R.id.ButtonRB};
        final int[] commands = {111, 114, 115, 140, 150, 122, 124, 125};

        for (int it = 0; it < imageButtons.length; it++) {
            imageButtons[it] = (ImageButton) findViewById(idlist[it]);
            final int finalIt = it;
            imageButtons[finalIt].setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    buttonsAction(event, commands[finalIt]);
                    return false;
                }
            });
        }
    }

    private void buttonsAction(MotionEvent event, int command) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sendComand(command);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            sendComand(160);
        }
    }

    private void pwmControl() {
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textSpeed = (TextView) findViewById(R.id.textView);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromuser) {
                progress = progressValue;
                textSpeed.setText("Speed: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendComand(progress);
                if (socket == null) {
                    seekBar.setProgress(50);
                }
            }
        });
    }



    private void sendComand(int x) {
        if (socket != null) {
            try {
                socket.getOutputStream().write((byte) x);
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
                btlist.setText("Error, check the connection!");
            }
        } else {
            Toast.makeText(getApplicationContext(), "First, connect to the platform", Toast.LENGTH_SHORT).show();
        }
    }

}

