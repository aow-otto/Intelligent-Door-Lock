package com.example.intelligentdoorlock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    public static final String TAG = "Chunna==BlueActivity";
    private List<HashMap> blueList;
    private PairedBluetoothDialogAdapter pairedAdapter;
    private TextView textView;
    public static BluetoothSocket socket;
    private ListView glvPaired;
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_match);
        Toolbar toolbar = findViewById(R.id.toolbar_bluetooth);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.current_con);
        initBlueTooth();
        glvPaired = findViewById(R.id.lv_blue_paired);
        if (adapter != null) {
            pairedAdapter = new PairedBluetoothDialogAdapter(this, blueList);
            pairedAdapter.notifyDataSetChanged();
            glvPaired.setAdapter(pairedAdapter);
            glvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BluetoothDevice gDevice = (BluetoothDevice) (((HashMap) pairedAdapter.getItem(position)).get("blue_device"));
                    String id_matched = (String) (((HashMap) pairedAdapter.getItem(position)).get("blue_name"));
                    Log.d(TAG, "想要连接的远程主机：" + gDevice);
                    assert gDevice != null;
                    Log.d(TAG, "想要连接的远程主机：" + gDevice.toString());
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("id_matched", id_matched);
                    editor.putString("mac_address", gDevice.toString());
                    editor.apply();
                    //然后就可以连接或者做操作啦

                    BluetoothSocket socket = null;
                    try {
                        // 蓝牙串口服务对应的UUID。如使用的是其它蓝牙服务，需更改下面的字符串
                        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                        socket = gDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (Exception e) {
                        Log.d("log", "获取Socket失败");
                        Toast.makeText(BluetoothActivity.this, "连接失败！\n错误原因：获取Socket失败。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        // Connect the device through the socket. This will block
                        // until it succeeds or throws an exception
                        textView.setText("正在连接中，请稍等……");
                        socket.connect();
                        Log.d("log", "连接成功");
                        //连接成功即切回到之前的主页面
                        Intent intent = new Intent(BluetoothActivity.this, MainActivity.class);
                        intent.putExtra("id_matched", id_matched);
                        intent.putExtra("mac_address", gDevice.toString());
                        //将mBluetoothSocket装入Application全局变量，可以在其他Activity中获取到该socket
                        ((GlobalVarious) getApplication()).setGlobalBlueSocket(socket);
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and get out
                        Log.d("log", "连接失败");
                        Toast.makeText(BluetoothActivity.this, "连接失败！\n请重新尝试，如实在无法连接，请到蓝牙配置界面手动连接。", Toast.LENGTH_SHORT).show();
                        try {
                            socket.close();
                            if (adapter != null) {
                                initBlueTooth();
                                pairedAdapter = new PairedBluetoothDialogAdapter(BluetoothActivity.this, blueList);
                                pairedAdapter.notifyDataSetChanged();
                                glvPaired.setAdapter(pairedAdapter);
                            }
                            textView.setText("请选择您要配对的设备");
                        } catch (IOException ignored) {
                            Toast.makeText(BluetoothActivity.this, "错误！\n关闭socket失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        Button gotobluetooth = findViewById(R.id.gotobluetooth);
        if (adapter == null) gotobluetooth.setEnabled(false);
        gotobluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            initBlueTooth();
            pairedAdapter = new PairedBluetoothDialogAdapter(this, blueList);
            pairedAdapter.notifyDataSetChanged();
            glvPaired.setAdapter(pairedAdapter);
        }
    }

    private void initBlueTooth() {
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                adapter.enable();
                //sleep one second ,avoid do not discovery
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            blueList = new ArrayList<HashMap>();
            Log.d(TAG, "获取已经配对devices" + devices.size());
            for (BluetoothDevice bluetoothDevice : devices) {
                Log.d(TAG, "已经配对的蓝牙设备：");
                Log.d(TAG, bluetoothDevice.getName());
                Log.d(TAG, bluetoothDevice.getAddress());
                HashMap blueHashMap = new HashMap();
                blueHashMap.put("blue_device", bluetoothDevice);
                blueHashMap.put("blue_name", bluetoothDevice.getName());
                blueHashMap.put("blue_address", bluetoothDevice.getAddress());
                blueList.add(blueHashMap);
                textView.setText("请选择您要配对的设备");
            }
        } else {
            textView.setText("此设备不支持蓝牙传输功能！");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blue, menu);
        Log.d(TAG, "Menu is on.");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.update_blue:
                if (adapter != null) {
                    initBlueTooth();
                    pairedAdapter = new PairedBluetoothDialogAdapter(this, blueList);
                    pairedAdapter.notifyDataSetChanged();
                    glvPaired.setAdapter(pairedAdapter);
                }
                Toast.makeText(this, "完成刷新！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.help_blue:
                if (adapter == null) {
                    Toast.makeText(this, "非常抱歉，您的蓝牙适配器不存在，无法连接装置！\n" +
                                    "您可以前往蓝牙设置检查自己的蓝牙设备是否正确安装，如果仍有关于蓝牙设备的问题，请咨询手机供应商。"
                            , Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "您好，请手动选择装置对应的蓝牙设备并点击，系统会自动帮您连接！\n" +
                                    "如果无法找到装置对应的蓝牙，请前往蓝牙配置界面搜索并配对，再回到此界面进行连接。"
                            , Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
        return true;
    }
}