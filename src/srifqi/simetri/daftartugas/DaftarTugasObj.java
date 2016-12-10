package srifqi.simetri.daftartugas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * Object container for Daftar Tugas.
 *
 * This helps DaftarTugas to be programmed easily.
 *
 * Make sure you run {@link read()} first before doing anything!
 */
public class DaftarTugasObj {

	private Context ctx;
	public String Teks;
	public String TeksMeta;
	public String TeksTema;
	public String TeksPengumuman;
	public ArrayList<String[]> ObjDaftarTugas;
	public ArrayList<String[]> SortedDaftarTugas;
	public JSONObject reader;
	public JSONObject L = new JSONObject();

	// School schedule.
	public String[][] schedule = { {}, { "76", "60", "70", "21" }, { "63", "11", "28", "8", "44" },
			{ "36", "43", "32", "14", "45" }, { "23", "8", "70", "63", "28", "3" }, { "16", "43", "32", "48" },
			{ "50", "73", "23", "3", "16" }, {} };

	DaftarTugasObj(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Read fetchdata.txt and parse as {@link DaftarTugasObj} (this).
	 *
	 * @return true on success, false on failure.
	 */
	public boolean read() {
		this.Teks = IOFile.read(this.ctx, "fetchdata.txt");
		return read(this.Teks);
	}

	/**
	 * Read a text and parse as {@link DaftarTugasObj} (this).
	 *
	 * @param data
	 *            The text to be parsed.
	 *
	 * @return true on success, false on failure.
	 */
	public boolean read(String data) {
		if (data.length() > 0) {
			this.Teks = data;

			String[] teks = data.split("\n\\|\\|\\|\\|\\|[\r\n]+");
			if (teks.length < 3)
				return false;

			this.TeksMeta = teks[0];

			String[] fetch = teks[1].split("\n`\\|\\|\\|`\n");
			if (fetch.length < 2)
				return false;

			//////////
			// Tema //
			//////////
			this.TeksTema = fetch[0];

			////////////////
			// Pengumuman //
			////////////////
			this.TeksPengumuman = "- " + fetch[1].replaceAll("\n", "<br>- ");

			//////////////////
			// Daftar Tugas //
			//////////////////
			String teksDaftarTugas = fetch[2];
			String[] teksPerTugas = teksDaftarTugas.split("\n");

			// Parse JSON data.
			this.L = null;
			try {
				this.reader = new JSONObject(teks[2]);
				try {
					this.L = this.reader.getJSONObject("L");
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
			} catch (JSONException e) {
				// Bad value.
				// e.printStackTrace();
			}

			// ID;TASK;DESC;LESSON;TCODE;Y,M,D
			ArrayList<String[]> Tugas = new ArrayList<String[]>();
			for (int i = 0; i < teksPerTugas.length; i++) {
				String tugas1 = teksPerTugas[i] + "|0|";
				String[] dataPerTugas = tugas1.split("\\|", -1);

				// Tolerate error.
				if (dataPerTugas.length < 8) {
					continue;
				}

				try {
					boolean done = this.L.getJSONArray(dataPerTugas[0]).getBoolean(0);
					dataPerTugas[6] = done ? "1" : "0";
					// Extra text for each task.
					String extraText = this.L.getJSONArray(dataPerTugas[0]).getString(1);
					dataPerTugas[7] = extraText;
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
				Tugas.add(dataPerTugas);
			}

			// Clone unsorted.
			this.ObjDaftarTugas = new ArrayList<String[]>(Tugas);

			// Sort all task using Insertion sort.
			for (int i = 1; i < Tugas.size(); i++) {
				String[] temp;
				for (int j = i; j > 0; j--) {
					String[] date1 = Tugas.get(j)[5].split(",");
					String[] date2 = Tugas.get(j - 1)[5].split(",");
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.set(Integer.parseInt(date1[0]), Integer.parseInt(date1[1]), Integer.parseInt(date1[2]));
					cal2.set(Integer.parseInt(date2[0]), Integer.parseInt(date2[1]), Integer.parseInt(date2[2]));
					if (cal1.getTimeInMillis() < cal2.getTimeInMillis()) {
						temp = Tugas.get(j);
						Tugas.set(j, Tugas.get(j - 1));
						Tugas.set(j - 1, temp);
					} else if (cal1.getTimeInMillis() == cal2.getTimeInMillis()) {
						if (Integer.parseInt(Tugas.get(j)[6]) < Integer.parseInt(Tugas.get(j - 1)[6])) {
							temp = Tugas.get(j);
							Tugas.set(j, Tugas.get(j - 1));
							Tugas.set(j - 1, temp);
						} else if (Tugas.get(j)[5].compareTo(Tugas.get(j - 1)[5]) == 0) {
							int da1 = cal1.get(Calendar.DAY_OF_WEEK);
							int da2 = cal2.get(Calendar.DAY_OF_WEEK);
							if (Arrays.asList(this.schedule[da1]).indexOf(Tugas.get(j)[3]) < Arrays
									.asList(this.schedule[da2]).indexOf(Tugas.get(j - 1)[3])) {
								temp = Tugas.get(j);
								Tugas.set(j, Tugas.get(j - 1));
								Tugas.set(j - 1, temp);
							} else if (Tugas.get(j)[3].compareTo(Tugas.get(j - 1)[3]) == 0) {
								if (Integer.parseInt(Tugas.get(j)[0]) < Integer.parseInt(Tugas.get(j - 1)[0])) {
									temp = Tugas.get(j);
									Tugas.set(j, Tugas.get(j - 1));
									Tugas.set(j - 1, temp);
								}
							}
						}
					}
				}
			}

			// Save sorted.
			this.SortedDaftarTugas = Tugas;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save into fetchdata.txt.
	 *
	 * @return true on success, false on failure.
	 */
	public boolean save() {
		String[] teks = android.text.TextUtils.split(this.Teks, "\n\\|\\|\\|\\|\\|\n");
		if (teks.length == 3) {
			teks[2] = reader.toString();
		}
		String newteks = android.text.TextUtils.join("\n|||||\n", teks);

		return IOFile.write(this.ctx, "fetchdata.txt", newteks);
	}

	/**
	 * Update task for it's done.
	 *
	 * @param id
	 *            ID number for task to be updated.
	 * @param checked
	 *            Status of the task.
	 *
	 * @return true on success, false on failure.
	 */
	public boolean updateTask(int id, boolean checked) {
		JSONArray nI;
		try {
			try {
				nI = this.L.getJSONArray("" + id);
			} catch (JSONException e) {
				this.L.put("" + id, new JSONArray());
				nI = this.L.getJSONArray("" + id);
			}
			nI.put(0, checked);
			this.L.put("" + id, nI);
			this.reader.put("L", this.L);
			return true;
		} catch (JSONException e) {
			// e.printStackTrace();
			return false;
		}
	}

	/**
	 * Update task for it's personal note.
	 *
	 * @param id
	 *            ID number for task to be updated.
	 * @param text
	 *            Text of the task's personal note.
	 *
	 * @return true on success, false on failure.
	 */
	public boolean updateTask(int id, String text) {
		JSONArray nI;
		try {
			try {
				nI = this.L.getJSONArray("" + id);
			} catch (JSONException e) {
				this.L.put("" + id, new JSONArray());
				nI = this.L.getJSONArray("" + id);
			}
			if (nI.isNull(0)) {
				nI.put(0, false);
			}
			nI.put(1, text);
			this.L.put("" + id, nI);
			this.reader.put("L", this.L);
			return true;
		} catch (JSONException e) {
			// e.printStackTrace();
			return false;
		}
	}
}
