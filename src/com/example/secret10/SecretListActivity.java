package com.example.secret10;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SecretListActivity extends Activity {
	
	private WebSocketClient mWebSocketClient;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sampleclient);
		
		connectWebSocket();
		
		Button sendBtn = (Button) findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage();				
			}
		});
		
		TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
		loginScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			};
		});
	}
	
	private void connectWebSocket() {
		Log.i("Websocket", "initiate");
		  URI uri;
		  try {
		    uri = new URI("ws://192.168.1.68:8080");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
		      Log.i("Websocket", "Opened");
		      mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
		    }

		    @Override
		    public void onMessage(String s) {
		      final String message = s;
		      Log.i("Websocket", s);
		      runOnUiThread(new Runnable() {
		        @Override
		        public void run() {
		          //TextView textView = (TextView)findViewById(R.id.messages);
		          //textView.setText(textView.getText() + "\n" + message);
		        }
		      });
		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		      Log.i("Websocket", "Closed " + s);
		    }

		    @Override
		    public void onError(Exception e) {
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  
		  Log.i("Websocket", "connecting");
		  mWebSocketClient.connect();
		}
	
	public void sendMessage() {
		EditText editText = (EditText)findViewById(R.id.message);
		mWebSocketClient.send(editText.getText().toString());
		editText.setText("");
	}
	

}
