package com.example.secret10;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
		
		final EditText mGender = (EditText) findViewById(R.id.genderField);
		final EditText mCountry = (EditText) findViewById(R.id.countryField);
		
		Button loginBtn = (Button) findViewById(R.id.signUpLogin);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v){			
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	login(mGender.getText().toString(), mCountry.getText().toString());
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
	public void login(String gender, String country) throws UnsupportedEncodingException {
		Log.d("login", "login() inside");
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(LOGIN_URL);
		ResponseHandler<String> handler = new BasicResponseHandler(); 
		
		// add data in List<NameValuePair> form then converted to urlencodedformentity
		// and finally strapped onto the post
		List<NameValuePair> listData = new ArrayList<NameValuePair>();
		listData.add(new BasicNameValuePair("gender", gender));
		listData.add(new BasicNameValuePair("country", country));
		post.setEntity(new UrlEncodedFormEntity(listData));
		
        try {  
            String result = httpclient.execute(post, handler);
            
            if( result.equals("ok") ) { // don't use == for string equality
            	Log.d("login", "sign up ok, load next page");
            	Intent i = new Intent(getApplicationContext(), SecretListActivity.class);
				startActivity(i);					
            }
            
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        httpclient.getConnectionManager().shutdown();
	}
}