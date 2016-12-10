package srifqi.simetri.daftartugas;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AlarmTugasActivity extends Activity {

	private Display display;
	private float displayDensity;

	private TextView TimeTextView;
	private ListView AlarmTugasListView;

	private TimerTask timeticker;

	private DaftarTugasObj DTO;
	private DaftarTugas.TugasListAdapter ListArrayAdapter;

	private AudioManager am;
	private OnAudioFocusChangeListener audioFocusChangeListener;
	private MediaPlayer mp;
	private boolean mpReleased = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		setContentView(R.layout.activity_alarm_tugas);

		display = getWindowManager().getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		displayDensity = metrics.density;

		TimeTextView = (TextView) findViewById(R.id.TimeTextView);
		AlarmTugasListView = (ListView) findViewById(R.id.AlarmTugasListView);

		timeticker = new TimerTask() {

			// Display correct time in TimeTextView.
			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Calendar c = new GregorianCalendar();
						TimeTextView.setText(DaftarTugas.formatNumber(c.get(Calendar.HOUR_OF_DAY), 2) + ":"
								+ DaftarTugas.formatNumber(c.get(Calendar.MINUTE), 2));
					}
				});
			}
		};

		Timer timetickertimer = new Timer();
		timetickertimer.schedule(timeticker, new Date(), 1000);

		DTO = new DaftarTugasObj(getApplicationContext());
		DTO.read();

		// Delete already-exist Views from Layout.
		ListArrayAdapter = new DaftarTugas.TugasListAdapter();

		// Remove divider between items. Because we want to build ourself.
		AlarmTugasListView.setDivider(null);

		// Make a TextView and add into Layout.
		// ID;TASK;DESC;LESSON;TCODE;Y,M,D
		String last_day = "";
		int task_num = 1;
		for (int i = 0; i < DTO.SortedDaftarTugas.size(); i++) {
			String[] ti = DTO.SortedDaftarTugas.get(i);
			// If the task already done, don't show it.
			if (ti[6].compareTo("0") == 1) {
				continue;
			}

			// Only shows 3 undone task.
			if (task_num++ > 3) {
				break;
			}

			String tid = ti[5].trim();
			if (last_day.compareToIgnoreCase(tid) != 0) {
				last_day = tid;
				String[] ymd = ti[5].split(",");
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]) - 1, Integer.parseInt(ymd[2]));

				View separatorView = new View(this);
				ListView.LayoutParams paramsw = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
						(int) (2 * displayDensity));
				separatorView.setLayoutParams(paramsw);
				separatorView.setBackgroundColor(ContextCompat.getColor(this, R.color.blackDivider));

				ListArrayAdapter.addView(separatorView, false);

				LinearLayout dayLL = new LinearLayout(this);
				ListView.LayoutParams paramll = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
						(int) (46 * displayDensity) // Already subtracted by 2dp
													// for divider.
				);
				dayLL.setLayoutParams(paramll);
				dayLL.setOrientation(LinearLayout.VERTICAL);
				dayLL.setGravity(Gravity.CENTER_VERTICAL);

				TextView dayTextView = new TextView(this);
				dayTextView.setText(Html.fromHtml("<b>" + DaftarTugas.days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", "
						+ cal.get(Calendar.DATE) + " " +
						// mod 12 just in case somebody is stupid enough.
						DaftarTugas.months[cal.get(Calendar.MONTH) % 12] + " " + cal.get(Calendar.YEAR) + "</b>"));
				dayTextView.setTextSize(14);
				dayTextView.setTextColor(ContextCompat.getColor(this, R.color.blackSecondary));
				LinearLayout.LayoutParams paramd = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				paramd.setMargins((int) (16 * displayDensity), 0, (int) (16 * displayDensity), 0);
				dayTextView.setLayoutParams(paramd);

				dayLL.addView(dayTextView);
				ListArrayAdapter.addView(dayLL, false);
			}

			LinearLayout taskLL = new LinearLayout(this);
			ListView.LayoutParams paramll = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,
					(int) (70 * displayDensity));
			taskLL.setLayoutParams(paramll);
			taskLL.setOrientation(LinearLayout.HORIZONTAL);
			taskLL.setGravity(Gravity.CENTER_VERTICAL);
			taskLL.setBackgroundResource(R.color.white);

			LinearLayout textLL = new LinearLayout(this);
			LinearLayout.LayoutParams paramtll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			paramtll.setMargins((int) (16 * displayDensity), 0, (int) (16 * displayDensity), 0);
			textLL.setLayoutParams(paramtll);
			textLL.setOrientation(LinearLayout.VERTICAL);
			textLL.setGravity(Gravity.START);

			TextView tv = new TextView(this);
			tv.setText(Html.fromHtml(ti[1]));
			tv.setSingleLine();
			tv.setMaxLines(1);
			tv.setEllipsize(TruncateAt.END);
			LinearLayout.LayoutParams paramtv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(paramtv);
			tv.setTextColor(ContextCompat.getColor(this, R.color.blackPrimary));
			tv.setTextSize(16); // In sp.

			textLL.addView(tv);

			TextView stv = new TextView(this);
			stv.setText(
					Html.fromHtml("<i>" + ti[3] + " (" + ti[4] + ")</i>" + (ti[2].length() < 1 ? "" : " | " + ti[2])));
			stv.setSingleLine();
			stv.setMaxLines(1);
			stv.setEllipsize(TruncateAt.END);
			LinearLayout.LayoutParams paramstv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			stv.setLayoutParams(paramstv);
			stv.setTextColor(ContextCompat.getColor(this, R.color.blackSecondary));
			stv.setTextSize(14); // In sp.

			textLL.addView(stv);

			taskLL.addView(textLL);

			ListArrayAdapter.addView(taskLL, false);
		}

		AlarmTugasListView.setAdapter(ListArrayAdapter);

		// http://stackoverflow.com/a/20177743
		Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mp = new MediaPlayer();
		try {
			mp.setDataSource(getApplicationContext(), ringtone);
			mp.setAudioStreamType(AudioManager.STREAM_ALARM);
			mp.prepare();
		} catch (IOException e) {
			return;
			// e.printStackTrace();
		}
		mp.setLooping(true);
		mp.setScreenOnWhilePlaying(true);

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		// http://stackoverflow.com/a/16252044
		audioFocusChangeListener = new OnAudioFocusChangeListener() {

			@Override
			public void onAudioFocusChange(int focusChange) {
				switch (focusChange) {
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					mp.setVolume(0.1f, 0.1f);
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					mp.pause();
					break;

				case AudioManager.AUDIOFOCUS_LOSS:
					mp.stop();
					break;

				case AudioManager.AUDIOFOCUS_GAIN:
					mp.setVolume(1f, 1f);
					mp.start();
					break;

				default:
					break;
				}
			}
		};

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			mp.start();
		} else {
			mp.release();
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	public void onPause() {
		super.onPause();

		stopAlarm();
	}

	// Disable any key up event except Escape button.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			stopAlarm();

			this.finish();
			this.finishActivity(RESULT_OK);

			super.onKeyUp(keyCode, event);
			return false;
		} else {
			if (mpReleased == true) {
				super.onKeyUp(keyCode, event);
				return false;
			}

			stopAlarm();
			return true;
		}
	}

	// Disable any key down event.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	public void stopAlarm() {
		if (mpReleased == true)
			return;
		mp.stop();
		mp.release();
		mpReleased = true;

		am.abandonAudioFocus(audioFocusChangeListener);
	}

	public void stopAlarm(View v) {
		stopAlarm();

		this.finish();
		this.finishActivity(RESULT_OK);
	}
}
