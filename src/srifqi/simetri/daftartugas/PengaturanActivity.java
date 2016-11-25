package srifqi.simetri.daftartugas;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PengaturanActivity extends AppCompatActivity {

	private Resources rsc;

	private Display display;
	private float displayDensity;

	private String TOKEN;

	private ItemListAdapter ListArrayAdapter;
	private ListView PengaturanListView;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		// Read token.txt, if exists.
		TOKEN = IOFile.read(getApplicationContext(), "token.txt");

		// Get resources.
		rsc = getResources();

		setContentView(R.layout.activity_pengaturan);

		display = getWindowManager().getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		displayDensity = metrics.density;

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		PengaturanListView = (ListView) findViewById(R.id.PengaturanListView);

		ListArrayAdapter = new ItemListAdapter();
		PengaturanListView.setAdapter(ListArrayAdapter);

		// Manually add item to the list.
		String[] itemName = {
			rsc.getString(R.string.title_activity_pengaturan_alarm),
			rsc.getString(R.string.title_activity_pengaturan_sinkronasi),
			rsc.getString(R.string.title_activity_tentang),
			rsc.getString(R.string.action_logout)
		};
		OnClickListener[] itemCallback = {
			new OnClickListener() {

				@Override
				public void onClick(View v) {
					optionAlarm();
				}
			},
			new OnClickListener() {

				@Override
				public void onClick(View v) {
					optionSinkronasi();
				}
			},
			new OnClickListener() {

				@Override
				public void onClick(View v) {
					optionTentang();
				}
			},
			new OnClickListener() {

				@Override
				public void onClick(View v) {
					optionKeluar();
				}
			},
		};
		for (int i = 0; i < itemName.length; i ++) {
			LinearLayout itemLL = new LinearLayout(this);
			ListView.LayoutParams paramill = new ListView.LayoutParams(
				ListView.LayoutParams.MATCH_PARENT,
				(int) (68 * displayDensity)
			);
			itemLL.setOrientation(LinearLayout.VERTICAL);
			itemLL.setGravity(Gravity.CENTER_VERTICAL);
			itemLL.setLayoutParams(paramill);

			itemLL.setOnTouchListener(new OnTouchListener() {

				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return DaftarTugas.onListItemTouch(v, event);
				}
			});
			itemLL.setOnClickListener(itemCallback[i]);

			TextView itemTV = new TextView(this);
			itemTV.setText(itemName[i]);
			itemTV.setSingleLine();
			itemTV.setMaxLines(1);
			itemTV.setEllipsize(TruncateAt.END);
			itemTV.setTextColor(ContextCompat.getColor(this, R.color.blackPrimary));
			LinearLayout.LayoutParams paramitv = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			);
			paramitv.setMargins(
				(int) (16 * displayDensity), 0,
				(int) (16 * displayDensity), 0
			);
			itemTV.setLayoutParams(paramitv);
			itemTV.setTextSize(16); // In sp.

			itemLL.addView(itemTV);
			ListArrayAdapter.addView(itemLL, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check for DT.conf, has it empty because of log out.
		if (IOFile.read(getApplicationContext(), "DT.conf") == "") {
			runMasuk();
		}
	}

	public void optionAlarm() {
		Intent intent = new Intent(this, PengaturanAlarmActivity.class);
		startActivity(intent);
	}

	public void optionSinkronasi() {
		Intent intent = new Intent(this, PengaturanSinkronasiActivity.class);
		startActivity(intent);
	}

	public void optionTentang() {
		Intent intent = new Intent(this, Tentang.class);
		startActivity(intent);
	}

	public void optionKeluar() {
		AlertDialog.Builder dlgb = new AlertDialog.Builder(this);
		dlgb.setMessage(R.string.ask_logout);

		dlgb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), R.string.see_you, Toast.LENGTH_SHORT).show();

				// Cancel all alarms.
				Intent alarmIntent = new Intent(getApplicationContext(), AlarmSyncReceiver.class);
				PendingIntent alarmPIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_NO_CREATE
				);
				Intent syncIntent = new Intent(getApplicationContext(), AlarmSyncReceiver.class);
				PendingIntent syncPIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, syncIntent, PendingIntent.FLAG_NO_CREATE
				);
				AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
				if (alarmPIntent != null) {
					alarmMgr.cancel(alarmPIntent);
				}
				if (syncPIntent != null) {
					alarmMgr.cancel(syncPIntent);
				}

				DelTokenT dlt = new DelTokenT();
				dlt.setContext(getApplicationContext());
				dlt.setMethod("POST");
				dlt.dontSave();
				dlt.run(
					DaftarTugas.FETCHURL +
					Setting.get(getApplicationContext(), Setting.PROJECT_ID) +
					"/api/deletetoken",
					"token=" + TOKEN
				);

				// Empty all personal files.
				IOFile.write(getApplicationContext(), "userpass.txt", "");
				IOFile.write(getApplicationContext(), "token.txt", "");
				IOFile.write(getApplicationContext(), "fetchdata.txt", "");
				IOFile.write(getApplicationContext(), "DT.conf", "");

				runMasuk();
			}
		});

		dlgb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Nothing to do.
			}
		});

		AlertDialog dlg = dlgb.create();
		dlg.show();
	}

	/**
	 * ItemListAdapter
	 *
	 * An adapter extended from BaseAdapter that is specialized for
	 * item list.
	 */
	public class ItemListAdapter extends BaseAdapter implements ListAdapter {

		public ArrayList<View> Views = new ArrayList<View>();
		public ArrayList<Boolean> Enabled = new ArrayList<Boolean>();

		public void addView(View view, boolean enabled) {
			this.Views.add(view);
			this.Enabled.add(enabled);
		}

		public void clear() {
			this.Views.clear();
			this.Enabled.clear();
		}

		@Override
		public int getCount() {
			return this.Views.size();
		}

		@Override
		public View getItem(int position) {
			return this.Views.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return this.Views.get(position);
		}

		public boolean isEnabled(int position) {
			return this.Enabled.get(position);
		}
	}

	public class DelTokenT extends DownloadTask {

	}

	public void runMasuk() {
		Intent intent = new Intent(this, MasukActivity.class);
		startActivity(intent);

		this.finish();
		this.finishActivity(RESULT_OK);
	}
}
