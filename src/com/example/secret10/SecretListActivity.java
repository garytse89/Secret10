package com.example.secret10;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
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

    public static WebSocketClient mWebSocketClient;

    ListView listView;
    List<String> usersList = new ArrayList<String>(); // for populating the list view
    Hashtable usersTable = new Hashtable(); // same as above, but also contains the user IDs. Note that this table is the opposite of
    // the server's usernames table; the key here is the usernames, and the value is the userIDs

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
                String username = (String) listView.getItemAtPosition(position);
                Log.i("Websocket", "Selected: " + username);
                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("username", username);
                i.putExtra("userID", (String) usersTable.get(username)); // get userID from hashtable
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
                String registerSocket = "{ \"event\": \"register\", \"data\": \"" + InitialActivity.myUserID + "\" }";
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                mWebSocketClient.send(registerSocket);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                String eventType = null;
                try {
                    final JSONObject obj = new JSONObject(s);
                    final JSONObject data = obj.getJSONObject("data"); // everything will be inside here
                    eventType = obj.getString("event");

                    // if new incoming message
                    if (!eventType.isEmpty()) {
                        Log.i("Websocket", eventType);

                        if (eventType.equals("update_userlist")) {
                            Log.i("Websocket", "update_userlist");
                            final Context context = getApplicationContext();
                            final CharSequence text = "user joined or left";
                            final int duration = Toast.LENGTH_SHORT;

                            try {
                                // parse the JSON object within the message that contains associative array of users
                                JSONObject allOnlineUsers = new JSONObject(obj.getString("data"));
                                Iterator<?> keys = allOnlineUsers.keys(); // keys are the userIDs, so allOnlineUsers.getString(key) gives the username

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    // Log.i("Websocket", "print the key: " + key + "<=>" + allOnlineUsers.getString(key));
                                    String oneUsername = allOnlineUsers.getString(key);
                                    String oneUserID = key;
                                    if(!usersList.contains(oneUsername))
                                        usersList.add(oneUsername);
                                    if(!usersTable.containsKey(oneUsername))
                                        usersTable.put(oneUsername, oneUserID);
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
                        } else if(eventType.equals("chat_message")) {
                            try {
                                JSONArray messages = data.getJSONArray("messages");
                                // store messages inside local storage - sharedPreferences, SQLite, etc.
                                String senderID = data.getString("origin");

                                // refactor this later inside a separate class
                                SharedPreferences chatDocs = getSharedPreferences("chatDocs", 0);
                                SharedPreferences.Editor editor = chatDocs.edit();

                                String chatLog = chatDocs.getString(senderID, "");
                                Log.i("Websocket", "existing chat log = " + chatLog);
                                JSONArray chatLogArray;

                                if(!chatLog.isEmpty()) {
                                    // convert chatLog from String to JSONArray
                                     chatLogArray = new JSONArray(chatLog);
                                } else {
                                    chatLogArray = new JSONArray();
                                }

                                chatLogArray.put(messages.getJSONObject(0).getString("message")); // only one message per incoming payload
                                // convert back to String
                                chatLog = chatLogArray.toString();

                                // store into SharedPreferences again
                                Log.i("Websocket", "new chat log to store = " + chatLog);
                                editor.putString(senderID, chatLog);
                                editor.commit();
                            } catch(Exception e) {
                                Log.i("Websocket", e.toString() + " at chat_message part");
                            }
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

    /*
    Saves a chat document based on sender's ID
    */
    public void saveChat(List<String> messageList, String senderID) {
        try {
            FileOutputStream fos = openFileOutput(senderID, Context.MODE_PRIVATE);
            fos.write(messageList.toString().getBytes());
            fos.close();
        }
        catch (Exception e) {
            Log.e("InternalStorage", e.toString());
        }
    }

    public ArrayList<String> readChat(String senderID) {
        ArrayList<String> toReturn = new ArrayList<String>();
        FileInputStream fis;
        try {
            fis = openFileInput(senderID);
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (ArrayList<String>) oi.readObject();
            oi.close();
        } catch (Exception e) {
            Log.e("InternalStorage", e.toString());
        }
        return toReturn; // if chat document not found (if you click on a contact you've never chatted with)
        // then return an empty ArrayList
    }

}
