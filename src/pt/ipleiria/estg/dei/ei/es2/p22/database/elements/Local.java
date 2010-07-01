/**
 * 
 */
package pt.ipleiria.estg.dei.ei.es2.p22.database.elements;

import java.util.LinkedList;

/**
 * @author cesperanc
 */
public class Local extends SimpleObject {
	private String description;
	private int gpsLat;
	private int gpsLon;
	private int altitude;
	private LocalType type;
	private LinkedList<Media> media;
	@SuppressWarnings("unused")
	private String[] images; // TODO adicionar suporte às imagens e aos componentes de media
	
	public Local(long id, String name, String description, int gpsLat, int gpsLon, int altitude, LocalType localType) {
		super(id, name);
		this.setDescription(description);
		this.setGpsLat(gpsLat);
		this.setGpsLon(gpsLon);
		this.setAltitude(altitude);
		this.setLocalType(localType);
	}
	
	public Local(long id, String name, String description, int gpsLat, int gpsLon, int altitude, long typeId, String typeName) {
		this(id, name, description, gpsLat, gpsLon, altitude, new LocalType(typeId, typeName));
	}
	
	public Local(long id, String name, String description, int gpsLat, int gpsLon, long typeId, String typeName) {
		this(id, name, description, gpsLat, gpsLon, 0, typeId, typeName);
	}
	
	public Local(String name, String description, int gpsLat, int gpsLon, long typeId, String typeName) {
		this(-1, name, description, gpsLat, gpsLon, 0, typeId, typeName);
	}
	
	public Local(String name, String description, int gpsLat, int gpsLon, int altitude, long typeId, String typeName) {
		this(-1, name, description, gpsLat, gpsLon, altitude, typeId, typeName);
	}
	
	public Local(String name, String description, int gpsLat, int gpsLon, int altitude, LocalType localType) {
		this(-1, name, description, gpsLat, gpsLon, altitude, localType);
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param gpsLat the gpsLat to set
	 */
	public void setGpsLat(int gpsLat) {
		this.gpsLat = gpsLat;
	}

	/**
	 * @return the gpsLat
	 */
	public int getGpsLat() {
		return gpsLat;
	}

	/**
	 * @param gpsLon the gpsLon to set
	 */
	public void setGpsLon(int gpsLon) {
		this.gpsLon = gpsLon;
	}

	/**
	 * @return the gpsLon
	 */
	public int getGpsLon() {
		return gpsLon;
	}

	/**
	 * @param gpsLon the altitude to set
	 */
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}
	
	/**
	 * @return the altitude
	 */
	public int getAltitude() {
		return altitude;
	}
	
	/**
	 * @return the localTypeName
	 */
	public String getLocalTypeName() {
		if(this.getLocalType()!=null){
			return this.getLocalType().getName();
		}
		return null;
	}

	public long getLocalTypeId() {
		if(this.getLocalType()!=null){
			return this.getLocalType().getId();
		}
		return -1;
	}

	/**
	 * @return the local type
	 */
	public LocalType getLocalType() {
		return type;
	}

	/**
	 * @param type the local type to set
	 */
	public void setLocalType(LocalType type) {
		this.type = type;
	}

	/**
	 * @return the media
	 */
	public LinkedList<Media> getMedia() {
		return media;
	}

	/**
	 * @param media the media to add
	 */
	public void addMedia(Media media) {
		this.media.add(media);
	}
	
	/**
	 * @param media the media to add
	 */
	public void addAllMedia(LinkedList<Media> media) {
		this.media.addAll(media);
	}
	
	/**
	 * @param media the media to remove
	 */
	public void removeMedia(Media media){
		this.media.remove(media);
	}
	
	/**
	 * Remove all the elements from the media list
	 */
	public void removeAllMedia(){
		this.media.clear();
	}
}
