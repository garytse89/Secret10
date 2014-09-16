package com.example.secret10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class InitialActivity extends Activity {


    public static final String HOST = "192.168.1.68";
    public static SQLiteDatabase mydatabase;
    public static String myUserID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);

        mydatabase = openOrCreateDatabase("secret10", MODE_PRIVATE, null);

        // for testing only
        mydatabase.execSQL("DROP TABLE IF EXISTS UserInfo;"); // uncomment to force sign up again

        try {
            Cursor resultSet = mydatabase.rawQuery("Select * from UserInfo",null);
            resultSet.moveToFirst();
            String username = resultSet.getString(0);
            String userID = resultSet.getString(1);

            Log.i("database", username);
            Log.i("database", userID);
            Log.i("database", "Number of rows = " + resultSet.getCount());

            myUserID = userID;

            // if user already installed this app, go directly to SecretListActivity
            // but we still need to connect to server here
        } catch(Exception e) {
            //Inserting delay here
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e.printStackTrace();
            }

            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }

    }

}
