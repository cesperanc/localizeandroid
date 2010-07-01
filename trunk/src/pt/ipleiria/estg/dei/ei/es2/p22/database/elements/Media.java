package pt.ipleiria.estg.dei.ei.es2.p22.database.elements;

public class Media extends SimpleObject {
	private String url;
	private String mediaType;

	public Media(long id, String name, String url, String mediaType) {
		super(id, name);
		this.url = url;
		this.mediaType = mediaType;
	}
	
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.mediaType = type;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return mediaType;
	}
}
