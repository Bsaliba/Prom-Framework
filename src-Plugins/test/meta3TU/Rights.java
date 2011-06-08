package test.meta3TU;

public enum Rights {
	UNKNOWN ("Unknown"),
	PUBLIC ("Public"),
	NON_DISCLOSURE_AGREEMENT ("Non-Disclosure Agreement");
	
	private String textualRepresentation;
	private Rights(String textualRepresentation){
		this.textualRepresentation = textualRepresentation;
	}
	
	@Override
	public String toString() {
	  return textualRepresentation;
	}
}
