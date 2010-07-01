package pt.ipleiria.estg.dei.ei.es2.p22;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import pt.ipleiria.estg.dei.ei.es2.p22.activities.SpaceSearch;
import pt.ipleiria.estg.dei.ei.es2.p22.database.DbAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class Localize extends Activity {
	Activity activity;
	private boolean canSkip=false;
	private TextView status;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.activity = this;
        this.setContentView(R.layout.localize_splash);
        this.status = (TextView) this.findViewById(R.id.tvStartupLabel);
        this.status.setText(getString(R.string.localize_label_loading));
        
        new Thread() {
			public void run() {
				try{
					// Criar/actualizar a base de dados
					DbAdapter dbAdapter = new DbAdapter(activity);
			        dbAdapter.open();
			        
			        // Se a base de dados tiver sido criada ou se não existirem locais, inserir alguns locais na base de dados
			        if(dbAdapter.databaseCreated()||dbAdapter.getAllLocals().size()<=0){
			        	copyIcons();
			        	insertExampleData(dbAdapter);
			        }
			        dbAdapter.close();

			        mHandler.postDelayed(goToSearch, DbAdapter.SPLASH_TIME);
			        canSkip=true;
				}catch(Exception e){
					Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
					goToSearch();
				}
			}
		}.start();
    }
    
    /**
	 * handler para o retornar à thread gráfica
	 */
	private final Handler mHandler = new Handler();

    /**
	 * Runnable para actualizar ocultar a janela de loading e destruir a actividade
	 */
    private final Runnable goToSearch = new Runnable() {
        public void run() {
        	goToSearch();
        }
    };
    
    /**
     * Inicia a próxima actividade, oculta a janela de progresso e termina a actividade actual
     */
    private void goToSearch(){
        status.setText(getString(R.string.localize_label_welcome));
    	startActivity(new Intent(activity, SpaceSearch.class));
    	activity.finish();
    }
    
    /**
     * Avança, se possível, para a actividade de pesquisa
     */
    @Override
	public boolean onTouchEvent(MotionEvent event){
		if (this.canSkip && event.getAction() == MotionEvent.ACTION_DOWN){
			// Remove a chamada ao goToSearch da lista
			mHandler.removeCallbacks(goToSearch);
			// Inicia a próxima actividade e termina a actual
			goToSearch();
		}
		return true;
	}
    
    /**
     * Insere alguns locais de exemplo
     * 
     * @param dbAdapter {@link DbAdapter} a utilizar para as operações
     */
    private void insertExampleData(DbAdapter dbAdapter){
    	if(dbAdapter.isOpen()){
    		File destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    	// Inserções de tipos de locais
	        dbAdapter.insertLocalType("Universidade", destinationPath.toString()+"/university.png");
	        dbAdapter.insertLocalType("Edifício", destinationPath.toString()+"/villa-tourism.png");
	        dbAdapter.insertLocalType("Biblioteca", destinationPath.toString()+"/library.png");
	        dbAdapter.insertLocalType("Sala de aula", destinationPath.toString()+"/school.png");
	        dbAdapter.insertLocalType("Laboratório", destinationPath.toString()+"/laboratory.png");
	        dbAdapter.insertLocalType("Bar", destinationPath.toString()+"/fastfood.png");
	        dbAdapter.insertLocalType("Refeitório", destinationPath.toString()+"/restaurant.png");
	        dbAdapter.insertLocalType("Auditório", destinationPath.toString()+"/dates.png");
	        // Inserções de locais
	        dbAdapter.insertLocal("ESTG", "Escola Superior de Tecnologia e Gestão de Leiria \nCampus 2 do Instituto Politécnico de Leiria \nPágina web: http://www.estg.ipleiria.pt", 39734883, -8821787, 0, "Universidade");
	        dbAdapter.insertLocal("Edificio A", "Edificio A do Campus 2 da ESTG, Portugal", 39735565, -8820985, 0, "Edifício");
	        dbAdapter.insertLocal("Edificio B", "Edificio B do Campus 2 da ESTG, Portugal", 39734418, -8821678, 0, "Edifício");
	        dbAdapter.insertLocal("Edificio C", "Edificio C do Campus 2 da ESTG, Portugal", 39733663, -8822026, 0, "Edifício");
	        dbAdapter.insertLocal("Edificio D", "Edificio D do Campus 2 da ESTG, Portugal", 39734331, -8821121, 0, "Edifício");
	        dbAdapter.insertLocal("Biblioteca José Saramago", "Biblioteca do Campus 2 da ESTG, Portugal", 39733258, -8820776, 0, "Biblioteca");
	        dbAdapter.insertLocal("Refeitório A", "Refeitório A do Campus 2 da ESTG, Portugal", 39733341, -8821522, 0, "Refeitório");
	        dbAdapter.insertLocal("Refeitório B", "Refeitório B do Campus 2 da ESTG, Portugal", 39734711, -8822475, 0, "Refeitório");
	        dbAdapter.insertLocal("Bar 1", "Bar 1 do Campus 2 da ESTG, Portugal", 39735631, -8820737, 0, "Bar");
	        dbAdapter.insertLocal("Bar 2", "Bar 2 do Campus 2 da ESTG, Portugal", 39733246, -8821665, 1, "Bar");
	        dbAdapter.insertLocal("Bar 3", "Bar 3 do Campus 2 da ESTG, Portugal", 39734738, -8822441, 1, "Bar");
	        dbAdapter.insertLocal("Auditório 1", "Auditório 1 do Campus 2 da ESTG, Portugal", 39734558, -8821777, -1, "Auditório");
    	}
    }
    
    /**
     * Copia os icones da pasta assets para o dispositivo
     */
    private void copyIcons(){
    	try {
    		//File destinationPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    		File destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    		
    		// Se o directório de destino não existe, vamos tentar criá-lo
    		if(destinationPath!=null && !destinationPath.exists()){
    			destinationPath.mkdirs();
    		}
    		
    		// Se for possivel, copiar os ficheiros para o directório de destino
    		if(destinationPath!=null && destinationPath.isDirectory() && destinationPath.canWrite()){
    			AssetManager am = this.getResources().getAssets();
	    		String iconsPath = "icons";
	    		ArrayList<String> filesToIndex = new ArrayList<String>();
				InputStream sourceInputStream;
		        File destinationFile;
		        OutputStream destinationOutputStream;
		        String icons[] = am.list(iconsPath);
	    		 
				for(int a=0; a<icons.length; a++){
					// Vamos copiar o ficheiro
					sourceInputStream = am.open(iconsPath+"/"+icons[a]);
					destinationFile = new File(destinationPath, icons[a]);
					destinationOutputStream = new FileOutputStream(destinationFile);
					byte[] data = new byte[sourceInputStream.available()];
					sourceInputStream.read(data);
					destinationOutputStream.write(data);
		            sourceInputStream.close();
		            destinationOutputStream.close();

		            filesToIndex.add(destinationFile.toString());
		        	Log.d(DbAdapter.APP_NAME, icons[a]+" copiado para "+destinationFile.toString());
		        }
				// Solicitar a indexação dos ficheiros pelo MediaScanner
	        	MediaScannerConnection.scanFile(this.getApplicationContext(), (String[])filesToIndex.toArray(new String[filesToIndex.size()]), null, null);
	        	
    		}else{
    			Log.e(DbAdapter.APP_NAME, "Sem permissões para copiar os icones para o directório de dados externo ("+destinationPath.getAbsolutePath()+")");
    		}
		}catch(Exception e){
			Log.e(DbAdapter.APP_NAME, "Ocorreu um erro", e);
		}
    }
}