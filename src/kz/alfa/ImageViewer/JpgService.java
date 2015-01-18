package kz.alfa.ImageViewer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import kz.alfa.ImageViewer.util.HttpClient;
import kz.alfa.ImageViewer.util.HttpTask;
import kz.alfa.ImageViewer.util.ListUrl;
import kz.alfa.ImageViewer.util.LoadJTask;
import kz.alfa.ImageViewer.util.Net;
import kz.alfa.ImageViewer.util.PlayTask;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class JpgService extends Service {

	final String LOG_TAG = "JpgService";

	public void onCreate() {
		super.onCreate();
		Log.e(LOG_TAG, "onCreate");
		startService(new Intent(this, JpgService.class));
	}

	@SuppressWarnings("deprecation")
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(LOG_TAG, "onStartCommand TrackingService");
		if (true) {

			int icon = kz.alfa.ImageViewer.R.drawable.ic_launcher; // not
																	// visible
																	// if 0
																	// //ru.vmordo.earflap.R.drawable.ic_plusone_tall_off_client;//.ic_launcher;
			long when = System.currentTimeMillis();
			Context context = getBaseContext();
			Notification notification = new android.app.Notification(icon, "",
					when);
			// —оздание намерени€ с указанием класса вашей Activity, которую
			// хотите
			// вызвать при нажатии на оповещение.
			Intent notificationIntent = new Intent(this,
					FullscreenActivity.class);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			String txt = "Start Image Viewer";
			notification.setLatestEventInfo(context, "", txt, contentIntent);
			startForeground(1, notification);
			someJpgTask();
			someHtmlTask();
			// work();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
	}

	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return null;
	}

	void someJpgTask() {
		if (LoadJTask.taskStarted < 2) {
			LoadJTask taskJ = new LoadJTask();
			taskJ.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					new String[] { FullscreenActivity.BASE_URL, "HTML",
							FullscreenActivity.dirjpg });
		}
	}

	void someHtmlTask() {
		if (HttpTask.taskStarted < 7) {
			HttpTask task = new HttpTask();
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					new String[] { FullscreenActivity.BASE_URL, "HTML",
							FullscreenActivity.dirjpg });
			Log.e(LOG_TAG, "executeOnExecutor " + HttpTask.taskStarted);
		}
	}

	void work() {
		new Thread(new Runnable() {
			public void run() {
				Log.e(LOG_TAG, " someTask ");
				{
					boolean bWifi = FullscreenActivity.prefs.getBoolean(
							"chb_wifi", false);
					if (Net.isOnline(getApplicationContext())
							&& (((Net.getInetType(getApplicationContext()) == "WIFI")) || (!bWifi))) {
						List<String> list = ListUrl.get_list(10, "J");
						Log.i(LOG_TAG, "list.size() " + list.size());
						try {
							TimeUnit.SECONDS.sleep(1);
						} catch (InterruptedException e) {
							Log.e(LOG_TAG, e.toString());
							e.printStackTrace();
						}
						for (int i = 0; (i < list.size()); i++)
							try {
								Log.d(LOG_TAG,
										" list.get(" + i + ") = " + list.get(i));
								(new HttpClient()).getImage(list.get(i),
										FullscreenActivity.dirjpg);
								Log.i(LOG_TAG, " status for "
										+ " LoadJTask.taskStarted = "
										+ LoadJTask.taskStarted
										+ " HttpTask.taskStarted   = "
										+ HttpTask.taskStarted
										+ " PlayTask.taskStarted   = "
										+ PlayTask.taskStarted);

							} catch (Throwable e) {
								e.printStackTrace();
								Log.e(LOG_TAG, e.toString());
							}
					}
				}
			}
		}).start();
	}

}
