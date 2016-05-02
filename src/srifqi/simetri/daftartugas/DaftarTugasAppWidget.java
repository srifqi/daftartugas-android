// TODO How to use this? (^_^")\

package srifqi.simetri.daftartugas;

import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class DaftarTugasAppWidget extends AppWidgetProvider {
	
	private String[] USERPASS;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		
		// Read session.txt.
		USERPASS = IOFile.read(context.getApplicationContext(), "userpass.txt").split("\n");
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.daftar_tugas_app_widget);
		
		Intent intent = new Intent(context, DaftarTugas.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.AW_LL, pendingIntent);
		
		if (USERPASS[0] == "") {
			views.setViewVisibility(R.id.AW_btn_refresh, View.GONE);
			views.setTextViewText(R.id.AW_text_intro, "Silahkan masuk untuk menggunakan.");
		} else {
			views.setViewVisibility(R.id.AW_btn_refresh, View.VISIBLE);
			//views.setTextViewText(R.id.AW_text_intro, context.getResources().getText(R.string.daftar_tugas_intro));
			GregorianCalendar gc = new GregorianCalendar();
			views.setTextViewText(R.id.AW_text_intro, ""+gc.getTimeInMillis());
		}
		// TODO fix this
		ComponentName projectWidget = new ComponentName(context, DaftarTugasAppWidget.class);
		appWidgetManager.updateAppWidget(projectWidget, views);
	}
}