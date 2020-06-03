package com.example.intelligentdoorlock;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button idmatch;
    private String id_matched;
    private String mac_address;
    private BluetoothSocket socket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private String message;
    private Message message_to_transfer = new Message();
    public static final int UPDATE_TEXT1 = 1;
    public static final int UPDATE_TEXT2 = 2;
    public static final int UPDATE_TEXT3 = 3;
    private boolean thread_open_close = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT1:
                    Toast.makeText(MainActivity.this, "You've received: " + message, Toast.LENGTH_LONG).show();
                    break;
                case UPDATE_TEXT2:
                    Toast.makeText(MainActivity.this, "Arriving checking point !", Toast.LENGTH_LONG).show();
                    break;
                case UPDATE_TEXT3:
                    Toast.makeText(MainActivity.this, "读取信息流失败！", Toast.LENGTH_LONG).show();
                default:
                    break;
            }
        }
    };

    public void save(String inputText) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("changeSettings", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);

        //此部分代码预设各参数值，仅供同步功能实现以前调试使用
        //实际情况中，应当在连接时即进行参数同步，而不需要此步工作
        //此处部分参数可看作未设置时的默认值
        ((GlobalVarious) getApplication()).setCurrent_mode("waiting");

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("id_matched", "");
        editor.putString("mac_address", "");
        editor.apply();
        socket = null;

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        id_matched = pref.getString("id_matched", "");

        final ToggleButton toggleButton;
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //String inputText = "match " + id_matched + "\nsys_control open\nrequest_setting_file";
                    //save(inputText);
                    if (id_matched != null) {
                        try {
                            mmOutStream.write("sys_control open".getBytes());
                            ((GlobalVarious) getApplication()).setOpen_close("open");
                            Toast.makeText(MainActivity.this, "已开启！", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(MainActivity.this, "同步失败！", Toast.LENGTH_SHORT).show();
                            toggleButton.setChecked(false);
                        }
                    }
                } else {
                    if (id_matched != null) {
                        //String inputText = "match " + id_matched + "\nsys_control close";
                        //save(inputText);
                        try {
                            mmOutStream.write("sys_control close".getBytes());
                            ((GlobalVarious) getApplication()).setOpen_close("close");
                            Toast.makeText(MainActivity.this, "已关闭！", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(MainActivity.this, "同步失败！", Toast.LENGTH_SHORT).show();
                            toggleButton.setChecked(true);
                        }
                    }
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "上一次修改时间：", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        idmatch = (Button) findViewById(R.id.idmatch);
        if (Objects.equals(id_matched, "")) idmatch.setText("未连接设备       ");
        else idmatch.setText("门锁ID：" + id_matched + "    ");
        idmatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(id_matched, ""))
                    Toast.makeText(MainActivity.this, "您还尚未配对，请点击下面的“重新配对”按钮进行配对。",
                            Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MainActivity.this, "您的id是：" + id_matched +
                            "\n如果您想要重新配对，请点击下面的“重新配对”按钮。", Toast.LENGTH_LONG).show();
            }
        });

        Button rematch = (Button) findViewById(R.id.rematch);
        rematch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Objects.equals(id_matched, "")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("提示消息");
                    dialog.setMessage("您确定要重新配对吗？");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                thread_open_close = false;
                                socket.close();
                                id_matched = "";
                                mac_address = "";
                                ((GlobalVarious) getApplication()).setGlobalBlueSocket(null);
                                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                                startActivityForResult(intent, 1);
                            } catch (IOException ignored) {
                                Toast.makeText(MainActivity.this, "错误！\n关闭socket失败！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });

        if (Objects.equals(id_matched, "")) button1.setEnabled(false);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (((GlobalVarious) getApplication()).getCurrent_mode().equals("waiting")) {
                        mmOutStream.write("unlock_door".getBytes());
                        ((GlobalVarious) getApplication()).setCurrent_mode("working");
                        Toast.makeText(MainActivity.this, "正在开门中！", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "开门失败！\n请检查蓝牙连接后重试。", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (Objects.equals(id_matched, "")) button2.setEnabled(false);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在开发中……", Toast.LENGTH_SHORT).show();
            }
        });

        if (Objects.equals(id_matched, "")) button3.setEnabled(false);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在开发中……", Toast.LENGTH_SHORT).show();
            }
        });

        if (Objects.equals(id_matched, "")) button4.setEnabled(false);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在开发中……", Toast.LENGTH_SHORT).show();
            }
        });

        if (Objects.equals(id_matched, "")) button5.setEnabled(false);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在开发中……", Toast.LENGTH_SHORT).show();
            }
        });

        if (Objects.equals(id_matched, "")) button6.setEnabled(false);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "正在开发中……", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_SHORT).show();
                id_matched = data.getStringExtra("id_matched");
                mac_address = data.getStringExtra("mac_address");
                //获取全局对象mBluetoothSocket
                socket = ((GlobalVarious) getApplication()).getGlobalBlueSocket();
                try {
                    mmInStream = socket.getInputStream();
                } catch (IOException e) {
                    Toast.makeText(this, "获取InputStream错误！", Toast.LENGTH_SHORT).show();
                }
                try {
                    mmOutStream = socket.getOutputStream();
                } catch (IOException e) {
                    Toast.makeText(this, "获取OutputStream错误！", Toast.LENGTH_SHORT).show();
                }
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("id_matched", id_matched);
                editor.putString("mac_address", mac_address);
                editor.apply();
                idmatch.setText("门锁ID：" + id_matched + "    ");
                button1.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(true);
                button4.setEnabled(true);
                button5.setEnabled(true);
                button6.setEnabled(true);
                thread_open_close = true;
                new Thread(() -> {
                    Log.d(TAG, "Having created new thread.");
                    while (thread_open_close) {
                        try {
                            message = null;
                            mmInStream = socket.getInputStream();
                            Log.d(TAG, "Having gotten inputStream.");

                            //实验代码：
                            /*try {
                                byte[] buffer = new byte[1024];
                                int bytes = mmInStream.read(buffer);
                                message = String.valueOf(bytes);
                            } catch (IOException e) {
                                break;
                            }*/

                            //方法1：
                            //message = readStreamToString(mmInStream);

                            //方法2：
                            //Scanner scanner = new Scanner(mmInStream, "UTF-8");
                            //message = scanner.useDelimiter("\\A").next();
                            //scanner.close();

                            //方法3：
                            //message = IOUtils.toString(mmInStream, StandardCharsets.UTF_8);

                            //check
                            message = "abc def";

                            //checking point
                            message_to_transfer.what = UPDATE_TEXT2;
                            handler.sendMessage(message_to_transfer);
                            Log.d(TAG, "Having received message.");
                            String[] order = message.split("\\s+");
                            Log.d(TAG, "Having divided message.");
                            switch (order[1]) {
                                case "sys_control":
                                    ((GlobalVarious) getApplication()).setOpen_close(order[2]);
                                    break;
                                case "sys_time":

                                    break;
                                case "sys_autocontrol":

                                    break;
                                case "set_safety_mode":
                                    ((GlobalVarious) getApplication()).setSafety_mode(order[2]);
                                    break;
                                case "set_steer_angle":
                                    ((GlobalVarious) getApplication()).setSteer_angle(order[2]);
                                    break;
                                case "set_unlock_direction":
                                    ((GlobalVarious) getApplication()).setUnlock_direction(order[2]);
                                    break;
                                case "set_indicator_light_mode":
                                    ((GlobalVarious) getApplication()).setIndicator_light_mode(order[2]);
                                    break;
                                case "sys_battery":
                                    ((GlobalVarious) getApplication()).setBattery(order[2]);
                                    break;
                                case "current_mode":
                                    ((GlobalVarious) getApplication()).setCurrent_mode(order[2]);
                                    break;
                                default:
                                    Log.d(TAG, "Having received message that can not be executed.");
                                    message_to_transfer.what = UPDATE_TEXT1;
                                    handler.sendMessage(message_to_transfer);
                            }
                        } catch (IOException e) {
                            message_to_transfer.what = UPDATE_TEXT3;
                            handler.sendMessage(message_to_transfer);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "Menu is on_on.");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.update:
                SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                id_matched = pref.getString("id_matched", "");
                mac_address = pref.getString("mac_address", "");
                if (!Objects.equals(id_matched, "")) {
                    idmatch.setText("门锁ID：" + id_matched + "    ");
                    button1.setEnabled(true);
                    button2.setEnabled(true);
                    button3.setEnabled(true);
                    button4.setEnabled(true);
                    button5.setEnabled(true);
                    button6.setEnabled(true);
                }
                Toast.makeText(this, "完成刷新！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.help:
                Toast.makeText(this, "正在开发中……", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    public static String readStreamToString(InputStream inputStream) throws IOException {
        //创建字节数组输出流 ，用来输出读取到的内容
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //创建读取缓存,大小为1024
        byte[] buffer = new byte[1024];
        //每次读取长度
        int len = 0;
        //开始读取输入流中的文件
        while ((len = inputStream.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
            byteArrayOutputStream.write(buffer, 0, len); // 把读取的内容写入到输出流中
        }
        //把读取到的字节数组转换为字符串
        String result = byteArrayOutputStream.toString();

        //返回字符串结果
        return result;
    }
}