package com.example.secret10;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class LoginActivity extends Activity {

	private final String LOGIN_URL = "http://" + InitialActivity.HOST + ":3000/login";

    /* GCM STUFF */

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "827721526936";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;

    /* END OF GCM STUFF */

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
        context = getApplicationContext();

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

        /* GCM onCLICK */
        gcm = GoogleCloudMessaging.getInstance(this);

        Button testGCMBtn = (Button) findViewById(R.id.testGCM);
        testGCMBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                regid = getRegistrationId(context);
                if (regid.isEmpty()) {
                    registerInBackground();
                }
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
            Log.d("login", result);

            if( result.equals("Invalid fields") ) {
                throw new Exception("Gender or Country not entered properly");
            }

            // first time sign up, store username and user_id
            JSONObject userInfo = new JSONObject(result);
            String username = userInfo.getString("username");


            String userID = userInfo.getString("user_id");
            // set userID in memory
            InitialActivity.myUserID = userID;

            Log.d("login", "Parsed into username = " + username + ", user_id = " + userID);

            SQLiteDatabase mydatabase = openOrCreateDatabase("secret10", MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS UserInfo(Username VARCHAR, UserID VARCHAR);");
            mydatabase.execSQL("DELETE FROM UserInfo;"); // only store one username/userID pair
            mydatabase.execSQL("INSERT INTO UserInfo VALUES('" + username + "', '" + userID + "');");

            Cursor resultSet = mydatabase.rawQuery("Select * from UserInfo",null);
            resultSet.moveToFirst();

            Log.d("login", "First result in local DB = " + username + ", " + userID + ", and number of rows = " + resultSet.getCount());

            Log.d("login", "sign up ok, load next page");
            Intent i = new Intent(getApplicationContext(), SecretListActivity.class);
			startActivity(i);
            
        } catch (Exception e) {
            Log.d("Exception at the bottom of LoginActivity", e.toString());
        };
        httpclient.getConnectionManager().shutdown();
	}

    /* GCM METHODS taken from DemoActivity.class from GCM client example */

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        Log.i(TAG, "registerInBackground() starts");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

                    Log.i(TAG, "registerInBackground() googogo");
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    Log.i(TAG, "registerInBackground caught exception = " + msg);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // whatever you want here. Toast?
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(LoginActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        Log.i(TAG, "sendRegistrationToBackend()");
        // code here
    }
}