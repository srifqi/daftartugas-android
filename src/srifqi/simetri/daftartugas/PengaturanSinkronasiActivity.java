package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PengaturanSinkronasiActivity extends AppCompatActivity {

	private Resources rsc;

	private TextView SyncInfoTextView;
	private CheckBox AutoSyncCheckBox;
	private TimePicker AutoSyncTimePicker;

	@TargetApi(23)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		// Get resources.
		rsc = getResources();

		setContentView(R.layout.activity_pengaturan_sinkronasi);

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		SyncInfoTextView = (TextView) findViewById(R.id.SyncInfoTextView);
		AutoSyncCheckBox = (CheckBox) findViewById(R.id.AutoSyncCheckBox);
		AutoSyncTimePicker = (TimePicker) findViewById(R.id.AutoSyncTimePicker);

		AutoSyncTimePicker.setIs24HourView(true);
		AutoSyncTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				setAutoSyncTime(hourOfDay, minute);
			}
		});

		int autoSyncEnabled = Setting.getAsInt(getApplicationContext(), Setting.AUTO_SYNC);
		if (autoSyncEnabled == 1) {
			AutoSyncCheckBox.setChecked(true);
			AutoSyncTimePicker.setVisibility(View.VISIBLE);
		} else {
			AutoSyncCheckBox.setChecked(false);
		}

		int alarmTimeH = Setting.getAsInt(getApplicationContext(), Setting.AUTO_SYNC_TIME_HOUR);
		int alarmTimeM = Setting.getAsInt(getApplicationContext(), Setting.AUTO_SYNC_TIME_MINUTE);
		if (alarmTimeH != -1 && alarmTimeM != -1) {
			AutoSyncTimePicker.setCurrentHour(alarmTimeH);
			AutoSyncTimePicker.setCurrentMinute(alarmTimeM);
		}

		DaftarTugasObj DTO = new DaftarTugasObj(getApplicationContext());
		DTO.read();

		String[] Info = DTO.TeksMeta.split("\n");
		long time4 = Long.parseLong(Info[1]);
		// long time5 = Long.parseLong(Info[2]);
		String rds4 = DaftarTugas.timestampToRelativeDateString(time4);
		String rds5 = DaftarTugas.timestampToRelativeDateString(IOFile.mtime(getApplicationContext(), "fetchdata.txt")/1000);
		String infoT = rsc.getString(R.string.sync_info) + "\n\n" +
			rsc.getString(R.string.last_list_update) +
			":\n" + rds4 + "\n\n" +
			rsc.getString(R.string.last_sync) +
			":\n" + rds5;
		SyncInfoTextView.setText(infoT);
	}

	public void setAutoSync() {
		AlarmSyncReceiver.setAutoSync(getApplicationContext());
	}

	public void toogleAutoSync(View v) {
		CheckBox cb = (CheckBox) v;
		int visibility = cb.isChecked() ? View.VISIBLE : View.GONE;
		AutoSyncTimePicker.setVisibility(visibility);

		Setting.set(getApplicationContext(), Setting.AUTO_SYNC, cb.isChecked() ? 1 : 0);

		Setting.set(
			getApplicationContext(), Setting.AUTO_SYNC_TIME_HOUR,
			AutoSyncTimePicker.getCurrentHour()
		);
		Setting.set(
			getApplicationContext(), Setting.AUTO_SYNC_TIME_MINUTE,
			AutoSyncTimePicker.getCurrentMinute()
		);

		setAutoSync();
	}

	public void setAutoSyncTime(int hour, int minute) {
		Setting.set(getApplicationContext(), Setting.AUTO_SYNC_TIME_HOUR, hour);
		Setting.set(getApplicationContext(), Setting.AUTO_SYNC_TIME_MINUTE, minute);

		setAutoSync();
	}
}
