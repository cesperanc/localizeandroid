package pt.ipleiria.estg.dei.ei.es2.p22.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import pt.ipleiria.estg.dei.ei.es2.p22.R;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * @author Diogo Serra <codedmind@gmail.com>, Cláudio Esperança <cesperanc@gmail.com>
 */
public class MapOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {
	MapPopupPanel popup;
	MapView mapView;
	private Bitmap bitmapIcon;
	private ArrayList<OverlayItem> overlays;
	private Hashtable<Integer, Long> overlaysMap;
    private boolean moveMap;
    private GeoPoint currentPosition;
    private boolean editable;
    
    private static MapOverlay currentOverlay;
    
    /**
	 * Constructor
	 * @param bitmap com o {@link Bitmap} a utilizar como icone
	 */
	public MapOverlay(Bitmap bitmap) {
		super(boundCenterBottom(new BitmapDrawable(bitmap)));
		this.overlays = new ArrayList<OverlayItem>();
		this.overlaysMap = new Hashtable<Integer, Long>();
		MapOverlay.currentOverlay = this;
		
		this.setEditable(false);
	}
	
	/**
	 * Constructor
	 * @param bitmap com o {@link Bitmap} a utilizar como icone
	 * @param mapView com o {@link MapView} pai
	 * @param popup com o {@link MapPopupPanel}
	 */
	public MapOverlay(Bitmap bitmap, MapView mapView, MapPopupPanel popup) {
		this(bitmap);
		this.popup = popup;
		this.mapView = mapView;
		this.bitmapIcon = bitmap;
		this.setEditable(false);
	}
	
	/**
	 * Constructor
	 * @param bitmap com o {@link Bitmap} a utilizar como icone
	 * @param LatitudeE6 {@link Integer} com a latitude
	 * @param LongitudeE6 {@link Integer} com a longitude
	 */
	public MapOverlay(Bitmap bitmap, int LatitudeE6, int LongitudeE6) {
		this(bitmap);
		this.bitmapIcon=bitmap;
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return this.overlays.get(i);
	}
	@Override
	public int size() {
		return this.overlays.size();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if(this.isEditable()){
			if (event.getAction() == MotionEvent.ACTION_UP){
				if(!this.moveMap){
					Projection proj = mapView.getProjection();
					GeoPoint point = this.currentPosition = proj.fromPixels((int)event.getX(), (int)event.getY());
					
					return setCurrentPosition(point)!=null?true:false;
				}
			}else if (event.getAction() == MotionEvent.ACTION_DOWN){
				this.moveMap = false;
			}else if (event.getAction() == MotionEvent.ACTION_MOVE){    
				this.moveMap = true;
			}
		}
		return super.onTouchEvent(event, mapView);
	}
	
	@Override
	protected boolean onTap(int index) {
		if(!this.isEditable() && this.popup!=null){
			OverlayItem item = this.getItem(index);
			View view = this.popup.getView();
			
			((TextView)view.findViewById(R.id.title)).setText(Utils.shrinkText(item.getTitle(), 50));
			((TextView)view.findViewById(R.id.content)).setText(Utils.shrinkText(item.getSnippet(), 100));
			
			this.popup.show(this.getLocalId(index));
			
			return this.popup.isVisible();
		}
		return super.onTap(index);
	}

	/**
	 * Adiciona um {@link OverlayItem}
	 * 
	 * @param localId {@link Long} com o identificador do local a associar ao item
	 * @param overlay {@link OverlayItem} a ser adicionado
	 * @return {@link Boolean} true se o item foi adicionado, false caso contrário; no caso de o modo de edição estar activo, apenas é permitido um {@link OverlayItem}
	 */
	public boolean addOverlay(Long localId, OverlayItem overlay) {
		if(!this.isEditable() || (this.isEditable() && this.overlays.size()<=0)){
			int index = this.overlays.size();
			this.overlays.add(index, overlay);
			this.overlaysMap.put(index, localId);
			
			populate();
			if(this.isEditable() && this.popup!=null){
				this.updateCurrentPosition(overlay.getPoint());
			}
		    return true;
		}
		return false;
	}
	
	/**
	 * Adiciona um {@link OverlayItem}
	 * 
	 * @param localId {@link Long} com o identificador do local a associar ao item
	 * @param overlay {@link OverlayItem} a ser adicionado
	 * @param marker {@link Drawable} com o icone a associar ao item
	 * @return {@link Boolean} true se o item foi adicionado, false caso contrário; no caso de o modo de edição estar activo, apenas é permitido um {@link OverlayItem}
	 */
	public boolean addOverlay(Long localId, OverlayItem overlay, Drawable marker) {
		if(!this.isEditable() || (this.isEditable() && this.size()<=0)){
			overlay.setMarker(boundCenterBottom(marker));
			
			return this.addOverlay(localId, overlay);
		}
		return false;
	}
	
	/**
	 * Obtém o identificador do local associado ao indice do item
	 * @param index {@link Integer} com o indice do item
	 * @return {@link Long} com o identificador do local associado
	 */
	public long getLocalId(int index){
		if(this.overlaysMap!=null){
			return this.overlaysMap.get(index);
		}
		return -1;
	}
	
	/**
	 * Remove o {@link OverlayItem} no indice especificado
	 * @param index {@link Integer} com o indice do {@link OverlayItem} a eliminar
	 */
	public void deleteOverlay(int index){
		if(this.overlays.get(index)!=null && this.overlays.remove(index)!=null){
			this.overlaysMap.remove(index);
		}
		setLastFocusedIndex(-1);
    }
	
	/**
	 * Remove o {@link OverlayItem} especificado
	 * @param overlay Remove o {@link OverlayItem}
	 */
	public void deleteOverlay(OverlayItem overlay){
		this.overlays.remove(overlay);
		populate();
    }
	
	/**
	 * Remove o {@link OverlayItem} especificado
	 * @param overlay Remove o {@link OverlayItem}
	 */
	public void clear(){
		this.setLastFocusedIndex(-1);
		this.overlays.clear();
		this.overlaysMap.clear();
		this.populate();
    }
	
	/**
	 * @return {@link Boolean} true se o mapa é editável, false caso contrário
	 */
	public boolean isEditable() {
		return this.editable;
	}

	/**
	 * @param editable {@link Boolean} true para tornar o mapa editável, false caso contrário
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		if(this.mapView!=null){
			this.mapView.setClickable(true);
		}
	}

	/**
	 * @return {@link GeoPoint} com a posição actual
	 */
	public GeoPoint getCurrentPosition() {
		return this.getCurrentOverlay().currentPosition;
	}
	
	/**
	 * @return {@link MapOverlay} com o overlay actual
	 */
	public MapOverlay getCurrentOverlay(){
		return MapOverlay.currentOverlay;
	}
	
	/**
	 * Define a posição actual com base no ponto especificado. Se o modo de edição estiver activo, será mostrado o popup
	 * @param point {@link GeoPoint} com o ponto a definir
	 */
	private void updateCurrentPosition(GeoPoint point){
		this.currentPosition = point;
		mapView.getController().animateTo(this.currentPosition);
		
		if(this.isEditable() && this.popup!=null){
		    View view = this.popup.getView();
			((TextView)view.findViewById(R.id.title)).setText(mapView.getContext().getString(R.string.label_position));
			((TextView)view.findViewById(R.id.content)).setText(
					mapView.getContext().getString(R.string.label_latitude)+String.valueOf(this.currentPosition.getLatitudeE6()/1000000.0)+
					"\n"+mapView.getContext().getString(R.string.label_longitude)+String.valueOf(this.currentPosition.getLongitudeE6()/1000000.0)
			);
		
			this.popup.show();
		}
	}
	
	/**
	 * Define a posição actual com base no ponto especificado. Se o modo de edição estiver activo, será mostrado o popup
	 * @param point {@link GeoPoint} com o ponto a definir
	 */
	public MapOverlay setCurrentPosition(GeoPoint point){
		MapOverlay copyOfme = this.clone(this.mapView, point);
		if(copyOfme!=null){
			MapOverlay.currentOverlay = copyOfme;
			this.updateCurrentPosition(point);
		}
		return copyOfme;
	}
	
	/**
	 * Cria um objecto idêntico ao item actual, com conteúdos semelhantes, mas no ponto indicado, removendo o objecto actual
	 * @param mapView {@link MapView} com o mapa actual
	 * @param point {@link GeoPoint} com a nova localização
	 * @return {@link MapOverlay} com a nova instância, a instância actual em caso de erro, ou null se não foi possível obter uma nova instẫncia ou a instância actual
	 */
	private MapOverlay clone(MapView mapView, GeoPoint point){
		MapOverlay copyOfme = new MapOverlay(this.bitmapIcon, mapView, this.popup);
		copyOfme.setEditable(this.isEditable());
		
		OverlayItem marker;
		
		Set<Integer> entries = this.overlaysMap.keySet();
	    Iterator<Integer> it = entries.iterator();
	    int a;
	    while (it.hasNext()){
	    	a = it.next();
	    	marker = this.getItem(a);
			copyOfme.addOverlay(this.overlaysMap.get(a), new OverlayItem(point, marker.getTitle() , marker.getSnippet()));
	    }
		
		// If we had a successful copy, return it
		if(mapView.getOverlays().add(copyOfme)){
			mapView.getOverlays().remove(this);
			return copyOfme;
		}
		return null;
	}
}
