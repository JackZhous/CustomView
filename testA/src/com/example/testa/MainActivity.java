package com.example.testa;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {
	
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
	}

	public void heartClick(View v){
		Toast.makeText(this, "heart was on clicked...", Toast.LENGTH_SHORT).show();
	}
	
}
