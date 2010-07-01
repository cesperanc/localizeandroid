package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.Local;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.SimpleObject;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;

public class SpaceEdit extends Activity {
	private DbAdapter dbAdapter;
	private Long placeId;
	private EditText spaceName;
	private EditText spaceDescription;
	private EditText spaceLatitude;
	private EditText spaceLongitude;
	private EditText spaceAltitude;
	private Spinner spaceType;
	private Activity activity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.space_edit);
		
		this.activity = this;
		this.dbAdapter =  new DbAdapter(this);
        this.dbAdapter.open();
        
        this.placeId = null;
        this.placeId = savedInstanceState != null ? savedInstanceState.getLong(DbAdapter.KEY_ID): null;
		if (this.placeId == null) {
			Bundle extras = getIntent().getExtras();            
			this.placeId = extras != null ? extras.getLong(DbAdapter.KEY_ID) : null;
		}
		
		this.spaceName = ((EditText) findViewById(R.id.etSpace));
		this.spaceDescription = ((EditText) findViewById(R.id.etDescription));
		this.spaceLatitude = ((EditText) findViewById(R.id.etLatitude));
		this.spaceLongitude = ((EditText) findViewById(R.id.etLongitude));
		this.spaceAltitude = ((EditText) findViewById(R.id.etAltitude));
		this.spaceType = (Spinner) findViewById(R.id.spTypeSpace);
		this.setLocalType(null);

		
		if(this.placeId!=null && this.placeId > -1){
			Local data = this.dbAdapter.getLocalByID(placeId);
			if(data!=null && data.getId()>-1){
				this.setLocalType(data.getLocalTypeName());
				this.spaceName.setText(data.getName());
				this.spaceDescription.setText(data.getDescription());
				this.spaceLatitude.setText(Integer.toString(data.getGpsLat()));
				this.spaceLongitude.setText(Integer.toString(data.getGpsLon()));
				this.spaceAltitude.setText(Integer.toString(data.getAltitude()));
			}
		}else{
			// Valores por omissão para novas inserções; tenta obter os dados a partir do dispositivo e, se não for possível, define valores por omissão
			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location!=null){
				Double latitude = location.getLatitude()*1E6;
			    Double longitude = location.getLongitude()*1E6;
			    Double altitude = location.getAltitude();

				this.spaceLatitude.setText(Integer.toString(latitude.intValue()));
				this.spaceLongitude.setText(Integer.toString(longitude.intValue()));
				this.spaceAltitude.setText(Integer.toString(altitude.intValue()));
			}else{
				this.spaceLatitude.setText("0");
				this.spaceLongitude.setText("0");
				this.spaceAltitude.setText("0");
			}
		}
		
		// Verificar qual a opção seleccionada e iniciar a actividade para adição de tipos de locais caso seja seleccionada a opção novo
		this.spaceType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				final String selectedItem = spaceType.getSelectedItem().toString();
				final String newItem = getString(R.string.option_new_locatype);
				if(selectedItem == newItem){
					Intent i = new Intent(activity, SpaceTypeEdit.class);
		            i.putExtra(DbAdapter.KEY_ID, new Long(-1));
		            startActivityForResult(i, DbAdapter.REQUEST_LOCAL_TYPES_UPDATE);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
				
		
		Button button = (Button) findViewById(R.id.btSave);
        button.setOnClickListener(new View.OnClickListener() {
        	                              	
            public void onClick(View v) {
            	SimpleObject localType = (SimpleObject) spaceType.getSelectedItem();

    			int iLat = Utils.gpsIntFromValue(spaceLatitude.getText().toString());
    			int iLon = Utils.gpsIntFromValue(spaceLongitude.getText().toString());
    			int iAlt = Integer.getInteger(spaceAltitude.getText().toString(), 0);
    			
            	if(dbAdapter.insertOrupdateLocal(
	    			new Local(
						placeId, 
						spaceName.getText().toString(), 
						spaceDescription.getText().toString(), 
						iLat, 
						iLon, 
						iAlt, 
	        			localType.getId(), 
	        			localType.getName()
	    			))>-1){
            		Toast.makeText(activity, getString(R.string.confirmation_text), Toast.LENGTH_LONG).show();
            		setResult(RESULT_OK);
            		activity.finish();
            	}else{
            		Toast.makeText(activity, getString(R.string.error_on_local_save), Toast.LENGTH_LONG).show();
            	}
            }
        });
        ImageButton btMap = (ImageButton) findViewById(R.id.btMap);
    	btMap.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			Intent i = new Intent(activity, SpaceEditMap.class);
	            i.putExtra(DbAdapter.KEY_LOCAL_GPS_LAT, Utils.gpsIntFromValue(spaceLatitude.getText().toString()));
	            i.putExtra(DbAdapter.KEY_LOCAL_GPS_LON, Utils.gpsIntFromValue(spaceLongitude.getText().toString()));
	            startActivityForResult(i, DbAdapter.REQUEST_SET_MAP_LOCATION);			
    		}
    	});    
    	
    	this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK){
			if(requestCode==DbAdapter.REQUEST_LOCAL_TYPES_UPDATE){
				long placeTypeId = data.getLongExtra(DbAdapter.KEY_ID, -1);
				if(placeTypeId>-1){
					this.setLocalType(this.dbAdapter.getLocalType(placeTypeId).getName());
				}
			}
			
			if(requestCode==DbAdapter.REQUEST_SET_MAP_LOCATION){
				this.spaceLatitude.setText(Integer.toString(data.getIntExtra(DbAdapter.KEY_LOCAL_GPS_LAT, 0)));
				this.spaceLongitude.setText(Integer.toString(data.getIntExtra(DbAdapter.KEY_LOCAL_GPS_LON, 0)));
			}
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
	
	/**
	 * Define o tipo de local na spinner
	 * @param name, com o nome do tipo de local
	 */
	private void setLocalType(String name){
		ArrayList<SimpleObject> localTypes = new ArrayList<SimpleObject>(this.dbAdapter.getAllLocalTypes()); 
		localTypes.add(new SimpleObject(-1, getString(R.string.option_new_locatype)));
		
		ArrayAdapter<SimpleObject> localTypesAdapter = new ArrayAdapter<SimpleObject>(this, android.R.layout.simple_spinner_item, localTypes);
		localTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		this.spaceType.setAdapter(localTypesAdapter);
		localTypesAdapter.notifyDataSetChanged();
		
		if(name!=null){
			for(int i=0;i<this.spaceType.getCount();i++){
				if(this.spaceType.getItemAtPosition(i).toString().equals(name)){
					this.spaceType.setSelection(i);
					break;
				}
			}
		}
	}
}
