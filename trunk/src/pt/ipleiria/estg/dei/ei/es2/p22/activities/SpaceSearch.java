package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.Local;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.LocalType;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.MapOverlay;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.MapPopupPanel;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Window;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnDismissListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SpaceSearch extends MapActivity{
	// Context menu
    private static final int CONTEXT_MENU_EDIT_ITEM=ContextMenu.FIRST+1;
    private static final int CONTEXT_MENU_DELETE_ITEM=ContextMenu.FIRST+2;
    
    // Options menus
    private static final int OPTIONS_MENU_SEARCH = Menu.FIRST + 1;
    private static final int OPTIONS_MENU_VISUALVIEW = Menu.FIRST + 2;
    
    private static final int OPTIONS_MENU_MANAGE = Menu.FIRST + 3;
	private static final int OPTIONS_SUBMENU_MANAGE_ADD_LOCALS = SubMenu.FIRST+4;
	private static final int OPTIONS_SUBMENU_MANAGE_LOCALS_TYPE = SubMenu.FIRST+5;

    private static final int OPTIONS_MENU_MAP = Menu.FIRST + 6;
	private static final int OPTIONS_SUBMENU_MAP_SATELLITE = SubMenu.FIRST+7;
	private static final int OPTIONS_SUBMENU_MAP_TRAFFIC = SubMenu.FIRST+8;
	private static final int OPTIONS_SUBMENU_MAP_STREET = SubMenu.FIRST+9;
	
	private static final int OPTIONS_MENU_HELP = Menu.FIRST + 10;
	
	// Preferences
	private static final String PREFERENCE_SEARCH_VIEW_NAME="search_view";
	private static final String PREFERENCE_SEARCH_VIEW_VALUE_MAP="map";
	private static final String PREFERENCE_SEARCH_VIEW_VALUE_LIST="list";
	
	private static final String PREFERENCE_MAP_SATELLITE_VIEW_NAME="search_view_satellite";
	private static final String PREFERENCE_MAP_SATELLITE_VIEW_VALUE="enabled";
	
	private static final String PREFERENCE_MAP_TRAFFIC_VIEW_NAME="search_view_traffic";
	private static final String PREFERENCE_MAP_TRAFFIC_VIEW_VALUE="enabled";
	
	private static final String PREFERENCE_MAP_STREET_VIEW_NAME="search_view_street";
	private static final String PREFERENCE_MAP_STREET_VIEW_VALUE="enabled";
	
	private static final int DIALOG_HELP=0;
    
	private LinkedList<Local> locals = new LinkedList<Local>();
	private LinkedList<Local> resultsCache = new LinkedList<Local>();
	private DbAdapter dbAdapter;
	private SearchListAdapter searchListAdapter;
	private String lastSearch=null;
	
	private SearchViewFlipper viewFlipper;
	private MapView searchMapView;
	private Context ctx;
	private TextView emptyText;
	private SearchManager searchManager;
	private MapOverlay itemizedoverlay;
	private MapPopupPanel popup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Vamos guardar o contexto num atributo pois é necessário noutros métodos
		this.ctx = this;
		this.searchManager = ((SearchManager) this.getSystemService(Context.SEARCH_SERVICE));

		// Vamos criar o layout para a pesquisa a partir do xml search
		LinearLayout searchLayout = (LinearLayout) View.inflate(this, R.layout.space_search, null);
		RelativeLayout mapLayout = new RelativeLayout(this);
		
		// Obter a lista de resultados para associar os listeners respectivos
		ListView searchListView = (ListView) searchLayout.findViewById(R.id.resultsList);
		// Construção da view com o mapa
		this.searchMapView = new MapView(this, DbAdapter.GOOGLE_MAPS_APY_KEY);
		this.searchMapView.setBuiltInZoomControls(true);
		mapLayout.addView(this.searchMapView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		// Construção do ViewFlipper para trocar entre vistas
		this.viewFlipper = new SearchViewFlipper(this);
		// Associação das vistas ao ViewFlipper
		this.viewFlipper.addView(searchLayout);
		this.viewFlipper.addView(mapLayout);
		
		// Associação do ViewFlipper à actividade actual
		this.setContentView(this.viewFlipper);
		// Adição de mais alguns elementos ao mapa
		
		View.OnClickListener applyEvent = new View.OnClickListener() {
			public void onClick(View v) {
				if(popup!=null && popup.getAssociatedLocalId()>-1){
					Intent i = new Intent(ctx, SpaceView.class);
		            i.putExtra(DbAdapter.KEY_ID, popup.getAssociatedLocalId());
		            startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
		            popup.hide();
				}
			}
		};

		this.popup = new MapPopupPanel(this, this.searchMapView, R.layout.space_search_map_popup_overlay, applyEvent);
        
        View view = this.popup.getView();
		((ImageButton)view.findViewById(R.id.closeButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.hide();
			}
		});
		((ImageButton)view.findViewById(R.id.applyButton)).setOnClickListener(applyEvent);
        
        this.itemizedoverlay = new MapOverlay(BitmapFactory.decodeResource(this.getResources(), R.drawable.default_local_type), this.searchMapView, this.popup);
        
        this.searchMapView.getOverlays().add(this.itemizedoverlay);
		this.searchMapView.setClickable(true);
		
		
		// Construção do ListAdapter e associação ao ListView
		this.searchListAdapter = new SearchListAdapter(this, this.locals);
		searchListView.setAdapter(this.searchListAdapter);
		// Associar um listener ao clique de um resultado na lista
        searchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long id) {
				Intent i = new Intent(ctx, SpaceView.class);
	            i.putExtra(DbAdapter.KEY_ID, id);
	            startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
			}
		});

		// Adicionar os itens do menu de contexto
        searchListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(){ 
            @Override 
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
                 menu.add(0, CONTEXT_MENU_EDIT_ITEM, 0, R.string.ctx_edit_btn);
                 menu.add(0, CONTEXT_MENU_DELETE_ITEM, 1, R.string.ctx_delete_btn);
            }
		});
        
        this.emptyText = (TextView) searchLayout.findViewById(R.id.etSearchEmpty);
        
        // Quando a caixa de pesquisa é ocultada e se não tivermos resultados, definir o texto inicial
        this.searchManager.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				if(locals.isEmpty()){
					emptyText.setText(getString(R.string.search_start_msg));
				}
			}
		});
		
		// Instanciar o adaptador para acesso à camada de dados
		this.dbAdapter = new DbAdapter(this);
        this.dbAdapter.open();
        
        // Define as preferências de visualização da procura
        String preference = this.dbAdapter.getPreference(SpaceSearch.PREFERENCE_SEARCH_VIEW_NAME);
        if(preference!=null && preference.equals(SpaceSearch.PREFERENCE_SEARCH_VIEW_VALUE_MAP)){
        	this.switchToMapSearchMode();
        }else if(this.mapSearchMode()){
			this.switchToListSearchMode();
		}
        
        // Define a preferência para a vista de satélite
        preference = this.dbAdapter.getPreference(SpaceSearch.PREFERENCE_MAP_SATELLITE_VIEW_NAME);
        this.searchMapView.setSatellite(preference!=null && preference.equals(SpaceSearch.PREFERENCE_MAP_SATELLITE_VIEW_VALUE));
        
        // Define a preferência para a vista de tráfego
        preference = this.dbAdapter.getPreference(SpaceSearch.PREFERENCE_MAP_TRAFFIC_VIEW_NAME);
        this.searchMapView.setTraffic(preference!=null && preference.equals(SpaceSearch.PREFERENCE_MAP_TRAFFIC_VIEW_VALUE));
        
        // Define a preferência para a vista de rua
        preference = this.dbAdapter.getPreference(SpaceSearch.PREFERENCE_MAP_STREET_VIEW_NAME);
        this.searchMapView.setStreetView(preference!=null && preference.equals(SpaceSearch.PREFERENCE_MAP_STREET_VIEW_VALUE));
        
        // Verificar o Intent pelo qual a actividade foi lançada		
		this.handleIntent(getIntent());
		
		// Reset aos elementos gráficos
		this.init();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if(this.lastSearch!=null){
			this.search(this.lastSearch);
		}else{
			updateResultsList();
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
	public void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//this.init();
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id){
			case DIALOG_HELP: 
				// janela de ajuda
				dialog = new Dialog(this.ctx);
				dialog.setContentView(R.layout.localize_help);
				dialog.setTitle(this.getString(R.string.title_help));
		        WebView wv = (WebView)dialog.findViewById(R.id.helpWebView);
		        wv.loadData("<html><head></head><body>"+this.getString(R.string.localize_label_loading)+"</body></html>", "text/html", "UTF-8");
		        wv.loadUrl("file:///android_asset/help/help.htm");
		        return dialog;
		    default:
		    	
		}
        
		return super.onCreateDialog(id);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final Local local = (Local) this.searchListAdapter.getItem(menuInfo.position);
		
		switch (item.getItemId()) { 
			case CONTEXT_MENU_EDIT_ITEM:
				try{
					Intent i = new Intent(this, SpaceEdit.class);
			        i.putExtra(DbAdapter.KEY_ID, local.getId());
			        startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
			        return true;
				}catch (Exception e) {
					Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
				} 
				break;
			
			case CONTEXT_MENU_DELETE_ITEM:
				final Context ctx = this;
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setMessage(getString(R.string.delete_local_msg)+" '"+local.getName()+"'?");
				dialog.setCancelable(false);
				dialog.setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						if(dbAdapter.removeLocalByID(local.getId())){
							Toast.makeText(ctx, getString(R.string.search_space_deleted), Toast.LENGTH_LONG).show();
							init();
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
			}
		return super.onContextItemSelected(item);
	}
	
	/**
	 * Para criar/actualizar o menu de opções nesta actividade
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		((SearchManager) this.getSystemService(Context.SEARCH_SERVICE)).stopSearch();
		menu.clear();
		// activa a possibilidade de atalhos de teclado com letras
		menu.setQwertyMode(true);
		
		MenuItem item = menu.add(0, OPTIONS_MENU_SEARCH, 0, R.string.ctx_search_btn);
		item.setAlphabeticShortcut('s');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_search, 32, 32));
				
		if(this.mapSearchMode()){
			item = menu.add(0, OPTIONS_MENU_VISUALVIEW, 0, R.string.ctx_textview_btn);
			item.setAlphabeticShortcut('t');
			item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_list, 32, 32));
			
			SubMenu subMenuMap = menu.addSubMenu(0, OPTIONS_MENU_MAP, 0, R.string.label_map_options);
			item.setAlphabeticShortcut('p');
			subMenuMap.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_view, 32, 32));
			subMenuMap.add(0, OPTIONS_SUBMENU_MAP_SATELLITE, 0, (this.searchMapView.isSatellite())?this.getString(R.string.label_disable_satellite_mode):this.getString(R.string.label_enable_satellite_mode));
			subMenuMap.add(0, OPTIONS_SUBMENU_MAP_TRAFFIC, 0, (this.searchMapView.isTraffic())?this.getString(R.string.label_disable_traffic_mode):this.getString(R.string.label_enable_traffic_mode));
			subMenuMap.add(0, OPTIONS_SUBMENU_MAP_STREET, 0, (this.searchMapView.isStreetView())?this.getString(R.string.label_disable_street_mode):this.getString(R.string.label_enable_street_mode));
		}else{
			item = menu.add(0, OPTIONS_MENU_VISUALVIEW, 0, R.string.ctx_visualview_btn);
			item.setAlphabeticShortcut('v');
			item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_mapmode, 32, 32));
		}
		SubMenu subMenu = menu.addSubMenu(0, OPTIONS_MENU_MANAGE, 0, R.string.ctx_manage_btn);
		subMenu.add(0, OPTIONS_SUBMENU_MANAGE_ADD_LOCALS, 0, R.string.ctx_add_locals);
		subMenu.add(0, OPTIONS_SUBMENU_MANAGE_LOCALS_TYPE, 0, R.string.ctx_manage_locals_types_btn);
		subMenu.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_preferences, 32, 32));
		
		item = menu.add(0, OPTIONS_MENU_HELP, 0, R.string.title_help);
		item.setAlphabeticShortcut('h');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_help, 32, 32));
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	/**
	 * Ao seleccionar uma opção do menu de opções
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case OPTIONS_MENU_SEARCH:
				//init();
				this.onSearchRequested();
				return true;
				
			case OPTIONS_MENU_HELP:
				this.showDialog(DIALOG_HELP);
				return true;
			
			case OPTIONS_MENU_VISUALVIEW:
				this.viewFlipper.setInAnimation(inFromRightAnimation());
				this.viewFlipper.setOutAnimation(outToLeftAnimation());
				this.viewFlipper.showNext();
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceSearch.PREFERENCE_SEARCH_VIEW_NAME, this.mapSearchMode()?SpaceSearch.PREFERENCE_SEARCH_VIEW_VALUE_MAP:SpaceSearch.PREFERENCE_SEARCH_VIEW_VALUE_LIST);
				return true;
				
			case OPTIONS_SUBMENU_MAP_SATELLITE:
				this.searchMapView.setSatellite(!this.searchMapView.isSatellite());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceSearch.PREFERENCE_MAP_SATELLITE_VIEW_NAME, this.searchMapView.isSatellite()?SpaceSearch.PREFERENCE_MAP_SATELLITE_VIEW_VALUE:"");
				Toast.makeText(this, this.searchMapView.isSatellite()?this.getString(R.string.info_mode_satellite_enabled):this.getString(R.string.info_mode_satellite_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_SUBMENU_MAP_TRAFFIC:
				this.searchMapView.setTraffic(!this.searchMapView.isTraffic());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceSearch.PREFERENCE_MAP_TRAFFIC_VIEW_NAME, this.searchMapView.isSatellite()?SpaceSearch.PREFERENCE_MAP_TRAFFIC_VIEW_VALUE:"");
				Toast.makeText(this, this.searchMapView.isTraffic()?this.getString(R.string.info_mode_traffic_enabled):this.getString(R.string.info_mode_traffic_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_SUBMENU_MAP_STREET:
				this.searchMapView.setStreetView(!this.searchMapView.isStreetView());
				// Guarda a definição actual
				this.dbAdapter.setPreference(SpaceSearch.PREFERENCE_MAP_STREET_VIEW_NAME, this.searchMapView.isSatellite()?SpaceSearch.PREFERENCE_MAP_STREET_VIEW_VALUE:"");
				Toast.makeText(this, this.searchMapView.isStreetView()?this.getString(R.string.info_mode_street_enabled):this.getString(R.string.info_mode_street_disabled), Toast.LENGTH_LONG).show();
				return true;
				
			case OPTIONS_SUBMENU_MANAGE_LOCALS_TYPE:
		        startActivityForResult(new Intent(this, SpaceTypeManage.class), DbAdapter.REQUEST_LOCAL_TYPES_UPDATE);
				return true;
			
			case OPTIONS_SUBMENU_MANAGE_ADD_LOCALS:
				Intent i = new Intent(this, SpaceEdit.class);
		        i.putExtra(DbAdapter.KEY_ID, new Long(-1));
		        startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
				return true;
				
			default:
				return super.onOptionsItemSelected(item);	
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Executado sempre que é solicitada uma pesquisa; neste caso, copiamos o texto da pesquisa anterior para a caixa de pesquisa
	 */
	@Override
	public boolean onSearchRequested() {
		boolean result = super.onSearchRequested();
		this.startSearch(this.lastSearch, true, null, false);
        return result;
	}

	/**
	 * Vista para a lista de resultados
	 * @author cesperanc
	 */
	private class SearchListAdapter extends BaseAdapter{
		private LinkedList<Local> locals;
		private Context context;
	    
		public SearchListAdapter(Context context, LinkedList<Local> locals){
			this.context = context;
			this.locals = locals;
		}

		/* (non-Javadoc)
		 * @see android.widget.BaseAdapter#notifyDataSetChanged()
		 */
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			
			//updateVisualItens();
		}

		@Override
		public int getCount() {
			return this.locals.size();
		}

		@Override
		public Local getItem(int position) {
			return this.locals.get(position);
		}

		@Override
		public long getItemId(int position) {
			return this.locals.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
	        if (convertView == null) {
	             // Make up a new view
	        	 LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        	 view = inflater.inflate(R.layout.space_search_result, null);
	        }
	        
			Local local = this.getItem(position);
			
			TextView t = (TextView) view.findViewById(R.id.searchItemTitle);
	        t.setText(local.getLocalTypeName()+": "+local.getName());
	        
	        t = (TextView) view.findViewById(R.id.searchItemDescription);
	        t.setText(Utils.shrinkText(local.getDescription(), 100));
	        
	        LocalType localType = dbAdapter.getLocalType(local.getLocalTypeId());
	        ImageView iv = (ImageView) view.findViewById(R.id.spaceTypeIcon);
	        iv.setImageBitmap(localType.getIconBitmap(iv.getWidth(), iv.getHeight(), this.context));
	        
	        return view;
		}	
	}
	
	/**
	 * Para contornar um bug na API relativo ao ViewFlipper
	 * @see http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob_plain;f=core/java/android/widget/ViewFlipper.java;hb=HEAD
	 */
	private class SearchViewFlipper extends ViewFlipper {
		public SearchViewFlipper(Context context) {
			super(context);
		}
		/**
		 * Para contornar o erro do SDK 7 e 8 no ViewFlipper ao rodar o ecrã
		 */
		@Override
		protected void onDetachedFromWindow() {
		    try {
		        super.onDetachedFromWindow();
		    }
		    catch (IllegalArgumentException e) {
		        stopFlipping();
		    }
		}

	}
	
	/**
	 * Método de pesquisa
	 * @param search
	 */
	private void search(String search){
		if(search.toLowerCase().equals("--help") || search.equals("-?")){
			this.showDialog(DIALOG_HELP);
		}else{
			final ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.search_searching), true);
			this.emptyText.setText("");
			this.locals.clear();
			this.searchListAdapter.notifyDataSetChanged();
			this.lastSearch = search;
			
			new Thread() {
				public void run() {
					try{
						resultsCache.clear();
						resultsCache.addAll(dbAdapter.search(lastSearch));
						
						mHandler.post(mUpdateResults);
					}catch(Exception e){
						Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
					}finally{
						dialog.dismiss();
					}
				}
			}.start();
		}
	}
	
	/**
	 * Inicialização da lista de resultados
	 */
	private void init(){
		this.locals.clear();
		this.searchListAdapter.notifyDataSetChanged();
		if(this.lastSearch==null){
			this.onSearchRequested();
		}else{
			this.onStart();
		}
	}
	
	/**
	 * Gere o intent search
	 * @param intent a analisar
	 */
	private void handleIntent(Intent intent) {
	    if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	search(intent.getStringExtra(SearchManager.QUERY));
	    }
	}
	
	/**
	 * Actualiza a lista de resultados
	 */
	private void updateResultsList(){
		this.locals.clear();
		this.locals.addAll(this.resultsCache);
		this.searchListAdapter.notifyDataSetChanged();
		
		this.emptyText.setText(this.locals.isEmpty()?getString(R.string.no_results):"");
		if(this.lastSearch!=null && !this.lastSearch.equals("")){
			Toast.makeText(ctx, getString(R.string.x_locals_found, this.locals.size()), Toast.LENGTH_LONG).show();
		}
		
		//if(this.mapSearchMode()){
			updateMapItens();
		//}
	}
	
	/**
	 * Actualiza os itens no mapa com base na lista de locais interna
	 */
	private void updateMapItens(){
		Local local;
		LocalType localType;
		OverlayItem overlayitem;
		
		// Limpar os itens anteriores
		this.itemizedoverlay.clear();
		
		// Ocultar o popup
		this.popup.hide();
		
		// Percorrer a lista de locais e adicioná-los ao mapa
		Iterator<Local> i = this.locals.iterator();
		while(i.hasNext()){
			local = i.next();
			overlayitem = new OverlayItem(new GeoPoint(local.getGpsLat(), local.getGpsLon()), local.getName(), local.getDescription());
			localType = this.dbAdapter.getLocalType(local.getLocalTypeId());
			
			this.itemizedoverlay.addOverlay(local.getId(), overlayitem, new BitmapDrawable(localType.getIconBitmap(37, 37, this.ctx)));
		}
		
		// Forçar o mapa a ser redesenhado
		this.searchMapView.invalidate();
	}
	
	/**
	 * Detecta se a pesquisa está a ser efectuada em modo mapa ou não
	 * @return boolean true, se o mapa estiver visivel, false caso contrário
	 */
	private boolean mapSearchMode(){
		return this.searchMapView.isShown();
	}
	
	/**
	 * Permuta o modo de pesquisa para o mapa
	 */
	private void switchToMapSearchMode(){
		if(!this.mapSearchMode()){
			this.viewFlipper.showNext();
		}
	}
	
	/** 
	 * Permuta o modo de pesquisa para lista
	 */
	private void switchToListSearchMode(){
		if(this.mapSearchMode()){
			this.viewFlipper.showNext();
		}
	}
	
	/**
	 * handler para o retorna à thread gráfica
	 */
	private final Handler mHandler = new Handler();

    /**
	 * Runnable para actualizar a lista de resultados
	 */
    private final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	updateResultsList();
        }
    };
	
    /**
     * Animação para ViewFlipper
     * @return {@link Animation}
     */
	private Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}
	
	/**
     * Animação para ViewFlipper
     * @return {@link Animation}
     */
	private Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	/**
     * Animação para ViewFlipper
     * @return {@link Animation}
     */
	@SuppressWarnings("unused")
	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}
	
	/**
     * Animação para ViewFlipper
     * @return {@link Animation}
     */
	@SuppressWarnings("unused")
	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
}
