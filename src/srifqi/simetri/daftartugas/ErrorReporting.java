package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

public class ErrorReporting extends Activity {
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.error_reporting);
		
		String errMessage = getIntent().getStringExtra("Message");
		String errStack = getIntent().getStringExtra("StackTrace");
		
		EditText errmsg = (EditText) findViewById(R.id.ErrorLog);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			errmsg.setTextIsSelectable(true);
		errmsg.setKeyListener(null);
		errmsg.setText(errMessage + "\n-----\n" + errStack);
	}
}
