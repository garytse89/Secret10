package com.example.secret10;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



public class SecretListActivity extends Activity  {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.secretlist);
		
		final ListView listView = (ListView) findViewById(R.id.list);
		
		// populate list view with array of strings
		String[] values = new String[]{ "jyj", "micky", "yoochun" };
		
		// define an adapter and attach it to listview
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
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
	
}
