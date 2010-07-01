package pt.ipleiria.estg.dei.ei.es2.p22.database;

import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.Local;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.LocalType;
import pt.ipleiria.estg.dei.ei.es2.p22.database.elements.SimpleObject;

import java.util.LinkedList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Classe base da camada de acesso à base de dados
 * @author cesperanc
 */
public class DbAdapter {
	public static final String APP_NAME = "locallize@ndroid";
    private static final String DATABASE_NAME = "locallize@android";
    private static final int DATABASE_VERSION = 2;
    public static final String GOOGLE_MAPS_APY_KEY = "06ldeaFjfWr-n_n7H-5pVnKsW3S6qP82-MZfeDQ";
    public static final int SPLASH_TIME = 3000;
    
    private static final String TABLE_NAME_LOCALS = "places_tbl";
    private static final String TABLE_NAME_LOCALS_SEARCH = "places_search_tbl";
    private static final String TABLE_NAME_LOCALS_TYPE = "places_type_tbl";
    private static final String TABLE_NAME_PREFERENCES = "preferences_tbl";
    private static final String TABLE_NAME_MEDIA = "media_tbl";
    private static final String TABLE_NAME_MEDIA_TYPE = "media_type_tbl";
    private static final String TABLE_NAME_IMAGES = "images_tbl";
    
    public static final String KEY_ID = "_id";
    public static final String KEY_LOCAL_NAME = "name";
    public static final String KEY_LOCAL_DESCRIPTION = "description";
    public static final String KEY_LOCAL_GPS = "gps";
    public static final String KEY_LOCAL_GPS_LAT = "gps_lat";
    public static final String KEY_LOCAL_GPS_LON = "gps_lon";
    public static final String KEY_LOCAL_ALTITUDE = "altitude";
    public static final String KEY_LOCAL_TYPE = "type";
    public static final String KEY_LOCAL_TYPE_NAME = "name";
    public static final String KEY_LOCAL_TYPE_ICON = "icon";
    public static final String KEY_PREFERENCE_NAME = "name";
    public static final String KEY_PREFERENCE_VALUE = "value";
	
	private final Context context;
	private DbHelper dbHelper;
    private SQLiteDatabase sqlDatabase;
    
    public static long GENERIC_ERROR = -1;
    public static long GENERIC_NO_RESULTS = -2;
    public static long LOCAL_INSERT_FAILED = -10;
    public static long LOCAL_INSERT_FAILED_LOCAL_ID_EXISTS = -11;
    public static long LOCAL_INSERT_FAILED_LOCAL_GPS_EXISTS = -12;
    public static long LOCALTYPE_INSERT_FAILED = -20;
    public static long LOCALTYPE_INSERT_FAILED_LOCALTYPE_ID_EXISTS = -21;
    public static long LOCALTYPE_INSERT_FAILED_LOCALTYPE_NAME_EXISTS = -22;
    
    public static int REQUEST_LOCAL_TYPES_UPDATE = 10;
    public static int REQUEST_SET_MAP_LOCATION = 11;
    public static int REQUEST_LOCAL_UPDATE = 12;
    public static int REQUEST_LOCAL_TYPE_ICON = 13;
	
	/**
	 * Sub-classe para gerir a criação e actualização da base de dados do programa
	 * @author cesperanc
	 */
	private static class DbHelper extends SQLiteOpenHelper {
		private boolean databaseCreated=false;
		/**
		 * Constructor
		 * @param context {@link Context} com o contexto da aplicação
		 */
		DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.i(APP_NAME, "A criar a base de dados...");
        	
        	try{
	        	// Criamos as tabelas
        		// Tabela com os tipos de media
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_MEDIA_TYPE+"] (" +
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY ON CONFLICT ABORT AUTOINCREMENT, " +
	        			"[name] VARCHAR NOT NULL ON CONFLICT ROLLBACK UNIQUE ON CONFLICT ROLLBACK" +
	        		");"
	        	);
        		// Tabela para a media
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_MEDIA+"] (" +
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY ON CONFLICT ABORT AUTOINCREMENT, " +
	        			"[url] TEXT NOT NULL ON CONFLICT ROLLBACK, " +
	        			"[name] VARCHAR, " +
	        			"[type] INTEGER NOT NULL ON CONFLICT ROLLBACK DEFAULT 1 REFERENCES ["+TABLE_NAME_MEDIA_TYPE+"]("+KEY_ID+") ON DELETE RESTRICT ON UPDATE CASCADE" +
        			");"
	        	);
        		// Tabela para os tipos de locais
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_LOCALS_TYPE+"] (" +
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY ON CONFLICT ABORT AUTOINCREMENT, " +
	        			"["+KEY_LOCAL_TYPE_NAME+"] VARCHAR NOT NULL ON CONFLICT ABORT UNIQUE ON CONFLICT ABORT, " +
	        			"["+KEY_LOCAL_TYPE_ICON+"] TEXT " +
	        		");"
	        	);
        		// Tabela com os locais
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_LOCALS+"] (" + 
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY AUTOINCREMENT, " + 
	        			"["+KEY_LOCAL_NAME+"] TEXT NOT NULL ON CONFLICT ABORT, " + 
	        			"["+KEY_LOCAL_DESCRIPTION+"] TEXT, " +
	        			"["+KEY_LOCAL_GPS_LON+"] INTEGER NOT NULL, " + 
	        			"["+KEY_LOCAL_GPS_LAT+"] INTEGER NOT NULL, " + 
	        			"["+KEY_LOCAL_ALTITUDE+"] INTEGER DEFAULT 0, " +
	        			"["+KEY_LOCAL_TYPE+"] INTEGER NOT NULL ON CONFLICT ABORT DEFAULT 1 REFERENCES ["+TABLE_NAME_LOCALS_TYPE+"]("+KEY_ID+") ON DELETE RESTRICT ON UPDATE CASCADE, " +
    					"CONSTRAINT 'gps_with_altitude' UNIQUE ( ["+KEY_LOCAL_GPS_LAT+"], ["+KEY_LOCAL_GPS_LON+"], ["+KEY_LOCAL_ALTITUDE+"] ) ON CONFLICT FAIL " +
	        		");"
	        	);
        		        		
        		// Tabela para virtual para a pesquisa de locais
        		db.execSQL(
        			"CREATE VIRTUAL TABLE ["+TABLE_NAME_LOCALS_SEARCH+"] USING FTS3 (" +
        				"["+KEY_ID+"] INTEGER PRIMARY KEY," +
        				"["+KEY_LOCAL_NAME+"] TEXT," +
        				"["+KEY_LOCAL_TYPE+"] TEXT," +
        				"["+KEY_LOCAL_DESCRIPTION+"] TEXT," +
        				"["+KEY_LOCAL_GPS+"] TEXT," +
        				"["+KEY_LOCAL_ALTITUDE+"] TEXT" +
    				");"
        		);
        		
        		// Tabela para as imagens
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_IMAGES+"] (" +
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY AUTOINCREMENT, " +
	        			"[name] VARCHAR NOT NULL, " +
	        			"[date] BLOB, " +
	        			"[local] INTEGER NOT NULL ON CONFLICT ROLLBACK REFERENCES ["+TABLE_NAME_LOCALS+"]("+KEY_ID+") ON DELETE CASCADE ON UPDATE CASCADE" +
	        		");"
	        	);
        		
        		// Tabela para as preferências
        		db.execSQL(
        			"CREATE TABLE IF NOT EXISTS ["+TABLE_NAME_PREFERENCES+"] (" +
	        			"["+KEY_ID+"] INTEGER PRIMARY KEY ON CONFLICT ABORT AUTOINCREMENT, " +
	        			"["+KEY_PREFERENCE_NAME+"] VARCHAR NOT NULL ON CONFLICT ABORT UNIQUE ON CONFLICT ABORT, " +
	        			"["+KEY_PREFERENCE_VALUE+"] VARCHAR NOT NULL" +
	        		");"
	        	);
        		
        		// Trigger para bloquear a eliminação de um tipo quando o mesmo está associado a um registo de media
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_MEDIA+"_"+TABLE_NAME_MEDIA_TYPE+"_del1] " +
	        			"BEFORE DELETE ON ["+TABLE_NAME_MEDIA_TYPE+"] WHEN (old.["+KEY_ID+"] IN (SELECT [type] FROM ["+TABLE_NAME_MEDIA+"] GROUP BY [type])) BEGIN " +
        				"SELECT RAISE( ABORT, 'Foreign key violated: fk_"+TABLE_NAME_MEDIA+"_"+TABLE_NAME_MEDIA_TYPE+"_del1' ); " +
        			"END;"
	        	);
        		// Trigger para actualizar o id do tipo de media, em caso de alterações
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_MEDIA+"_"+TABLE_NAME_MEDIA_TYPE+"_upd1] BEFORE UPDATE ON ["+TABLE_NAME_MEDIA_TYPE+"] WHEN (old.["+KEY_ID+"] IN (SELECT [type] FROM ["+TABLE_NAME_MEDIA+"] GROUP BY [type])) BEGIN " +
	        			"UPDATE ["+TABLE_NAME_MEDIA+"] SET [type] = new.["+KEY_ID+"] WHERE [type] = old.["+KEY_ID+"]; " +
	        		"END;"
	        	);
        		// Trigger para eliminar as imagens associadas a um local após a eliminação deste
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_IMAGES+"_"+TABLE_NAME_LOCALS+"_del1] BEFORE DELETE ON ["+TABLE_NAME_LOCALS+"] WHEN (old.["+KEY_ID+"] IN (SELECT [local] FROM ["+TABLE_NAME_IMAGES+"] GROUP BY [local])) BEGIN " +
	        			"DELETE FROM ["+TABLE_NAME_IMAGES+"] WHERE [local] = old.["+KEY_ID+"]; " +
	        		"END;"
	        	);
        		// Trigger para actualizar o id do local, em caso de actualização
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_IMAGES+"_"+TABLE_NAME_LOCALS+"_upd1] BEFORE UPDATE ON ["+TABLE_NAME_LOCALS+"] WHEN (old.["+KEY_ID+"] IN (SELECT [local] FROM ["+TABLE_NAME_IMAGES+"] GROUP BY [local])) BEGIN " +
	        			"UPDATE ["+TABLE_NAME_IMAGES+"] SET [local] = new.["+KEY_ID+"] WHERE [local] = old.["+KEY_ID+"]; " +
	        		"END;"
	        	);
        		
        		// Trigger para bloquear a eliminação de um tipo de local associado a um local
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_LOCALS+"_"+TABLE_NAME_LOCALS_TYPE+"_del1] BEFORE DELETE ON ["+TABLE_NAME_LOCALS_TYPE+"] WHEN (old.["+KEY_ID+"] IN (SELECT ["+KEY_LOCAL_TYPE+"] FROM ["+TABLE_NAME_LOCALS+"] GROUP BY ["+KEY_LOCAL_TYPE+"])) BEGIN " +
	        			"SELECT RAISE( ABORT, 'Foreign key violated: fk_places_tbl_places_type_tbl_del1'); " +
	        		"END;"
	        	);
        		
        		// Trigger para actualizar o id do tipo de local na tabela de locais no caso de actualização do id na tabela tipo de local
        		db.execSQL(
        			"CREATE TRIGGER IF NOT EXISTS [fk_"+TABLE_NAME_LOCALS+"_"+TABLE_NAME_LOCALS_TYPE+"_upd1] BEFORE UPDATE ON ["+TABLE_NAME_LOCALS_TYPE+"] WHEN (old.["+KEY_ID+"] IN (SELECT ["+KEY_LOCAL_TYPE+"] FROM ["+TABLE_NAME_LOCALS+"] GROUP BY ["+KEY_LOCAL_TYPE+"])) BEGIN " +
	        			"UPDATE ["+TABLE_NAME_LOCALS+"] SET ["+KEY_LOCAL_TYPE+"] = new.["+KEY_ID+"] WHERE ["+KEY_LOCAL_TYPE+"] = old.["+KEY_ID+"]; " +
	        		"END;"
	        	);
        		
        		// Trigger para inserir os novos registos da tabela de locais na tabela de pesquisa de locais
        		db.execSQL(
        			"CREATE TRIGGER [on_insert_on_"+TABLE_NAME_LOCALS+"] AFTER INSERT ON ["+TABLE_NAME_LOCALS+"] BEGIN " +
        				"INSERT INTO ["+TABLE_NAME_LOCALS_SEARCH+"] " +
        					"(["+KEY_ID+"], ["+KEY_LOCAL_NAME+"], ["+KEY_LOCAL_DESCRIPTION+"], ["+KEY_LOCAL_GPS+"], ["+KEY_LOCAL_ALTITUDE+"], ["+KEY_LOCAL_TYPE+"]) VALUES " +
        					"(new.["+KEY_ID+"], new.["+KEY_LOCAL_NAME+"], new.["+KEY_LOCAL_DESCRIPTION+"], new.["+KEY_LOCAL_GPS_LAT+"]||','||new.["+KEY_LOCAL_GPS_LON+"], new.["+KEY_LOCAL_ALTITUDE+"], (SELECT ["+KEY_LOCAL_TYPE_NAME+"] FROM ["+TABLE_NAME_LOCALS_TYPE+"] WHERE ["+KEY_ID+"]=new.["+KEY_LOCAL_TYPE+"])); " +
					"END;"
        		);
        		
        		// Trigger para impedir a inserção de registos com as mesmas coordenadas GPS e altitude
        		db.execSQL(
        			"CREATE TRIGGER [on_2_insert_on_"+TABLE_NAME_LOCALS+"] BEFORE INSERT ON ["+TABLE_NAME_LOCALS+"] WHEN ((SELECT count(*) FROM ["+TABLE_NAME_LOCALS+"] WHERE ["+KEY_LOCAL_GPS_LAT+"]=new.["+KEY_LOCAL_GPS_LAT+"] AND ["+KEY_LOCAL_GPS_LON+"]=new.["+KEY_LOCAL_GPS_LON+"] AND ["+KEY_LOCAL_ALTITUDE+"]=new.["+KEY_LOCAL_ALTITUDE+"] GROUP BY ["+KEY_LOCAL_GPS_LAT+"], ["+KEY_LOCAL_GPS_LON+"], ["+KEY_LOCAL_ALTITUDE+"])>0) BEGIN " +
        				"SELECT RAISE( ABORT, 'Local exists with the specified altitude and GPS position'); " +
					"END;"
        		);
        		
        		// Trigger para actualizar os registos modificados da tabela de locais na tabela de pesquisa de locais
        		db.execSQL(
        			"CREATE TRIGGER [on_update_on_"+TABLE_NAME_LOCALS+"] AFTER UPDATE ON ["+TABLE_NAME_LOCALS+"] BEGIN " +
        				"UPDATE ["+TABLE_NAME_LOCALS_SEARCH+"] SET " +
        					"["+KEY_ID+"]=new.["+KEY_ID+"], " +
        					"["+KEY_LOCAL_NAME+"]=new.["+KEY_LOCAL_NAME+"], " +
        					"["+KEY_LOCAL_DESCRIPTION+"]=new.["+KEY_LOCAL_DESCRIPTION+"], " +
        					"["+KEY_LOCAL_TYPE+"]=(SELECT ["+KEY_LOCAL_TYPE_NAME+"] FROM ["+TABLE_NAME_LOCALS_TYPE+"] WHERE ["+KEY_ID+"]=new.["+KEY_LOCAL_TYPE+"]), " +
        					"["+KEY_LOCAL_GPS+"]=new.["+KEY_LOCAL_GPS_LAT+"]||','||new.["+KEY_LOCAL_GPS_LON+"], " +
        					"["+KEY_LOCAL_ALTITUDE+"]=new.["+KEY_LOCAL_ALTITUDE+"] " +
    					"WHERE ["+KEY_ID+"] = old.["+KEY_ID+"]; " +
					"END;"
    			);
        		
        		// Trigger para impedir a actualização de registos com as mesmas coordenadas GPS e altitude
        		db.execSQL(
        			"CREATE TRIGGER [on_2_update_on_"+TABLE_NAME_LOCALS+"] BEFORE UPDATE ON ["+TABLE_NAME_LOCALS+"] WHEN ((SELECT count(*) FROM ["+TABLE_NAME_LOCALS+"] WHERE ["+KEY_LOCAL_GPS_LAT+"]=new.["+KEY_LOCAL_GPS_LAT+"] AND ["+KEY_LOCAL_GPS_LON+"]=new.["+KEY_LOCAL_GPS_LON+"] AND ["+KEY_LOCAL_ALTITUDE+"]=new.["+KEY_LOCAL_ALTITUDE+"] AND "+KEY_ID+"!=new.["+KEY_ID+"] GROUP BY ["+KEY_LOCAL_GPS_LAT+"], ["+KEY_LOCAL_GPS_LON+"], ["+KEY_LOCAL_ALTITUDE+"])>0) BEGIN " +
        				"SELECT RAISE( ABORT, 'Local exists with the specified altitude and GPS position'); " +
					"END;"
        		);
        		
        		// Trigger para eliminar os registos eliminados da tabela de locais da tabela de pesquisa de locais
        		db.execSQL(
        			"CREATE TRIGGER [on_delete_on_"+TABLE_NAME_LOCALS+"] AFTER DELETE ON ["+TABLE_NAME_LOCALS+"] BEGIN " +
        				"DELETE FROM ["+TABLE_NAME_LOCALS_SEARCH+"] WHERE ["+KEY_ID+"] = old.["+KEY_ID+"]; " +
    				"END;"
        		);
        		
        		this.databaseCreated = true;
        	} catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro ao criar a base de dados: "+e.toString());
    			this.deleteDatabaseStructure(db);
    		}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(APP_NAME, "A actualizar da versão " + oldVersion + " para a versão " + newVersion + "...");
            
            this.deleteDatabaseStructure(db);
            // Recriar as tabelas
            this.onCreate(db);
        }
        
        /**
         * Método auxiliar que permite confirmar se o método de criação da estrutura da base de dados foi executado ou não
         * @return {@link Boolean} true se a estrutura da base de dados foi criada, false caso contrário
         */
        public boolean databaseCreated(){
        	return this.databaseCreated;
        }
        
        /**
         * Destroi a estrutura da base de dados
         * @param db {@link SQLiteDatabase} com a instância da base de dados a destruir
         * @return {@link Boolean} true se o método foi executado com sucesso, false caso contrário
         */
        private boolean deleteDatabaseStructure(SQLiteDatabase db){
        	try{
	            // Eliminação dos triggers
	    		db.execSQL("DROP TRIGGER IF EXISTS [fk_"+TABLE_NAME_MEDIA+"_"+TABLE_NAME_MEDIA_TYPE+"_del1];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [fk_"+TABLE_NAME_MEDIA+"_"+TABLE_NAME_MEDIA_TYPE+"_upd1];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [fk_"+TABLE_NAME_IMAGES+"_"+TABLE_NAME_LOCALS+"_del1];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [fk_"+TABLE_NAME_IMAGES+"_"+TABLE_NAME_LOCALS+"_upd1];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [on_insert_on_"+TABLE_NAME_LOCALS+"];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [on_update_on_"+TABLE_NAME_LOCALS+"];");
	    		db.execSQL("DROP TRIGGER IF EXISTS [on_delete_on_"+TABLE_NAME_LOCALS+"];");
	    		
	            // Eliminação das tabelas
	    		db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_IMAGES+"];");
	    		db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_MEDIA+"];");
	    		db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_LOCALS_SEARCH+"];");
	    		db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_LOCALS+"];");
	    		db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_LOCALS_TYPE+"];");
	            db.execSQL("DROP TABLE IF EXISTS ["+TABLE_NAME_MEDIA_TYPE+"];");
	            
	            return true;
            }catch (Exception e) {
            	Log.e(APP_NAME, "Occorreu um erro ao eliminar as bases de dados: "+e.toString(), e);
			}
            return false;
        }
    }
	
	/**
     * Constructor - Utiliza o contexto para permitir a criação e/ou abertura da base de dados
     * 
     * @param context com o Context a utilizar
     */
    public DbAdapter(Context context) {
        this.context = context;
    }

    /**
     * Abre a base de dados; 
     * Se não for possível abrir a base de dados, tenta criar uma nova instância da base de dados; 
     * Se não for possível criar a base de dados emite uma excepção
     * 
     * @return {@link Boolean} true se a instância da base de dados foi aberta ou criada com sucesso
     * @throws {@link SQLException] se não foi possível abrir e/ou criar a base de dados
     */
    public boolean open() throws SQLException {
    	try{
    		this.dbHelper = new DbHelper(this.context);
    		this.sqlDatabase = this.dbHelper.getWritableDatabase();
    		if(this.databaseCreated()){
    			// Vamos definir o encoding da nossa base de dados para português
    			this.sqlDatabase.setLocale(new Locale("pt", "PT"));
    			// Inserções de tipos de media
    			this.sqlDatabase.execSQL("INSERT INTO ["+TABLE_NAME_MEDIA_TYPE+"] (name) VALUES ('imagem');");
    			this.sqlDatabase.execSQL("INSERT INTO ["+TABLE_NAME_MEDIA_TYPE+"] (name) VALUES ('video');");
    			this.sqlDatabase.execSQL("INSERT INTO ["+TABLE_NAME_MEDIA_TYPE+"] (name) VALUES ('audio');");
    		}
            return this.sqlDatabase.isOpen();
    	}catch (SQLException e) {
			throw e;
		}
    }
    
    /**
     * Fecha a ligação à base de dados
     * @return {@link Boolean} true se a ligação foi terminada, false caso contrário
     */
    public boolean close() {
    	this.dbHelper.close();
    	return !this.sqlDatabase.isOpen();
    }
    
    /**
     * Método auxiliar para verificar se o método de criação da estrutura da base de dados foi executado ou não
     * 
     * @return {@link Boolean} true se a estrutura da base de dados foi criada, false caso contrário
     */
    public boolean databaseCreated(){
    	return this.dbHelper.databaseCreated();
    }
    
    /**
     * Método auxiliar para verificar se o método de criação da estrutura da base de dados foi executado ou não
     * 
     * @return {@link Boolean} true se a estrutura da base de dados foi criada, false caso contrário
     */
    public boolean isOpen(){
    	return this.sqlDatabase.isOpen();
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeId {@link Long} com o id do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, long localTypeId) {
    	if(localTypeId>-1 && !name.equals("")){
    		try{
	    		ContentValues values = new ContentValues();
	    		if(id>-1){
	    			if(this.getLocalByID(id)!=null){
	    				Log.d(APP_NAME, "Não é possível inserir o local: já existe um local com o id "+id);
	    				return LOCAL_INSERT_FAILED_LOCAL_ID_EXISTS;
	    			}
	    			values.put(KEY_ID, id);
	    		}
	    		if(this.getLocalIdByCoordinates(gpsLat, gpsLon, altitude)>-1){
	    			Log.d(APP_NAME, "Não é possível inserir o local: já existe um local com as coordenadas "+gpsLat+","+gpsLon+" à altitude de "+altitude);
    				return LOCAL_INSERT_FAILED_LOCAL_GPS_EXISTS;
	    		}
	            values.put(KEY_LOCAL_GPS_LAT, gpsLat);
	            values.put(KEY_LOCAL_GPS_LON, gpsLon);
	            values.put(KEY_LOCAL_ALTITUDE, altitude);
	            values.put(KEY_LOCAL_NAME, name);
	            values.put(KEY_LOCAL_DESCRIPTION, description);
	            values.put(KEY_LOCAL_TYPE, localTypeId);
	            
	            id = this.sqlDatabase.insert(TABLE_NAME_LOCALS, null, values);
	            // Como em algumas situações, a linha anterior devolve um id válido, mas o registo é eliminado, foi necessário confirmar se o registo foi mesmo inserido, com as linhas seguintes
	            if(id>-1){
	            	if(this.getLocalByID(id)==null){
	            		id = this.getLocalIdByCoordinates(gpsLat, gpsLon, altitude);
	            	}
	            }
	            return id;
    		}catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
			}
    	}else{
    		Log.d(APP_NAME, "Identificador "+localTypeId+" para o tipo de local é inválido e/ou nome do local não foi definido");
    	}
    	return LOCAL_INSERT_FAILED;
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeId {@link Long} com o id do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(long id, String name, String description, int gpsLat, int gpsLon, long localTypeId) {
    	return this.insertLocal(id, name, description, gpsLat, gpsLon, 0, localTypeId);
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeId {@link Long} com o id do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(String name, String description, int gpsLat, int gpsLon, long localTypeId) {
    	return this.insertLocal(-1, name, description, gpsLat, gpsLon, localTypeId);
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeId {@link Long} com o id do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(String name, String description, int gpsLat, int gpsLon, int altitude, long localTypeId) {
    	return this.insertLocal(-1, name, description, gpsLat, gpsLon, altitude, localTypeId);
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(String name, String description, int gpsLat, int gpsLon, String localTypeName) {
    	return this.insertLocal(name, description, gpsLat, gpsLon, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(String name, String description, int gpsLat, int gpsLon, int altitude, String localTypeName) {
    	return this.insertLocal(name, description, gpsLat, gpsLon, altitude, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(long id, String name, String description, int gpsLat, int gpsLon, String localTypeName) {
    	return this.insertLocal(id, name, description, gpsLat, gpsLon, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, String localTypeName) {
    	return this.insertLocal(id, name, description, gpsLat, gpsLon, altitude, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere um local na base de dados da aplicação
     * 
     * @param local {@link Local} com o local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public long insertLocal(Local local) {
    	return this.insertLocal(local.getId(), local.getName(), local.getDescription(), local.getGpsLat(), local.getGpsLon(), local.getAltitude(), local.getLocalTypeId());
    }
    
    /**
     * Actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeId {@link Long} com o identificador do tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, long localTypeId) {
    	if(id>-1 && localTypeId>-1 && !name.equals("")){
    		try{
	    		ContentValues values = new ContentValues();
	            values.put(KEY_LOCAL_NAME, name);
	            values.put(KEY_LOCAL_DESCRIPTION, description);
	            values.put(KEY_LOCAL_GPS_LON, gpsLon);
	            values.put(KEY_LOCAL_GPS_LAT, gpsLat);
	            values.put(KEY_LOCAL_ALTITUDE, altitude);
	            values.put(KEY_LOCAL_TYPE, localTypeId);
	            
	            return this.sqlDatabase.update(TABLE_NAME_LOCALS, values, KEY_ID + "=" + id, null)>0;
    		}catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
			}
    	}
    	return false;
    }
    /**
     * Actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeId {@link Long} com o identificador do tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocal(long id, String name, String description, int gpsLat, int gpsLon, long localTypeId) {
    	return this.updateLocal(id, name, description, gpsLat, gpsLon, 0, localTypeId);
    }
    
    /**
     * Actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, String localTypeName) {
    	return this.updateLocal(id, name, description, gpsLat, gpsLon, altitude, this.getLocalType(localTypeName));
    }
    
    /**
     * Actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocal(long id, String name, String description, int gpsLat, int gpsLon, String localTypeName) {
    	return this.updateLocal(id, name, description, gpsLat, gpsLon, this.getLocalType(localTypeName));
    }
    
    /**
     * Actualiza um local na base de dados da aplicação
     * 
     * @param local {@link Local} com o local
     * @return {@link Long} com o identificador do novo local ou -1 em caso de erro
     */
    public boolean updateLocal(Local local) {
    	return this.updateLocal(local.getId(), local.getName(), local.getDescription(), local.getGpsLat(), local.getGpsLon(), local.getAltitude(), local.getLocalTypeId());
    }
    
    /**
     * Insere ou actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeId {@link Long} com o identificador do tipo de local
     * @return {@link Long} com o identificador do local ou -1 em caso de erro
     */
    public long insertOrupdateLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, long localTypeId) {
    	if(localTypeId>-1 && !name.equals("")){
    		if(id>-1){
    			if(this.updateLocal(id, name, description, gpsLat, gpsLon, altitude, localTypeId)){
    				return id;
    			}
    		}else{
    			return this.insertLocal(name, description, gpsLat, gpsLon, altitude, localTypeId);
    		}
    	}
    	return GENERIC_ERROR;
    }
    
    /**
     * Insere ou actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeId {@link Long} com o identificador do tipo de local
     * @return {@link Long} com o identificador do local ou -1 em caso de erro
     */
    public long insertOrupdateLocal(long id, String name, String description, int gpsLat, int gpsLon, long localTypeId) {
    	return this.insertOrupdateLocal(id, name, description, gpsLat, gpsLon, 0, localTypeId);
    }
    
    /**
     * Insere ou actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do local ou -1 em caso de erro
     */
    public long insertOrupdateLocal(long id, String name, String description, int gpsLat, int gpsLon, int altitude, String localTypeName) {
    	return this.insertOrupdateLocal(id, name, description, gpsLat, gpsLon, altitude, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere ou actualiza um local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do local
     * @param name {@link String} com o nome
     * @param description {@link String} com a descrição
     * @param gpsLat {@link Integer} com a coordenada GPS para a latitude
     * @param gpsLon {@link Integer} com a coordenada GPS para a longitude
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do local ou -1 em caso de erro
     */
    public long insertOrupdateLocal(long id, String name, String description, int gpsLat, int gpsLon, String localTypeName) {
    	return this.insertOrupdateLocal(id, name, description, gpsLat, gpsLon, this.getLocalType(localTypeName));
    }
    
    /**
     * Insere ou actualiza um local na base de dados da aplicação
     * 
     * @param local {@link Local} com o local
     * @return {@link Long} com o identificador do local ou -1 em caso de erro
     */
    public long insertOrupdateLocal(Local local) {
    	return this.insertOrupdateLocal(local.getId(), local.getName(), local.getDescription(), local.getGpsLat(), local.getGpsLon(), local.getAltitude(), local.getLocalTypeId());
    }
    
    /**
     * Remove um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @return {@link Boolean} true se o local foi eliminado, false caso contrário
     */
    public boolean removeLocalByCoordinates(int latitude, int longitude, int altitude){
    	return this.removeLocalByID(this.getLocalIdByCoordinates(latitude, longitude, altitude));
    }
    
    /**
     * Remove um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @return {@link Boolean} true se o local foi eliminado, false caso contrário
     */
    public boolean removeLocalByCoordinates(int latitude, int longitude){
    	return this.removeLocalByCoordinates(latitude, longitude, 0);
    }
    
    /**
     * Elimina um local com base no seu identificador
     * 
     * @param id {@link Long} com o identificador do local
     * @return {@link Boolean} true se o local foi eliminado, false caso contrário
     */
    public boolean removeLocalByID(long id){    	
    	try{
    		if(id>-1){
    			return this.sqlDatabase.delete(TABLE_NAME_LOCALS, KEY_ID + "=" + id, null) > 0;
    		}
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}
	    return false;
    }

    /**
     * Obtém o identificador de um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @return {@link Long} com o identificar único do local, -1 caso o local não seja encontrado
     */
    public long getLocalIdByCoordinates(int latitude, int longitude, int altitude){
    	Cursor cursor = null;
    	long result = GENERIC_NO_RESULTS;
    	try{
	    	cursor = this.sqlDatabase.query(true, TABLE_NAME_LOCALS, new String[] {KEY_ID}, KEY_LOCAL_GPS_LON+" = " + longitude + " AND "+KEY_LOCAL_GPS_LAT+" = "+latitude + " AND "+KEY_LOCAL_ALTITUDE+" = "+altitude, null, null, null, null, "1");
		    if (cursor!=null && cursor.moveToFirst()) {
		    	result =  cursor.getLong(cursor.getColumnIndex(KEY_ID));
		    }
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
	    return result;
    }

    /**
     * Obtém o identificador de um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @return {@link Long} com o identificar único do local, -1 caso o local não seja encontrado
     */
    public long getLocalIdByCoordinates(int latitude, int longitude){
    	return this.getLocalIdByCoordinates(latitude, longitude, 0);
    }
    
    /**
     * Obtém um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @param altitude {@link Integer} com o valor para a altitude
     * @return {@link Local} com o local, ou null caso o local não seja encontrado
     */
    public Local getLocalByCoordinates(int latitude, int longitude, int altitude){
    	return this.getLocalByID(this.getLocalIdByCoordinates(latitude, longitude, altitude));
    }
    
    /**
     * Obtém um local com base nas suas coordenadas GPS
     * 
     * @param latitude {@link Integer} com a latitude
     * @param longitude {@link Integer} com a longitude
     * @return {@link Local} com o local, ou null caso o local não seja encontrado
     */
    public Local getLocalByCoordinates(int latitude, int longitude){
    	return this.getLocalByID(this.getLocalIdByCoordinates(latitude, longitude));
    }
    
    /**
     * Obtém um local com base no seu identificador
     * 
     * @param id {@link Long} com o identificador do local
     * @return {@link Local} com o local, ou null caso o local não seja encontrado
     */
    public Local getLocalByID(long id){
    	if(id>-1){
    		try{
		    	LinkedList<Local> locals = new LinkedList<Local>();
		    	locals = this.getLocals("l.["+KEY_ID+"] = "+id+" LIMIT 1 ");
		    	
		    	if(locals.size()==1){
		    		return locals.getFirst();
		    	}
    		}catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
			}
    	}
	    return null;
    }
    
    /**
     * Devolve uma lista com todos os locais da base de dados da aplicação
     * 
     * @return LinkedList<Local> com os locais da base de dados
     */
    public LinkedList<Local> getAllLocals() {
    	return this.getLocals("");
    }
    
    /**
     * 
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do tipo de local
     * @param typeName {@link String} com o nome do tipo de local
     * @param iconPath {@link String} com o caminho para o ícone associado ao tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou menor que 0 em caso de erro
     */
    public long insertLocalType(long id, String typeName, String iconPath) {
    	if(!typeName.equals("")){
    		try{
	    		ContentValues values = new ContentValues();
	    		if(id>-1){
	    			if(this.getLocalType(id)!=null){
	    				Log.d(APP_NAME, "Não é possível inserir o tipo de local: já existe um tipo de local com o id "+id);
	    				return LOCALTYPE_INSERT_FAILED_LOCALTYPE_ID_EXISTS;
	    			}
	    			values.put(KEY_ID, id);
	    		}
    			if(this.getLocalType(typeName)>-1){
    				Log.d(APP_NAME, "Não é possível inserir o tipo de local: já existe um tipo de local com o nome "+typeName);
    				return LOCALTYPE_INSERT_FAILED_LOCALTYPE_NAME_EXISTS;
    			}
	            values.put(KEY_LOCAL_TYPE_NAME, typeName);
	            values.put(KEY_LOCAL_TYPE_ICON, iconPath);
	            
	            return this.sqlDatabase.insertOrThrow(TABLE_NAME_LOCALS_TYPE, null, values);
    		}catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
			}
    	}else{
    		Log.d(APP_NAME, "O nome do tipo de local não foi definido");
    	}
    	return LOCALTYPE_INSERT_FAILED;
    }
    
    /**
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do tipo de local
     * @param typeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou menor que 0 em caso de erro
     */
    public long insertLocalType(long id, String typeName) {
    	return this.insertLocalType(id, typeName, "");
    }
    
    /**
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param typeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou -1 em caso de erro
     */
    public long insertLocalType(String typeName) {
    	return this.insertLocalType(-1, typeName);
    }
    
    /** 
     * 
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param typeName {@link String} com o nome do tipo de local
     * @param iconPath {@link String} com o caminho para o ícone associado ao tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou -1 em caso de erro
     */
    public long insertLocalType(String typeName, String iconPath) {
    	return this.insertLocalType(-1, typeName, iconPath);
    }
    
    /**
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param localType {@link SimpleObject} com o tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou -1 em caso de erro
     * 
     * @deprecated utilizar o insertLocalType(LocalType localType) em substituição
     */
    public long insertLocalType(SimpleObject localType) {
    	return this.insertLocalType(localType.getId(), localType.getName());
    }
    
    /** 
     * 
     * Insere um tipo de local na base de dados da aplicação
     * 
     * @param localType {@link LocalType} com o tipo de local
     * @return {@link Long} com o identificador do novo tipo de local ou -1 em caso de erro
     */
    public long insertLocalType(LocalType localType) {
    	return this.insertLocalType(localType.getId(), localType.getName(), localType.getIcon());
    }
    
    /**
     * 
     * Actualiza um tipo de local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do tipo de local
     * @param typeName {@link String} com o nome do tipo de local
     * @param iconPath {@link String} com o caminho para o ícone associado ao tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocalType(long id, String typeName, String iconPath) {
    	if(id>-1 && !typeName.equals("")){
    		try{
	    		ContentValues values = new ContentValues();
	            values.put(KEY_LOCAL_TYPE_NAME, typeName);
	            values.put(KEY_LOCAL_TYPE_ICON, iconPath);
	            
	            return this.sqlDatabase.update(TABLE_NAME_LOCALS_TYPE, values, KEY_ID + "=" + id, null)>0;
    		}catch (Exception e) {
    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
			}
    	}
    	return false;
    }
    
    /**
     * Actualiza um tipo de local na base de dados da aplicação
     * 
     * @param id {@link Long} com o identificador do tipo de local
     * @param typeName {@link String} com o nome do tipo de local
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocalType(long id, String typeName) {
    	return this.updateLocalType(id, typeName, "");
    }
    
    /** 
     * 
     * Actualiza um tipo de local na base de dados da aplicação
     * 
     * @param id {@link LocalType} com o tipo de local actualizado
     * @return {@link Boolean} true se o registo foi actualizado, false caso contrário
     */
    public boolean updateLocalType(LocalType localType) {
    	return this.updateLocalType(localType.getId(), localType.getName(), localType.getIcon());
    }
    
    /**
     * Elimina um tipo de local com base no seu identificador
     * 
     * @param id {@link Long} com o identificador do tipo de local
     * @return {@link Boolean} true se o tipo de local foi eliminado, false caso contrário
     */
    public boolean removeLocalType(long id){    	
    	try{
    		if(id>-1){
    			return this.sqlDatabase.delete(TABLE_NAME_LOCALS_TYPE, KEY_ID + "=" + id, null) > 0;
    		}
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}
	    return false;
    }
    
    /**
     * Obtem o identificador de um tipo de local com base no nome do tipo
     * 
     * @param localTypeName {@link String} com o nome do tipo de local
     * @return {@link Long} com o identificador do registo, menor que 0 em caso de erro
     */
    public long getLocalType(String localTypeName){
    	Cursor cursor = null;
    	long result = GENERIC_NO_RESULTS;
    	try{
	    	cursor = this.sqlDatabase.query(true, TABLE_NAME_LOCALS_TYPE, new String[] {KEY_ID}, "LOWER("+KEY_LOCAL_TYPE_NAME+") LIKE LOWER(" + DatabaseUtils.sqlEscapeString(localTypeName) + ")", null, null, null, null, "1");
		    if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()) {
		    	result =  cursor.getLong(cursor.getColumnIndex(KEY_ID));
		    }
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
		
	    return result;
    }
    
    /**
     * 
     * Obtem um tipo de um local com base no seu identificador
     * 
     * @param localTypeId {@link Long} com o identificador do tipo de local
     * @return {@link LocalType} com o tipo de local, ou null
     */
    public LocalType getLocalType(long localTypeId){
    	Cursor cursor = null;
    	LocalType result = null;
    	try{
	    	cursor = this.sqlDatabase.query(true, TABLE_NAME_LOCALS_TYPE, new String[] {KEY_LOCAL_TYPE_NAME, KEY_LOCAL_TYPE_ICON}, KEY_ID+"="+localTypeId, null, null, null, null, "1");
		    if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()) {
		    	result = new LocalType(localTypeId, cursor.getString(cursor.getColumnIndex(KEY_LOCAL_TYPE_NAME)), cursor.getString(cursor.getColumnIndex(KEY_LOCAL_TYPE_ICON)));
		    }
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
	    return result;
    }
    
    /**
     * Devolve uma lista com todos os tipos de locais da base de dados da aplicação
     * 
     * @return LinkedList<LocalType> com os locais da base de dados
     * 
     * @example:
        LinkedList<LocalType> localTypes = this.dbAdapter.getAllLocalTypes();
		
		if(localTypes.isEmpty()){
			Log.d(DbAdapter.APP_NAME, "Não foram encontrados tipos de locais");
		}else{
			Iterator<LocalType> i = localTypes.iterator();
			LocalType localType;
			while(i.hasNext()){
				localType = i.next();
				Log.d(DbAdapter.APP_NAME, "Foi encontrados o tipo de local "+localType.getName());
			}
		}
     */
    public LinkedList<LocalType> getAllLocalTypes() {
    	LinkedList<LocalType> localTypes = new LinkedList<LocalType>();
    	Cursor cursor = null;
    	try{
    		cursor = this.sqlDatabase.query(true, TABLE_NAME_LOCALS_TYPE, new String[] {KEY_ID, KEY_LOCAL_TYPE_NAME, KEY_LOCAL_TYPE_ICON}, null, null, null, null, null, null);
    		
    		if(cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()){
    			int iId = cursor.getColumnIndex(KEY_ID);
    			int iName = cursor.getColumnIndex(KEY_LOCAL_TYPE_NAME);
    			int iIcon = cursor.getColumnIndex(KEY_LOCAL_TYPE_ICON);
    			
	    		do{
	    			localTypes.add(new LocalType(cursor.getLong(iId), cursor.getString(iName), cursor.getString(iIcon)));		
	    		}while(cursor.moveToNext());
    		}
		}catch(Exception e){
			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
		
    	return localTypes;
    }
    
    /**
     * Permite efecutar a pesquisa de locais. 
     * Utiliza o operador MATCH do sqlite e a respectiva sintaxe para fulltext search 3 é suportada.
     * Exemplos:
     * 		search("sala teste"); // procura pelo texto "sala" e "teste" em todo o espaço pesquisável da tabela
     * 		
     * 		search("sala OR anfiteatro"); // procura pelo texto "sala" OU "teste" em todo o espaço pesquisável da tabela. O operador OR tem de ser introduzido em maiúsculas.
     * 
     * 		search("name:sala description:estudo gps:123,234 type:sala de aula altitude:1"); // procura por todos os locais que tenham o nome sala, descrição estudo, coordenadas de gps 123,234, do tipo sala na altitude 1 
     * 
     * 		search("\"sala de teste\""); // procura pela frase completa "sala de teste" em todo o espaço pesquisável da tabela.
     * 
     * 		search("sala -1.2"); // procura por todos os locais com o texto "sala" mas que excluindo os que tenham o texto "1.2"
     * 
     * 		search("anf*"); // procura por todos os locais com texto que começe por "anf"
     * 
     * @param search {@link String} com a expressão de pesquisa
     * @return {@link LinkedList} de {@link Local} com os locais encontrados utilizando a expressão
     */
    public LinkedList<Local> search(String search) {
    	// Se foi introduzido o asterisco, apresentar todos os locais
    	if(search.equals("*")){
    		return this.getAllLocals();
    	}
    	
    	LinkedList<Local> locals = new LinkedList<Local>();
    	Cursor cursor = null;
    	try{
    		cursor = this.sqlDatabase.query(true, TABLE_NAME_LOCALS_SEARCH, new String[] { KEY_ID }, TABLE_NAME_LOCALS_SEARCH + " MATCH ?", new String[] { search }, null, null, null, null);
    		
    		if(cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()){
    			int iId = cursor.getColumnIndex(KEY_ID);

    			StringBuffer query = new StringBuffer();
	    		do{
	    			query.append((query.length()>0?", ":"")+DatabaseUtils.sqlEscapeString(cursor.getString(iId)));
	    			//locals.add(this.getLocalByID(cursor.getLong(iId)));		
	    		}while(cursor.moveToNext());
	    		
	    		locals = this.getLocals("l.["+KEY_ID+"] IN ("+query+")");
    		}
		}catch(Exception e){
			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
		
    	return locals;
    }
    
    /**
     * Executa uma query sql para obter locais
     * 
     * @param extraWhere {@link String} com os parâmetros extra a adicionar à cláusula where
     * @return {@link Cursor} com o resultado da query ou null em causo de erro
     */
    private Cursor sqlQueryForLocal(String extraWhere) {
		Cursor cursor;
    	try {
    		String query = "SELECT l.["+KEY_ID+"] AS `"+KEY_ID+"`, " +
					"l.["+KEY_LOCAL_NAME+"] AS `name`, " +
					"l.["+KEY_LOCAL_DESCRIPTION+"] AS `description`, " +
					"l.["+KEY_LOCAL_GPS_LON+"] AS `gpsLon`, " +
					"l.["+KEY_LOCAL_GPS_LAT+"] AS `gpsLat`, " +
					"l.["+KEY_LOCAL_ALTITUDE+"] AS `altitude`, " +
					"lt.["+KEY_ID+"] AS `typeID`, " +
					"lt.["+KEY_LOCAL_TYPE_NAME+"] AS `typeName` " +
				"FROM ["+TABLE_NAME_LOCALS+"] l, " +
					"["+TABLE_NAME_LOCALS_TYPE+"] lt " +
				"WHERE l.["+KEY_LOCAL_TYPE+"] = lt.["+KEY_ID+"] " + 
				(!extraWhere.equals("")?" AND "+extraWhere:"") +
				"";
    		cursor = this.sqlDatabase.rawQuery(query, null);
    		
    		return cursor;
		} catch (Exception e) {
			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}
		
    	return null;
    }
    
    /**
     * Devolve uma lista com os locais da base de dados com base nos parâmetros de uma query
     * 
     * @param extraWhere {@link String} com os parâmetros extra a adicionar à cláusula where
     * @return LinkedList<Local> com os locais encontrados
     */
    private LinkedList<Local> getLocals(String extraWhere) {
    	LinkedList<Local> locals = new LinkedList<Local>();
    	Cursor cursor = null;
    	try {
    		cursor = this.sqlQueryForLocal(extraWhere);
    		
    		if(cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()){
    			int iId = cursor.getColumnIndex(KEY_ID);
    			int iName = cursor.getColumnIndex("name");
    			int iDescription = cursor.getColumnIndex("description");
    			int iGpsLon = cursor.getColumnIndex("gpsLon");
    			int iGpsLat = cursor.getColumnIndex("gpsLat");
    			int iAltitude = cursor.getColumnIndex("altitude");
    			int iTypeId = cursor.getColumnIndex("typeID");
    			int iTypeName = cursor.getColumnIndex("typeName");
    			
	    		do{
	    			locals.add(new Local(
	    							cursor.getLong(iId), 
	    							cursor.getString(iName), 
	    							cursor.getString(iDescription), 
	    							cursor.getInt(iGpsLat), 
	    							cursor.getInt(iGpsLon), 
	    							cursor.getInt(iAltitude), 
	    							cursor.getLong(iTypeId), 
	    							cursor.getString(iTypeName)
	    						)
	    			);		
	    		}while(cursor.moveToNext());
    		}
		} catch (Exception e) {
			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
		
    	return locals;
    }
    
    /**
     * Devolve uma string com o valor da preferência
     * 
     * @param preferenceName {@link String} com o nome da preferência
     * @return {@link String} com o valor da preferência, ou null
     * 
     * 
     */
    public String getPreference(String preferenceName){
    	Cursor cursor = null;
    	String result = null;
    	try{
	    	cursor = this.sqlDatabase.query(true, TABLE_NAME_PREFERENCES, new String[] {KEY_PREFERENCE_VALUE}, "LOWER("+KEY_PREFERENCE_NAME+") LIKE LOWER(" + DatabaseUtils.sqlEscapeString(preferenceName) + ")", null, null, null, null, "1");
		    if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()) {
		    	result = cursor.getString(cursor.getColumnIndex(KEY_PREFERENCE_VALUE));
		    }
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
	    return result;
    }
    
    /**
     * Verifica se um dada preferência existe
     * 
     * @param preferenceName {@link String} com o nome da preferência
     * @return {@link Long} com o identicador da preferência, <0 se a preferência não for encontrada
     */
    private long getPreferenceId(String preferenceName){
    	long result=GENERIC_ERROR;
    	Cursor cursor = null;
    	
    	try{
	    	cursor = this.sqlDatabase.query(true, TABLE_NAME_PREFERENCES, new String[] {KEY_ID}, "LOWER("+KEY_PREFERENCE_NAME+") LIKE LOWER(" + DatabaseUtils.sqlEscapeString(preferenceName) + ")", null, null, null, null, "1");
		    if (cursor!=null && cursor.getCount()>0 && cursor.moveToFirst()) {
		    	result = cursor.getLong(cursor.getColumnIndex(KEY_ID));
		    }else{
		    	result = GENERIC_NO_RESULTS;
		    }
    	}catch (Exception e) {
    		Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
		}finally{
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}
	    return result;
    }
    
    /**
     * Verifica se um dada preferência existe
     * 
     * @param preferenceName {@link String} com o nome da preferência
     * @return {@link Boolean} true se a preferência existe, false caso contrário
     * 
     * 
     */
    public boolean preferenceExists(String preferenceName){
    	return (this.getPreferenceId(preferenceName)>-1);
    }
    
    /**
     * Define o valor de uma preferência, adicionando o respectivo registo caso não exista uma preferência com o nome especificado
     * 
     * @param preferenceName {@link String} com o nome da preferência
     * @param preferenceValue {@link String} com o valor da preferência
     * @return {@link Boolean} true se o registo foi inserido/actualizado, false caso contrário
     * 
     * 
     */
    public boolean setPreference(String preferenceName, String preferenceValue) {
    	if(!preferenceName.equals("")){
        	long preferenceId = -1;
    		if((preferenceId=this.getPreferenceId(preferenceName))>-1){
	    		try{
		    		ContentValues values = new ContentValues();
		            values.put(KEY_PREFERENCE_VALUE, preferenceValue);
		            
		            return this.sqlDatabase.update(TABLE_NAME_PREFERENCES, values, KEY_ID + "=" + preferenceId, null)>0;
	    		}catch (Exception e) {
	    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
				}
    		}else{
    			try{
    	    		ContentValues values = new ContentValues();
    	    		values.put(KEY_PREFERENCE_NAME, preferenceName);
		            values.put(KEY_PREFERENCE_VALUE, preferenceValue);
    	
    	            return this.sqlDatabase.insertOrThrow(TABLE_NAME_PREFERENCES, null, values)>0;
        		}catch (Exception e) {
        			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
    			}
    		}
    	}else{
    		Log.d(APP_NAME, "O nome da preferência não foi definido");
    	}
    	return false;
    }
    
    /**
     * Remove a preferência com o valor especificado
     * 
     * @param preferenceName {@link String} com o nome da preferência
     * @return {@link Boolean} true se o registo foi eliminado, false caso contrário
     * 
     * 
     */
    public boolean removePreference(String preferenceName) {
    	if(!preferenceName.equals("")){
        	long preferenceId = -1;
    		if((preferenceId=this.getPreferenceId(preferenceName))>-1){
	    		try{
	    			return this.sqlDatabase.delete(TABLE_NAME_PREFERENCES, KEY_ID + "=" + preferenceId, null) > 0;
	    		}catch (Exception e) {
	    			Log.e(APP_NAME, "Ocorreu um erro: "+e.toString(), e);
				}
    		}
    	}else{
    		Log.d(APP_NAME, "O nome da preferência não foi especificado");
    	}
    	return false;
    }
}
