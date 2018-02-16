package providers;

public class RCLError {

	private String code;
	private Object data;
	
	public RCLError(String code, Object data) {
		this.code = code;
		this.data = data;
	}
	
	public RCLError(String code) {
		this(code, null);
	}
	
	public String getCode() {
		return code;
	}
	public Object getData() {
		return data;
	}
}
