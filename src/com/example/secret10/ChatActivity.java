package com.example.secret10;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity  {

    ListView listView;
    List<String> messageList = new ArrayList<String>();
    String targetUsername;
    String targetUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        listView = (ListView) findViewById(R.id.messageList);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // set action bar name based on the passed in contact name (from bundle)
        try {
            targetUsername = getIntent().getStringExtra("username");
            targetUserID = getIntent().getStringExtra("userID");
            setTitle(targetUsername);
        } catch(Exception e) {
            Log.e("chat", e.toString());
        }

        // populate list view with array of strings
        // define an adapter and attach it to listview
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messageList);
        listView.setAdapter(adapter);

        // set listener on listView
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        (String) listView.getItemAtPosition(position), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

//    private void connectWebSocket() {
//        Log.i("Websocket", "initiate");
//        URI uri;
//        try {
//            uri = new URI("ws://" + InitialActivity.HOST + ":8080");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        mWebSocketClient = new WebSocketClient(uri) {
//            @Override
//            public void onOpen(ServerHandshake serverHandshake) {
//                Log.i("Websocket", "Opened");
//                // mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
//            }
//
//            @Override
//            public void onMessage(String s) {
//                final String message = s;
//                String eventType = null;
//                try {
//                    final JSONObject obj = new JSONObject(s);
//                    eventType = obj.getString("event");
//
//                    // if new incoming message
//                    if(!eventType.isEmpty()) {
//                        Log.i("Websocket", eventType);
//                        if(eventType.equals("message")) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        String msg = obj.getString("data");
//                                        String sender = obj.getString("user");
//                                        addToList(sender + ": " + msg);
//                                    } catch (Exception e) {
//                                    }
//                                    ;
//                                }
//                            });
//                        }
//                    }
//                } catch(Exception e){};
//                Log.i("Websocket", s);
//            }
//
//            @Override
//            public void onClose(int i, String s, boolean b) {
//                Log.i("Websocket", "Closed " + s);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i("Websocket", "Error " + e.getMessage());
//            }
//        };
//
//        Log.i("Websocket", "connecting");
//        mWebSocketClient.connect();
//    }

    public void addToList(String msg) {
        messageList.add(msg);
        // update UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //stuff that updates ui
                listView.invalidateViews();
            }
        });
    }

    public void sendMessage() {
        EditText editText = (EditText)findViewById(R.id.messageField);
        String msg = editText.getText().toString();

        // construct appropriate JSON object as a string
        String messageObj = "{\"event\":\"message\", " +
                            "\"data\": { \"from\": \"" + InitialActivity.myUserID + "\" ," +
                                         "\"to\" : \"" + targetUserID + "\"," +
                                         "\"message\": \"" + msg + "\"} }";
        Log.i("Websocket", "sending out this message : " + messageObj);
        SecretListActivity.mWebSocketClient.send(messageObj);
        editText.setText("");
    }

}
