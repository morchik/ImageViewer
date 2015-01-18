package kz.alfa.ImageViewer.util;

import java.util.List;

import kz.alfa.ImageViewer.FullscreenActivity;
import android.os.AsyncTask;
import android.util.Log;

// stub
public class LoadJTask extends AsyncTask<String, Integer, String> {

	public static int taskStarted = 0;

	@Override
	protected String doInBackground(String... params) {
		taskStarted = taskStarted + 1;
		try {
			// download images
			List<String> list = ListUrl.get_list(5000, "J");
			Log.i("LoadJTask", "list.size() " + list.size());
			for (int i = 0; (i < list.size()); i++)
				try {
					Log.d("LoadJTask" + i, "read " + list.get(i));
					Log.e("LoadJTask", " status for "
							+ " LoadJTask.taskStarted = "
							+ LoadJTask.taskStarted
							+ " HttpTask.taskStarted   = "
							+ HttpTask.taskStarted
							+ " PlayTask.taskStarted   = "
							+ PlayTask.taskStarted);
					(new HttpClient()).getImage(list.get(i), params[2]);
					publishProgress(i);
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("LoadJTask", e.toString());
				}
		} finally {
			taskStarted = taskStarted - 1;
		}
		return "";
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Log.i("LoadJTask", "onProgressUpdate values.length =" + values.length);
		FullscreenActivity.fsA.refreshView(FullscreenActivity.dirjpg);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}
