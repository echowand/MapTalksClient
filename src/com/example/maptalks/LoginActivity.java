package com.example.maptalks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
					ConnectivityManager connMgr = (ConnectivityManager)
								getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
					if(networkinfo != null && networkinfo.isConnected()){
						new AsyncPostAgent().execute(this);
					}
				}
		});
        
        final Button registerBtn = (Button)findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AsyncRegisterAgent().execute();
			}
		});
    }
    
    private class AsyncRegisterAgent extends AsyncTask{

		@Override
		protected Object doInBackground(Object... params) {
			String username = ((EditText)findViewById(R.id.username)).getText().toString();
			String password = ((EditText)findViewById(R.id.loginPass)).getText().toString();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://maptalks-manifesto.rhcloud.com/register.php");
			String resultString = null;
			try{
				List<NameValuePair> nameValPair = new ArrayList<NameValuePair>();
				nameValPair.add(new BasicNameValuePair("username", username));
				nameValPair.add(new BasicNameValuePair("password", password));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValPair));
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				resultString = convertStreamToString(is);
				return resultString;
			}catch(IOException e){
				
			}
			return null;
		}
		@Override
		public void onPostExecute(Object result){
			if(result != null){
				if(((String)result).startsWith("succ")){
					Toast.makeText(getApplicationContext(), "Register successful. Please login now.", Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(getApplicationContext(), "Name already taken. Choose another one please.", Toast.LENGTH_LONG).show();
				}
			}
		}
    	
    }
    
    private class AsyncPostAgent extends AsyncTask{
    	private LoginActivity rootAct;
    	private String username;
		@Override
		protected String doInBackground(Object... arg0) {
			//rootAct = (LoginActivity)arg0[0];
			String username = ((EditText)findViewById(R.id.username)).getText().toString();
			String password = ((EditText)findViewById(R.id.loginPass)).getText().toString();
			this.username = username;
			//Toast.makeText(getApplicationContext(), username + " " + password, Toast.LENGTH_SHORT).show();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://maptalks-manifesto.rhcloud.com/login.php");
			String resultString = null;
			try{
				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
				nameValuePair.add(new BasicNameValuePair("username", username));
				nameValuePair.add(new BasicNameValuePair("password", password));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				//Log.d("Login", "Heer2");
			     resultString = convertStreamToString(is);
				//Toast.makeText(getApplicationContext(), resultString, Toast.LENGTH_LONG).show();
			}catch(ClientProtocolException e){
				Log.d("Login", "Clp");
				//e.printStackTrace();
			}catch(IOException e){
				Log.d("Login", "IOE");
			}finally{
			}
						
		return resultString;
		}
    	
		@Override
		protected void onPostExecute(Object result){
			Toast.makeText(getApplicationContext(), (String)result, Toast.LENGTH_LONG).show();
			if(((String)result).startsWith("succ")){
				Toast.makeText(getApplicationContext(), "Start map", Toast.LENGTH_LONG).show();	
				Intent intent = new Intent(LoginActivity.this, MyMapActivity.class);
				intent.putExtra(android.content.Intent.EXTRA_TEXT, this.username);
				startActivity(intent);
			}
		}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
