package srifqi.simetri.daftartugas;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		AlarmEnabledCheckBox = (CheckBox) findViewById(R.id.AlarmEnabledCheckBox);
		AlarmTimeTimePicker = (TimePicker) findViewById(R.id.AlarmTimeTimePicker);
		AlarmOnlyIfHasntDoneCheckBox = (CheckBox) findViewById(R.id.AlarmOnlyIfHasntDoneCheckBox);
		AlarmOnlyTomorrowCheckBox = (CheckBox) findViewById(R.id.AlarmOnlyTomorrowCheckBox);

		AlarmTimeTimePicker.setIs24HourView(true);
		AlarmTimeTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				setAlarmTime(hourOfDay, minute);
			}
		});

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
		AlarmTugasReceiver.setAlarm(getApplicationContext());
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

		Setting.set(getApplicationContext(), Setting.ALARM_ENABLED, cb.isChecked() ? 1 : 0);

		Setting.set(
			getApplicationContext(), Setting.ALARM_TIME_HOUR,
			AlarmTimeTimePicker.getCurrentHour()
		);
		Setting.set(
			getApplicationContext(), Setting.ALARM_TIME_MINUTE,
			AlarmTimeTimePicker.getCurrentMinute()
		);

		setAlarm();

		// Toast.makeText(this, Setting.writeAll(getApplicationContext()), Toast.LENGTH_LONG).show();
	}

	public void setAlarmTime(int hour, int minute) {
		Setting.set(getApplicationContext(), Setting.ALARM_TIME_HOUR, hour);
		Setting.set(getApplicationContext(), Setting.ALARM_TIME_MINUTE, minute);

		setAlarm();
	}

	public void toogleAlarmOnlyIfHasntDone(View v) {
		CheckBox cb = (CheckBox) v;
		int visibility = cb.isChecked() ? View.VISIBLE : View.GONE;
		AlarmOnlyTomorrowCheckBox.setVisibility(visibility);

		Setting.set(getApplicationContext(), Setting.ALARM_ONLY_IF_HASNT_DONE, cb.isChecked() ? 1 : 0);
	}

	public void toogleAlarmOnlyTomorrow(View v) {
		CheckBox cb = (CheckBox) v;

		Setting.set(getApplicationContext(), Setting.ALARM_ONLY_TOMORROW, cb.isChecked() ? 1 : 0);
	}
}
