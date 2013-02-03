package com.example.maptalks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MyMapActivity extends MapActivity {
	private MapView mapView;
	private Button submitBt;
	private LocationManager locMngr;
	private String username;
	private LocationListener locListener;
	protected Vector <InfoPoint> messageList;
	private SimpleItemizedOverlay soverlay;
	private List<Overlay> mapOverlays;
	private Timer myTimer;
	private Drawable drawable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		mapView = (MapView)findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		submitBt = (Button)findViewById(R.id.post_submit);
		locMngr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locListener = new LocationListener(){
			@Override
			public void onLocationChanged(Location arg0) {
                // TODO Auto-generated method stub

            }

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
				
			}
		};
		final boolean gpsEnabled = locMngr.isProviderEnabled(LocationManager.GPS_PROVIDER);
		username = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
	    if (!gpsEnabled) {
	    	Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_LONG).show();	
	    }
	    locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 5, locListener);
	    myTimer = new Timer();
	    myTimer.schedule(new TimerTask(){

			@Override
			public void run() {
				new AsyncRefresher().execute();
			}
	    }, 300, 30000);
	    
	    mapOverlays = mapView.getOverlays();
	    drawable = this.getResources().getDrawable(R.drawable.ic_launcher);
	    soverlay = new SimpleItemizedOverlay(drawable, this.mapView);
	    //GeoPoint point = new GeoPoint(19240000, -99120000);
	    //OverlayItem overlayItem = new OverlayItem(point, "Leo", "Hello");
	    //soverlay.addOverlay(overlayItem);
	    //mapOverlays.add(soverlay);
	    messageList = new Vector<InfoPoint>();
	    submitBt.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				new AsyncPostAgent().execute();
			}
	    });
	}
	private class AsyncRefresher extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			String resultString = null;
			HttpPost httpPost = new HttpPost("https://maptalks-manifesto.rhcloud.com/retrieve.php");
			messageList.clear();
			try{
				List<NameValuePair> nameValPair = new ArrayList<NameValuePair>();
				httpPost.setEntity(new UrlEncodedFormEntity(nameValPair));
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				resultString = convertStreamToString(is);
				String[] entries = resultString.split("\\|");
				//Log.d("List", entries[entries.length - 1]);
				for(int i = 0; i < entries.length - 1; i++){
					//Log.d("List", entries[i]);
					String[] fields = entries[i].split(",");
					//for(int j = 0;j < fields.length; j ++){
					//	Log.d("List", "username: " + fields[0].split(":")[1]);
					//}
					messageList.add(new InfoPoint((int)(Double.valueOf(fields[3].split(":")[1]) * 1E6),//latt
							(int)(Double.valueOf(fields[2].split(":")[1])*1E6),
							fields[0].split(":")[1],
							fields[4].split(":")[1] + ":" + fields[4].split(":")[2] + ":" + fields[4].split(":")[3], 
							fields[1].split(":")[1]));
					//Log.d("List", fields[4]);
				}
				return resultString;
			}catch(UnsupportedEncodingException e){
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Object result){
			//Toast.makeText(getApplicationContext(), (String)result, Toast.LENGTH_LONG).show();
			//Log.d("Map", "Refreshed " + (String)result);
			soverlay = new SimpleItemizedOverlay(drawable, mapView);
			for(int i = 0;i < messageList.size(); i ++){
				soverlay.addOverlay(new OverlayItem(messageList.get(i), 
						messageList.get(i).getTitle(),
						messageList.get(i).getMessage()));
			}
			mapOverlays.clear();
			mapOverlays.add(soverlay);
		}
		
		
		private String convertStreamToString(InputStream is) {

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
	
	private class AsyncPostAgent extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			String postMessage = ((EditText)findViewById(R.id.post_field)).getText().toString();
			if(postMessage.equals("")){
				return "Can not post an empty message.";
			}
			Location loc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(loc != null){
				String latt = Double.toString(loc.getLatitude());
				String longi = Double.toString(loc.getLongitude());
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("https://maptalks-manifesto.rhcloud.com/post.php");
				String resultString = null;
				try{
					List<NameValuePair> nameValPair = new ArrayList<NameValuePair>();
					nameValPair.add(new BasicNameValuePair("username", username));
					nameValPair.add(new BasicNameValuePair("message", postMessage));
					nameValPair.add(new BasicNameValuePair("longi", longi));
					//Log.d("Map", "longi:" + longi);
					nameValPair.add(new BasicNameValuePair("latt", latt));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValPair));
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					resultString = convertStreamToString(is);
					//Log.d("Map", resultString);
					return resultString;
					}catch(ClientProtocolException e){
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				
			}
			//Log.d("Map", "Loc is null");
			return null;
		}
		@Override
		protected void onPostExecute(Object result){
			Toast.makeText(getApplicationContext(), (String)result, Toast.LENGTH_LONG).show();
			if(((String)result).startsWith("succ")){
				Toast.makeText(getApplicationContext(), "post success", Toast.LENGTH_LONG).show();	
				new AsyncRefresher().execute();
			}
		}
		
		private String convertStreamToString(InputStream is) {

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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	class InfoPoint extends GeoPoint{
		private String title;
		private String username;
		private String message;
		public InfoPoint(int latt, int longi, String username, String time, String message) {
			super(latt, longi);
			this.title = username + " - " + time;
			this.username = username;
			this.message = message;
		}
		public String getTitle(){
			return title;
		}
		public String getMessage(){
			return message;
		}
	}
}
