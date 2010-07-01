package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.LocalType;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;

/**
 * @author cesperanc
 */
public class SpaceTypeEdit extends Activity {
	private DbAdapter dbAdapter;
	private Long placeTypeId;
	private EditText spaceTypeName;
	private ImageButton spaceTypeIcon;
	private String iconPath;
	private Activity activity;
	public static final int LOCAL_TYPES_UPDATED=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.space_type_edit);
		
		this.activity = this;
		this.dbAdapter = new DbAdapter(this);
        this.dbAdapter.open();
        
        this.placeTypeId = null;
        this.placeTypeId = savedInstanceState != null ? savedInstanceState.getLong(DbAdapter.KEY_ID): null;
		if (this.placeTypeId == null) {
			Bundle extras = getIntent().getExtras();            
			this.placeTypeId = extras != null ? extras.getLong(DbAdapter.KEY_ID) : null;
		}
		
		this.spaceTypeName = ((EditText) findViewById(R.id.etSpaceType));
		
		// Botão para seleccionar o icone
		this.spaceTypeIcon = ((ImageButton) findViewById(R.id.ibIcon));
		this.spaceTypeIcon.setOnClickListener(new View.OnClickListener() {    			
    		public void onClick(View v) {
    			// To open up a gallery browser
    			Intent intent = new Intent();
    			intent.setType("image/png");
    			intent.setAction(Intent.ACTION_GET_CONTENT);
    			
    			startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select_icon)), DbAdapter.REQUEST_LOCAL_TYPE_ICON);			
    		}
    	});
		
		// Evento associado ao clique no botão de guardar
		Button button = (Button) findViewById(R.id.btSave);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(placeTypeId!=null && placeTypeId > -1 && dbAdapter.getLocalType(placeTypeId)!=null){
            		if(dbAdapter.updateLocalType(new LocalType(placeTypeId, spaceTypeName.getText().toString(), iconPath))){
            			Toast.makeText(activity, getString(R.string.confirmation_text), Toast.LENGTH_LONG).show();
            			Intent intent = new Intent();
            		    intent.putExtra(DbAdapter.KEY_ID, placeTypeId);
                		setResult(RESULT_OK, intent);
                		activity.finish();
            		}else{
            			Toast.makeText(activity, getString(R.string.error_on_local_type_save), Toast.LENGTH_LONG).show();
            		}
            	}else{
            		placeTypeId = dbAdapter.insertLocalType(new LocalType(spaceTypeName.getText().toString(), iconPath));
            		if(placeTypeId>-1){
            			Toast.makeText(activity, getString(R.string.confirmation_text), Toast.LENGTH_LONG).show();
            			Intent intent = new Intent();
            		    intent.putExtra(DbAdapter.KEY_ID, placeTypeId);
                		setResult(RESULT_OK, intent);
                		activity.finish();
            		}else{
            			Toast.makeText(activity, getString(R.string.error_on_local_type_save), Toast.LENGTH_LONG).show();
            		}
            	}
            }
        });
		
        // Caso exista um identificador para o tipo de local, preencher o texto e o icone associado ao tipo de local
		if(this.placeTypeId!=null && this.placeTypeId > -1){
			LocalType placeType = this.dbAdapter.getLocalType(this.placeTypeId);
			this.spaceTypeName.setText((placeType!=null)?placeType.getName():"");
			this.updateIbIconImage((placeType!=null)?placeType.getIcon():"");
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		this.dbAdapter.close();
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK) {
			if(requestCode==DbAdapter.REQUEST_LOCAL_TYPE_ICON){
				this.updateIbIconImage(Utils.getRealPathFromURI(this, data.getData()));
			}
		}
	}
	
	/**
	 * Actualiza o icone do botão de acordo com o path
	 * @param path {@link String} com o caminho para o ficheiro
	 */
	private void updateIbIconImage(String path){
		LocalType tmp = new LocalType("", path);
		this.spaceTypeIcon.setImageBitmap(tmp.getIconBitmap(this.spaceTypeIcon.getWidth(), this.spaceTypeIcon.getHeight(), this));
		this.iconPath = tmp.getIcon();
	}
}
