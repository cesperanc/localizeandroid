package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.MapPopupPanel;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.MapOverlay;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class SpaceEditMap extends MapActivity {
	private MapView mapView;
	private MapPopupPanel popup;
	private MapOverlay itemizedoverlay;
	private MapActivity activity;
	private DbAdapter dbAdapter;
	private LocationManager mlocManager;
	private GeoPoint initialPoint;
	
	// Preferences
	private static final String PREFERENCE_MAP_SATELLITE_EDIT_VIEW_NAME="space_edit_map_satellite";
	private static final String PREFERENCE_MAP_SATELLITE_EDIT_VIEW_VALUE="enabled";
	
	private static final String PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_NAME="space_edit_map_traffic";
	private static final String PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_VALUE="enabled";
	
	private static final String PREFERENCE_MAP_STREET_EDIT_VIEW_NAME="space_edit_map_street";
	private static final String PREFERENCE_MAP_STREET_EDIT_VIEW_VALUE="enabled";
	
	// menu
	private static final int OPTIONS_APLLY = Menu.FIRST;
	private static final int OPTIONS_MAP_SHOW_POPUP = Menu.FIRST+1;
	private static final int OPTIONS_MAP_SATELLITE = Menu.FIRST+2;
	private static final int OPTIONS_MAP_TRAFFIC = Menu.FIRST+3;
	private static final int OPTIONS_MAP_STREET = Menu.FIRST+4;
	private static final int OPTIONS_MAP_SET_MY_POSITION = Menu.FIRST+5;
	private static final int OPTIONS_MAP_RESET_POSITION = Menu.FIRST+6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.activity = this;
        this.mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        this.mapView = new MapView(this, DbAdapter.GOOGLE_MAPS_APY_KEY);    
        this.mapView.setBuiltInZoomControls(true);
        this.setContentView(this.mapView);
        
        int latitude = 0;
        latitude = savedInstanceState != null ? savedInstanceState.getInt(DbAdapter.KEY_LOCAL_GPS_LAT): 0;
		if(latitude==0){
			Bundle extras = getIntent().getExtras();            
			latitude = extras != null ? extras.getInt(DbAdapter.KEY_LOCAL_GPS_LAT) : 0;
		}
		
		int longitude = 0;
		longitude = savedInstanceState != null ? savedInstanceState.getInt(DbAdapter.KEY_LOCAL_GPS_LON): 0;
		if(longitude==0){
			Bundle extras = getIntent().getExtras();            
			longitude = extras != null ? extras.getInt(DbAdapter.KEY_LOCAL_GPS_LON) : 0;
		}
		
		View.OnClickListener applyEvent = new View.OnClickListener() {
			public void onClick(View v) {
				apply();
			}
		};
        
        this.popup = new MapPopupPanel(this, this.mapView, R.layout.space_edit_map_popup_overlay, applyEvent);
        
        View view = popup.getView();
		((ImageButton)view.findViewById(R.id.closeButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.hide();
			}
		});
		((ImageButton)view.findViewById(R.id.applyButton)).setOnClickListener(applyEvent);
        
        this.itemizedoverlay = new MapOverlay(BitmapFactory.decodeResource(this.getResources(), R.drawable.default_local_type), this.mapView, this.popup);
        
        this.itemizedoverlay.setEditable(true);
        
        try{
        	this.initialPoint = new GeoPoint(latitude,longitude);
        }catch (Exception e) {
        	this.initialPoint = new GeoPoint(19240000,-99120000);
		}
        
        OverlayItem overlayitem = new OverlayItem(this.initialPoint, "", "");
        this.itemizedoverlay.addOverlay(new Long(-1), overlayitem);
        this.mapView.getOverlays().add(this.itemizedoverlay);
        
        // Instanciar o adaptador para acesso à camada de dados
		this.dbAdapter = new DbAdapter(this);
        this.dbAdapter.open();
        
        // Define a preferência para a vista de satélite
        String preference = this.dbAdapter.getPreference(SpaceEditMap.PREFERENCE_MAP_SATELLITE_EDIT_VIEW_NAME);
        this.mapView.setSatellite(preference!=null && preference.equals(SpaceEditMap.PREFERENCE_MAP_SATELLITE_EDIT_VIEW_VALUE));
        
        // Define a preferência para a vista de tráfego
        preference = this.dbAdapter.getPreference(SpaceEditMap.PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_NAME);
        this.mapView.setTraffic(preference!=null && preference.equals(SpaceEditMap.PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_VALUE));
        
        // Define a preferência para a vista de rua
        preference = this.dbAdapter.getPreference(SpaceEditMap.PREFERENCE_MAP_STREET_EDIT_VIEW_NAME);
        this.mapView.setStreetView(preference!=null && preference.equals(SpaceEditMap.PREFERENCE_MAP_STREET_EDIT_VIEW_VALUE));
    }
    
    /**
	 * Ao seleccionar uma opção do menu de opções
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
			case OPTIONS_APLLY:
				apply();
				return true;
		
			case OPTIONS_MAP_SATELLITE:
				this.mapView.setSatellite(!this.mapView.isSatellite());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceEditMap.PREFERENCE_MAP_SATELLITE_EDIT_VIEW_NAME, this.mapView.isSatellite()?SpaceEditMap.PREFERENCE_MAP_SATELLITE_EDIT_VIEW_VALUE:"");
				Toast.makeText(this, this.mapView.isSatellite()?this.getString(R.string.info_mode_satellite_enabled):this.getString(R.string.info_mode_satellite_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_MAP_TRAFFIC:
				this.mapView.setTraffic(!this.mapView.isTraffic());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceEditMap.PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_NAME, this.mapView.isSatellite()?SpaceEditMap.PREFERENCE_MAP_TRAFFIC_EDIT_VIEW_VALUE:"");
				Toast.makeText(this, this.mapView.isTraffic()?this.getString(R.string.info_mode_traffic_enabled):this.getString(R.string.info_mode_traffic_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_MAP_STREET:
				this.mapView.setStreetView(!this.mapView.isStreetView());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceEditMap.PREFERENCE_MAP_STREET_EDIT_VIEW_NAME, this.mapView.isSatellite()?SpaceEditMap.PREFERENCE_MAP_STREET_EDIT_VIEW_VALUE:"");
				Toast.makeText(this, this.mapView.isStreetView()?this.getString(R.string.info_mode_street_enabled):this.getString(R.string.info_mode_street_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_MAP_SHOW_POPUP:
				this.popup.show();
				return true;
			
			case OPTIONS_MAP_SET_MY_POSITION:
				if(this.mlocManager!=null){
					Location location = this.mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(location!=null){
						Double latitude = location.getLatitude()*1E6;
					    Double longitude = location.getLongitude()*1E6;
						this.itemizedoverlay.setCurrentPosition(new GeoPoint(latitude.intValue(), longitude.intValue()));
					}else{
						Toast.makeText(this, this.getString(R.string.error_while_getting_the_position), Toast.LENGTH_LONG).show();
					}
				}
				return true;
			
			case OPTIONS_MAP_RESET_POSITION:
				/*
				MapOverlay current = this.itemizedoverlay.setCurrentPosition(this.initialPoint);
				this.mapView.getOverlays().remove(this.itemizedoverlay);
				this.itemizedoverlay = current;
				*/
				return true;
				
			default:
				return super.onOptionsItemSelected(item);	
		}
	}
	/**
	 * Para criar/actualizar o menu de opções nesta actividade
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// activa a possibilidade de atalhos de teclado com letras
		menu.setQwertyMode(true);
		
		MenuItem item = menu.add(0, OPTIONS_APLLY, 0, this.getString(R.string.label_use_position));
		item.setAlphabeticShortcut('a');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_upload, 32, 32));
		
		item = menu.add(0, OPTIONS_MAP_SATELLITE, 0, (this.mapView.isSatellite())?this.getString(R.string.label_disable_satellite_mode):this.getString(R.string.label_enable_satellite_mode));
		item.setAlphabeticShortcut('s');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_map, 32, 32));
		
		item = menu.add(0, OPTIONS_MAP_TRAFFIC, 0, (this.mapView.isTraffic())?this.getString(R.string.label_disable_traffic_mode):this.getString(R.string.label_enable_traffic_mode));
		item.setAlphabeticShortcut('t');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_share, 32, 32));
		
		item = menu.add(0, OPTIONS_MAP_STREET, 0, (this.mapView.isStreetView())?this.getString(R.string.label_disable_street_mode):this.getString(R.string.label_enable_street_mode));
		item.setAlphabeticShortcut('e');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_view, 32, 32));
		
		item = menu.add(0, OPTIONS_MAP_SHOW_POPUP, 0, this.getString(R.string.label_show_position_info_window));
		item.setAlphabeticShortcut('p');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_info_details, 32, 32));
		
		item = menu.add(0, OPTIONS_MAP_SET_MY_POSITION, 0, this.getString(R.string.label_set_my_position));
		item.setAlphabeticShortcut('m');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_mylocation, 32, 32));
		/*
		item = menu.add(0, OPTIONS_MAP_RESET_POSITION, 0, this.getString(R.string.label_reset_position));
		item.setAlphabeticShortcut('r');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_rotate, 32, 32));
		*/
		return super.onPrepareOptionsMenu(menu);
	}
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
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
	 * Retorna o ponto actual para a actividade que chamou esta actividade
	 */
	private void apply(){
		GeoPoint point = itemizedoverlay.getCurrentPosition();
		Intent intent = new Intent();
	    intent.putExtra(DbAdapter.KEY_LOCAL_GPS_LAT, point.getLatitudeE6());
	    intent.putExtra(DbAdapter.KEY_LOCAL_GPS_LON, point.getLongitudeE6());
	    setResult(RESULT_OK, intent);
		
		activity.finish();
	}
}