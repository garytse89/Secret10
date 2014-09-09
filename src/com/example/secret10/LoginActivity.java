package com.example.secret10;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	
	private final String LOGIN_URL = "http://192.168.1.68:3000/login";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);
		registerScreen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);				
			}
		});
		
		Button loginBtn = (Button) findViewById(R.id.btnLogin);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v){			
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	login();
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
				    }
				});

				thread.start(); 
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// defined methods
	public void login() {
		Log.d("login", "login() inside");
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(LOGIN_URL);
		ResponseHandler<String> handler = new BasicResponseHandler();  
        try {  
            String result = httpclient.execute(get, handler);  
            Log.d("login", result);
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        httpclient.getConnectionManager().shutdown();
	}
}