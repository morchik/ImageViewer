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
		// конструктор суперкласса
		super(context, "dbURL", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(LOG_TAG, "--- before onCreate database ---");
		// создаем таблицу url-ов
		db.execSQL("create table tblURL ("
				+ "_id integer primary key autoincrement," // обязательное поле
				+ "url text unique," // самая
										// главная
										// информация
				+ "typeJH text," // тип url {J|H} J - Image, H - html page
				+ "from_page text," // url страницы на которой был найден этот
									// url
				+ "status_code INTEGER," // http status последней попытки
											// скачать данные
				+ "sd_file text," // файл на диске в случае успешного скачивания
				+ "dt_LastLoad real unique," // время последнего успешного
											// скачивания данных c url
				+ "lenfl INTEGER," // длинна скачанного файла
				+ "ctrl_sum REAL, " // контрольная сумма файла
				+ "date_time TIMESTAMP default CURRENT_TIMESTAMP );");
		Log.v(LOG_TAG, "--- after onCreate database ---");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void ins_url(String url, String fromPage, String urlType) {
		ContentValues cv = new ContentValues();
		// подключаемся к БД
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- before Insert in mytable: ---");
		// подготовим данные для вставки в виде пар: наименование
		// столбца=значение
		cv.put("url", url.toLowerCase());
		cv.put("from_page", fromPage.toLowerCase());
		cv.put("typeJH", urlType);
		// вставляем запись и получаем ее ID
		long rowID = db.insert("tblURL", null, cv);
		Log.v(LOG_TAG, "row inserted, ID = " + rowID);
		this.close();
	}

	// изменить статус последней загрузки после закачки, потом нужно добавить
	// остальныеполя в случае успеха 200
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

	// проверить наличие url нужно для проверки необходимости добавления
	public boolean exist_url(String url) {
		boolean result = true;
		SQLiteDatabase db = this.getWritableDatabase();
		// делаем запрос всех данных из таблицы , получаем Cursor
		String[] w = { url.toLowerCase() };
		Cursor c = db.query("tblURL", null, "url = ?", w, null, null, null);
		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (!c.moveToFirst()) {
			Log.v(LOG_TAG, "0 rows found");
			result = false;
		}
		c.close();
		this.close();
		return result;
	}

	// проверить http status последней попытки нужно для закачки
	public String get_st_url(String url) {
		String result = ""; // если еще не делалась закачка
		int n = 0;
		// подключаемся к БД
		SQLiteDatabase db = this.getWritableDatabase();
		// делаем запрос всех данных из таблицы , получаем Cursor
		String[] w = { url.toLowerCase() };
		Cursor c = db.query("tblURL", null, "url = ?", w, null, null, null);

		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("status_code");
			do {
				n = n + 1;
				result = c.getString(nameColIndex); // http responce code
				Log.v(LOG_TAG, result + " " + url + " №" + n);
			} while (c.moveToNext());
			Log.v(LOG_TAG + ".get_st_url", n + " rows selected");
		} else {
			Log.v(LOG_TAG + ".get_st_url", "0 rows selected");
			result = "NOT FOUND"; // особенный случай
		}
		c.close();
		this.close();
		return result;
	}

	// отладочная печать содержимого
	public void print_tblURL() {
		// подключаемся к БД
		SQLiteDatabase db = this.getWritableDatabase();

		Log.v(LOG_TAG, "--- Rows in  tblURL table: ---");
		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query("tblURL", null, " status_code is null ", null, null, null, "_id desc");

		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (c.moveToFirst()) {

			// определяем номера столбцов по имени в выборке
			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("url");
			int status_codeColIndex = c.getColumnIndex("status_code");
			int tsColIndex = c.getColumnIndex("date_time");
			int indn = 0;
			do {
				indn = indn + 1;
				// получаем значения по номерам столбцов и пишем все в лог
				Log.v(LOG_TAG,
						"ID = " + c.getInt(idColIndex) + ", url = "
								+ c.getString(nameColIndex)
								+ ", typeJH = "	+ c.getString(c.getColumnIndex("typeJH"))
								+ ", status_code = "
								+ c.getString(status_codeColIndex)
								+ ", time = " + c.getString(tsColIndex));
				// переход на следующую строку
				// а если следующей нет (текущая - последняя), то false -
				// выходим из цикла
			} while ((c.moveToNext())&&(indn < 10));
		} else
			Log.v(LOG_TAG, "0 rows");
		c.close();
		// закрываем подключение к БД
		this.close();
	}

	// получить список урлов типа J or H для последующей загрузки
	public List<String> get_list(int cnt, String tJH) { // ограничить размер списка
		List<String> list = new ArrayList<String>();
		int n = 0;
		// подключаемся к БД
		SQLiteDatabase db = this.getWritableDatabase();

		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query("tblURL", null,
				"typeJH = '"+tJH+"' and status_code is null ", null, null, null,
				" _id ASC ");

		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
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
	
		// получить список файлов для отображения
		public List<String> get_files_list(int cnt) { // ограничить размер списка
			List<String> list = new ArrayList<String>();
			int n = 0;
			// подключаемся к БД
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
