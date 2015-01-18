package kz.alfa.ImageViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kz.alfa.ImageViewer.util.Db_Helper;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class List_files {
	private static final int maxArr = 15001;
	private static int cur = 0;
	private static int max = 0;

	public static String[] sList = new String[maxArr];

	public static void getFromDir(Context context, String path) {
		File file = new File(path);
		if (file.exists()) { // Если папка существует
			String[] fl = file.list();
			max = 0;
			String curFN = sList[cur];
			Log.v("list_files", cur+" curFN = "+ sList[cur]+" sList max = " + fl.length);
			for (int i = 0; ((i < maxArr - 1) && (i < fl.length)); i++) {
				sList[max] = "file://" + path + "/" + fl[i];
				if ((curFN != null)&&(curFN.compareTo(sList[max])==0)){
					Log.v("list_files", "curFN==sList[max] "+cur+" max= "+max); 
					cur = max;
				}
				max = max + 1;
			}
		} else {
			Toast.makeText(context, "not found " + path, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public static void getFromDB(Context context) {
		List<String> list = new ArrayList<String>();
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
		try {
			list = dbHelp.get_files_list(maxArr-1);
		} catch (Exception e) {
			Log.e("list_files.getFromDB", e.toString());
			try {
				TimeUnit.SECONDS.sleep(1);
				list = dbHelp.get_files_list(500);
			} catch (Exception e2) {
				Log.e("list_files.getFromDB", e2.toString());
			}
		}
		String curFN = sList[cur];
		max = 0;
		Log.v("list_files", cur+" curFN = "+ sList[cur]+ " list.size() = " + list.size());
		for (int i = 0; ((i < maxArr - 1) && (i < list.size())); i++) {
			//Log.v("list_files", " list.get(" + i + ") = " + list.get(i));
			sList[max] = "file://" + list.get(i);
			//Log.v("list_files", "sList [" + max + "] = " + sList[max]);
			if ((curFN != null)&&(curFN.compareTo(sList[max])==0)){
				Log.v("list_files", "curFN==sList[max] "+cur+" max= "+max); 
				cur = max;
			}
			max = max + 1;
		}
		if (max == 0){
			Log.e("list_files", " not from DB or error ");
		}
	}

	public static int getCur(int offset) {
		int newCur = cur + offset;
		if (newCur > max - 1) {
			newCur = 0;
		}
		if (newCur < 0) {
			newCur = max - 1;
		}
		return newCur;
	}

	public static String getCurF(int offset) {
		return sList[getCur(offset)];
	}

	public static String getNext() {
		Log.i("List_files", "getNext cur =" + cur);
		cur = cur + 1;
		if (cur > max - 1) {
			cur = 0;
		}
		return sList[cur];
	}

	public static String getPrev() {
		cur = cur - 1;
		if (cur < 0) {
			cur = max - 1;
		}
		return sList[cur];
	}

	public static int getMax() {
		return max;
	}

	public static String getNText() {
		return (cur + 1) + "/" + getMax();
	}
}
