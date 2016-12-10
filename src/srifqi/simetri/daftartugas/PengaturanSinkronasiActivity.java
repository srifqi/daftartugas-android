package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PengaturanSinkronasiActivity extends AppCompatActivity {

	private Context ctx;

	private Resources rsc;

	private TextView SyncInfoTextView;
	private CheckBox AutoSyncCheckBox;
	private TimePicker AutoSyncTimePicker;

	@TargetApi(23)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		ctx = this;

		// Get resources.
		rsc = getResources();

		setContentView(R.layout.activity_pengaturan_sinkronasi);

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

		toolbar1.setNavigationIcon(R.drawable.ic_close);
		toolbar1.setNavigationContentDescription(R.string.close);
		toolbar1.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder dlgb = new AlertDialog.Builder(ctx);
				dlgb.setMessage(R.string.ask_apply);

				dlgb.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setAutoSync();
					}
				});

				dlgb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						close();
					}
				});

				AlertDialog dlg = dlgb.create();
				dlg.show();
			}
		});

		SyncInfoTextView = (TextView) findViewById(R.id.SyncInfoTextView);
		AutoSyncCheckBox = (CheckBox) findViewById(R.id.AutoSyncCheckBox);
		AutoSyncTimePicker = (TimePicker) findViewById(R.id.AutoSyncTimePicker);

		AutoSyncTimePicker.setIs24HourView(true);

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
		String rds5 = DaftarTugas
				.timestampToRelativeDateString(IOFile.mtime(getApplicationContext(), "fetchdata.txt") / 1000);
		String infoT = rsc.getString(R.string.sync_info) + "\n\n" + rsc.getString(R.string.last_list_update) + ":\n"
				+ rds4 + "\n\n" + rsc.getString(R.string.last_sync) + ":\n" + rds5;
		SyncInfoTextView.setText(infoT);
	}

	public void setAutoSync() {
		Setting.set(getApplicationContext(), Setting.AUTO_SYNC, AutoSyncCheckBox.isChecked() ? 1 : 0);

		Setting.set(getApplicationContext(), Setting.AUTO_SYNC_TIME_HOUR, AutoSyncTimePicker.getCurrentHour());
		Setting.set(getApplicationContext(), Setting.AUTO_SYNC_TIME_MINUTE, AutoSyncTimePicker.getCurrentMinute());

		AlarmSyncReceiver.setAutoSync(getApplicationContext());

		this.finish();
		this.finishActivity(RESULT_OK);
	}

	public void toogleAutoSync(View v) {
		CheckBox cb = (CheckBox) v;
		int visibility = cb.isChecked() ? View.VISIBLE : View.GONE;
		AutoSyncTimePicker.setVisibility(visibility);
	}

	public void close() {
		this.finish();
		this.finishActivity(RESULT_OK);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pengaturan_sinkronasi, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_syncapply) {
			setAutoSync();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
