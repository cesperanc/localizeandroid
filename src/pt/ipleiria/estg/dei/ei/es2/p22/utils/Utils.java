package pt.ipleiria.estg.dei.ei.es2.p22.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {
	private Utils(){
		
	}
	/**
	 * @author Nazmul
	 * {@link} http://developerlife.com/tutorials/?p=304
	 * @param ctx
	 * @param resId
	 * @param w
	 * @param h
	 * @return
	 */
	public static Drawable resizeImage(Context ctx, int resId, int w, int h) {
		// load the original Bitmap
		Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(), resId);

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;
	
		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
	
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);
	
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
	
		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
	  	return new BitmapDrawable(resizedBitmap);

	}
	

	/**
	 * Convert the image URI to the file system path of the file
	 * 
	 * @param activity {@link Activity} with the activity to use in the query
	 * @param contentUri {@link Uri} with the Uri of the resource
	 * @return {@link String} with the path of the file
	 * 
	 * @see http://www.androidsnippets.org/snippets/130/ for reference
	 */
	public static String getRealPathFromURI(Activity activity, Uri contentUri) {
		String [] proj={MediaStore.Images.Media.DATA};
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}
	
	/** // TODO criar o teste unitário para este método
	 * From a given string, try to extract a int coordinate value
	 * @param str {@link String} with the given string
	 * @return {@link Integer} value
	 * @author Cláudio Esperança <cesperanc@gmail.com>
	 */
	public static int gpsIntFromValue(String str){
		int iCoordinate = Utils.getInteger(str, 0);
		
		if(iCoordinate==0 && !str.equals("0")){
			try{
				double dCoordinate = Double.parseDouble(str);
				if(!Double.isInfinite(dCoordinate)){
					return Utils.getInteger(Long.toString(Math.round(dCoordinate*1000000.0)),0);
				}
				return Utils.getInteger(Double.toString(dCoordinate),0);
			}catch (Exception e) {}
		}
		return iCoordinate;
	}
	
	/** // TODO criar o teste unitário para este método
	 * Integer.getInteger substitute that actually do what's is supposed to do
	 * @param str {@link String} to try to the get the {@link Integer} value from
	 * @param defaultValue {@link Integer} to return in case of we can't get an value from the {@link String}
	 * @return {@link Integer} with the parsed value from the {@link String}, {@link Integer} with the defaultValue otherwise
	 * @author Cláudio Esperança <cesperanc@gmail.com>
	 */
	public static int getInteger(String str, int defaultValue){
		try{
			return Integer.decode(str);
		}catch (Exception e) {}
		return defaultValue;
	}
	
	/**
	 * To substring a string adding the ... if any characters were removed from the string
	 * @param text {@link String} text to be shrinked
	 * @param length {@link Integer} with the maximum permitted size
	 * @return {@link String} with the maximum desired size
	 */
	public static String shrinkText(String text, int length){
		if(text.length()>length){
			int finalLength = (text.substring(0, length-3).lastIndexOf(" ")-1);
			if(finalLength<=0){
				finalLength = length-3;
			}
			text = text.substring(0, finalLength)+"...";
        }
		return text;
	}
}
