package formula;

public class AttributeElement {

	private String id;
	private String value;
	
	public AttributeElement(String id, String value) {
		this.id = id;
		this.value = value;
	}
	
	public String getId() {
		return id;
	}
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return id + "=" + value;
	}
}
