package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TimePicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PengaturanAlarmActivity extends AppCompatActivity {

	private CheckBox AlarmEnabledCheckBox;
	private TimePicker AlarmTimeTimePicker;
	private CheckBox AlarmOnlyIfHasntDoneCheckBox;
	private CheckBox AlarmOnlyTomorrowCheckBox;

	@TargetApi(23)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		setContentView(R.layout.activity_pengaturan_alarm);

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

		toolbar1.setNavigationIcon(R.drawable.ic_close);
		toolbar1.setNavigationContentDescription(R.string.close);
		toolbar1.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				beforeClose();
			}
		});

		AlarmEnabledCheckBox = (CheckBox) findViewById(R.id.AlarmEnabledCheckBox);
		AlarmTimeTimePicker = (TimePicker) findViewById(R.id.AlarmTimeTimePicker);
		AlarmOnlyIfHasntDoneCheckBox = (CheckBox) findViewById(R.id.AlarmOnlyIfHasntDoneCheckBox);
		AlarmOnlyTomorrowCheckBox = (CheckBox) findViewById(R.id.AlarmOnlyTomorrowCheckBox);

		AlarmTimeTimePicker.setIs24HourView(true);

		int alarmEnabled = Setting.getAsInt(getApplicationContext(), Setting.ALARM_ENABLED);
		if (alarmEnabled == 1) {
			AlarmEnabledCheckBox.setChecked(true);
			AlarmTimeTimePicker.setVisibility(View.VISIBLE);
			AlarmOnlyIfHasntDoneCheckBox.setVisibility(View.VISIBLE);
		} else {
			AlarmEnabledCheckBox.setChecked(false);
		}

		int alarmTimeH = Setting.getAsInt(getApplicationContext(), Setting.ALARM_TIME_HOUR);
		int alarmTimeM = Setting.getAsInt(getApplicationContext(), Setting.ALARM_TIME_MINUTE);
		if (alarmTimeH != -1 && alarmTimeM != -1) {
			AlarmTimeTimePicker.setCurrentHour(alarmTimeH);
			AlarmTimeTimePicker.setCurrentMinute(alarmTimeM);
		}

		int alarmifhasntdone = Setting.getAsInt(
			getApplicationContext(),
			Setting.ALARM_ONLY_IF_HASNT_DONE
		);
		if (alarmifhasntdone == 1) {
			AlarmOnlyIfHasntDoneCheckBox.setChecked(true);
			AlarmOnlyTomorrowCheckBox.setVisibility(View.VISIBLE);
		}

		int alarmonlytomorrow = Setting.getAsInt(
			getApplicationContext(),
			Setting.ALARM_ONLY_TOMORROW
		);
		if (alarmonlytomorrow == 1) {
			AlarmOnlyTomorrowCheckBox.setChecked(true);
		} else {
			AlarmOnlyTomorrowCheckBox.setChecked(false);
		}
	}

	public void setAlarm() {
		Setting.set(
			getApplicationContext(), Setting.ALARM_ENABLED,
			AlarmEnabledCheckBox.isChecked() ? 1 : 0
		);

		Setting.set(
			getApplicationContext(), Setting.ALARM_TIME_HOUR,
			AlarmTimeTimePicker.getCurrentHour()
		);
		Setting.set(
			getApplicationContext(), Setting.ALARM_TIME_MINUTE,
			AlarmTimeTimePicker.getCurrentMinute()
		);

		Setting.set(
			getApplicationContext(), Setting.ALARM_ONLY_IF_HASNT_DONE,
			AlarmOnlyIfHasntDoneCheckBox.isChecked() ? 1 : 0
		);
		Setting.set(
			getApplicationContext(), Setting.ALARM_ONLY_TOMORROW,
			AlarmOnlyTomorrowCheckBox.isChecked() ? 1 : 0
		);

		AlarmTugasReceiver.setAlarm(getApplicationContext());

		this.finish();
		this.finishActivity(RESULT_OK);
	}

	public void toogleAlarm(View v) {
		CheckBox cb = (CheckBox) v;
		int visibility = cb.isChecked() ? View.VISIBLE : View.GONE;
		AlarmTimeTimePicker.setVisibility(visibility);
		AlarmOnlyIfHasntDoneCheckBox.setVisibility(visibility);

		if (cb.isChecked() && AlarmOnlyIfHasntDoneCheckBox.isChecked()) {
			AlarmOnlyTomorrowCheckBox.setVisibility(View.VISIBLE);
		} else {
			AlarmOnlyTomorrowCheckBox.setVisibility(View.GONE);
		}
	}

	public void toogleAlarmOnlyIfHasntDone(View v) {
		CheckBox cb = (CheckBox) v;
		int visibility = cb.isChecked() ? View.VISIBLE : View.GONE;
		AlarmOnlyTomorrowCheckBox.setVisibility(visibility);
	}

	public void beforeClose() {
		AlertDialog.Builder dlgb = new AlertDialog.Builder(this);
		dlgb.setMessage(R.string.ask_apply);

		dlgb.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setAlarm();
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

	public void close() {
		this.finish();
		this.finishActivity(RESULT_OK);
	}

	@Override
	public void onBackPressed() {
		beforeClose();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pengaturan_alarm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_alarmapply) {
			setAlarm();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
