package kz.alfa.ImageViewer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

public class JpgUtil {

	public static Bitmap drawTextToBitmap(Bitmap bitmap, String gText) {
		//Resources resources = gContext.getResources();
		//float scale = resources.getDisplayMetrics().density;

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		// resource bitmaps are imutable,
		// so we need to convert it to mutable one
		bitmap = bitmap.copy(bitmapConfig, true);
		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color - #3D3D3D
		paint.setColor(Color.rgb(61, 61, 61));
		// text size in pixels
		paint.setTextSize((int) (21)); //* scale));
		// text shadow
		paint.setShadowLayer(2f, 1f, 1f, Color.WHITE);
		// draw text to the Canvas center
		//Rect bounds = new Rect();
		//paint.getTextBounds(gText, 0, gText.length(), bounds);
		int x = bitmap.getWidth() - 150;//bounds.width()) - 150;
		int y = bitmap.getHeight() - 27;//bounds.height()) - 30;
		// fill
		canvas.drawRect(x, y, x+150, y+27, paint);
		canvas.drawText(gText, x, y+20, paint);
		return bitmap;
	}

	void saveImage(Bitmap originalBitmap, File myDir) {
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);

			// NEWLY ADDED CODE STARTS HERE [
			Canvas canvas = new Canvas(originalBitmap);

			Paint paint = new Paint();
			paint.setColor(Color.WHITE); // Text Color
			paint.setStrokeWidth(12); // Text Size
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text
																					// Overlapping
																					// Pattern
			// some more settings...

			canvas.drawBitmap(originalBitmap, 0, 0, paint);
			canvas.drawText("Testing...", 10, 10, paint);
			// NEWLY ADDED CODE ENDS HERE ]

			originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap cropJpgFile(String inFl, String outFl) throws IOException
	{
	    Bitmap bmpIn = BitmapFactory.decodeFile(inFl);
	    Bitmap bmOverlay = Bitmap.createBitmap(bmpIn.getWidth(), bmpIn.getHeight(), Bitmap.Config.ARGB_8888);
	    Paint p = new Paint();
	    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));              
	    Canvas c = new Canvas(bmOverlay); 
	    c.drawBitmap(bmpIn, 0, 0, null); 
	    //c.drawRect(30, 30, 100, 100, p);
	    File fileOut = new File(outFl);
		FileOutputStream out = new FileOutputStream(fileOut);
		bmOverlay = drawTextToBitmap(bmOverlay, "Image Viewer");
	    bmOverlay.compress(Bitmap.CompressFormat.JPEG, 100, out);
	    return bmOverlay;
	}
	
	public static boolean saveRes(Context gContext, int res, String dirjpg) {
		InputStream is = gContext.getApplicationContext().getResources()
				.openRawResource(res);
		boolean rez = false;
		try {
			FileOutputStream rtFOS = new FileOutputStream(dirjpg + "/" + res
					+ ".gif");
			byte[] buffer = new byte[4096];
			int n = -1;
			while ((n = is.read(buffer, 0, 4095)) != -1) {
				if (n > 0)
					rtFOS.write(buffer, 0, n);
				rez = true;
			}
			rtFOS.flush();
			rtFOS.close();
		} catch (Exception e) {
			Log.e("FullscreenActivity", e.toString());
		}
		return rez;
	}

}
