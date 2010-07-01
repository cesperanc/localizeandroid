package pt.ipleiria.estg.dei.ei.es2.p22.activities;

import java.util.LinkedList;

import pt.ipleiria.estg.dei.ei.es2.p22.R;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.LocalType;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Utils;
import pt.ipleiria.estg.dei.ei.es2.p22.utils.Window;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SpaceTypeManage extends ListActivity{
	// Context menu
    private static final int CONTEXT_MENU_EDIT_ITEM=ContextMenu.FIRST+1;
    private static final int CONTEXT_MENU_DELETE_ITEM=ContextMenu.FIRST+2;
    
    // Options menus
    private static final int OPTIONS_MENU_ADD_LOCAL_TYPE = Menu.FIRST + 1;
    
	private LinkedList<LocalType> localTypes = new LinkedList<LocalType>();
	private DbAdapter dbAdapter;
	private LocalTypesListAdapter localTypesListAdapter;
	
	private Context ctx;
	private TextView emptyResultsText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Vamos guardar o contexto num atributo pois é necessário noutros métodos
		this.ctx = this;
		this.setContentView(R.layout.space_type_manage);
		
		// Obter a lista de resultados para associar os listeners respectivos
		ListView localTypesListView = this.getListView();
		
		// Construção do ListAdapter e associação ao ListView
		this.localTypesListAdapter = new LocalTypesListAdapter(this, this.localTypes);
		localTypesListView.setAdapter(this.localTypesListAdapter);
		// Associar um listener ao clique de um resultado na lista
		localTypesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long id) {
				Intent i = new Intent(ctx, SpaceTypeEdit.class);
	            i.putExtra(DbAdapter.KEY_ID, id);
	            startActivityForResult(i, DbAdapter.REQUEST_LOCAL_TYPES_UPDATE);
			}
		});

		// Adicionar os itens do menu de contexto
		localTypesListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener(){ 
            @Override 
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
                 menu.add(0, CONTEXT_MENU_EDIT_ITEM, 0, R.string.ctx_edit_btn);
                 menu.add(0, CONTEXT_MENU_DELETE_ITEM, 1, R.string.ctx_delete_btn);
            }
		});
        
        this.emptyResultsText = (TextView) this.findViewById(R.id.tvEmptyList);
        
		// Instanciar o adaptador para acesso à camada de dados
		this.dbAdapter = new DbAdapter(this);
        this.dbAdapter.open();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		updateResultsList();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.updateResultsList();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		
		final LocalType localType = (LocalType) this.localTypesListAdapter.getItem(menuInfo.position);
		
		switch (item.getItemId()) { 
			case CONTEXT_MENU_EDIT_ITEM:
				try{
					Intent i = new Intent(this, SpaceTypeEdit.class);
			        i.putExtra(DbAdapter.KEY_ID, localType.getId());
			        startActivityForResult(i, DbAdapter.REQUEST_LOCAL_UPDATE);
			        return true;
				}catch (Exception e) {
					Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
				} 
				break;
			
			case CONTEXT_MENU_DELETE_ITEM:
				final Context ctx = this;
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setMessage(getString(R.string.delete_local_type_msg)+" '"+localType.getName()+"'?");
				dialog.setCancelable(false);
				dialog.setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						if(dbAdapter.removeLocalType(localType.getId())){
							Toast.makeText(ctx, getString(R.string.local_type_deleted), Toast.LENGTH_LONG).show();
							updateResultsList();
						}else{
							Window.showError(ctx, null, getString(R.string.error_deleting_local_type));
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
		menu.clear();
		// activa a possibilidade de atalhos de teclado com letras
		menu.setQwertyMode(true);
		
		MenuItem item = menu.add(0, OPTIONS_MENU_ADD_LOCAL_TYPE, 0, R.string.label_add_local_type);
		item.setAlphabeticShortcut('a');
		item.setIcon(Utils.resizeImage(this, R.drawable.ic_menu_add, 32, 32));
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	/**
	 * Ao seleccionar uma opção do menu de opções
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			
			case OPTIONS_MENU_ADD_LOCAL_TYPE:
				Intent i = new Intent(this, SpaceTypeEdit.class);
		        i.putExtra(DbAdapter.KEY_ID, new Long(-1));
		        startActivityForResult(i, DbAdapter.REQUEST_LOCAL_TYPES_UPDATE);
				return true;
				
			default:
				return super.onOptionsItemSelected(item);	
		}
	}

	/**
	 * Vista para a lista de resultados
	 * @author cesperanc
	 */
	private class LocalTypesListAdapter extends BaseAdapter{
		private LinkedList<LocalType> localTypes;
		private Context context;
	    
		public LocalTypesListAdapter(Context context, LinkedList<LocalType> localTypes){
			this.context = context;
			this.localTypes = localTypes;
		}

		@Override
		public int getCount() {
			return this.localTypes.size();
		}

		@Override
		public LocalType getItem(int position) {
			return this.localTypes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return this.localTypes.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
	        if (convertView == null) {
	        	 LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        	 view = inflater.inflate(R.layout.space_type_manage_item, null);
	        }
	        
	        LocalType localType = this.getItem(position);
			
			TextView t = (TextView) view.findViewById(R.id.spaceTypeName);
	        t.setText(localType.getName());
	        
	        ImageView iv = (ImageView) view.findViewById(R.id.spaceTypeIcon);
	        iv.setImageBitmap(localType.getIconBitmap(iv.getWidth(), iv.getHeight(), this.context));
	        
	        return view;
		}	
	}
	
	/**
	 * Actualiza a lista de resultados
	 */
	private void updateResultsList(){
		final ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.info_updating), true);
		this.emptyResultsText.setText("");
		this.localTypes.clear();
		this.localTypesListAdapter.notifyDataSetChanged();
		
		new Thread() {
			public void run() {
				try{
					localTypes.addAll(dbAdapter.getAllLocalTypes());
					mHandler.post(mUpdateResults);
				}catch(Exception e){
					Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
				}finally{
					dialog.dismiss();
				}
			}
		}.start();
		
	}
	
	private void finishUpdate(){
		this.localTypesListAdapter.notifyDataSetChanged();
		this.emptyResultsText.setText(this.localTypes.isEmpty()?getString(R.string.label_empty_local_types_list):"");
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
        	finishUpdate();
        }
    };
}
