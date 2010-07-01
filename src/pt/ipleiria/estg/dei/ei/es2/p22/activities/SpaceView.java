package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.Local;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Window;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class SpaceView extends Activity {
	private DbAdapter dbAdapter;
    private Long placeId;
    
    private static final int ctxBtnEdit = Menu.FIRST + 1;
    private static final int ctxBtnDelete = Menu.FIRST + 2;
	private Context ctx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.space_view);
		this.ctx = this;
		
		this.dbAdapter =  new DbAdapter(this);
        this.dbAdapter.open();
        
        this.placeId = savedInstanceState != null ? savedInstanceState.getLong(DbAdapter.KEY_ID): null;
		if (this.placeId == null) {
			Bundle extras = getIntent().getExtras();            
			this.placeId = extras != null ? extras.getLong(DbAdapter.KEY_ID) : null;
		}
		
		this.getPlace();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		this.dbAdapter.close();
		super.onDestroy();
	}
	
	/**
	 * Para criar o menu de opções neste contexto
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// activa a possibilidade de atalhos de teclado com letras
		menu.setQwertyMode(true);
		MenuItem item = menu.add(0, ctxBtnEdit, 0, R.string.ctx_edit_btn);
		item.setAlphabeticShortcut('e');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_edit, 32, 32));
		
		item = menu.add(0, ctxBtnDelete, 0, R.string.ctx_delete_btn);
		item.setAlphabeticShortcut('a');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_delete, 32, 32));
		
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Ao seleccionar uma opção do menu de opções
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		switch (item.getItemId()) {
			case ctxBtnEdit:
				Intent i = new Intent(this, SpaceEdit.class);
		        i.putExtra(DbAdapter.KEY_ID, this.placeId);
		        startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
				return true;
				
			case ctxBtnDelete:
				dialog.setMessage(R.string.delete_local_msg);
				dialog.setCancelable(false);
				dialog.setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						if(dbAdapter.removeLocalByID(placeId)){
							Toast.makeText(ctx, getString(R.string.search_space_deleted), Toast.LENGTH_LONG).show();
							setResult(RESULT_OK);
							finish();
						}else{
							Window.showError(ctx, null, getString(R.string.error_deleting_local));
						}
					}
				});
				dialog.setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				dialog.show();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);	
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.getPlace();
	}
	
	/**
	 * Obtém um espaço com base no id interno
	 */
	private void getPlace() {
		this.getPlace(this.placeId);
	}
	
	/** 
	 * Obtém um espaço com base no identificador de um local
	 * @param id {@link Long} com o identificador do local
	 */
	private void getPlace(long id) {
		if (id>-1 && this.dbAdapter.isOpen()) {
			Local space = this.dbAdapter.getLocalByID(id);
			if(space!=null){
				((TextView) findViewById(R.id.etSpaceName)).setText(space.getName());
				((TextView) findViewById(R.id.etDescription)).setText(space.getDescription());
				((TextView) findViewById(R.id.etLatitude)).setText(Integer.toString(space.getGpsLat()));
				((TextView) findViewById(R.id.etLongitude)).setText(Integer.toString(space.getGpsLon()));
				((TextView) findViewById(R.id.etAltitude)).setText(Integer.toString(space.getAltitude()));
				((TextView) findViewById(R.id.etTipoEspaco)).setText(space.getLocalTypeName());
			}else{
				Toast.makeText(ctx, getString(R.string.error_while_retrieving_the_local), Toast.LENGTH_LONG).show();
				((TextView) findViewById(R.id.etSpaceName)).setText("");
				((TextView) findViewById(R.id.etDescription)).setText("");
				((TextView) findViewById(R.id.etLatitude)).setText("");
				((TextView) findViewById(R.id.etLongitude)).setText("");
				((TextView) findViewById(R.id.etAltitude)).setText("");
				((TextView) findViewById(R.id.etTipoEspaco)).setText("");
			}
	    }
	}
	
	
}
