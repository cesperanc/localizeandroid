/**
 * 
 */
package pt.ipleiria.estg.dei.ei.es2.p22.database.elements;

/**
 * @author cesperanc
 *
 */
public class SimpleObject {
	private long _id;
	private String name;
	
	/**
	 * Constructor sem definição do ID 
	 */
	public SimpleObject() {
		super();
		this.setId(-1);
	}
	
	/**
	 * @param id com o identificador do registo
	 */
	public SimpleObject(long id) {
		super();
		this.setId(id);
	}
	/**
	 * @param id com o identificador do registo
	 * @param name com o name do registo
	 */
	public SimpleObject(long id, String name) {
		this(id);
		this.setName(name);
	}
	/**
	 * @param id com o identificador do registo
	 */
	private void setId(long id) {
		this._id = id;
	}
	/**
	 * @return o identificador do registo
	 */
	public long getId() {
		return _id;
	}
	/**
	 * @param name com o name do registo
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return o valor do name do registo
	 */
	public String getName() {
		return this.name;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new SimpleObject(this.getId(), this.getName());
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof SimpleObject){
			SimpleObject obj = ((SimpleObject) o);
			if(obj.getId()==this.getId() && obj.getName().equals(this.getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}
	
}
