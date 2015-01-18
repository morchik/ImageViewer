package kz.alfa.ImageViewer.util;

import java.util.concurrent.TimeUnit;

import kz.alfa.ImageViewer.FullscreenActivity;
import android.os.AsyncTask;
import android.util.Log;

// показывать слайд шоу
public class PlayTask extends AsyncTask<String, Integer, Integer> {
	public static boolean play = false;
	public static int taskStarted = 0;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i("PlayTask", "onPreExecute  taskStarted = " + taskStarted);
	}

	@Override
	protected Integer doInBackground(String... params) {
		taskStarted = taskStarted + 1;
		int i = 0;
		Log.i("PlayTask", "doInBackground taskStarted =" + taskStarted);
		try {
			while (true) {
				try {
					i = i + 1;
					Log.e("PlayTask "+PlayTask.play, i+" status for "
							+ " LoadJTask.taskStarted = "
							+ LoadJTask.taskStarted
							+ " HttpTask.taskStarted   = "
							+ HttpTask.taskStarted
							+ " PlayTask.taskStarted   = "
							+ PlayTask.taskStarted);
					if (this.isCancelled()) {
						Log.e("PlayTask " + i,
								"doInBackground isCancelled i=  " + i);
						return i;
					}
					if (PlayTask.play) {
						publishProgress(i);
						Log.v("PlayTask " + i,
								" doInBackground publishProgress(i) i=  " + i);
					}
					String pp = FullscreenActivity.prefs.getString(
							"play_pause", "3");
					TimeUnit.SECONDS.sleep(1);
					for (int k = 1; k < Integer.valueOf(pp); ++k) {
						if (FullscreenActivity.needUpdate) 
							publishProgress(i);
						TimeUnit.SECONDS.sleep(1);
						//FullscreenActivity.fsA.startService(new Intent(FullscreenActivity.fsA, JpgService.class));
					}
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("LoadJTask " + i, e.toString());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Log.e("LoadJTask 2", e.toString());
		} finally {
			Log.i("PlayTask", "finally doInBackground taskStarted ="
					+ taskStarted);
			taskStarted = taskStarted - 1;
		}
		return 0;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (FullscreenActivity.inFocus) {
			if (PlayTask.play) {
				Log.v("PlayTask", PlayTask.play+" onProgressUpdate play values.length ="
						+ values.length + " values[0]= " + values[0]);
				FullscreenActivity.fsA.next(null);
			}
			if (FullscreenActivity.needUpdate) {
				Log.v("PlayTask", "onProgressUpdate needUpdate values.length ="
						+ values.length + " values[0]= " + values[0]);
				FullscreenActivity.fsA.initIView(0);
			}
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Log.i("PlayTask", result + " onPostExecute  taskStarted = "
				+ taskStarted);
	}
}
