package srifqi.simetri.daftartugas;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DaftarTugas extends AppCompatActivity {

	public final static String FETCHURL = "http://srifqi.tk/assets/xipa3/daftar_tugas";
	// public final static String FETCHURL = "http://192.168.x.y/daftar_tugas";
	public final static int VERSION_CODE = 13;
	private ProgressDialog pd;
	private TextView textAmbilData;
	private SwipeRefreshLayout swipeContainer;
	private TextView TemaTextView;
	private TextView PengumumanTextView;
	private TextView DaftarTugasHeader;
	private LinearLayout DaftarTugasLinearLayout;
	private TextView DaftarTugasInfo;
	private Button PengumumanSembunyiButton;
	private int DONE = 4;
	private boolean OPENUpdateActivity = true;
	private String[] USERPASS;
	private String TOKEN;
	private JSONObject reader;
	private JSONObject L = new JSONObject();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);
		
		toolbar1.setBackgroundColor(0xFF9C27B0);
		toolbar1.setTitleTextColor(0xFFFFFFFF);
		
		textAmbilData = (TextView) findViewById(R.id.textAmbilData);
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
		TemaTextView = (TextView) findViewById(R.id.welcomeTextView);
		PengumumanTextView = (TextView) findViewById(R.id.PengumumanTextView);
		DaftarTugasHeader = (TextView) findViewById(R.id.DaftarTugasHeader);
		DaftarTugasLinearLayout = (LinearLayout) findViewById(R.id.DaftarTugasLinearLayout);
		DaftarTugasInfo = (TextView) findViewById(R.id.DaftarTugasInfo);
		
		PengumumanSembunyiButton = (Button) findViewById(R.id.btn_pengumuman_buka);
		
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
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void renderDaftarTugas(String data) {
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
			
			String[] fetch = teks[1].split("\n;;;\n");
			if (fetch.length < 2) return;
			
			//////////
			// Tema //
			//////////
			String teksTema =
				getResources().getString(R.string.welcome_text) +
				" " + USERPASS[0] + "!\n" + fetch[0];
			
			TemaTextView.setText(teksTema);
			
			////////////////
			// Pengumuman //
			////////////////
			String teksPengumuman = "● "+
					fetch[1].replaceAll("\n","<br>● ");
			
			PengumumanTextView.setText(Html.fromHtml(teksPengumuman));
			
			//////////////////
			// Daftar Tugas //
			//////////////////
			String teksDaftarTugas = (String) fetch[2].replaceAll("</?([^>])*>", "");
			String[] teksPerTugas = teksDaftarTugas.split("\n");
			
			// Delete already-exist Views from Layout.
			DaftarTugasLinearLayout.removeAllViewsInLayout();
			
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
			
			// Group all task based on the day.
			String[] hari = {"Minggu", "Senin", "Selasa", "Rabu",
					"Kamis", "Jumat", "Sabtu", "Lain"};
			List<List<String[]>> tugasPerHari = new ArrayList<List<String[]>>();
			for (int i = 0; i < hari.length; i ++)
				tugasPerHari.add(new ArrayList<String[]>());
			
			for (int i = 0; i < teksPerTugas.length; i ++) {
				String tugas1 = teksPerTugas[i] + ";0;";
				String[] dataPerTugas = tugas1.split(";", -1);
				try {
					boolean done = L.getJSONArray(dataPerTugas[0]).getBoolean(0);
					dataPerTugas[5] = done ? "1" : "0";
					// Extra text for each task.
					String extraText = L.getJSONArray(dataPerTugas[0]).getString(1);
					dataPerTugas[6] = extraText;
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
				int harike = Integer.parseInt(dataPerTugas[4]);
				tugasPerHari.get(harike).add(dataPerTugas);
			}
			
			// Day of the week.
			Calendar calendar = Calendar.getInstance();
			int dow = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Monday = 1 -> 0
			
			// Sort for each day using Insertion sort.
			// Then, add into the Layout.
			int d = 0;
			for (int day = 0; day < tugasPerHari.size(); day ++) {
				int da = day;
				if (day < 7) da = (day + dow) % 7;
				List<String[]> tugasSatuHari = tugasPerHari.get(da);
				
				String[] temp;
				for (int i = 1; i < tugasSatuHari.size(); i ++) {
					for (int j = i; j > 0; j --) {
						if (
							Integer.parseInt(tugasSatuHari.get(j  )[5]) <
							Integer.parseInt(tugasSatuHari.get(j-1)[5])
						) {
							temp = tugasSatuHari.get(j);
							tugasSatuHari.set(j, tugasSatuHari.get(j-1));
							tugasSatuHari.set(j-1, temp);
						} else if (
							tugasSatuHari.get(j)[5].compareTo(tugasSatuHari.get(j-1)[5]) == 0
						) {
							if (
								Arrays.asList(schedule[da]).indexOf(tugasSatuHari.get(j  )[3]) <
								Arrays.asList(schedule[da]).indexOf(tugasSatuHari.get(j-1)[3])
							) {
								temp = tugasSatuHari.get(j);
								tugasSatuHari.set(j, tugasSatuHari.get(j-1));
								tugasSatuHari.set(j-1, temp);
							} else if (
									tugasSatuHari.get(j)[3].compareTo(tugasSatuHari.get(j-1)[3]) == 0
							) {
								if (
									Integer.parseInt(tugasSatuHari.get(j  )[0]) <
									Integer.parseInt(tugasSatuHari.get(j-1)[0])
								) {
									temp = tugasSatuHari.get(j);
									tugasSatuHari.set(j, tugasSatuHari.get(j-1));
									tugasSatuHari.set(j-1, temp);
								}
							}
						}
					}
				}
				
				// Add tasks to Layout.
				// Check for undone task.
				int undone = 0;
				for (int k = 0; k < tugasSatuHari.size(); k ++)
					if (tugasSatuHari.get(k)[5].compareTo("0") == 0)
						undone ++;
				// Header for each day.
				TextView dayTextView = new TextView(getApplicationContext());
				if (undone > 0) {
					dayTextView.setText(Html.fromHtml("<b>" + hari[da] + " (" + undone + ")</b>"));
				} else {
					dayTextView.setText(Html.fromHtml("<b>" + hari[da] + "</b>"));
				}
				dayTextView.setTextColor(0xFFE040FB);
				LinearLayout.LayoutParams paramd = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT
				);
				paramd.setMargins(0, 16, 0, 4);
				dayTextView.setLayoutParams(paramd);
				DaftarTugasLinearLayout.addView(dayTextView);
				// For each task in a day.
				for (int k = 0; k < tugasSatuHari.size(); k ++) {
					String[] dataPerTugas = tugasSatuHari.get(k);
					String teksCaption = dataPerTugas[1] +
							" (" + dataPerTugas[2]+")" +
							" (" + dataPerTugas[3]+")";
					CheckedTextView taskCB = new CheckedTextView(getApplicationContext());
					
					int id = Integer.parseInt(dataPerTugas[0]);
					
					// Id starts from ten thousand to prevent Id overlap.
					taskCB.setId(10000+id);
					taskCB.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							CheckedTextView ctv = (CheckedTextView) view;
							int t = ctv.getId()-10000;
							boolean checked = ctv.isChecked();
							JSONArray nI;
							try {
								try {
									nI = L.getJSONArray(""+t);
								} catch (JSONException e) {
									L.put(""+t, new JSONArray());
									nI = L.getJSONArray(""+t);
								}
								nI.put(0, !checked);
								L.put(""+t, nI);
								reader.put("L", L);
								saveDaftarTugas();
							} catch (JSONException e) {
								// e.printStackTrace();
							}
						}
						
					});
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT
					);
					params.setMargins(0, 0, 0, 4);
					taskCB.setLayoutParams(params);
					taskCB.setTextColor(0xFF000000); // It's black.
					
			        taskCB.setCheckMarkDrawable(R.drawable.btn_check); // Orange checkmark
					
			        boolean done = dataPerTugas[5].compareTo("1") == 0;
			        if (done == true) d ++;
					taskCB.setChecked(done);
					SpannableString teksCaptionR = new SpannableString(teksCaption);
					if (done) {
						teksCaptionR.setSpan(new StrikethroughSpan(), 0, teksCaption.length()-1, 0);
					}
					taskCB.setText(teksCaptionR);
					
					DaftarTugasLinearLayout.addView(taskCB);
				}
				if (tugasSatuHari.size() < 1) {
					TextView notaskTextView = new TextView(getApplicationContext());
					notaskTextView.setText(R.string.tiada_tugas);
					notaskTextView.setTextColor(0xFF000000);
					DaftarTugasLinearLayout.addView(notaskTextView);
				}
			}
			
			// Information about data.
			String[] Info = teks[0].split("\n");
			long time4 = Long.parseLong(Info[1]);
			long time5 = Long.parseLong(Info[2]);
			String rds4 = DaftarTugas.timestampToRelativeDateString(time4);
			String rds5 = DaftarTugas.timestampToRelativeDateString(time5);
			String infoT =	"Pembaruan daftar terakhir:\n\t" + rds4 + "\n" +
							"Sinkronasi terakhir:\n\t" + rds5;
			SpannableString infoTR = new SpannableString(infoT);
			infoTR.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, infoT.length(), 0);
			DaftarTugasInfo.setText(infoTR);
			
			// Show the progress.
			DaftarTugasHeader.setText(
				getResources().getString(R.string.daftar_tugas) +
				" - " + d + "/" + teksPerTugas.length +
				" (" + Integer.toString(Math.round(d*100/teksPerTugas.length)) +
				"%)"
			);
			
			textAmbilData.setVisibility(View.GONE);
			swipeContainer.setVisibility(View.VISIBLE);
			if (pd != null) pd.dismiss();
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
		dlt.run(FETCHURL + "/api/transaction", strUrlParam);
	}
	
	private void saveDaftarTugas(){
		String[] teks = IOFile.read(getApplicationContext(), "fetchdata.txt").split("\n;;;;;\n");
		teks[2] = reader.toString();
		String newteks = android.text.TextUtils.join("\n;;;;;\n", teks);
		
		IOFile.write(getApplicationContext(), "fetchdata.txt", newteks);
		renderDaftarTugas(newteks);
		
		swipeContainer.setRefreshing(true);
		refreshDaftarTugas();
	}
	
	private class DownloadDT extends DownloadTask {
		
		@Override
		public boolean onAfterExecute(String result) {
			DONE ++;
			renderDaftarTugas(result);
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
	
	public void hidePengumuman(View view) {
		if(PengumumanTextView.getVisibility()==View.VISIBLE) {
			PengumumanTextView.setVisibility(View.GONE);
			PengumumanSembunyiButton.setText(R.string.tampilkan);
		} else {
			PengumumanSembunyiButton.setText(R.string.sembunyikan);
			PengumumanTextView.setVisibility(View.VISIBLE);
		}
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
}