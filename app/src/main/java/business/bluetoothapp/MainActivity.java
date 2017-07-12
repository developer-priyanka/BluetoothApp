package business.bluetoothapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.provider.ContactsContract.Intents.Insert.NAME;

public class MainActivity extends AppCompatActivity  {
    private BluetoothAdapter mBA;
    private TextView tV;
    ListView listView;
    private ProgressDialog mProgressDlg;
    private Set<BluetoothDevice> pairedDevices;
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    ArrayList list = new ArrayList();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mBA = BluetoothAdapter.getDefaultAdapter();
        listView=(ListView)findViewById(R.id.Blist);
        tV=(TextView)findViewById(R.id.title_new_devices);

        mProgressDlg 		= new ProgressDialog(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mBA.cancelDiscovery();
            }
        });


        if (!mBA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
            return;
        } else {
            pairedDevices = mBA.getBondedDevices();
            listView.setAdapter(null);

            for (BluetoothDevice bt : pairedDevices)
                list.add(bt.getName() + "\n" + bt.getAddress());
          //  Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
            tV.setVisibility(View.VISIBLE);


        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the device MAC address, which is the last 17 chars in the View
               String info = (String)listView.getItemAtPosition(i);
                String address = info.substring(info.length()-17);

                BluetoothDevice device= mBA.getRemoteDevice(address);
                Toast.makeText(MainActivity.this,device.getAddress(),Toast.LENGTH_LONG).show();
                // Make an intent to start next activity while taking an extra which is the MAC address.th()-17);

                Intent intent = new Intent(MainActivity.this, BluetoothChatActivity.class);
                  intent.putExtra("address", address);
                startActivity(intent);


            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

    }

    public  void makeDiscoverable(View v){
       listView.setVisibility(v.VISIBLE); Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void scan(View v){
        doDiscovery();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);


    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        mProgressDlg.show();

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        // If we're already discovering, stop it
        if (mBA.isDiscovering()){
            mBA.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBA.startDiscovery();


    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                list.add(deviceName + "\n" + deviceHardwareAddress);
                adapter.notifyDataSetChanged();
            }
            Log.i("RECEIVER",list.get(0).toString());
        }
    };




}
