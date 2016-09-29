package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PengaturanActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pengaturan);
		
		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		
		toolbar1.setBackgroundColor(0xFF9C27B0);
		toolbar1.setTitleTextColor(0xFFFFFFFF);
	}
}
