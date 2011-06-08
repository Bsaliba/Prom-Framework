package test.meta3TU;

/**
 * Types of processes as defined in Van der Aalst et al (2005) 
 */
public enum ProcessType{
	UNKNOWN ("Unknown"),
	UNSTRUCTURED ("Unstructured"),
	AD_HOC_STRUCTURED ("Ad-Hoc structured"),
	IMPLICITLY_STRUCTURED ("Implicitly structured"),
	EXPLICITLY_STRUCTURED ("Explicitly structured");		
	
	private String textualRepresentation;
	private ProcessType(String textualRepresentation){
		this.textualRepresentation = textualRepresentation;
	}
	
	@Override
	public String toString() {
	  return textualRepresentation;
	}
}