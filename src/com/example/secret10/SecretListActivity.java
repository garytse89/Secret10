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

public class SecretListActivity extends Activity {

    private WebSocketClient mWebSocketClient;

    ListView listView;
    List<String> usersList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactlist);

        listView = (ListView) findViewById(R.id.contactList);

        connectWebSocket();

        // populate list view with array of strings
        // define an adapter and attach it to listview
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usersList);
        listView.setAdapter(adapter);

        // set listener on listView
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contact = (String) listView.getItemAtPosition(position);
                Log.i("Websocket", "Selected: " + contact);
                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("contact", contact);
                startActivity(i);
            }
        });
    }

    private void connectWebSocket() {
        Log.i("Websocket", "initiate");
        URI uri;
        try {
            uri = new URI("ws://" + InitialActivity.HOST + ":8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                // mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                String eventType = null;
                try {
                    final JSONObject obj = new JSONObject(s);
                    eventType = obj.getString("event");

                    // if new incoming message
                    if (!eventType.isEmpty()) {
                        Log.i("Websocket", eventType);

                        if (eventType.equals("update_userlist")) {
                            Log.i("Websocket", "update_userlist");
                            final Context context = getApplicationContext();
                            final CharSequence text = "user joined or left";
                            final int duration = Toast.LENGTH_SHORT;

                            // users list
                            ArrayList<String> users = new ArrayList<String>();

                            try {
                                // parse the JSON object within the message that contains associative array of users
                                JSONObject allOnlineUsers = new JSONObject(obj.getString("data"));
                                Iterator<?> keys = allOnlineUsers.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    // Log.i("Websocket", "print the key: " + key + "<=>" + allOnlineUsers.getString(key));
                                    usersList.add(new String(allOnlineUsers.getString(key)));
                                }
                            } catch (Exception e) {
                                Log.i("Websocket", e.toString());
                            }
                            ;


                            // update UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //stuff that updates ui
                                    listView.invalidateViews();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    Log.i("Websocket", "Invalid JSON message");
                }
                ;
                Log.i("Websocket", s);
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

}
