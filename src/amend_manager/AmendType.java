package amend_manager;

public enum AmendType {
	UPDATE("U"),
	DELETE("D");
	
	private String code;
	AmendType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static AmendType fromCode(String code) {
		
		if (code == null)
			return null;
		
		for (AmendType type : AmendType.values()) {
			if (type.getCode().equalsIgnoreCase(code))
				return type;
		}
		
		return null;
	}
}
