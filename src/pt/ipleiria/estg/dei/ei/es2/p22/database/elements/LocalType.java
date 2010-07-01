package pt.ipleiria.estg.dei.ei.es2.p22.database.elements;

import pt.ipleiria.estg.dei.ei.es2.p22.R;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Classe que define um tipo de local
 * 
 * @author cesperanc
 */
public class LocalType extends SimpleObject {
	private String iconPath;
	
	public LocalType(long id, String name, String iconPath) {
		super(id, name);
		this.setIcon(iconPath);
	}
	
	public LocalType(long id, String name) {
		this(id, name, "");
	}
	
	public LocalType(String name, String iconPath) {
		this(-1, name, iconPath);
	}
	
	public LocalType(String name) {
		this(-1, name, "");
	}
	
	/**
	 * @param path with the path for the icon
	 */
	public void setIcon(String path) {
		this.iconPath = path;
	}

	/**
	 * @return the path for the icon
	 */
	public String getIcon() {
		return iconPath;
	}
	
	/**
	 * Return the icon bitmap from the path
	 * 
	 * @return {@link Bitmap} with the scaled image
	 */
	public Bitmap getIconBitmap(){
		String path = this.getIcon();
		if(path!=null && !path.equals("")){
			return BitmapFactory.decodeFile(this.getIcon());
		}
		return null;
	}
	
	/**
	 * Return the icon bitmap from the path
	 * 
	 * @param width {@link integer} with the desired width
	 * @param height {@link integer} with the desired height
	 * @return {@link Bitmap} with the scaled image
	 */
	public Bitmap getIconBitmap(int width, int height){
		width = width<=0?37:width;
		height = height<=0?37:height;
		
		Bitmap bMap = this.getIconBitmap();
		if(bMap!=null){
	        return Bitmap.createScaledBitmap(bMap, width, height, true);
		}
		return null;
	}
	
	/**
	 * Return the icon bitmap from the path, or the default icon for the local type (updating the icon path to empty string in this last case)
	 * 
	 * @param width {@link integer} with the desired width
	 * @param height {@link integer} with the desired height
	 * @param context {@link Context} with the context to retrieve the default resource
	 * @return {@link Bitmap} with the scaled image
	 */
	public Bitmap getIconBitmap(int width, int height, Context context){
		width = width<=0?37:width;
		height = height<=0?37:height;
		
		Bitmap bMap = getIconBitmap(width, height);
		if(bMap!=null){
	        return bMap;
		}else{
			this.setIcon("");
			return this.getIconDefaultBitmap(width, height, context);
		}
	}
	
	/**
	 * Return the default icon bitmap for the local type
	 * 
	 * @param context {@link Context} with the context to retrieve the default resource
	 * @return {@link Bitmap} with the default image
	 */
	public Bitmap getIconDefaultBitmap(Context context){
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_local_type);
	}
	
	/**
	 * Return the default icon bitmap for the local type
	 * 
	 * @param width {@link integer} with the desired width
	 * @param height {@link integer} with the desired height
	 * @param context {@link Context} with the context to retrieve the default resource
	 * @return {@link Bitmap} with the default image
	 */
	public Bitmap getIconDefaultBitmap(int width, int height, Context context){
		width = width<=0?37:width;
		height = height<=0?37:height;
		
		Bitmap bMap = this.getIconDefaultBitmap(context);
		if(bMap!=null){
			return Bitmap.createScaledBitmap(bMap, width, height, true);
		}
		return null;
	}
}
