package kz.alfa.ImageViewer.util;

import java.util.List;
import kz.alfa.ImageViewer.FullscreenActivity;

public class ListUrl {

	public static void addJ(String url, String source_page) {
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
		if (!dbHelp.exist_url(url))
			dbHelp.ins_url(url, source_page, "J");
	}

	public static void addH(String url, String source_page) {
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
		if (!dbHelp.exist_url(url))
			dbHelp.ins_url(url, source_page, "H");
	}

	public static List<String> get_list(int cnt, String tJH) { // ограничить
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);														// размер списка
		return dbHelp.get_list(cnt, tJH);
	}

	public static long updUS(String url, int status_code) {
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
		return dbHelp.upd_st(url, status_code);
	}

	public static long upd_J_US(String url, String sd_file, int len, double csum) {
		Db_Helper dbHelp = new Db_Helper(FullscreenActivity.fsA);
		return dbHelp.upd_J_st(url, sd_file, len, csum);
	}

}
