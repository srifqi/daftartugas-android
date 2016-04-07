package srifqi.simetri.daftartugas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;

public class Tentang extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tentang);
	}
	
	public void openTOS(View view) {
		AlertDialog.Builder dlgb = new AlertDialog.Builder(this);
		dlgb.setTitle(R.string.tos);
		// Hard-coded FTW!
		dlgb.setMessage(
"Syarat dan Ketentuan Aplikasi Daftar Tugas\n"+
"\n\n"+
"Simetri Creative Code Labs (yang selanjutnya disebut dengan \"pihak pertama\" atau \"kami\") memberikan Aplikasi Daftar Tugas (yang selanjutnya disebut dengan \"aplikasi ini\") kepada pengguna dari murid Simetri (yang selanjutnya disebut dengan \"pihak kedua\" atau \"Anda\") dengan ketentuan sebagai berikut.\n"+
"\n"+
"1. PENGGUNAAN\n"+
"	Kami menyediakan aplikasi ini secara gratis untuk Anda. Anda sepakat untuk tidak menyebarkan aplikasi ini dalam bentuk dan cara apapun ke orang lain. Hanya murid Simetri yang diperbolehkan memiliki aplikasi ini dengan ketentuan telah memiliki akun untuk aplikasi ini dengan mendaftarkannya lewat versi web.\n"+
"	Jika Anda menemukan iklan di aplikasi ini, maka iklan tersebut bukan berasal dari kami melainkan dari pihak ketiga (misalnya penyedia layanan internet).\n"+
"\n"+
"2. PENGOLAHAN DATA\n"+
"	Kami sangat berhati-hati dalam menyimpan data Anda. Kami tidak akan pernah memberikan data apapun milik Anda kepada pihak ketiga.\n"+
"	Kami dapat mengubah struktur data, mengubah isi data, maupun menghapus permanen data Anda tanpa pemberitahuan terlebih dahulu.\n"+
"\n"+
"3. GARANSI\n"+
"	Kami memberikan aplikasi ini kepada Anda dengan harapan akan berguna tetapi DENGAN TANPA GARANSI; bahkan tanpa menyiratkan garansi untuk MEMILIKI KEMAMPUAN UNTUK TUJUAN TERTENTU. Segala bentuk kerusakan merupakan resiko Anda. Kami mungkin melakukan kesalahan dalam membuat aplikasi ini dan berhak untuk meminta atau menerima segala bentuk laporan, keluhan, maupun aduan tetapi TANPA MEMILIKI KEWAJIBAN UNTUK MEMPERBAIKI PERANGKAT ANDA.\n"+
"\n"+
"4. PERUBAHAN\n"+
"	Syarat dan ketentuan ini dapat kami ubah sewaktu-waktu tanpa pemberitahuan terlebih dahulu.\n"+
"\n\n"+
"Hak Cipta Muhammad Rifqi Priyo Susanto dkk. Simetri Creative Code Labs"
		);
		dlgb.setCancelable(true);
		dlgb.setNeutralButton("Tutup", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing.
			}
		});
		
		AlertDialog dlg = dlgb.create();
		dlg.show();
	}
}
