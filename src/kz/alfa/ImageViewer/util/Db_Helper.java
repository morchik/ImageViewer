package kz.alfa.ImageViewer.util;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class Db_Helper extends SQLiteOpenHelper {

	final String LOG_TAG = "Db_Helper";

	public Db_Helper(Context context) {
		// ����������� �����������
		super(context, "dbURL", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(LOG_TAG, "--- before onCreate database ---");
		// ������� ������� url-��
		db.execSQL("create table tblURL ("
				+ "_id integer primary key autoincrement," // ������������ ����
				+ "url text unique," // �����
										// �������
										// ����������
				+ "typeJH text," // ��� url {J|H} J - Image, H - html page
				+ "from_page text," // url �������� �� ������� ��� ������ ����
									// url
				+ "status_code INTEGER," // http status ��������� �������
											// ������� ������
				+ "sd_file text," // ���� �� ����� � ������ ��������� ����������
				+ "dt_LastLoad real unique," // ����� ���������� ���������
											// ���������� ������ c url
				+ "lenfl INTEGER," // ������ ���������� �����
				+ "ctrl_sum REAL, " // ����������� ����� �����
				+ "date_time TIMESTAMP default CURRENT_TIMESTAMP );");
		Log.v(LOG_TAG, "--- after onCreate database ---");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void ins_url(String url, String fromPage, String urlType) {
		ContentValues cv = new ContentValues();
		// ������������ � ��
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- before Insert in mytable: ---");
		// ���������� ������ ��� ������� � ���� ���: ������������
		// �������=��������
		cv.put("url", url.toLowerCase());
		cv.put("from_page", fromPage.toLowerCase());
		cv.put("typeJH", urlType);
		// ��������� ������ � �������� �� ID
		long rowID = db.insert("tblURL", null, cv);
		Log.v(LOG_TAG, "row inserted, ID = " + rowID);
		this.close();
	}

	// �������� ������ ��������� �������� ����� �������, ����� ����� ��������
	// ������������� � ������ ������ 200
	public long upd_st(String url, int status_code) {
		ContentValues cv = new ContentValues();
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- before update_status in mytable: ---");
		cv.put("status_code", status_code);
		String[] w = { url.toLowerCase() };
		long rowID = db.update("tblURL", cv, "url = ?", w);
		Log.v(LOG_TAG, "row updated  = " + rowID+" url = "+url);
		this.close();
		return rowID;
	}

	public long upd_J_st(String url, String sd_file, int len, double csum) {
		ContentValues cv = new ContentValues();
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- before update_status J in mytable: ---");
		cv.put("sd_file", sd_file);
		cv.put("dt_LastLoad", System.currentTimeMillis());
		cv.put("lenfl", len);
		cv.put("ctrl_sum", csum);
		String[] w = { url.toLowerCase() };
		long rowID = db.update("tblURL", cv, "url = ?", w);
		Log.v(LOG_TAG, "J row updated  = " + url+" sd_file = "+sd_file);
		this.close();
		return rowID;
	}

	// ��������� ������� url ����� ��� �������� ������������� ����������
	public boolean exist_url(String url) {
		boolean result = true;
		SQLiteDatabase db = this.getWritableDatabase();
		// ������ ������ ���� ������ �� ������� , �������� Cursor
		String[] w = { url.toLowerCase() };
		Cursor c = db.query("tblURL", null, "url = ?", w, null, null, null);
		// ������ ������� ������� �� ������ ������ �������
		// ���� � ������� ��� �����, �������� false
		if (!c.moveToFirst()) {
			Log.v(LOG_TAG, "0 rows found");
			result = false;
		}
		c.close();
		this.close();
		return result;
	}

	// ��������� http status ��������� ������� ����� ��� �������
	public String get_st_url(String url) {
		String result = ""; // ���� ��� �� �������� �������
		int n = 0;
		// ������������ � ��
		SQLiteDatabase db = this.getWritableDatabase();
		// ������ ������ ���� ������ �� ������� , �������� Cursor
		String[] w = { url.toLowerCase() };
		Cursor c = db.query("tblURL", null, "url = ?", w, null, null, null);

		// ������ ������� ������� �� ������ ������ �������
		// ���� � ������� ��� �����, �������� false
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("status_code");
			do {
				n = n + 1;
				result = c.getString(nameColIndex); // http responce code
				Log.v(LOG_TAG, result + " " + url + " �" + n);
			} while (c.moveToNext());
			Log.v(LOG_TAG + ".get_st_url", n + " rows selected");
		} else {
			Log.v(LOG_TAG + ".get_st_url", "0 rows selected");
			result = "NOT FOUND"; // ��������� ������
		}
		c.close();
		this.close();
		return result;
	}

	// ���������� ������ �����������
	public void print_tblURL() {
		// ������������ � ��
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- Rows in  tblURL table: ---");
		// ������ ������ ���� ������ �� ������� mytable, �������� Cursor
		Cursor c = db.query("tblURL", null, " status_code is null ", null, null, null, "_id desc");

		// ������ ������� ������� �� ������ ������ �������
		// ���� � ������� ��� �����, �������� false
		if (c.moveToFirst()) {

			// ���������� ������ �������� �� ����� � �������
			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("url");
			int status_codeColIndex = c.getColumnIndex("status_code");
			int tsColIndex = c.getColumnIndex("date_time");
			int indn = 0;
			do {
				indn = indn + 1;
				// �������� �������� �� ������� �������� � ����� ��� � ���
				Log.v(LOG_TAG,
						"ID = " + c.getInt(idColIndex) + ", url = "
								+ c.getString(nameColIndex)
								+ ", typeJH = "	+ c.getString(c.getColumnIndex("typeJH"))
								+ ", status_code = "
								+ c.getString(status_codeColIndex)
								+ ", time = " + c.getString(tsColIndex));
				// ������� �� ��������� ������
				// � ���� ��������� ��� (������� - ���������), �� false -
				// ������� �� �����
			} while ((c.moveToNext())&&(indn < 10));
		} else
			Log.v(LOG_TAG, "0 rows");
		c.close();
		// ��������� ����������� � ��
		this.close();
	}

	// �������� ������ ����� ���� J or H ��� ����������� ��������
	public List<String> get_list(int cnt, String tJH) { // ���������� ������ ������
		List<String> list = new ArrayList<String>();
		int n = 0;
		// ������������ � ��
		SQLiteDatabase db = this.getWritableDatabase();

		// ������ ������ ���� ������ �� ������� mytable, �������� Cursor
		Cursor c = db.query("tblURL", null,
				"typeJH = '"+tJH+"' and status_code is null ", null, null, null,
				" _id ASC ");

		// ������ ������� ������� �� ������ ������ �������
		// ���� � ������� ��� �����, �������� false
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("url");
			do {
				n = n + 1;
				Log.v(LOG_TAG, "  " + c.getString(nameColIndex));
				list.add(c.getString(nameColIndex));
			} while ((c.moveToNext()) && (n < cnt));
		} else
			Log.v(LOG_TAG, "0 rows selected");
		c.close();
		this.close();
		return list;
	}
	
		// �������� ������ ������ ��� �����������
		public List<String> get_files_list(int cnt) { // ���������� ������ ������
			List<String> list = new ArrayList<String>();
			int n = 0;
			// ������������ � ��
			SQLiteDatabase db = this.getWritableDatabase();
			
			Cursor c = db.query("tblURL", null,
					" typeJH = 'J' and status_code = 200 and sd_file is not null ", null, null, null,
					" dt_LastLoad DESC, _id desc ");
			Log.v(LOG_TAG, c.toString()+ "  cnt = " + cnt);
			if (c.moveToFirst()) {
				int nameColIndex = c.getColumnIndex("sd_file");
				do {
					n = n + 1;
					String path_file = c.getString(nameColIndex);
					Log.v(LOG_TAG, "  " + path_file);
					list.add(path_file);
				} while ((c.moveToNext()) && (n < cnt));
			} else
				Log.e(LOG_TAG, "0 rows selected in get_files_list ");
			c.close();
			this.close();
			return list;
		}
}
