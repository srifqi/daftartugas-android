package srifqi.simetri.daftartugas;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DaftarTugas extends AppCompatActivity {

	public final static String FETCHURL = "http://srifqi.tk/assets/xipa3/daftar_tugas";
	// public final static String FETCHURL = "http://192.168.x.y/xi/daftar_tugas";
	public final static int VERSION_CODE = 14;
	
	private Display display;
	private float displayWidth;
	private float displayDensity;

	private ProgressDialog pd;
	private TextView textAmbilData;
	private SwipeRefreshLayout swipeContainer;

	private LinearLayout ContainerLinearLayout;
	private TugasListAdapter ListArrayAdapter;
	private ListView ListListView;
	private ScrollView ContentScrollView;
	private LinearLayout ContentLinearLayout;
	
	private TextView TaskTitle;
	private TextView TaskStatus;
	private TextView TaskDescription;
	private TextView TaskUserDescription;
	private Button TaskSaveUserDescription;

	private int DONE = 4;

	private boolean OPENUpdateActivity = true;

	private String[] USERPASS;
	private String TOKEN;

	private String TeksMeta;
	private String TeksTema;
	private String TeksPengumuman;
	private ArrayList<String[]> ObjDaftarTugas;
	private ArrayList<String[]> SortedDaftarTugas;
	private JSONObject reader;
	private JSONObject L = new JSONObject();
	private int lastOpened = -2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(_UEH);

		// Check version (init).
		CheckVersionDT cvt = new CheckVersionDT();

		// Read token.txt, if exists.
		TOKEN = IOFile.read(getApplicationContext(), "token.txt");

		cvt.setContext(getApplicationContext());
		cvt.setSaveFilename("version.txt");
		cvt.setMethod("POST");

		// Check version.
		cvt.run(FETCHURL + "/api/androidversion",
			TOKEN != "" ? "token=" + TOKEN : "");

		// Read session.txt.
		USERPASS = IOFile.read(getApplicationContext(), "userpass.txt").split("\n");
		if (USERPASS[0] == "") {
			runMasuk();
			return;
		}

		setContentView(R.layout.daftar_tugas);
		
		display = getWindowManager().getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		displayWidth = metrics.widthPixels;
		displayDensity = metrics.density;

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);

		toolbar1.setBackgroundColor(0xFF9C27B0);
		toolbar1.setTitleTextColor(0xFFFFFFFF);

		textAmbilData = (TextView) findViewById(R.id.textAmbilData);
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

		ContainerLinearLayout = (LinearLayout) findViewById(R.id.ContainerLinearLayout);
		ListListView = (ListView) findViewById(R.id.ListListView);
		ContentScrollView = (ScrollView) findViewById(R.id.ContentScrollView);
		ContentLinearLayout = (LinearLayout) findViewById(R.id.ContentLinearLayout);
		
		ListListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowYPos =
					(ListListView == null || ListListView.getChildCount() == 0) ?
						0 : ListListView.getChildAt(0).getTop();
				swipeContainer.setEnabled(
					(firstVisibleItem == 0 && topRowYPos >= 0 && displayWidth <= 600 && lastOpened == -2) || 
					(lastOpened != -2 && displayWidth <= 600) ||
					(firstVisibleItem == 0 && topRowYPos >= 0 && displayWidth > 600)
				);
			}
		});
		
		ObjDaftarTugas = new ArrayList<String[]>();
		ListArrayAdapter = new TugasListAdapter();
		ListListView.setAdapter(ListArrayAdapter);
		
		TaskTitle = (TextView) findViewById(R.id.TaskTitle);
		TaskStatus = (TextView) findViewById(R.id.TaskStatus);
		TaskDescription = (TextView) findViewById(R.id.TaskDescription);
		TaskUserDescription = (TextView) findViewById(R.id.TaskUserDescription);
		TaskSaveUserDescription = (Button) findViewById(R.id.TaskSaveUserDescription);

		swipeContainer.setColorSchemeResources(
			R.color.black,
			R.color.purple,
			R.color.orange
		);
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refreshDaftarTugas();
			}
		});
		swipeContainer.setRefreshing(true);

		pd = new ProgressDialog(DaftarTugas.this);

		if (IOFile.read(getApplicationContext(), "fetchdata.txt") == "") {
			if (pd != null) {
				pd.setTitle("Memulai");
				pd.setMessage(
					getResources().getString(R.string.ambil_data)
				);
				pd.setIndeterminate(true);
				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pd.setCancelable(false);
				pd.setCanceledOnTouchOutside(false);
				pd.show();
			}
		}
		refreshDaftarTugas();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Bugfix: Crash when dismiss dialog after activity destroyed.
		if (pd != null) pd.dismiss();
		pd = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Re-initiate variable.
		// It's okay to re-initiate because at onPause, pd already dismissed.
		if (pd == null) pd = new ProgressDialog(DaftarTugas.this);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (lastOpened != -2 && displayWidth <= 600) {
				openContent(-2);
				return true;
			}
		}
		
		super.onKeyUp(keyCode, event);
		return false;
	}

	public void openContent(int id) {
		// Show lists.
		if (id == -2) {
			// Just do nothing.
		// Shows Pengumuman.
		} else if (id == -1) {
			TaskTitle.setText(R.string.pengumuman);
			TaskStatus.setText("INFO");
			TaskDescription.setText(Html.fromHtml(
				TeksTema + "<br><br><br>" +
				TeksPengumuman
			));
			TaskUserDescription.setText("");
			TaskSaveUserDescription.setVisibility(View.GONE);
		} else {
			String[] tugas = ObjDaftarTugas.get(id);
			TaskTitle.setText(Html.fromHtml(tugas[1]));
			TaskStatus.setText(
				tugas[6] == "1" ? "SELESAI" : "BELUM SELESAI"
			);
			TaskDescription.setText(Html.fromHtml(tugas[2]));
			TaskUserDescription.setText(tugas[7]);
			TaskSaveUserDescription.setVisibility(View.VISIBLE);
		}
		
		// Reflow content.
		if (displayWidth <= 600 && lastOpened != id) {
			LinearLayout.LayoutParams lsv = new LinearLayout.LayoutParams(
				(int) displayWidth,
				LinearLayout.LayoutParams.MATCH_PARENT
			);
			ListListView.setLayoutParams(lsv);
			if (id == -2) {
				Animation ListListViewAnimation = new Animation() {
					
					@Override
					protected void applyTransformation(float interpolatedTime, Transformation t) {
						LinearLayout.LayoutParams lsv = (LayoutParams) ListListView.getLayoutParams();
						lsv.setMargins((int) (-displayWidth + displayWidth * interpolatedTime), 0, 0, 0);
						ListListView.setLayoutParams(lsv);
					}
				};
				ListListViewAnimation.setInterpolator(new FastOutSlowInInterpolator());
				ListListViewAnimation.setDuration(300);
				
				ListListView.startAnimation(ListListViewAnimation);
			} else {
				Animation ListListViewAnimation = new Animation() {
					
					@Override
					protected void applyTransformation(float interpolatedTime, Transformation t) {
						LinearLayout.LayoutParams lsv = (LayoutParams) ListListView.getLayoutParams();
						lsv.setMargins((int) (-displayWidth * interpolatedTime), 0, 0, 0);
						ListListView.setLayoutParams(lsv);
					}
				};
				ListListViewAnimation.setInterpolator(new FastOutSlowInInterpolator());
				ListListViewAnimation.setDuration(300);
				
				ListListView.startAnimation(ListListViewAnimation);
			}
			LinearLayout.LayoutParams csv = new LinearLayout.LayoutParams(
				(int) displayWidth,
				LinearLayout.LayoutParams.MATCH_PARENT
			);
			ContentScrollView.setLayoutParams(csv);
		}
		
		lastOpened = id;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void renderDaftarTugas() {
		// Reflow content.
		if (displayWidth > 600) {
			LinearLayout.LayoutParams lsv = new LinearLayout.LayoutParams(
				(int) (displayWidth * 0.4),
				LinearLayout.LayoutParams.MATCH_PARENT
			);
			ListListView.setLayoutParams(lsv);
			LinearLayout.LayoutParams csv = new LinearLayout.LayoutParams(
				(int) (displayWidth * 0.6),
				LinearLayout.LayoutParams.MATCH_PARENT
			);
			ContentScrollView.setLayoutParams(csv);
		}
		
		// Name of days.
		String[] hari = {"Minggu", "Senin", "Selasa", "Rabu",
				"Kamis", "Jumat", "Sabtu"};

		// Name of months.
		String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
				"Agu", "Sep", "Okt", "Nov", "Des"};

		// Delete already-exist Views from Layout.
		ListArrayAdapter.clear();

		// Add Pengumuman at the first line.
		LinearLayout PengumumanLinearLayout = new LinearLayout(getApplicationContext());
		ListView.LayoutParams parampll = new ListView.LayoutParams(
			ListView.LayoutParams.MATCH_PARENT,
			(int) (72 * displayDensity)
		);
		PengumumanLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		PengumumanLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
		PengumumanLinearLayout.setLayoutParams(parampll);
		
		TextView PengumumanTextView = new TextView(getApplicationContext());
		PengumumanTextView.setText("Pengumuman");
		PengumumanTextView.setTextColor(0xFF000000);
		LinearLayout.LayoutParams paramptv = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		);
		paramptv.setMargins(
			(int) (80 * displayDensity), 0,
			(int) (16 * displayDensity), 0
		);
		PengumumanTextView.setLayoutParams(paramptv);

		PengumumanTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				openContent(-1);
			}
		});

		PengumumanLinearLayout.addView(PengumumanTextView);
		PengumumanLinearLayout.setClickable(true);
		
		ListArrayAdapter.addView(PengumumanLinearLayout, true);

		// Make a CheckedTextView and add into Layout.
		// ID;TASK;DESC;LESSON;TCODE;Y,M,D
		String last_day = "";
		for (int i = 0; i < SortedDaftarTugas.size(); i++) {
			String[] ti = SortedDaftarTugas.get(i);
			String tid = ti[5].trim();
			if (last_day.compareToIgnoreCase(tid) != 0) {
				last_day = tid;
				String[] ymd = ti[5].split(",");
				Calendar cal = Calendar.getInstance();
				cal.set(
					Integer.parseInt(ymd[0]),
					Integer.parseInt(ymd[1]) - 1,
					Integer.parseInt(ymd[2])
				);
				
				LinearLayout dayLL = new LinearLayout(getApplicationContext());
				ListView.LayoutParams paramll = new ListView.LayoutParams(
					ListView.LayoutParams.MATCH_PARENT,
					(int) (48 * displayDensity)
				);
				dayLL.setLayoutParams(paramll);
				dayLL.setOrientation(LinearLayout.HORIZONTAL);
				dayLL.setGravity(Gravity.BOTTOM + Gravity.START);

				TextView dayTextView = new TextView(getApplicationContext());
				dayTextView.setText(Html.fromHtml(
					"<b>" +
					hari[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", " +
					cal.get(Calendar.DATE) + " " +
					// mod 12 just in case somebody is stupid enough.
					bulan[cal.get(Calendar.MONTH) % 12] + " " +
					cal.get(Calendar.YEAR) +
					"</b>"
				));
				dayTextView.setTextColor(0xFF000000); // Black color.
				LinearLayout.LayoutParams paramd = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				);
				paramd.setMargins(
					(int) (16 * displayDensity), 0,
					(int) (16 * displayDensity), 0
				);
				dayTextView.setLayoutParams(paramd);
				
				dayLL.addView(dayTextView);
				ListArrayAdapter.addView(dayLL, false);
			}

			final int id = Integer.parseInt(ti[0]);

			LinearLayout taskLL = new LinearLayout(getApplicationContext());
			ListView.LayoutParams paramll = new ListView.LayoutParams(
				ListView.LayoutParams.MATCH_PARENT,
				(int) (72 * displayDensity)
			);
			taskLL.setLayoutParams(paramll);
			taskLL.setOrientation(LinearLayout.HORIZONTAL);
			taskLL.setGravity(Gravity.CENTER_VERTICAL);
			taskLL.setBackgroundResource(R.color.white);

			CheckBox cb = new CheckBox(getApplicationContext());
			LinearLayout.LayoutParams paramcb = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			);
			paramcb.setMargins((int) (16 * displayDensity), 0, 0, 0);
			cb.setLayoutParams(paramcb);

			cb.setTag(id);
			cb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					CheckBox cb = (CheckBox) view;
					updateTask((Integer) view.getTag(), cb.isChecked());
				}

			});

			boolean done = ti[6].compareTo("1") == 0;
			cb.setChecked(done);
			taskLL.addView(cb);

			TextView tv = new TextView(getApplicationContext());
			tv.setText(Html.fromHtml(ti[1] +
				"<br><i>" + ti[3] + " (" + ti[4] + ")</i>"
			));
			LinearLayout.LayoutParams paramtv = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			);
			paramtv.setMargins(
				(int) (16 * displayDensity), 0,
				(int) (16 * displayDensity), 0
			);
			tv.setLayoutParams(paramtv);
			tv.setTextColor(0xFF000000); // Black color.
			
			taskLL.addView(tv);

			taskLL.setTag(id);
			taskLL.setClickable(true);
			taskLL.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					openContent((Integer) view.getTag());
				}

			});

			ListArrayAdapter.addView(taskLL, true);
		}

		// Information about data.
		String[] Info = TeksMeta.split("\n");
		long time4 = Long.parseLong(Info[1]);
		long time5 = Long.parseLong(Info[2]);
		String rds4 = DaftarTugas.timestampToRelativeDateString(time4);
		String rds5 = DaftarTugas.timestampToRelativeDateString(time5);
		String infoT =	"Pembaruan daftar terakhir:\n\t" + rds4 + "\n" +
						"Sinkronasi terakhir:\n\t" + rds5;
		SpannableString infoTR = new SpannableString(infoT);
		infoTR.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, infoT.length(), 0);

		TextView SyncInfo = new TextView(getApplicationContext());
		SyncInfo.setText(infoTR);
		ListView.LayoutParams paramtv = new ListView.LayoutParams(
			ListView.LayoutParams.MATCH_PARENT,
			ListView.LayoutParams.WRAP_CONTENT
		);
		SyncInfo.setLayoutParams(paramtv);
		SyncInfo.setTextColor(0xFF000000); // Black color.
		ListArrayAdapter.addView(SyncInfo, false);
		
		ListListView.setAdapter(ListArrayAdapter);
		
		if (displayWidth > 600) {
			openContent(lastOpened == -2 ? -1 : lastOpened);
		} else {
			if (lastOpened != -2) {
				openContent(lastOpened);
			}
		}
	}

	private void parseDaftarTugas(String data) {
		if (data != "") {
			if (DONE == 2 && (data.compareTo("403") == 0 ||
				IOFile.read(getApplicationContext(), "token.txt") == "")) {
				// Invalid token.
				Toast.makeText(getApplicationContext(), "Silahkan masuk terlebih dahulu", Toast.LENGTH_SHORT).show();

				// Empty all personal files.
				IOFile.write(getApplicationContext(), "userpass.txt", "");
				IOFile.write(getApplicationContext(), "token.txt", "");
				IOFile.write(getApplicationContext(), "fetchdata.txt", "");

				runMasuk();
				return;
			}

			String[] teks = data.split("\n;;;;;\n");
			if (teks.length < 3) return;

			TeksMeta = teks[0];

			String[] fetch = teks[1].split("\n=;;;=\n");
			if (fetch.length < 2) return;

			//////////
			// Tema //
			//////////
			TeksTema =
				getResources().getString(R.string.welcome_text) +
				" " + USERPASS[0] + "!<br>" + fetch[0];

			////////////////
			// Pengumuman //
			////////////////
			TeksPengumuman = "● "+
					fetch[1].replaceAll("\n", "<br>● ");

			//////////////////
			// Daftar Tugas //
			//////////////////
			String teksDaftarTugas = (String) fetch[2].replaceAll("</?([^>])*>", "");
			String[] teksPerTugas = teksDaftarTugas.split("\n");

			// Parse JSON data.
			L = null;
			try {
				reader = new JSONObject(teks[2]);
				try {
					L = reader.getJSONObject("L");
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
			} catch (JSONException e) {
				// Bad value.
				// e.printStackTrace();
			}

			// School schedule.
			String[][] schedule = {
				{},
				{"76", "60", "70", "21"},
				{"63", "11", "28", "8",  "44"},
				{"36", "43", "32", "14", "45"},
				{"23", "8",  "70", "63", "28", "3"},
				{"16", "43", "32", "48"},
				{"50", "73", "23", "3",  "16"},
				{}
			};

			// ID;TASK;DESC;LESSON;TCODE;Y,M,D
			ArrayList<String[]> Tugas = new ArrayList<String[]>();
			for (int i = 0; i < teksPerTugas.length; i ++) {
				String tugas1 = teksPerTugas[i] + ";0;";
				String[] dataPerTugas = tugas1.split(";", -1);
				
				// Subtract 1 from month, January is 1.
				String[] date = dataPerTugas[5].split(",");
				date[1] = "" + (Integer.parseInt(date[1]) - 1);
				dataPerTugas[5] = date[0] + "," + date[1] + "," + date[2];
				
				try {
					boolean done = L.getJSONArray(dataPerTugas[0]).getBoolean(0);
					dataPerTugas[6] = done ? "1" : "0";
					// Extra text for each task.
					String extraText = L.getJSONArray(dataPerTugas[0]).getString(1);
					dataPerTugas[7] = extraText;
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
				Tugas.add(dataPerTugas);
			}
			
			// Clone unsorted.
			ObjDaftarTugas = new ArrayList<String[]>(Tugas);

			// Sort all task using Insertion sort.
			for (int i = 1; i < Tugas.size(); i ++) {
				String[] temp;
				for (int j = i; j > 0; j --) {
					String[] date1 = Tugas.get(j  )[5].split(",");
					String[] date2 = Tugas.get(j-1)[5].split(",");
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.set(
							Integer.parseInt(date1[0]),
							Integer.parseInt(date1[1]),
							Integer.parseInt(date1[2])
					);
					cal2.set(
							Integer.parseInt(date2[0]),
							Integer.parseInt(date2[1]),
							Integer.parseInt(date2[2])
					);
					if (
						cal1.getTimeInMillis() < cal2.getTimeInMillis()
					) {
						temp = Tugas.get(j);
						Tugas.set(j, Tugas.get(j-1));
						Tugas.set(j-1, temp);
					} else if (
						cal1.getTimeInMillis() == cal2.getTimeInMillis()
					) {
						if (
							Integer.parseInt(Tugas.get(j  )[6]) <
							Integer.parseInt(Tugas.get(j-1)[6])
						) {
							temp = Tugas.get(j);
							Tugas.set(j, Tugas.get(j-1));
							Tugas.set(j-1, temp);
						} else if (
								Tugas.get(j)[5].compareTo(Tugas.get(j-1)[5]) == 0
						) {
							int da1 = cal1.get(Calendar.DAY_OF_WEEK);
							int da2 = cal2.get(Calendar.DAY_OF_WEEK);
							if (
								Arrays.asList(schedule[da1]).indexOf(Tugas.get(j  )[3]) <
								Arrays.asList(schedule[da2]).indexOf(Tugas.get(j-1)[3])
							) {
								temp = Tugas.get(j);
								Tugas.set(j, Tugas.get(j-1));
								Tugas.set(j-1, temp);
							} else if (
									Tugas.get(j)[3].compareTo(Tugas.get(j-1)[3]) == 0
							) {
								if (
									Integer.parseInt(Tugas.get(j  )[0]) <
									Integer.parseInt(Tugas.get(j-1)[0])
								) {
									temp = Tugas.get(j);
									Tugas.set(j, Tugas.get(j-1));
									Tugas.set(j-1, temp);
								}
							}
						}
					}
				}
			}

			// Save sorted.
			SortedDaftarTugas = Tugas;

			textAmbilData.setVisibility(View.GONE);
			swipeContainer.setVisibility(View.VISIBLE);
			ContainerLinearLayout.setVisibility(View.VISIBLE);
			if (pd != null) pd.dismiss();

			renderDaftarTugas();
		}
	}

	private void refreshDaftarTugas() {
		if (DONE < 1) {
			return;
		}

		DONE = 0;

		DownloadDT dlt = new DownloadDT();

		dlt.setContext(getApplicationContext());
		dlt.setSaveFilename("fetchdata.txt");
		dlt.setMethod("POST");

		String strUrlParam = "";
		long lastSaved = (long) (IOFile.mtime(getApplicationContext(), "fetchdata.txt")/1e3);

		String[] teks = IOFile.read(getApplicationContext(), "fetchdata.txt")
						.split("\n;;;;;\n");
		if (teks.length == 3) {
			// Parse JSON data
			L = null;
			try {
				reader = new JSONObject(teks[2]);
				try {
					L = reader.getJSONObject("L");
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
			} catch (JSONException e) {
				// Bad value.
				// e.printStackTrace();
			}
		}

		try {
			strUrlParam = "token=" + TOKEN +
						 "&lastsaved=" + lastSaved +
						 "&dataL=" + URLEncoder.encode(L.toString(), "UTF-8");
			if (IOFile.read(getApplicationContext(), "fetchdata.txt").length() > 0) {
				strUrlParam += "&save=1";
			} else {
				strUrlParam += "&save=0";
			}
			strUrlParam += "&version=" + DaftarTugas.VERSION_CODE;
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		
		swipeContainer.setRefreshing(true);
		dlt.run(FETCHURL + "/api/transaction", strUrlParam);
	}
	
	private void updateTask(int id, boolean checked) {
		JSONArray nI;
		try {
			try {
				nI = L.getJSONArray(""+id);
			} catch (JSONException e) {
				L.put(""+id, new JSONArray());
				nI = L.getJSONArray(""+id);
			}
			nI.put(0, checked);
			L.put(""+id, nI);
			reader.put("L", L);
			saveDaftarTugas();
		} catch (JSONException e) {
			// e.printStackTrace();
		}
	}
	
	public void updateRecent(View view) {
		if (lastOpened > -1) {
			boolean enabled = ObjDaftarTugas.get(lastOpened)[6].compareTo("1") == 0;
			updateTask(
				lastOpened,
				!(enabled ? true : false)
			);
		}
	}

	private void saveDaftarTugas(){
		String[] teks = IOFile.read(getApplicationContext(), "fetchdata.txt").split("\n;;;;;\n");
		teks[2] = reader.toString();
		String newteks = android.text.TextUtils.join("\n;;;;;\n", teks);

		IOFile.write(getApplicationContext(), "fetchdata.txt", newteks);

		parseDaftarTugas(newteks);

		refreshDaftarTugas();
	}

	private class DownloadDT extends DownloadTask {

		@Override
		public boolean onAfterExecute(String result) {
			DONE ++;
			parseDaftarTugas(result);
			if (DONE == 2) {
				swipeContainer.setRefreshing(false);
			}
			return true;
		}

		@Override
		public boolean onNoConnection() {
			if(IOFile.read(getApplicationContext(), "fetchdata.txt") == "") {
				AlertDialog.Builder dlgb = new AlertDialog.Builder(DaftarTugas.this);
				dlgb.setMessage(R.string.tanpa_koneksi);

				dlgb.setPositiveButton("Coba lagi", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (pd != null) {
							pd.setTitle("Memulai");
							pd.setMessage(
								getResources().getString(R.string.ambil_data)
							);
							pd.setIndeterminate(true);
							pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							pd.setCancelable(false);
							pd.setCanceledOnTouchOutside(false);
							pd.show();
						}
						refreshDaftarTugas();
					}
				});

				dlgb.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DaftarTugas.this.finish();
						DaftarTugas.this.finishActivity(RESULT_OK);
					}
				});

				dlgb.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						DaftarTugas.this.finish();
						DaftarTugas.this.finishActivity(RESULT_OK);
					}

				});

				AlertDialog dlg = dlgb.create();
				dlg.show();

				if (pd != null) pd.dismiss();
			}
			return true;
		}
	}

	private class CheckVersionDT extends DownloadTask {

		@Override
		public boolean onAfterExecute(String result) {
			if (result == "") return false;
			if (!OPENUpdateActivity) return false;

			String[] version = result.split("\n");
			if (version.length > 2 && Integer.parseInt(version[2]) > DaftarTugas.VERSION_CODE) {
				runPerbarui();
				OPENUpdateActivity = false;
			} else {
				File dir = new File(
					Environment.getExternalStorageDirectory() +
					"/DaftarTugas"
				);
				dir.mkdirs();
				File file = new File(dir, "DaftarTugas.apk");

				if (file.exists()) file.delete();
			}
			return true;
		}
	}

	public class DelTokenT extends DownloadTask {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.daftar_tugas, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			swipeContainer.setRefreshing(true);
			refreshDaftarTugas();
			return true;
		} else if (id == R.id.action_about) {
			runTentang();
			return true;
		} else if (id == R.id.action_logout) {
			AlertDialog.Builder dlgb = new AlertDialog.Builder(this);
			dlgb.setMessage(R.string.tanya_keluar);

			dlgb.setPositiveButton("Ya", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(getApplicationContext(), "Sampai jumpa", Toast.LENGTH_SHORT).show();

					// Empty all personal files.
					IOFile.write(getApplicationContext(), "userpass.txt", "");
					IOFile.write(getApplicationContext(), "token.txt", "");
					IOFile.write(getApplicationContext(), "fetchdata.txt", "");

					DelTokenT dlt = new DelTokenT();
					dlt.setContext(getApplicationContext());
					dlt.setMethod("POST");
					dlt.dontSave();
					dlt.run(FETCHURL + "/api/deletetoken", "token=" + TOKEN);

					runMasuk();
				}
			});

			dlgb.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Nothing to do.
				}
			});

			AlertDialog dlg = dlgb.create();
			dlg.show();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * <p>Convert from timestamp into relative date string (Indonesian).</p>
	 * <p>
	 * For example:<br>
	 * - If the timestamp is today, then: "Hari ini, &lt;time&gt;"<br>
	 * - If the timestamp is yesterday, then: "Kemarin, &lt;time&gt;"<br>
	 * - Else: "&lt;date&gt; &lt;time&gt;"<br>
	 * </p>
	 *
	 * @param timestamp
	 *			Timestamp to be converted (in seconds)
	 *
	 * @return Converted timestamp
	 */
	public static String timestampToRelativeDateString(long timestamp) {
		String str = "";
		// GregorianCalendar object of now.
		GregorianCalendar gcn = new GregorianCalendar();
		int nowD = gcn.get(Calendar.DATE);
		int nowM = gcn.get(Calendar.MONTH);
		int nowY = gcn.get(Calendar.YEAR);

		// GregorianCalendar of yesterday.
		GregorianCalendar gcy = new GregorianCalendar();
		gcy.setTimeInMillis(gcn.getTimeInMillis()-86400000);
		int yD = gcy.get(Calendar.DATE);
		int yM = gcy.get(Calendar.MONTH);
		int yY = gcy.get(Calendar.YEAR);

		// GregorianCalendar of the timestamp.
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp*1000);
		int tD = gc.get(Calendar.DATE);
		int tM = gc.get(Calendar.MONTH);
		int tY = gc.get(Calendar.YEAR);

		// Months in Indonesian.
		String[] months = {
			"Januari",	"Februari",	"Maret",
			"April",	"Mei",		"Juni",
			"Juli",		"Agustus",	"September",
			"Oktober",	"November",	"Desember"
		};

		if (
			nowD == tD &&
			nowM == tM &&
			nowY == tY
		) {
			str += "Hari ini, ";
		} else if (
			yD == tD &&
			yM == tM &&
			yY == tY
		) {
			str += "Kemarin, ";
		} else {
			str += tD + " " + months[tM] + " " + tY + ", ";
		}
		String hour = "" + gc.get(Calendar.HOUR_OF_DAY);
		String minute = "" + gc.get(Calendar.MINUTE);
		if (hour.length() < 2) hour = "0" + hour;
		if (minute.length() < 2) minute = "0" + minute;
		str += hour + ":" + minute;

		return str;
	}

	public void runMasuk() {
		Intent intent = new Intent(this, MasukActivity.class);
		startActivity(intent);

		this.finish();
		this.finishActivity(RESULT_OK);
	}

	public void runPerbarui() {
		Intent intent = new Intent(this, PerbaruiActivity.class);
		startActivity(intent);

		this.finish();
		this.finishActivity(RESULT_OK);
	}

	public void runTentang() {
		Intent intent = new Intent(this, Tentang.class);
		startActivity(intent);
	}
	
	/**
	 * TugasListAdapter
	 * 
	 * An adapter extended from BaseAdapter that is specialized for
	 * DaftarTugas.
	 */
	public class TugasListAdapter extends BaseAdapter implements ListAdapter {
		
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
	
	// http://stackoverflow.com/a/19945692
	// http://stackoverflow.com/a/26560727
	// private UncaughtExceptionHandler defaultUEH;
	private Thread.UncaughtExceptionHandler _UEH = new Thread.UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			ex.printStackTrace();
			
			Intent intent = new Intent(getApplicationContext(), srifqi.simetri.daftartugas.ErrorReporting.class);
			intent.putExtra("Message", ex.getMessage());
			
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuilder stackTraceString = new StringBuilder();
			for (StackTraceElement el : stackTrace) {
				stackTraceString.append(el.toString()).append("\n");
			}
			intent.putExtra("StackTrace", stackTraceString.toString());
			
			startActivity(intent);
			
			/* Maybe not, it disturbs the UI.
			if (defaultUEH != null) {
				// Delegates to Andoid's error handling.
				defaultUEH.uncaughtException(thread, ex);
			} */

			System.exit(2); // Prevents app from freezing.
		}
		
	};
}
