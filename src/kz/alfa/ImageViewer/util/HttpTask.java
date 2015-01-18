package kz.alfa.ImageViewer.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class HttpTask extends AsyncTask<String, Void, String> {
	private String TAG = "HttpTask ";
	public String data = "OK";
	public URL page_url;
	public String dirjpg;
	public static int taskStarted = 0;
	private boolean hasPageUrl = false;

	protected void scan_html(String data) {
		int start = 1;
		int pos = data.indexOf("<a ", start);
		while (pos > 1) {
			int end = data.indexOf(">", pos + 1);
			try {
				int src = data.indexOf("href=\"", pos + 1);
				if (src > 1) {
					int srcend = data.indexOf("\"", src + 8);
					if (srcend > 1) {
						String url = data.substring(src + 6, srcend);
						Log.v(TAG + "scan_html" + src, "start scan " + url);
						if (hasPageUrl && (url.indexOf("//") < 0)) {
							if (url.indexOf("/") > 0)
								url = "http://" + page_url.getHost() + "/"
										+ url;
							else if (url.indexOf("/") == 0)
								url = "http://" + page_url.getHost() + url;
							else if (url.indexOf("/") < 0)
								url = "http://" + page_url.getHost()
										+ page_url.getPath() + url;
						}
						Log.d(TAG + src, url);
						if ((url.indexOf("http://") == 0)) {
							// Log.d(TAG, url);
							try {
								URL href_url = new URL(url);
								// Log.d(TAG,
								// "href_url.toString() = "+href_url.toString());
								// Log.d(TAG,
								// "page_url.getHost() = "+page_url.getHost());
								// только с этого сайта нужны страницы
								if (href_url.toString().compareToIgnoreCase(
										page_url.toString()) != 0)
									if ((href_url.getHost()
											.compareToIgnoreCase(
													page_url.getHost()) == 0)
											|| (page_url.getHost().indexOf(
													"neolabshelp") >= 0)) {
										Log.d(TAG, "scan_html  add url = "
												+ url);
										ListUrl.addH(url, href_url.toString());
									}
							} catch (MalformedURLException e) {
								Log.e(TAG,
										"MalformedURLException url = "
												+ e.toString());
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e("HttpTask", "scan_html " + e.toString());
			}

			start = end;
			pos = data.indexOf("<a ", start);
		}
	}

	protected void scan_a_jpg(String data) {
		int start = 1;
		int pos = data.indexOf("<a ", start);
		Log.v(TAG + "scan_a_jpg" + pos, "start scan " + pos);

		while (pos > 1) {
			int end = data.indexOf(">", pos + 1);
			try {
				int src = data.indexOf("href=\"", pos + 1);
				if (src > 1) {
					int srcend = data.indexOf("\"", src + 8);
					if (srcend > 1) {
						String url = data.substring(src + 6, srcend);
						Log.d(TAG + "1 scan_a_jpg" + src, "start scan " + url
								+ " page_url.getHost() = " + page_url.getHost());

						if (hasPageUrl && (url.indexOf("//") < 0)) {
							if (url.indexOf("/") > 0)
								url = "http://" + page_url.getHost() + "/"
										+ url;
							else if (url.indexOf("/") == 0)
								url = "http://" + page_url.getHost() + url;
							else if (url.indexOf("/") < 0)
								url = "http://" + page_url.getHost()
										+ page_url.getPath() + url;
						}
						Log.d(TAG + "2 scan_a_jpg" + src, url);
						if ((url.indexOf("http://") == 0)
								&& (url.indexOf(".jpg") >= 5)) {
							Log.d(TAG, url);
							try {
								URL href_url = new URL(url);
								// Log.d(TAG,
								// "href_url.toString() = "+href_url.toString());
								// Log.d(TAG,
								// "page_url.getHost() = "+page_url.getHost());
								// только с этого сайта нужны страницы
								if (href_url.toString().compareToIgnoreCase(
										page_url.toString()) != 0)
									if ((href_url.getHost()
											.compareToIgnoreCase(
													page_url.getHost()) == 0)
											|| (page_url.getHost().indexOf(
													"neolabshelp") >= 0)) {
										Log.d(TAG, "scan_a_jpg  add url = "
												+ url);
										ListUrl.addJ(url, href_url.toString());
									}
							} catch (MalformedURLException e) {
								Log.e(TAG,
										"MalformedURLException url = "
												+ e.toString());
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e("HttpTask", "scan_a_jpg " + e.toString());
			}

			start = end;
			pos = data.indexOf("<a ", start);
		}
	}

	public static void scan(String data, String ext, URL page_url) {
		// scan for jpg
		int start = 1;
		int posJ = data.indexOf("." + ext + "&quot;", start);
		while (posJ > 1) {
			try {
				String tryStr = data.substring(posJ - 215, posJ + ext.length()
						+ 1);
				int posH = tryStr.lastIndexOf("&quot;http://");
				if (posH > 1) {
					String url = tryStr.substring(posH + 6);
					Log.v("HttpTask " + posJ, url);
					try {
						URL testUrl = new URL(url);
						ListUrl.addJ(testUrl.toString(), page_url.toString());
					} catch (MalformedURLException e) {
						Log.w("HttpTask " + posJ, "MalformedURLException "
								+ url);
					}
				}
			} catch (Exception e) {
				Log.e("HttpTask", "scan_html " + e.toString());
			}
			start = posJ + 1; // ?????
			posJ = data.indexOf("." + ext + "&quot;", start);
		}
	}

	public static void scan_jpg(String data, URL page_url) {
		int start = 1;
		int pos = data.indexOf("<img", start);
		while (pos > 1) {
			int end = data.indexOf(">", pos + 1);
			Log.v("HttpTask " + pos, data.substring(pos, end));
			try {
				int src = data.indexOf("src=", pos + 1);
				if (src > 1) {
					int srcend = data.indexOf("\"", src + 5);
					if (srcend > 1) {
						String url = data.substring(src + 5, srcend);
						Log.v("HttpTask " + src, url);
						if (url.indexOf("/") == 0) {
							url = "http://" + page_url.getHost() + url;
							Log.v("HttpTask " + src, url);
							try {
								URL testUrl = new URL(url);
								ListUrl.addJ(testUrl.toString(),
										page_url.toString());
								Log.d("HttpTask scan jpg add url = ", url);
							} catch (MalformedURLException e) {
								Log.w("HttpTask " + pos,
										"MalformedURLException " + url);
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e("HttpTask", "scan_jpg " + e.toString());
			}
			start = end;
			pos = data.indexOf("<img", start);
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String doInBackground(String... params) {
		taskStarted = taskStarted + 1;
		try {
			dirjpg = params[2];
			try {
				page_url = new URL(params[0]);
				hasPageUrl = true;
				if (params[1] == "JPG") {
					(new HttpClient()).getImage(params[0], dirjpg);
				} else {
					data = (new HttpClient()).getData(params[0]);
					scan(data, "jpg", page_url);
					scan(data, "jpeg", page_url);
					scan_jpg(data, page_url);
					scan_a_jpg(data);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				Log.e("HttpTask", e.toString());
				data = e.toString();
			}
			// download images
			List<String> list = ListUrl.get_list(10, "J");
			for (int i = 0; (i < list.size()); i++)
				try {
					Log.d("HttpTask J " + i, "read " + list.get(i));
					(new HttpClient()).getImage(list.get(i), dirjpg);
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e("HttpTask J", e.toString());
				}
			// сканировать все ссылки на страницы
			try {
				scan_html(data);
				scan_a_jpg(data);
				list = ListUrl.get_list(10, "H");
				for (int i = 0; (i < list.size()); i++) {
					Log.e("HttpTask", " status for "
							+ " LoadJTask.taskStarted = "
							+ LoadJTask.taskStarted
							+ " HttpTask.taskStarted   = "
							+ HttpTask.taskStarted
							+ " PlayTask.taskStarted   = "
							+ PlayTask.taskStarted);
					data = (new HttpClient()).getData(list.get(i));
					Log.v("HttpTask " + i, "read " + list.get(i));
					scan_html(data);
					scan_a_jpg(data);
					scan(data, "jpg", new URL(list.get(i)));
					scan(data, "jpeg", new URL(list.get(i)));
					scan_jpg(data, new URL(list.get(i)));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				Log.e(TAG, "page_url=" + params[1]);
			}
		} finally {
			taskStarted = taskStarted - 1;
		}
		return "OK";
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}
