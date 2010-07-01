package pt.ipleiria.estg.dei.ei.es2.p22.utils;

import android.app.AlertDialog;
import android.content.Context;

public class Window {
	private Window(){
		
	}
	/**
	 * Mostra uma mensagem de erro
	 * @param title
	 * @param error
	 */
	public static void showError(Context ctx, String title, String error){
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle(title);
		dialog.setMessage(error);
		dialog.show();
	}
	/**
	 * Mostra uma mensagem de erro
	 * @param title
	 * @param error
	 */
	public static void showError(Context ctx, String title, int error){
		showError(ctx, title, ctx.getString(error));
	}
}
