package test;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.meta.extensions.AbstractXBaseExtensionImpl;

import test.meta3TU.LogType;
import test.meta3TU.NDAContact;
import test.meta3TU.ProcessType;
import test.meta3TU.Rights;

public class XMetaData3TUExtension extends AbstractXBaseExtensionImpl {

	/**
	 * Unique URI of this extension.
	 */
	public static final URI EXTENSION_URI = URI.create("http://www.xes-standard.org/meta_3TU.xesext");
	public static final String EXTENSION_PREFIX = "meta_3TU";
	public static final String EXTENSION_NAME = "MetaData_3TU";

	/**
    * 
    */
	private static final long serialVersionUID = -1229899891268051920L;

	/**
	 * Singleton instance of this extension.
	 */
	private static XMetaData3TUExtension singleton = new XMetaData3TUExtension();

	/**
	 * Registers the extension at the extensionmanager
	 */
	static {
		XExtensionManager.instance().register(singleton);
	}

	// List of attributes defined
	protected static XAttributeLiteral ATTR_DOI;
	protected static XAttributeLiteral ATTR_DESCRIPTION;
	protected static XAttributeLiteral ATTR_LANGUAGE;
	protected static XAttributeDiscrete ATTR_VERSION;
	protected static XAttributeTimestamp ATTR_CREATION_TIME;
	protected static XAttributeLiteral ATTR_CREATION_PLACE;
	protected static XAttributeLiteral ATTR_CREATION_INSTITUTE;
	protected static XAttributeLiteral ATTR_CREATION_PERSON;

	protected static XAttributeLiteral ATTR_SOURCE_INSTITUTE;
	protected static XAttributeLiteral ATTR_SOURCE_INSTITUTE_TYPE;
	protected static XAttributeLiteral ATTR_SOURCE_PROGRAM;
	protected static XAttributeLiteral ATTR_SOURCE_MODEL;
	protected static XAttributeLiteral ATTR_PROCESS_TYPE;
	protected static XAttributeLiteral ATTR_LOG_TYPE;
	protected static XAttributeLiteral ATTR_RIGHTS;
	protected static XAttributeLiteral ATTR_NDA_CONTACTS;
	protected static XAttributeTimestamp ATTR_NDA_END_TIME;

	/**
	 * Provides access to the singleton instance of this extension.
	 * 
	 * @return The General MetaData extension singleton.
	 */
	public static XMetaData3TUExtension instance() {
		return singleton;
	}

	/**
	 * Creates a new instance (hidden constructor).
	 */
	private XMetaData3TUExtension() {
		super(EXTENSION_NAME, EXTENSION_PREFIX, EXTENSION_URI);

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		ATTR_DOI = factory.createAttributeLiteral(EXTENSION_PREFIX + ":doi", LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_DESCRIPTION = factory.createAttributeLiteral(EXTENSION_PREFIX + ":description", LITERAL_NOT_DEFINED_VALUE,
				this);
		ATTR_LANGUAGE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":language", LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_VERSION = factory.createAttributeDiscrete(EXTENSION_PREFIX + ":version", DISCRETE_NOT_DEFINED_VALUE, this);
		ATTR_CREATION_TIME = factory.createAttributeTimestamp(EXTENSION_PREFIX + ":creation_time",
				TIMESTAMP_NOT_DEFINED_VALUE, this);
		ATTR_CREATION_INSTITUTE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":creation_institute",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_CREATION_PLACE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":creation_place",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_CREATION_PERSON = factory.createAttributeLiteral(EXTENSION_PREFIX + ":creation_person",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_SOURCE_INSTITUTE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":source_institute",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_SOURCE_INSTITUTE_TYPE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":source_institute_type",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_SOURCE_PROGRAM = factory.createAttributeLiteral(EXTENSION_PREFIX + ":source_program",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_SOURCE_MODEL = factory.createAttributeLiteral(EXTENSION_PREFIX + ":source_model",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_PROCESS_TYPE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":process_type",
				LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_LOG_TYPE = factory.createAttributeLiteral(EXTENSION_PREFIX + ":log_type", LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_RIGHTS = factory.createAttributeLiteral(EXTENSION_PREFIX + ":rights", LITERAL_NOT_DEFINED_VALUE, this);
		ATTR_NDA_CONTACTS = factory.createAttributeLiteral(EXTENSION_PREFIX + ":nda_contact", MULTIPLE_VALUE, this);
		ATTR_NDA_END_TIME = factory.createAttributeTimestamp(EXTENSION_PREFIX + ":nda_end_time",
				TIMESTAMP_NOT_DEFINED_VALUE, this);
	}

	/**
	 * Get the DOI (unique log identifier) from the log
	 */
	public String extractDOI(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_DOI.getKey());
	}

	/**
	 * Assign the DOI (unique log identifier) to the log
	 */
	public void assignDOI(XLog log, String doi) {
		assignAttribute(log, (XAttribute) ATTR_DOI, doi);
	}

	/**
	 * Get the description from the log
	 */
	public String extractDescription(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_DESCRIPTION.getKey());
	}

	/**
	 * Assign the description to the log
	 */
	public void assignDescription(XLog log, String description) {
		assignAttribute(log, (XAttribute) ATTR_DESCRIPTION, description);
	}

	/**
	 * Get the display name of the primary language in the log from the log
	 */
	public String extractLanguage(XLog log) {
		String languageCode = extractLanguageCode(log);
		for (Locale language : Locale.getAvailableLocales()) {
			try {
				if (language.getISO3Language().equals(languageCode))
					return language.getDisplayLanguage();
			} catch (MissingResourceException ex) {
			} // This indicates the language does not have an ISO 639-2 code;
				// Just continue with next language
		}

		return null;
	}

	/**
	 * Get the ISO 639-2 language code of the primary language in the log from
	 * the log
	 */
	public String extractLanguageCode(XLog log) {
		String languageCode = (String) super.extractAttributeValue(log, ATTR_LANGUAGE.getKey());
		if ((languageCode == null) || LITERAL_NOT_DEFINED_VALUE.equals(languageCode))
			return Locale.ROOT.getLanguage();
		else if (languageCode.length() == 2) { // A code of length 2 might
			// indicate a 639-1 language
			// code, attempt to translate
			// this
			for (Locale language : Locale.getAvailableLocales())
				if (language.getLanguage().equals(new Locale(languageCode, "", "").getLanguage()))
					return language.getISO3Language();
			return languageCode; // No match with a 2 length code, just return
			// the value found
		} else
			return languageCode;
	}

	/**
	 * Assign the ISO 639-2 language code of the primary language in the log to
	 * the log
	 */
	public void assignLanguageCode(XLog log, String languageCode) {
		boolean found = false;
		for (Locale language : Locale.getAvailableLocales()) {
			try {
				if (language.getISO3Language().equals(languageCode)) {
					found = true;
					break;
				}
			} catch (MissingResourceException ex) {
			} // This indicates the language does not have an ISO 639-2 code;
				// Just continue with next language
		}
		if (!found && !Locale.ROOT.getLanguage().equals(languageCode))
			throw new InvalidParameterException("Parameter languageCode is not a 3 letter ISO 639-2 language code");

		if (languageCode.equals(Locale.ROOT.getLanguage()))
			assignAttribute(log, (XAttribute) ATTR_LANGUAGE, LITERAL_NOT_DEFINED_VALUE);
		else
			assignAttribute(log, (XAttribute) ATTR_LANGUAGE, languageCode);
		;
	}

	/**
	 * Get the version from the log
	 */
	public Long extractVersion(XLog log) {
		return (Long) super.extractAttributeValue(log, ATTR_VERSION.getKey());
	}

	/**
	 * Assign the version to the log
	 */
	public void assignVersion(XLog log, String version) {
		assignAttribute(log, (XAttribute) ATTR_VERSION, version);
	}

	/**
	 * Get the creation time from the log. This is the time when the log was
	 * created (from the raw data).
	 */
	public Date extractCreationTime(XLog log) {
		return (Date) super.extractAttributeValue(log, ATTR_CREATION_TIME.getKey());
	}

	/**
	 * Assign the creation time to the log. This is the time when the log was
	 * created (from the raw data).
	 */
	public void assignCreationTime(XLog log, Date creationTime) {
		assignAttribute(log, (XAttribute) ATTR_CREATION_TIME, creationTime);
	}

	/**
	 * Get the creation institute from the log. This is the institute where the
	 * log was created (from the raw data).
	 */
	public String extractCreationInstitute(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_CREATION_INSTITUTE.getKey());
	}

	/**
	 * Assign the creation institute to the log. This is the institute where the
	 * log was created (from the raw data).
	 */
	public void assignCreationInstitute(XLog log, String creationInstitute) {
		assignAttribute(log, (XAttribute) ATTR_CREATION_INSTITUTE, creationInstitute);
	}

	/**
	 * Get the creation place from the log. This is the place where the log was
	 * created (from the raw data).
	 */
	public String extractCreationPlace(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_CREATION_PLACE.getKey());
	}

	/**
	 * Assign the creation place to the log. This is the place where the log was
	 * created (from the raw data).
	 */
	public void assignCreationPlace(XLog log, String creationPlace) {
		assignAttribute(log, (XAttribute) ATTR_CREATION_PLACE, creationPlace);
	}

	/**
	 * Get the creator (person) from the log. This is the main researcher
	 * involved in creating the event log (from the raw data).
	 */
	public String extractCreationPerson(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_CREATION_PERSON.getKey());
	}

	/**
	 * Assign the creator (person) to the log. This is the main researcher
	 * involved working on the event log (from the raw data).
	 */
	public void assignCreationPerson(XLog log, String creationPerson) {
		assignAttribute(log, (XAttribute) ATTR_CREATION_PERSON, creationPerson);
	}

	/**
	 * Get the source institute from the log. This is the name of the
	 * institution where the raw log data originated from.
	 */
	public String extractSourceInstitute(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_SOURCE_INSTITUTE.getKey());
	}

	/**
	 * Assign the source institute to the log. This is the name of the
	 * institution where the raw log data originated from.
	 */
	public void assignSourceInstitute(XLog log, String sourceInstitute) {
		assignAttribute(log, (XAttribute) ATTR_SOURCE_INSTITUTE, sourceInstitute);
	}

	/**
	 * Get the source institute type from the log. This is the type of
	 * institution where the raw log data originated from.
	 */
	public String extractSourceInstituteType(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_SOURCE_INSTITUTE_TYPE.getKey());
	}

	/**
	 * Assign the source institute type to the log. This is the type of
	 * institution where the raw log data originated from.
	 */
	public void assignSourceInstituteType(XLog log, String sourceInstituteType) {
		assignAttribute(log, (XAttribute) ATTR_SOURCE_INSTITUTE_TYPE, sourceInstituteType);
	}

	/**
	 * Get the source program from the log. This is the program that was used to
	 * create the raw log data.
	 */
	public String extractSourceProgram(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_SOURCE_PROGRAM.getKey());
	}

	/**
	 * Assign the source program to the log. This is the program that was used
	 * to create the raw log data.
	 */
	public void assignSourceProgram(XLog log, String sourceProgram) {
		assignAttribute(log, (XAttribute) ATTR_SOURCE_PROGRAM, sourceProgram);
	}

	/**
	 * Get the source model from the log. This is the model that was used to
	 * create the raw log data.
	 */
	public String extractSourceModel(XLog log) {
		return (String) super.extractAttributeValue(log, ATTR_SOURCE_MODEL.getKey());
	}

	/**
	 * Assign the source model to the log. This is the model that was used to
	 * create the raw log data.
	 */
	public void assignSourceModel(XLog log, String sourceModel) {
		assignAttribute(log, (XAttribute) ATTR_SOURCE_MODEL, sourceModel);
	}

	/**
	 * Get the process type from the log
	 */
	public ProcessType extractProcessType(XLog log) {
		String textualRepresentation = (String) super.extractAttributeValue(log, ATTR_PROCESS_TYPE.getKey());
		if (ProcessType.UNSTRUCTURED.toString().equalsIgnoreCase(textualRepresentation))
			return ProcessType.UNSTRUCTURED;
		else if (ProcessType.AD_HOC_STRUCTURED.toString().equalsIgnoreCase(textualRepresentation))
			return ProcessType.AD_HOC_STRUCTURED;
		else if (ProcessType.IMPLICITLY_STRUCTURED.toString().equalsIgnoreCase(textualRepresentation))
			return ProcessType.IMPLICITLY_STRUCTURED;
		else if (ProcessType.EXPLICITLY_STRUCTURED.toString().equalsIgnoreCase(textualRepresentation))
			return ProcessType.EXPLICITLY_STRUCTURED;
		else
			return ProcessType.UNKNOWN;
	}

	/**
	 * Assign the process type to the log
	 */
	public void assignProcessType(XLog log, ProcessType processType) {
		assignAttribute(log, (XAttribute) ATTR_PROCESS_TYPE, processType.toString());
	}

	/**
	 * Get the log type from the log
	 */
	public LogType extractLogType(XLog log) {
		String textualRepresentation = (String) super.extractAttributeValue(log, ATTR_LOG_TYPE.getKey());
		if (LogType.REAL_LIFE.toString().equalsIgnoreCase(textualRepresentation))
			return LogType.REAL_LIFE;
		else if (LogType.SYNTHETIC_LOG.toString().equalsIgnoreCase(textualRepresentation))
			return LogType.SYNTHETIC_LOG;
		else if (LogType.SYNTHETIC_MODEL.toString().equalsIgnoreCase(textualRepresentation))
			return LogType.SYNTHETIC_MODEL;
		else
			return LogType.UNKNOWN;
	}

	/**
	 * Assign the log type to the log
	 */
	public void assignLogType(XLog log, LogType logType) {
		assignAttribute(log, (XAttribute) ATTR_LOG_TYPE, logType.toString());
	}

	/**
	 * Get the publication rights attribute from the log
	 */
	public XAttributeLiteral extractRightsAttribute(XLog log) {
		return (XAttributeLiteral) super.extractAttribute(log, ATTR_RIGHTS.getKey());
	}

	/**
	 * Get the publication rights from the log
	 */
	public Rights extractRights(XLog log) {
		String textualRepresentation = (String) super.extractAttributeValue(log, ATTR_RIGHTS.getKey());
		if (Rights.NON_DISCLOSURE_AGREEMENT.toString().equalsIgnoreCase(textualRepresentation))
			return Rights.NON_DISCLOSURE_AGREEMENT;
		else if (Rights.PUBLIC.toString().equalsIgnoreCase(textualRepresentation))
			return Rights.PUBLIC;
		else
			return Rights.UNKNOWN;
	}

	/**
	 * Assign the publication rights to the log
	 */
	public XAttributeLiteral assignRights(XLog log, Rights rights) {
		return (XAttributeLiteral) assignAttribute(log, (XAttribute) ATTR_RIGHTS, rights.toString());
	}

	public Date extractNDAEndTime(XLog log) {
		XAttributeLiteral rightsAttribute = extractRightsAttribute(log);
		if (rightsAttribute == null)
			return null;
		return (Date) extractAttributeValue(rightsAttribute, ATTR_NDA_END_TIME.getKey());
	}

	public void assignNDAEndTime(XLog log, Date ndaEndTime) {
		XAttributeLiteral rightsAttribute = extractRightsAttribute(log);
		if (rightsAttribute == null)
			rightsAttribute = assignRights(log, Rights.NON_DISCLOSURE_AGREEMENT);

		assignAttribute(rightsAttribute, ATTR_NDA_END_TIME, ndaEndTime);
	}

	/**
	 * Get all contacts for the Non-Disclosure Agreement from the log
	 */
	public List<NDAContact> extractNDAContacts(XLog log) {
		ArrayList<NDAContact> result = new ArrayList<NDAContact>();
		XAttributeLiteral rightsAttribute = extractRightsAttribute(log);
		if (rightsAttribute == null)
			return result;

		XAttributeLiteral ndaContactsAttribute = (XAttributeLiteral) extractAttribute(rightsAttribute,
				ATTR_NDA_CONTACTS.getKey());
		if (ndaContactsAttribute == null)
			return result;

		for (XAttribute ndaContactAttribute : ndaContactsAttribute.getAttributes().values())
			if (XAttributeLiteral.class.isAssignableFrom(ndaContactAttribute.getClass()))
				result.add(new NDAContact(ndaContactAttribute.getKey(), ((XAttributeLiteral) ndaContactAttribute)
						.getValue()));

		return result;
	}

	/**
	 * Get the contact for the Non-Disclosure Agreement for a specific
	 * organization from the log
	 */
	public NDAContact extractNDAContact(XLog log, String organisation) {
		XAttributeLiteral rightsAttribute = extractRightsAttribute(log);
		if (rightsAttribute == null)
			return null;

		XAttributeLiteral ndaContactsAttribute = (XAttributeLiteral) extractAttribute(rightsAttribute,
				ATTR_NDA_CONTACTS.getKey());
		if (ndaContactsAttribute == null)
			return null;

		XAttributeLiteral ndaContactAttribute = (XAttributeLiteral) extractAttribute(ndaContactsAttribute, organisation);
		if (ndaContactAttribute == null)
			return null;
		else
			return new NDAContact(ndaContactAttribute.getKey(), ndaContactAttribute.getValue());
	}

	/**
	 * Assign a contact for the Non-Disclosure Agreement to the log
	 */
	public void assignNDAContact(XLog log, NDAContact contact) {
		assignNDAContact(log, contact.organisation, contact.contactPerson);
	}

	/**
	 * Assign a contact for the Non-Disclosure Agreement to the log
	 */
	public void assignNDAContact(XLog log, String organisation, String contactPerson) {
		XAttributeLiteral rightsAttribute = extractRightsAttribute(log);
		if (rightsAttribute == null)
			rightsAttribute = assignRights(log, Rights.NON_DISCLOSURE_AGREEMENT);

		XAttributeLiteral ndaContactsAttribute = (XAttributeLiteral) extractAttribute(rightsAttribute,
				ATTR_NDA_CONTACTS.getKey());
		if (ndaContactsAttribute == null)
			ndaContactsAttribute = (XAttributeLiteral) assignAttribute(rightsAttribute, ATTR_NDA_CONTACTS);

		XAttributeLiteral newAttribute = XFactoryRegistry.instance().currentDefault()
				.createAttributeLiteral(organisation, contactPerson, this);
		assignAttribute(ndaContactsAttribute, newAttribute);
	}
}
