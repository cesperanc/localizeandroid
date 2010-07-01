package pt.ipleiria.estg.dei.ei.es2.p22.utils;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * Classe para apresentar um popup
 * 
 * @see http://github.com/commonsguy/cw-advandroid/tree/master/Maps/EvenNooerYawk/
 * @author Mark Murphy <mmurphy@commonsware.com>, Diogo Serra <codedmind@gmail.com>, Cláudio Esperança <cesperanc@gmail.com>
 */
public class MapPopupPanel {
	View popup;
	ViewGroup parent;
	boolean isVisible=false;
	private long localId;
	
	/**
	 * Cria um novo popup
	 * @param mapActivity {@link MapActivity} que contém os elementos onde este popup vai ser associado
	 * @param mapView {@link MapView} com o {@link ViewGroup} pai para adicionar este popup
	 * @param layout identificador do XML resource layout a utilizar para este popup
	 */
	public MapPopupPanel(MapActivity mapActivity, MapView mapView, int layout){
		this.parent=(ViewGroup) mapView.getParent();
		this.popup=mapActivity.getLayoutInflater().inflate(layout, this.parent, false);
	}
	
	/**
	 * Cria um novo popup
	 * @param mapActivity {@link MapActivity} que contém os elementos onde este popup vai ser associado
	 * @param mapView {@link MapView} com o {@link ViewGroup} pai para adicionar este popup
	 * @param layout identificador do XML resource layout a utilizar para este popup
	 * @param onClick {@link View.OnClickListener} com a função a executar ao clique na janela do popup
	 */
	public MapPopupPanel(MapActivity mapActivity, MapView mapView, int layout, View.OnClickListener onClick){
		this(mapActivity, mapView, layout);
		
		this.popup.setOnClickListener(onClick);
	}
	
	/**
	 * Retorna a view do popup
	 * @return {@link View} do popup
	 */
	public View getView() {
		return(this.popup);
	}
	
	/**
	 * Mostra o popup
	 */
	public void show() {
		this.hide();

		if(this.parent!=null){
			this.parent.addView(this.popup);
			this.isVisible=true;
		}
	}
	
	/**
	 * Mostra o popup e armazena o localId internamente para o caso de ser necessário mudar para uma actividade que necessite deste valors
	 * @param localId {@link Long} com o identificador do local
	 */
	public void show(long localId) {
		this.localId = localId;
		
		this.show();
	}
	
	/**
	 * Oculta o popup
	 */
	public void hide(){
		if(this.isVisible){
			this.isVisible=false;
			((ViewGroup) popup.getParent()).removeView(this.popup);
		}
	}
	
	/**
	 * Devolve o identificador do local associado à janela de popup
	 * @return {@link Long} com o identificador interno do local
	 */
	public long getAssociatedLocalId(){
		return this.localId;
	}
	
	/**
	 * Devolve a visibilidade do elemento
	 * @return {@link Boolean} true se está visivel, false caso contrário
	 */
	public boolean isVisible() {
		return this.isVisible;
	}
}