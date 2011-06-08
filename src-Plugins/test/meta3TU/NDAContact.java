package test.meta3TU;

import test.XMetaData3TUExtension;


public class NDAContact {
	public String contactPerson;
	public String organisation;
	
	public NDAContact(){
		this.contactPerson = XMetaData3TUExtension.LITERAL_NOT_DEFINED_VALUE;
		this.organisation = XMetaData3TUExtension.LITERAL_NOT_DEFINED_VALUE;
	}
	
	public NDAContact(String organisation, String contactPerson)
	{
		this.contactPerson = contactPerson;
		this.organisation = organisation;
	}
	
	@Override
	public String toString() {
		if (organisation != null)
			return "Contact for " + organisation + ": " + contactPerson;
		else
			return "Contact:" + contactPerson;
	}
}
