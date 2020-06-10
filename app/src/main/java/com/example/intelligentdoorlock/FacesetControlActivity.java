package com.example.intelligentdoorlock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public class FacesetControlActivity extends AppCompatActivity {
    private static final String TAG = "FaceSetControl";
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    Toolbar toolbar;
    TextView textView;
    MenuItem menuItem;

    AlertDialog.Builder builder;
    LayoutInflater layoutInflater;
    AlertDialog dialog;
    EditText editText;
    View view, menuView;

    boolean deleteMode = false;

    HashMap<String, String> account_face = new HashMap<String, String>();
    String rootPath = "/Intelligent_door_lock/";
    String facesetPath = rootPath + "Face/data";
    String accountError = "操作失败，请检查API_key与API_secret是否匹配";
    ApiTask postRequest;

    public void PrintNotice(String message) {
        Toast.makeText(FacesetControlActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faceset_control);
        toolbar = findViewById(R.id.toolbar_faceset_control);
        setSupportActionBar(toolbar);

        layoutInflater = FacesetControlActivity.this.getLayoutInflater();

        toolbar.setTitle("人脸识别库管理");
        account_face.put("api_key", "pLkXcTYc1irj2Lqq2_ZzLPWflVrPjSG9");
        account_face.put("api_secret", "F6tjVfZxmsngYwC52TlSROKRtYqS5FBx");

        FacesetDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_faceset_control, menu);
        menuItem = menu.findItem(R.id.FacesetDelete);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressLint("ResourceType")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.APILogin:
                view = layoutInflater.inflate(R.layout.api_login, null);
                builder = new AlertDialog.Builder(FacesetControlActivity.this);
                dialog = builder.setView(view)
                        .setPositiveButton("保存", (dialog, id) -> {
                            editText = view.findViewById(R.id.api_key);
                            account_face.put("api_key", editText.getText().toString());
                            editText = view.findViewById(R.id.api_secret);
                            account_face.put("api_secret", editText.getText().toString());
                        })
                        .setNegativeButton("取消", (dialog, id) -> dialog.dismiss()).create();
                dialog.show();
                editText = dialog.findViewById(R.id.api_key);
                editText.setText(account_face.get("api_key"));
                editText = dialog.findViewById(R.id.api_secret);
                editText.setText(account_face.get("api_secret"));
                break;
            case R.id.FacesetCreate:
                view = layoutInflater.inflate(R.layout.faceset_create, null);
                builder = new AlertDialog.Builder(FacesetControlActivity.this);
                dialog = builder.setView(view)
                        .setPositiveButton("确定", (dialog1, id) ->
                        {
                            editText = view.findViewById(R.id.FacesetName);
                            String[] facesetNow = new File(facesetPath).list();
                            boolean facesetExist = false;
                            if (facesetNow != null) {
                                for (String faceset : facesetNow) {
                                    if (faceset.equals(editText.getText().toString())) {
                                        PrintNotice("该人脸识别库已存在！");
                                        facesetExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!facesetExist) {
                                try {
                                    FacesetCreate(editText.getText().toString());
                                } catch (InterruptedException e) {
                                    Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
                                }
                                Log.d(TAG, "Arriving here -- after FaceSetCreate.");
                                FacesetDisplay();
                                Log.d(TAG, "Arriving here -- after FaceSetDisplay.");
                            }
                        })
                        .setNegativeButton("取消", (dialog, id) -> dialog.dismiss()).create();
                dialog.show();
                break;
            case R.id.FacesetDelete:
                if (deleteMode) {
                    toolbar.setTitle("人脸识别库管理");
                    menuItem.setTitle("删除人脸识别库");
                    deleteMode = false;
                } else {
                    toolbar.setTitle("删除模式");
                    menuItem.setTitle("关闭删除模式");
                    deleteMode = true;
                }
                break;
        }
        return true;
    }

    public void FacesetDisplay() {
        String[] facesetNow = new File(facesetPath).list();
        if (facesetNow == null) return;
        listView = findViewById(R.id.FacesetList);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.faceset_control, facesetNow);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!deleteMode) {
                HashMap<String, String> data = account_face;
                data.put("setName", facesetNow[position]);
                Intent intent = new Intent(FacesetControlActivity.this, FaceControlActivity.class);
                intent.putExtra("data", data);
                FacesetControlActivity.this.startActivity(intent);
            } else {
                textView = findViewById(R.id.FacesetDeleteWarning);
                textView.setText("你确定要删除" + facesetNow[position] + "吗？\n库中存储的人脸将被清空。");
                dialog = builder.setView(layoutInflater.inflate(R.layout.faceset_delete, null))
                        .setPositiveButton("确定", (dialog, id1) -> {
                            try {
                                FacesetDelete(facesetPath + '/' + facesetNow[position]);
                            } catch (InterruptedException e) {
                                Toast.makeText(this, "删除失败！", Toast.LENGTH_SHORT).show();
                            }
                            FacesetDisplay();
                        })
                        .setNegativeButton("取消", (dialog, id12) -> dialog.dismiss()).create();
                dialog.show();
            }
        });
    }

    public void FileClear(String path) {
        String[] files = new File(path).list();
        File fileNow;
        if (files != null) {
            for (String file : files) {
                fileNow = new File(path + '/' + file);
                if (fileNow.isDirectory()) FileClear(path + '/' + file);
                fileNow.delete();
            }
        }
        fileNow = new File(path);
        fileNow.delete();
    }

    public JSONObject SendPostUrl(String api_url, JSONObject data) throws InterruptedException {
        JSONObject[] result = new JSONObject[1];
        data.put("url", api_url);
        postRequest = new ApiTask();
        postRequest.execute(data);
        while (result[0] == null) {
            Log.d(TAG, "SendPostUrl signal -- result[0]: " + result[0]);
            postRequest.setOnAsyncResponse(dataReceived -> result[0] = dataReceived);
            Thread.sleep(100);
        }
        return result[0];
    }

    public void FacesetCreate(String setName) throws InterruptedException {
        JSONObject data = new JSONObject(), result;
        data.put("api_key", account_face.get("api_key"));
        data.put("api_secret", account_face.get("api_secret"));
        data.put("outer_id", setName);
        data.put("check_empty", 0);
        data.put("url", "https://api-cn.faceplusplus.com/facepp/v3/faceset/create");
        result = SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/delete", data);
        Log.d(TAG, "FaceSetCreate signal -- after SendPostUrl.");
        if (result == null) return;
        if (result.containsKey("error_message")) PrintNotice(accountError);
        else new File(facesetPath + '/' + setName).mkdir();
    }

    public void FacesetDelete(String setName) throws InterruptedException {
        JSONObject data = new JSONObject(), result;
        data.put("api_key", account_face.get("api_key"));
        data.put("api_secret", account_face.get("api_secret"));
        data.put("outer_id", setName);
        data.put("check_empty", 0);
        result = SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/delete", data);
        if (result.containsKey("error_message")) PrintNotice(accountError);
        else FileClear(facesetPath + '/' + setName);
    }

    public interface AsyncResponse {
        void onDataReceivedSuccess(JSONObject dataReceived);
    }

    public class ApiTask extends AsyncTask<JSONObject, Void, JSONObject> {
        public AsyncResponse asyncResponse;

        public void setOnAsyncResponse(AsyncResponse asyncResponse) {
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... data) {
            PrintWriter out = null;
            BufferedReader in = null;
            JSONObject jsonObject = new JSONObject();
            String line, result = "";
/*			try
			{
				URL url=new URL(data[0].get("url").toString());
				URLConnection connection=url.openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				out=new PrintWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
				out.print(data);
				out.flush();
				in=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
				while((line=in.readLine())!=null) result+=line;
				jsonObject=JSONObject.fromObject(result);
			}
			catch(Exception e){}
			try
			{
				if(out!=null) out.close();
				if(in!=null) in.close();}
			catch(Exception e) {}*/
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            asyncResponse.onDataReceivedSuccess(result);
        }
    }
}
