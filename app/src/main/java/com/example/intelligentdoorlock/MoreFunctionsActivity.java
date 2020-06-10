package com.example.intelligentdoorlock;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.OutputStream;

public class MoreFunctionsActivity extends AppCompatActivity {
    private OutputStream mmOutStream;
    private long time;
    private static final int PERIOD = 19000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_functions);
        Toolbar toolbar = findViewById(R.id.toolbar_more_functions);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        time = intent.getLongExtra("time", 0);

        try {
            mmOutStream = (((GlobalVarious) getApplication()).getGlobalBlueSocket()).getOutputStream();
        } catch (IOException e) {
            Toast.makeText(this, "获取socket失败，请重试！", Toast.LENGTH_SHORT).show();
        }

        Button face_control = findViewById(R.id.face_control);
        face_control.setOnClickListener(v -> {
            if (System.currentTimeMillis() >= time && System.currentTimeMillis() <= time + PERIOD) {
                Toast.makeText(this, "正在开锁中，请稍后操作。", Toast.LENGTH_LONG).show();
            } else {
                Intent intent1 = new Intent(MoreFunctionsActivity.this, FacesetControlActivity.class);
                startActivity(intent1);
            }
        });

        Button clear_auto_control = findViewById(R.id.clear_auto_control);
        clear_auto_control.setOnClickListener(v -> {
            if (System.currentTimeMillis() >= time && System.currentTimeMillis() <= time + PERIOD) {
                Toast.makeText(this, "正在开锁中，请稍后操作。", Toast.LENGTH_LONG).show();
            } else {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MoreFunctionsActivity.this);
                dialog.setTitle("请选择您需要取消的内容：");
                dialog.setMessage("提示：取消之后无法恢复哦。");
                dialog.setCancelable(false);
                dialog.setNegativeButton("取消定时开机", (dialog11, which) -> {
                    if (!((GlobalVarious) getApplication()).getAuto_control_open().equals("")) {
                        AlertDialog.Builder dialog1 = new AlertDialog.Builder(MoreFunctionsActivity.this);
                        dialog1.setTitle("提示消息");
                        dialog1.setMessage("您确定要取消吗？");
                        dialog1.setCancelable(false);
                        dialog1.setPositiveButton("是的", (dialog111, which1) -> {
                            try {
                                mmOutStream.write("clear_sys_autocontrol open".getBytes());
                                ((GlobalVarious) getApplication()).setAuto_control_open("");
                                ((GlobalVarious) getApplication()).setLatest_modified("clear_sys_auto_control_open");
                                Toast.makeText(MoreFunctionsActivity.this, "取消成功！", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(MoreFunctionsActivity.this, "取消失败！", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog1.setNegativeButton("取消", (dialog121, which1) -> {
                        });
                        dialog1.show();
                    } else {
                        Toast.makeText(this, "您尚未设置定时开机！", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.setNeutralButton("取消", (dialog122, which) -> {
                });

                dialog.setPositiveButton("取消定时关机", (dialog12, which) -> {
                    if (((GlobalVarious) getApplication()).getAuto_control_close() != "") {
                        AlertDialog.Builder dialog1 = new AlertDialog.Builder(MoreFunctionsActivity.this);
                        dialog1.setTitle("提示消息");
                        dialog1.setMessage("您确定要取消吗？");
                        dialog1.setCancelable(false);
                        dialog1.setPositiveButton("是的", (dialog111, which1) -> {
                            try {
                                mmOutStream.write("clear_sys_autocontrol close".getBytes());
                                ((GlobalVarious) getApplication()).setAuto_control_close("");
                                ((GlobalVarious) getApplication()).setLatest_modified("clear_sys_auto_control_close");
                                Toast.makeText(MoreFunctionsActivity.this, "取消成功！", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(MoreFunctionsActivity.this, "取消失败！", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog1.setNegativeButton("取消", (dialog121, which1) -> {
                        });
                        dialog1.show();
                    } else {
                        Toast.makeText(this, "您尚未设置定时关机！", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });

        Button indicator_light = findViewById(R.id.indicator_light);
        indicator_light.setOnClickListener(v -> {
            Intent intent2 = new Intent(MoreFunctionsActivity.this, IndicatorLightActivity.class);
            startActivity(intent2);
        });

        Button about_us = findViewById(R.id.about_us);
        about_us.setOnClickListener(v -> {
            Intent intent3 = new Intent(MoreFunctionsActivity.this, AboutUsActivity.class);
            startActivity(intent3);
        });
    }
}
