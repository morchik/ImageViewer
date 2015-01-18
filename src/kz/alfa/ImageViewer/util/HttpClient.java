package kz.alfa.ImageViewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kz.alfa.ImageViewer.FullscreenActivity;
import android.util.Log;

public class HttpClient {

	// imitate chrome
	public void setRequestProperty(HttpURLConnection con) {
		con.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		con.setRequestProperty("Accept-Encoding", "deflate");
		con.setRequestProperty("Accept-Language",
				"ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
		con.setRequestProperty("Cache-Control", "no-cache");
		con.setRequestProperty("Connection", "keep-alive");
		con.setRequestProperty("DNT", "1");
		con.setRequestProperty("Pragma", "no-cache");
		con.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36");
	}

	public String getData(String l_url) {
		HttpURLConnection con = null;
		InputStream is = null;

		try {
			con = (HttpURLConnection) (new URL(l_url)).openConnection();
			con.setRequestMethod("GET");
			setRequestProperty(con);
			con.setDoInput(true);
			con.setDoOutput(false);
			con.setConnectTimeout(60000);
			con.connect();	
			int stt = con.getResponseCode();
			Log.d("HttpClient", "html getResponseCode " + stt+" "+l_url);
			ListUrl.updUS(con.getURL().toString(), stt);
			
			StringBuffer buffer = new StringBuffer();
			is = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null)
				buffer.append(line + "\r\n");

			is.close();
			con.disconnect();
			Log.v("HttpClient", "data: " + buffer.toString().substring(1, Math.min(buffer.length(), 80)));
			return buffer.toString();
		} catch (Throwable t) {
			t.printStackTrace();
			Log.e("HttpClient html", l_url + " error " + t.toString());
			try{
				ListUrl.updUS(l_url, -404);
			} catch (Throwable e) {
				e.printStackTrace();
				Log.e("HttpClient http updUS ", l_url + " error " + e.toString());
			}
		} finally {
			try {
				is.close();
			} catch (Throwable t) {
			}
			try {
				con.disconnect();
			} catch (Throwable t) {
			}
		}
		return null;
	}

	public void getImage(String l_url, String dirjpg) {
		HttpURLConnection con = null;
		InputStream is = null;

		try {
			con = (HttpURLConnection) (new URL(l_url)).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Request protocol", "HTTP/1.1");
			String host = con.getURL().getHost();
			con.setRequestProperty("Referer", "http://"+host+"/");
			con.setRequestProperty("Host", host);
			//Log.e("HttpClient", "host ="+host);
			con.setInstanceFollowRedirects(false);
			setRequestProperty(con);
			con.setDoInput(true);
			con.setDoOutput(false); // !!!!! VERY IMPORTANT FOR JPG !!!!!!

			// rem print
			/*
			Map<String, List<String>> headers = con.getRequestProperties();
			Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
			for (Map.Entry<String, List<String>> entry : entrySet) {
				String headerName = entry.getKey();
				Log.v("HttpClient", "RequestProp Name:" + headerName);
				List<String> headerValues = entry.getValue();
				for (String value : headerValues) {
					Log.v("HttpClient", "RequestProp value:" + value);
				}
			}
			*/
			con.setConnectTimeout(60000);
			con.connect();

			// rem print
			/*
			headers = con.getHeaderFields();
			entrySet = headers.entrySet();
			for (Map.Entry<String, List<String>> entry : entrySet) {
				String headerName = entry.getKey();
				Log.v("HttpClient", "Header Name:" + headerName);
				List<String> headerValues = entry.getValue();
				for (String value : headerValues) {
					Log.v("HttpClient", "Header value:" + value);
				}
			}
			*/
			int stt = con.getResponseCode();

			Log.d("HttpClient", "jpg getResponseCode " + stt +" "+l_url);
			// важно это сделать чтобы потом заново не качать 
			ListUrl.updUS(con.getURL().toString(), stt);
			
			if (stt == 200) {
				File sdPath = new File(dirjpg);
				sdPath.mkdirs();
				// формируем объект File, который содержит путь к файлу
				File sdFile = new File(sdPath, "jpg_"+host+System.currentTimeMillis() + ".jpg");
				is = con.getInputStream();
				byte[] buffer = new byte[4096];
				int n = -1, len = 0;
				double csum = 0;

				OutputStream output = new FileOutputStream(sdFile);
				while ((n = is.read(buffer, 0, 4095)) != -1) {
					if (n > 0) {
						// control sum
						output.write(buffer, 0, n);
						for (int i=0;i<n;i++)
							csum = csum + Math.abs(buffer[i]);
						len = len + n;
					}
				}
				Log.v("HttpClient", "sdFile " + sdFile.getName());
				output.flush();
				output.close();
				// rename file or delete
				Log.v("HttpClient", "len:" + len);
				if (len < 20000) // ???????
					sdFile.delete();
				else{
					csum = Math.round(csum);
					String newfn = len+"."+Math.round(csum)+".jpg";
					File file = new File(sdPath, newfn);
					if (host.compareToIgnoreCase("votrube.ru")==0){
						Log.v("HttpClient", "cropBitmap:" + file.getAbsolutePath()+" len:" + len);
						JpgUtil.cropJpgFile(sdFile.getAbsolutePath(), file.getAbsolutePath());
						sdFile.delete();
					} else
						sdFile.renameTo(file);
					ListUrl.upd_J_US(con.getURL().toString(), file.getAbsolutePath(), len, csum);
					Log.i("HttpClient", "file:" + file.getAbsolutePath()+" len:" + len);
					FullscreenActivity.needUpdate = true;
				}
				is.close();
			} else
				Log.e("HttpClient", "getResponseCode " + stt + " url = "+l_url);
			con.disconnect();
		} catch (Throwable t) {
			t.printStackTrace();
			Log.e("HttpClient jpg", l_url + " error " + t.toString());
			try{
				ListUrl.updUS(l_url, -404);
			} catch (Throwable e) {
				e.printStackTrace();
				Log.e("HttpClient jpg updUS ", l_url + " error " + e.toString());
			}
		} finally {
			try {
				is.close();
			} catch (Throwable t) {
			}
			try {
				con.disconnect();
			} catch (Throwable t) {
			}
		}
	}

}
