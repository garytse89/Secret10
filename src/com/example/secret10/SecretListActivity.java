package com.example.secret10;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SecretListActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
		loginScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			};
		});
	}

}
