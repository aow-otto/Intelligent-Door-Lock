package com.example.intelligentdoorlock;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;

public class FaceControlActivity extends AppCompatActivity {
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

    ImageSwitcher imageView;

    boolean deleteMode = false;

    HashMap<String, String> account_face = new HashMap<String, String>();
    String rootPath = "/Intelligent_door_lock/";
    String facePath = rootPath + "Face/data/";
    String accountError = "操作失败，请检查API_key与API_secret是否匹配";

    public void PrintNotice(String message) {
        Toast.makeText(FaceControlActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_control);
        toolbar = findViewById(R.id.toolbar_face_control);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();
        account_face = (HashMap<String, String>) intent.getSerializableExtra("data");
        facePath += account_face.get("setName");

        layoutInflater = FaceControlActivity.this.getLayoutInflater();

        toolbar.setTitle("人脸识别库 " + account_face.get("setName"));
        FaceDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_face_control, menu);
        menuItem = menu.findItem(R.id.FaceRemove);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.FaceAdd:
                view = layoutInflater.inflate(R.layout.face_add, null);
                builder = new AlertDialog.Builder(FaceControlActivity.this);
                dialog = builder.setView(view)
                        .setPositiveButton("确定", (DialogInterface.OnClickListener) (dialog1, id) ->
                        {
                            editText = (EditText) view.findViewById(R.id.FaceName);
                            String[] faceNow = new File(facePath).list();
                            boolean faceExist = false;
                            if (faceNow != null) {
                                for (String face : faceNow) {
                                    if (face.equals(editText.getText().toString())) {
                                        PrintNotice("存在同名人脸！");
                                        faceExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!faceExist) FaceAdd(editText.getText().toString());
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                break;
            case R.id.FaceRemove:
                if (deleteMode) {
                    toolbar.setTitle("人脸识别库 " + account_face.get("setName"));
                    menuItem.setTitle("删除人脸");
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

    public void FaceDisplay() {
        String[] faceNow = new File(facePath).list();
        if (faceNow == null) return;
        listView = (ListView) findViewById(R.id.FaceList);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.face_control, faceNow);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!deleteMode) {
                    String[] files = new File(facePath).list();
                    imageView.setImageURI(Uri.fromFile(new File(facePath + '/' + files[0])));
                } else {
                    view = layoutInflater.inflate(R.layout.face_remove, null);
                    textView = view.findViewById(R.id.FaceDeleteWarning);
                    textView.setText("你确定要移除" + faceNow[position] + "吗？");
                    dialog = builder.setView(view)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    FaceRemove(facePath + '/' + faceNow[position]);
                                    FaceDisplay();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                }
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

    public JSONObject SendPostUrl(String api_url, JSONObject data) {
        PrintWriter out = null;
        BufferedReader in = null;
        JSONObject jsonObject = null;
        String line, result = "";
        try {
            URL url = new URL(api_url);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            out.print(data);
            out.flush();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            while ((line = in.readLine()) != null) result += line;
            jsonObject = JSONObject.fromObject(result);
        } catch (Exception e) {
            PrintNotice("调用API失败，请检查网络！");
        }
        return jsonObject;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String GetBase64(String imagePath) {
        Base64.Encoder encoder = Base64.getEncoder();
        InputStream in = null;
        byte[] data = null;
        String result = null;
        try {
            in = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            data = new byte[in.available()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = encoder.encodeToString(data);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String CameraCatch(String savePath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put(MediaStore.Images.Media.DATA, savePath + "/stp.jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, 39);
        }
        if (new File(savePath + "/stp.jpg").exists() == false) return "KamiiBaka";
        else return FaceDetect(savePath + "/stp.jpg");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String FaceDetect(String imagePath) {
        JSONObject data = new JSONObject(), result;
        data.put("api_key", account_face.get("api_key"));
        data.put("api_secret", account_face.get("api_secret"));
        data.put("image_base64", GetBase64(imagePath));
        result = SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/detect", data);
        if (result.containsKey("error_message")) PrintNotice(accountError);
        else if ((int) result.get("face_num") == 1) {
            JSONArray stp = (JSONArray) result.get("faces");
            result = (JSONObject) stp.get(0);
            return (String) result.get("face_token");
        } else {
            if ((int) result.get("face_num") == 0) PrintNotice("未检测到人脸，请重新拍照！");
            else PrintNotice("检测到多张人脸，请重新拍照！");
        }
        return "SinogiBaka";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void FaceAdd(String faceName) {
        String face_token = CameraCatch(facePath);
        if (face_token.equals("KamiiBaka")) return;
        if (face_token.equals("SinogiBaka")) {
            File image = new File(facePath + "/stp.jpg");
            image.delete();
            return;
        }
        JSONObject data = new JSONObject(), result;
        data.put("api_key", account_face.get("api_key"));
        data.put("api_secret", account_face.get("api_secret"));
        data.put("outer_id", account_face.get("setName"));
        data.put("face_token", face_token);
        result = SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/addface", data);
        if (result.containsKey("error_message")) PrintNotice(accountError);
        else {
            new File(facePath + '/' + faceName).mkdir();
            File image = new File(facePath + "/stp.jpg");
            image.renameTo(new File(facePath + '/' + faceName + '/' + face_token));
        }
    }

    public void FaceRemove(String faceName) {
        String[] image = new File(facePath + '/' + faceName).list();
        image = image[0].split("\\.");
        JSONObject data = new JSONObject(), result;
        data.put("api_key", account_face.get("api_key"));
        data.put("api_secret", account_face.get("api_secret"));
        data.put("outer_id", account_face.get("setName"));
        data.put("face_token", image[0]);
        result = SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/removeface", data);
        if (result.containsKey("error_message")) PrintNotice(accountError);
        else FileClear(facePath + '/' + faceName);
    }
}
