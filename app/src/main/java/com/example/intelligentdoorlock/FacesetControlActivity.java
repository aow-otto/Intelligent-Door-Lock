package com.example.intelligentdoorlock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static java.lang.Thread.sleep;


public class FacesetControlActivity extends AppCompatActivity
{
	private ListView listView;
	private ArrayAdapter<String> arrayAdapter;

	Toolbar toolbar;
	TextView textView;
	MenuItem menuItem;

	AlertDialog.Builder builder;
	LayoutInflater layoutInflater;
	AlertDialog dialog;
	EditText editText;
	View view;

	boolean deleteMode=false;

	HashMap<String,String> account_face=new HashMap<String,String>();
	String facesetPath=Environment.getExternalStorageDirectory()+"/Intelligent_door_lock";
	String accountError="操作失败，请检查api_key与api_secret是否匹配";
	ApiTask postRequest;

	public void PrintNotice(String message)
	{
		Toast.makeText(FacesetControlActivity.this,message,Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faceset_control);
		toolbar=findViewById(R.id.toolbar_faceset_control);
		setSupportActionBar(toolbar);

		layoutInflater=FacesetControlActivity.this.getLayoutInflater();

		toolbar.setTitle("人脸识别库管理");
		account_face.put("api_key","pLkXcTYc1irj2Lqq2_ZzLPWflVrPjSG9");
		account_face.put("api_secret","F6tjVfZxmsngYwC52TlSROKRtYqS5FBx");

		try{Class.forName("android.os.AsyncTask");}
		catch(ClassNotFoundException e){e.printStackTrace();}

		File file=new File(facesetPath);
		if(!file.exists())
		{
			boolean stp=file.mkdirs();
			if(stp==true) PrintNotice("存储路径创建成功");
			else PrintNotice("存储路径创建失败，请检查APP权限设置");
		}

		FacesetDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_faceset_control,menu);
		menuItem=menu.findItem(R.id.FacesetDelete);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	@SuppressLint("ResourceType")
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.APILogin:
				view=layoutInflater.inflate(R.layout.api_login,null);
				builder=new AlertDialog.Builder(FacesetControlActivity.this);
				dialog=builder.setView(view)
					.setPositiveButton("保存", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int id)
						{
							editText=(EditText)view.findViewById(R.id.api_key);
							account_face.put("api_key",editText.getText().toString());
							editText=(EditText)view.findViewById(R.id.api_secret);
							account_face.put("api_secret",editText.getText().toString());
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.dismiss();
						}
					}).create();
				dialog.show();
				editText=(EditText)dialog.findViewById(R.id.api_key);
				editText.setText(account_face.get("api_key"));
				editText=(EditText)dialog.findViewById(R.id.api_secret);
				editText.setText(account_face.get("api_secret"));
				break;
			case R.id.FacesetCreate:
				view=layoutInflater.inflate(R.layout.faceset_create,null);
				builder=new AlertDialog.Builder(FacesetControlActivity.this);
				dialog=builder.setView(view)
					.setPositiveButton("确定", (DialogInterface.OnClickListener)(dialog1, id)->
					{
						editText=(EditText)view.findViewById(R.id.FacesetName);
						String[] facesetNow=new File(facesetPath).list();
						boolean facesetExist=false;
						if(facesetNow!=null)
						{
							for(String faceset:facesetNow)
							{
								if(faceset.equals(editText.getText().toString()))
								{
									PrintNotice("该人脸识别库已存在！");
									facesetExist=true;
									break;
								}
							}
						}
						if(!facesetExist)
						{
							FacesetCreate(editText.getText().toString());
							toolbar.setTitle("人脸识别库管理");
							menuItem.setTitle("删除人脸识别库");
							deleteMode=false;
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.dismiss();
						}
					}).create();
				dialog.show();
				break;
			case R.id.FacesetDelete:
				if(deleteMode)
				{
					toolbar.setTitle("人脸识别库管理");
					menuItem.setTitle("删除人脸识别库");
					deleteMode=false;
				}
				else
				{
					toolbar.setTitle("删除模式");
					menuItem.setTitle("关闭删除模式");
					deleteMode=true;
				}
				break;
		}
		return true;
	}

	public void FacesetDisplay()
	{
		String[] facesetNow=new File(facesetPath).list();
		if(facesetNow==null) return;
		listView=(ListView)findViewById(R.id.FacesetList);
		arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,facesetNow);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				if(!deleteMode)
				{
					HashMap<String,String> data=account_face;
					data.put("setName",facesetNow[position]);
					Intent intent=new Intent(FacesetControlActivity.this,FaceControlActivity.class);
					intent.putExtra("data",data);
					FacesetControlActivity.this.startActivity(intent);
				}
				else
				{
					view=layoutInflater.inflate(R.layout.faceset_delete,null);
					textView=view.findViewById(R.id.FacesetDeleteWarning);
					textView.setText("你确定要删除"+facesetNow[position]+"吗？\n库中存储的人脸将被清空。");
					builder=new AlertDialog.Builder(FacesetControlActivity.this);
					dialog=builder.setView(view)
						.setPositiveButton("确定", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								FacesetDelete(facesetNow[position]);
							}
						})
						.setNegativeButton("取消", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,int id)
							{
								dialog.dismiss();
							}
						}).create();
					dialog.show();
				}
			}
		});
	}

	public void FileClear(String path)
	{
		String[] files=new File(path).list();
		File fileNow;
		if(files!=null)
		{
			for(String file:files)
			{
				fileNow=new File(path+'/'+file);
				if(fileNow.isDirectory()) FileClear(path+'/'+file);
				fileNow.delete();
			}
		}
		fileNow=new File(path);
		fileNow.delete();
	}

	public void SendPostUrl(String api_url,JSONObject data)
	{
		JSONObject[] result=new JSONObject[1];
		data.put("url",api_url);
		postRequest= new ApiTask();
		postRequest.execute(data);
	}

	public void FacesetCreate(String setName)
	{
		JSONObject data=new JSONObject(false);
		data.put("api_id","FacesetCreate");
		data.put("api_key",account_face.get("api_key"));
		data.put("api_secret",account_face.get("api_secret"));
		data.put("outer_id",setName);
		SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/create",data);
	}

	public void FacesetDelete(String setName)
	{
		JSONObject data=new JSONObject(false);
		data.put("api_id","FacesetDelete");
		data.put("api_key",account_face.get("api_key"));
		data.put("api_secret",account_face.get("api_secret"));
		data.put("outer_id",setName);
		data.put("check_empty",0);
		SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/delete",data);
	}

	public class ApiTask extends AsyncTask<JSONObject,Integer,JSONObject>
	{
		@Override
		public void onPreExecute() {}

		@Override
		@RequiresApi(api = Build.VERSION_CODES.KITKAT)
		public JSONObject doInBackground(JSONObject... data)
		{
			DataOutputStream out;
			BufferedReader in;
			StringBuilder buffer=new StringBuilder();
			JSONObject result=new JSONObject(false);
			String stp,postData="";
			postData+="api_key="+data[0].get("api_key").toString();
			postData+="&api_secret="+data[0].get("api_secret").toString();
			postData+="&outer_id="+data[0].get("outer_id").toString();
			if(data[0].get("check_empty")!=null)
			{
				postData+="&check_empty"+data[0].get("check_empty").toString();
			}
			try
			{
				URL url=new URL(data[0].get("url").toString());
				HttpURLConnection connection=(HttpURLConnection)url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.connect();
				out=new DataOutputStream(connection.getOutputStream());
				out.writeBytes(postData);
				out.flush();
				in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((stp=in.readLine())!=null) buffer.append(stp);
				result=JSONObject.fromObject(buffer.toString());
				out.close();
				in.close();
			}
			catch(Exception e) {e.printStackTrace();}
			result.put("api_id",data[0].get("api_id"));
			return result;
		}

		@Override
		public void onPostExecute(JSONObject result)
		{
			Toast.makeText(FacesetControlActivity.this,result.toString(),Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
			if(result.get("api_id").toString().equals("FacesetCreate"))
			{
				if(result.containsKey("error_message")) PrintNotice(accountError);
				else new File(facesetPath+'/'+result.get("outer_id").toString()).mkdirs();
			}
			if(result.get("api_id").toString().equals("FacesetDelete"))
			{
				if(result.containsKey("error_message")) PrintNotice(accountError);
				else FileClear(facesetPath+'/'+result.get("outer_id").toString());
			}
			FacesetDisplay();
		}
	}
}
