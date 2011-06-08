package test.meta3TU;

public enum LogType {
	UNKNOWN ("Unknown"),
	REAL_LIFE ("Real-life"),
	SYNTHETIC_LOG ("Synthetic log"),
	SYNTHETIC_MODEL ("Synthetic model");
	
	private String textualRepresentation;
	private LogType(String textualRepresentation){
		this.textualRepresentation = textualRepresentation;
	}
	
	@Override
	public String toString() {
	  return textualRepresentation;
	}

}
