package kz.alfa.ImageViewer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import kz.alfa.ImageViewer.util.HttpTask;
import kz.alfa.ImageViewer.util.JpgUtil;
import kz.alfa.ImageViewer.util.LoadJTask;
import kz.alfa.ImageViewer.util.Net;
import kz.alfa.ImageViewer.util.PlayTask;
import kz.alfa.ImageViewer.util.SystemUiHider;
import kz.alfa.ImageViewer.util.upLoader;
import kz.alfa.ImageViewer.R;
import android.R.color;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;
import kz.alfa.ImageViewer.util.Db_Helper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint({ "ClickableViewAccessibility", "InlinedApi" })
public class FullscreenActivity extends Activity {
	public static final String TAG = "FullscreenActivity";
	public static boolean needUpdate = false;
	public static String dirjpg = "/storage/external_SD/ImageViewer";
	public static String BASE_URL = "http://votrube.ru/";
	// "http://neolabshelp.16mb.com/a/";
	private int initScale;
	private String lastShowFileName = "";
	private PlayTask playTask;

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 4000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private ImageSwitcher imageSwitcher;
	private Button viewNum;
	private Button viewNum2;
	public static FullscreenActivity fsA;
	public static SharedPreferences prefs;
	public static boolean inFocus = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi = menu.add(0, 1, 0, R.string.settings);
		mi.setIntent(new Intent(this, PrefActivity.class));
		mi = menu.add(0, 2, 0, R.string.refresh);
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Log.e("onCreateOptionsMenu", "onCreateOptionsMenu");
				FullscreenActivity.fsA.viewDlg(null);
				return true;
			}
		});

		mi = menu.add(0, 3, 0, R.string.upload);
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Log.e("onCreateOptionsMenu", "upload 100 new");
				Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
				List<String> list = dbHelp.get_files_list(100);
				(new upLoader()).listTaskUpLoad(list);
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView(dirjpg);
		Log.i("FullscreenActivity", "onResume refreshView(dirjpg);");
		inFocus = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("FullscreenActivity", "MainActivity: onPause()");
		inFocus = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		fsA = this;
		final View controlsViewT = findViewById(R.id.fullscreen_content_controls_top);
		final View controlsViewB = findViewById(R.id.fullscreen_content_controls_bottom);
		final View controlsViewL = findViewById(R.id.fullscreen_content_controls_left);
		final View controlsViewR = findViewById(R.id.fullscreen_content_controls_right);
		final View contentView = findViewById(R.id.fullscreen_content);
		viewNum = (Button) findViewById(R.id.viewNum);
		viewNum2 = (Button) findViewById(R.id.viewNum2);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String urlAdr = prefs.getString("url_address", "");
		if (urlAdr != "")
			try {
				URL tUrl = new URL(urlAdr);
				BASE_URL = tUrl.toString();
			} catch (Exception e) {
				Log.e(TAG, "not exists url_address " + urlAdr);
				Toast.makeText(this, "not exists url_address: " + urlAdr,
						Toast.LENGTH_SHORT).show();
			}
		String sdir = prefs.getString("ldir_address", "");
		Log.d(TAG, "prefs.getString(ldir_address) = " + sdir);
		if (sdir.compareTo("") == 0)
			sdir = dirjpg; // настройки нет
		else
			Toast.makeText(this, sdir, Toast.LENGTH_LONG).show();			
		File sdPath = new File(sdir);
		Log.v(TAG, " 1 sdir = " + sdir);
		sdPath.mkdirs();
		Log.v(TAG, " sdPath.getAbsolutePath = " + sdPath.getAbsolutePath()
				+ " sdPath.getFreeSpace()= " + sdPath.getFreeSpace());
		if (!sdPath.exists()) {
			Log.e(TAG, "not exists " + sdir);
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Log.e(TAG,
						"SD-карта не доступна: "
								+ Environment.getExternalStorageState());
				
				//sdPath = Environment.getDownloadCacheDirectory();
				//sdir = sdPath.getAbsolutePath();
				sdir = getApplicationContext().getFilesDir().getPath();
			} else {
			sdPath = Environment.getExternalStorageDirectory();
			sdir = sdPath.getAbsolutePath() + "/ImageViewer";
			}
			Log.v(TAG, "2 sdir = " + sdir);
			sdPath = new File(sdir);
			sdPath.mkdirs();
			if (!sdPath.exists()) {
				dirjpg = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
			} else {
				dirjpg = sdir;
			}
		} else
			dirjpg = sdir;
		sdPath = new File(sdir);
		Log.v(TAG, " 3 sdir = " + sdir);
		Log.d(TAG, " sdPath.getAbsolutePath = " + sdPath.getAbsolutePath()
				+ " sdPath.getFreeSpace()= " + sdPath.getFreeSpace());
		Log.v(TAG, " dirjpg = " + dirjpg);
		Toast.makeText(this, dirjpg + " " + sdPath.getFreeSpace(),
				Toast.LENGTH_LONG).show();
		
		JpgUtil.saveRes(this, R.drawable.g1, dirjpg);
		JpgUtil.saveRes(this, R.drawable.g2, dirjpg);
		JpgUtil.saveRes(this, R.drawable.g3, dirjpg);

		// ispolzuet fsA
		playTask = new PlayTask();
		playTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		imageSwitcher = (ImageSwitcher) contentView;
		if (imageSwitcher != null) {
			imageSwitcher.setFactory(new ViewFactory() {
				public View makeView() {
					WebView webView = new WebView(getApplicationContext());
					webView.setBackgroundColor(color.black);
					webView.getSettings().setSupportZoom(true);
					webView.getSettings().setDisplayZoomControls(false);
					webView.getSettings().setBuiltInZoomControls(true);
					webView.setPadding(0, 0, 0, 0);
					webView.setScrollbarFadingEnabled(true);
					webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
					webView.setClickable(false);
					webView.getSettings().setLoadWithOverviewMode(true);
					webView.getSettings().setUseWideViewPort(true);
					return webView;
				}
			});
		}
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeightT, mControlsHeightB, mControlsHeightL,
							mControlsHeightR;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							// top
							if (mControlsHeightT == 0) {
								mControlsHeightT = controlsViewT.getHeight();
							}
							controlsViewT
									.animate()
									.translationY(
											visible ? 0 : -mControlsHeightT)
									.setDuration(mShortAnimTime);
							// bottom
							if (mControlsHeightB == 0) {
								mControlsHeightB = controlsViewB.getHeight();
							}
							controlsViewB
									.animate()
									.translationY(
											visible ? 0 : mControlsHeightB)
									.setDuration(mShortAnimTime);
							// left
							if (mControlsHeightL == 0) {
								mControlsHeightL = controlsViewL.getWidth();
							}
							controlsViewL
									.animate()
									.translationX(
											visible ? 0 : -mControlsHeightL)
									.setDuration(mShortAnimTime * 5);
							// rigth
							if (mControlsHeightR == 0) {
								mControlsHeightR = controlsViewR.getWidth();
							}
							controlsViewR
									.animate()
									.translationX(
											visible ? 0 : mControlsHeightR)
									.setDuration(mShortAnimTime * 5);
						} else {
							controlsViewT.setVisibility(visible ? View.VISIBLE
									: View.GONE);
							controlsViewB.setVisibility(visible ? View.VISIBLE
									: View.GONE);
							controlsViewL.setVisibility(visible ? View.VISIBLE
									: View.GONE);
							controlsViewR.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.e("FullscreenActivity", " onClick ");
				FullscreenActivity.fsA.viewDlg(null);
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
		setOnTouch();
		// play(btn_pay);
		// startService(new Intent(this, JpgService.class));
		someTask();
	}

	public void setOnTouch() {
		findViewById(R.id.btn_next).setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.btn_prev).setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.btn_next10).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.btn_prev10).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.viewNum).setOnTouchListener(mDelayHideTouchListener);

		findViewById(R.id.btn_next_right).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.btn_prev_left).setOnTouchListener(
				mDelayHideTouchListener);

		findViewById(R.id.btn2_next)
				.setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.btn2_prev)
				.setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.btn2_next10).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.btn2_prev10).setOnTouchListener(
				mDelayHideTouchListener);
		findViewById(R.id.viewNum2).setOnTouchListener(mDelayHideTouchListener);
	}

	public void initIView(int inScale) { // init imageView
		WebView webView = (WebView) imageSwitcher.getCurrentView();
		String data = "";
		String ffn = ""; // full file name to show
		for (int i = 10; i >= 0; i = i - 1) {
			ffn = List_files.getCurF(i);
			data = "<br/><br/><img align='middle' src='" + ffn + "' /> " + data;
		}
		Log.d("lastShowFileName=", lastShowFileName + "  ffn = " + ffn);
		viewNum.setText(List_files.getNText());
		viewNum2.setText(List_files.getNText());
		if ((inScale == 0) && (lastShowFileName != null)
				&& (lastShowFileName.compareTo(ffn) == 0))
			return;
		lastShowFileName = ffn;
		String bs = (new File(ffn)).getParent();
		webView.loadDataWithBaseURL(bs, data, "text/html", "utf-8", null);
		initScale(webView, inScale, ffn);
	}

	@SuppressWarnings("deprecation")
	public void initScale(WebView webView, int inScale, String ffn) {
		android.view.Display display = ((android.view.WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Bitmap img = BitmapFactory.decodeFile(ffn.substring(7)); // without
																	// file://
		if (img != null) {
			int picWidth = img.getWidth();
			Double valW = 1d;
			if (picWidth > width)
				valW = Double.valueOf(width) / Double.valueOf(picWidth);
			valW = valW * 100d;

			int picHeight = img.getHeight();
			Double val = 1d;
			if (picHeight > height)
				val = Double.valueOf(height) / Double.valueOf(picHeight);
			val = val * 100d;
			if (inScale == 0)
				initScale = Math.min(val.intValue(), valW.intValue());
			else
				initScale = inScale;
			webView.setInitialScale(initScale);
		}
	}

	public void next(View view) {
		Log.v("FullscreenActivity", " next view =" + view);
		if (List_files.getMax() == 0)
			return;
		Animation in = AnimationUtils.makeInAnimation(this, false);
		Animation out = AnimationUtils.makeOutAnimation(this, false);
		imageSwitcher.setInAnimation(in);
		imageSwitcher.setOutAnimation(out);
		List_files.getNext();
		initIView(0);
	}

	public void next10(View view) {
		if (List_files.getMax() == 0)
			return;
		Animation in = AnimationUtils.makeInAnimation(this, false);
		Animation out = AnimationUtils.makeOutAnimation(this, false);
		imageSwitcher.setInAnimation(in);
		imageSwitcher.setOutAnimation(out);
		for (int i = 0; i < 10; i++)
			List_files.getNext();
		initIView(0);
	}

	public void prev(View view) {
		prev(view, false);
	}

	public void prev(View view, boolean bShow) {
		if (List_files.getMax() == 0)
			return;
		Animation in = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_in_left);
		Animation out = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
		imageSwitcher.setInAnimation(in);
		imageSwitcher.setOutAnimation(out);
		List_files.getPrev();
		initIView(0);
		if (bShow)
			Toast.makeText(this, List_files.getCurF(0), Toast.LENGTH_SHORT)
					.show();
	}

	public void prev10(View view) {
		if (List_files.getMax() == 0)
			return;
		Animation in = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_in_left);
		Animation out = AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
		imageSwitcher.setInAnimation(in);
		imageSwitcher.setOutAnimation(out);
		for (int i = 0; i < 10; i++)
			List_files.getPrev();
		initIView(0);
	}

	public void viewDlg(View view) {
		refreshView(dirjpg);
	}

	public void zoom_p(View view) {
		float zkoef = 1.2f;
		initScale = Math.round(initScale * zkoef);
		initIView(initScale);
		Log.i("zoom_m", "+ " + initScale);
	}

	public void zoom_m(View view) {
		float zkoef = 0.8f;
		initScale = Math.round(initScale * zkoef);
		initIView(initScale);
		Log.i("zoom_m", "- " + initScale);
	}

	public void play(View view) {
		// не больше одного
		Log.v("play", "play 1  " + initScale);
		if (PlayTask.play == false) {
			Log.i("PlayTask.taskStarted", "start PlayTask.taskStarted = "
					+ PlayTask.taskStarted);
			view.setBackgroundColor(getResources().getColor(
					android.R.color.background_light));
			PlayTask.play = true;
		} else if (PlayTask.play == true) {
			Log.i("PlayTask.taskStarted", "stop PlayTask.taskStarted = "
					+ PlayTask.taskStarted);
			view.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			PlayTask.play = false;
		}
		Log.v("play", "play 2  " + initScale);
	}

	public void search(View view) {
		Log.i("search", "search - " + initScale);
		WebView webView = (WebView) imageSwitcher.getCurrentView();
		webView.loadUrl(List_files.getCurF(0));
	}

	public void refreshView(String folderPath) {
		List_files.getFromDB(this);
		if (List_files.getMax() == 0)
			List_files.getFromDir(this, folderPath);
		if (List_files.getMax() >= 1) {
			initIView(0);
		} else {
			viewNum.setText("0 / 0");
			viewNum2.setText("0 / 0");
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.// не делать меньше 500 а то не работает !!!!!!!
		delayedHide(1000);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	void someTask() { // число потоков ограниченно :(
		Timer myTimer = new Timer(); // Создаем таймер
		Log.e("someTask", " someTask ");
		myTimer.schedule(new TimerTask() { // Определяем задачу
					@Override
					public void run() {
						boolean bWifi = prefs.getBoolean("chb_wifi", false);
						if (Net.isOnline(getApplicationContext())
								&& (((Net.getInetType(getApplicationContext()) == "WIFI")) || (!bWifi))) {
							Log.i("someTask", " start for " + BASE_URL
									+ " LoadJTask.taskStarted = "
									+ LoadJTask.taskStarted
									+ " HttpTask.taskStarted   = "
									+ HttpTask.taskStarted
									+ " PlayTask.taskStarted   = "
									+ PlayTask.taskStarted);
							if (LoadJTask.taskStarted < 3) {
								LoadJTask taskJ = new LoadJTask();
								taskJ.executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										new String[] { BASE_URL, "HTML", dirjpg });
							}
							if (HttpTask.taskStarted < 1) {
								HttpTask task = new HttpTask();
								task.executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										new String[] { BASE_URL, "HTML", dirjpg });
							}
						}
					}
				}, 2L * 1000L, 60L * 1000L); // интервал
	}
}
