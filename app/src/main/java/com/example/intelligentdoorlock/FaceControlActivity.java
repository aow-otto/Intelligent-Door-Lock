package com.example.intelligentdoorlock;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;

public class FaceControlActivity extends AppCompatActivity
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

	ImageSwitcher imageView;

	boolean deleteMode=false;

	HashMap<String,String> account_face=new HashMap<String,String>();
	String facePath= Environment.getExternalStorageDirectory()+"/Intelligent_door_lock/";
	String accountError="操作失败，请检查API_key与API_secret是否匹配";
	ApiTask postRequest;

	public void PrintNotice(String message)
	{
		Toast.makeText(FaceControlActivity.this,message,Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face_control);
		toolbar=findViewById(R.id.toolbar_face_control);
		setSupportActionBar(toolbar);

		final Intent intent=getIntent();
		account_face=(HashMap<String,String>)intent.getSerializableExtra("data");
		facePath+=account_face.get("setName");

		layoutInflater=FaceControlActivity.this.getLayoutInflater();

		toolbar.setTitle(account_face.get("setName"));
		FaceDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_face_control,menu);
		menuItem=menu.findItem(R.id.FaceRemove);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	@RequiresApi(api = Build.VERSION_CODES.O)
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.FaceAdd:
				CameraCatch();
				if(new File(facePath+"/stp.jpg").exists()==false) break;
				view=layoutInflater.inflate(R.layout.face_add,null);
				builder=new AlertDialog.Builder(FaceControlActivity.this);
				dialog=builder.setView(view)
					.setPositiveButton("确定", (DialogInterface.OnClickListener) (dialog1, id)->
					{
						editText=(EditText)view.findViewById(R.id.FaceName);
						String[] faceNow=new File(facePath).list();
						boolean faceExist=false;
						if(faceNow!=null)
						{
							for(String face:faceNow)
							{
								if(face.equals(editText.getText().toString()))
								{
									PrintNotice("存在同名人脸！");
									faceExist=true;
									break;
								}
							}
						}
						if(!faceExist)
						{
							FaceDetect(facePath+"/stp.jpg");
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
			case R.id.FaceRemove:
				if(deleteMode)
				{
					toolbar.setTitle(account_face.get("setName"));
					menuItem.setTitle("删除人脸");
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

	public void FaceDisplay()
	{
		String[] faceNow=new File(facePath).list();
		if(faceNow==null) return;
		listView=(ListView)findViewById(R.id.FaceList);
		arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,faceNow);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				if(!deleteMode)
				{
					String[] files=new File(facePath+faceNow[position]).list();
					imageView.setImageURI(Uri.fromFile(new File(facePath+'/'+faceNow[position]+'/'+files[0])));
				}
				else
				{
					view=layoutInflater.inflate(R.layout.face_remove,null);
					textView=view.findViewById(R.id.FaceDeleteWarning);
					textView.setText("你确定要移除"+faceNow[position]+"吗？");
					builder=new AlertDialog.Builder(FaceControlActivity.this);
					dialog=builder.setView(view)
						.setPositiveButton("确定", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								FaceRemove(faceNow[position]);
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
		postRequest= new FaceControlActivity.ApiTask();
		postRequest.execute(data);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public String GetBase64(String imagePath)
	{
		Base64.Encoder encoder=Base64.getEncoder();
		InputStream in;
		byte[] data=null;
		String result=null;
		try
		{
			in=new FileInputStream(imagePath);
			data=new byte[in.available()];
			in.read(data);
			result=encoder.encodeToString(data);
			in.close();
		}
		catch(Exception e){}
		result=result.replace("+","-");
		result=result.replace("/","_");
		result=result.replace("=","");
		int len=result.length();
		char[] stp=result.toCharArray();
		for(int a=0;a<len;a++)
		{
			if(stp[a]=='-' || stp[a]=='_')
			{
				PrintNotice("Sinogisb");
				break;
			}
		}
		PrintNotice(len+" "+stp[len-2]+" "+stp[len-1]);
		return result;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void CameraCatch()
	{
		Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager())!=null)
		{
			ContentValues contentValues=new ContentValues(2);
			contentValues.put(MediaStore.Images.Media.DATA,facePath+"/stp.jpg");
			contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
			Uri photoUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
			startActivityForResult(intent,39);
		}
//		imageCompress(8);
	}

	public void imageCompress(int inSampleSize)
	{
		File originFile=new File(facePath,"stp.jpg");
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		Bitmap emptyBitmap=BitmapFactory.decodeFile(originFile.getAbsolutePath(), options);
		options.inJustDecodeBounds=false;
		options.inSampleSize=inSampleSize;
		Bitmap resultBitmap=BitmapFactory.decodeFile(originFile.getAbsolutePath(), options);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		try
		{
			FileOutputStream fos=new FileOutputStream(new File(facePath,"stp.jpg"));
			fos.write(bos.toByteArray());
			fos.flush();
			fos.close();
		}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public void FaceDetect(String imagePath)
	{
		JSONObject data=new JSONObject();
		data.put("api_id","FaceDetecet");
		data.put("api_key",account_face.get("api_key"));
		data.put("api_secret",account_face.get("api_secret"));
		data.put("image_base64",GetBase64(imagePath));
		SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/detect",data);
	}

	public void FaceAdd(String faceName,String face_token)
	{
		JSONObject data=new JSONObject();
		data.put("api_id","FaceAdd");
		data.put("api_key",account_face.get("api_key"));
		data.put("api_secret",account_face.get("api_secret"));
		data.put("outer_id",account_face.get("setName"));
		data.put("face_tokens",face_token);
		SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/addface",data);
	}

	public void FaceRemove(String faceName)
	{
		JSONObject data=new JSONObject();
		String[] image=new File(facePath+'/'+faceName).list();
		image=image[0].split("\\.");
		data.put("api_id","FaceRemove");
		data.put("api_key",account_face.get("api_key"));
		data.put("api_secret",account_face.get("api_secret"));
		data.put("outer_id",account_face.get("setName"));
		data.put("face_tokens",image[0]);
		data.put("face_id",faceName);
		SendPostUrl("https://api-cn.faceplusplus.com/facepp/v3/faceset/removeface",data);
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
			if(data[0].get("outer_id")!=null)
			{
				postData+="&outer_id="+data[0].get("outer_id").toString();
			}
			if(data[0].get("face_tokens")!=null)
			{
				postData+="&face_tokens="+data[0].get("face_tokens").toString();
			}
			if(data[0].get("image_base64")!=null)
			{
				postData+="&image_base64="+data[0].get("image_base64").toString();
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
				in=new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				while((stp=in.readLine())!=null) buffer.append(stp);
				result=JSONObject.fromObject(buffer.toString());
				out.close();
				in.close();
			}
			catch(Exception e) {e.printStackTrace();}
			result.put("api_id",data[0].get("api_id"));
			if(data[0].get("face_tokens")!=null)
			{
				result.put("face_tokens",data[0].get("face_tokens"));
			}
			if(data[0].get("face_id")!=null)
			{
				result.put("face_id",data[0].get("face_id"));
			}
			return result;
		}

		@Override
		public void onPostExecute(JSONObject result)
		{
			Toast.makeText(FaceControlActivity.this,result.toString(),Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
/*			if(result.get("api_id").toString().equals("FaceDetect"))
			{
				if(result.containsKey("error_message")) PrintNotice(accountError);
				else if((int)result.get("face_num")==1)
				{
					JSONArray stp=(JSONArray)result.get("faces");
					result=(JSONObject)stp.get(0);
					PrintNotice("Sinogisb");
//					FaceAdd(result.get("face_id").toString(),result.get("face_token").toString());
				}
				else
				{
					if((int)result.get("face_num")==0) PrintNotice("未检测到人脸，请重新拍照！");
					else PrintNotice("检测到多张人脸，请重新拍照！");
					File image=new File(facePath+"/stp.jpg");
					image.delete();
					PrintNotice("Kamiisb");
				}
			}
			if(result.get("api_id").toString().equals("FaceAdd"))
			{
				if(result.containsKey("error_message")) PrintNotice(accountError);
				else
				{
					String faceName=result.get("face_id").toString();
					new File(facePath+'/'+faceName).mkdirs();
					File image=new File(facePath+"/stp.jpg");
					image.renameTo(new File(facePath+'/'+faceName+'/'+result.get("face_tokens").toString()+".jpg"));
				}
				FaceDisplay();
			}
			if(result.get("api_id").toString().equals("FaceRemove"))
			{
				if(result.containsKey("error_message")) PrintNotice(accountError);
				else FileClear(facePath+'/'+result.get("face_id").toString());
				FaceDisplay();
			}*/
		}
	}
}
