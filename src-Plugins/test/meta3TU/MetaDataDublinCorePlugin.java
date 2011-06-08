package test.meta3TU;

import info.clearthought.layout.TableLayout;

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.bind.ValidationException;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XsDateTimeFormat;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.meta.MetaDataResultImpl;
import org.processmining.plugins.log.meta.extensions.XMetaDataConceptExtension;
import org.processmining.plugins.log.meta.extensions.XMetaDataGeneralExtension;
import org.processmining.plugins.log.meta.extensions.XMetaDataLifeCycleExtension;
import org.processmining.plugins.log.meta.extensions.XMetaDataOrganizationalExtension;
import org.processmining.plugins.log.meta.extensions.XMetaDataTimeExtension;

import test.XMetaData3TUExtension;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.toedter.calendar.JDateChooser;

@Plugin(name = "Publish to 3TU datacenter", userAccessible = false, parameterLabels = { "Log" }, returnTypes = XLog.class, returnLabels = { "Result of adding the Dublin Core Meta Data" })
public class MetaDataDublinCorePlugin implements ActionListener {
	private UIPluginContext pluginContext;
	private JPanel panel;

	private JTextField contributerTextField = new JTextField();
	private JTextField creatorTextField = new JTextField();
	private JDateChooser creationDateChooser = new JDateChooser();
	private JTextField descriptionTextField = new JTextField();
	private JTextField extentTextField = new JTextField();
	private JComboBox languageComboBox;
	private JTextField publisherTextField = new JTextField();
	private JComboBox rightsComboBox;
	private JTextField subjectTextField = new JTextField();;
	private JTextField titleTextField = new JTextField();
	private JTextField collectionTextField = new JTextField();;

	private JTextField doiTextField = new JTextField("doi:10.4121/");
	private JTextField creationPlaceTextField = new JTextField();
	private JTextField sourceInstituteTypeTextField = new JTextField();
	private JTextField sourceProgramTextField = new JTextField();
	private JTextField sourceModelTextField = new JTextField();
	private JComboBox processTypeComboBox;
	private JComboBox logTypeComboBox;
	private JLabel ndaHasEndTimeLabel;
	private JCheckBox ndaHasEndTimeCheckBox;
	private JDateChooser ndaEndTimeDateChooser = new JDateChooser();
	private JLabel ndaContactsLabel;
	private JLabel ndaContactsOrganizationLabel;
	private JLabel ndaContactsContactPersonLabel;
	private JButton addNdaContactButton;
	private ArrayList<JTextField> ndaContactOrganizationTextFields = new ArrayList<JTextField>();
	private ArrayList<JTextField> ndaContactPersonTextFields = new ArrayList<JTextField>();

	@UITopiaVariant(author = "B.F. van Dongen", affiliation = UITopiaVariant.EHV, email = "B.F.v.Dongen@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "", requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) throws IOException {
		// make sure general meta data is present
		XExtension[] mE = new XExtension[] { XMetaDataGeneralExtension.instance(), XMetaDataTimeExtension.instance(),
				XMetaDataLifeCycleExtension.instance(), XMetaDataConceptExtension.instance(),
				XMetaDataOrganizationalExtension.instance() };
		List<XExtension> metaExtensions = new ArrayList<XExtension>();
		for (XExtension e : mE) {
			if (!log.getExtensions().contains(e)) {
				throw new IOException("Not all extensions are present in the log.");
			}
			metaExtensions.add(e);
		}
		metaExtensions.add(XMetaData3TUExtension.instance());

		// Create a MetaDataResult, supply the extension that describes the
		// calculated meta data (if available) and supply the calculated
		// attributes.
		MetaDataResultImpl result = new MetaDataResultImpl();

		// Put your marvelously complex meta data calculation code here ...
		pluginContext = context;

		// Set progress estimation to indeterminate.
		pluginContext.getProgress().setIndeterminate(true);
		pluginContext.getProgress().setCaption("Displaying wizard to enter 3TU Dublin core meta data");

		// Display GUI
		initComponents(log);
		JComponent dublin = initDublinCoreComponents(log);
		JComponent threeTU = init3TUComponents(log);
		setAttributes(log);

		InteractionResult guiResult = InteractionResult.PREV;
		boolean validated = false;

		// Process GUI results
		while (!(guiResult == InteractionResult.FINISHED) || !validated) {
			if (guiResult == InteractionResult.CANCEL) {
				pluginContext.getFutureResult(0).cancel(true);

				throw new CancellationException("The wizard has been cancelled.");
			} else if (guiResult == InteractionResult.NEXT) {
				try {
					guiResult = context.showWizard("3TU Meta Data addition", false, true, threeTU);
					validateFields();
					result.setExtension(XMetaData3TUExtension.instance());
					result.addLogAttributes(getAttributes());
					validated = true;
				} catch (ValidationException ex) {
					JOptionPane.showMessageDialog(context.getGlobalContext().getUI(), ex.getMessage());
					guiResult = InteractionResult.NEXT;
				}
			} else if (guiResult == InteractionResult.PREV) {
				guiResult = context.showWizard("3TU Dublin Core Meta Data addition", true, false, dublin);
			}
		}

		// Save the CSV file
		//	saveFile(context, log);

		// Now clone the log and add the data attributes from the extension, including the DOI
		final XLog newlog = (XLog) log.clone();
		newlog.getExtensions().add(result.getExtension());
		newlog.getAttributes().putAll(result.getLogAttributes());

		System.out.println(titleTextField.getText());
		XConceptExtension.instance().assignName(newlog, titleTextField.getText());

		File location = getFileLocation(context);

		saveCSVFile(context, newlog, location);

		File fl = new File(location.getAbsolutePath() + File.separator
				+ XConceptExtension.instance().extractName(newlog) + ".xes.gz");
		fl.createNewFile();

		OutputStream logOut = new BufferedOutputStream(new FileOutputStream(fl));
		new XesXmlGZIPSerializer().serialize(newlog, logOut);
		logOut.close();

		Set<XExtension> extensions = newlog.getExtensions();

		File f = new File(location.getAbsolutePath() + File.separator
				+ XConceptExtension.instance().extractName(newlog) + "-meta.xml");
		f.createNewFile();

		if (f != null) {
			FileOutputStream target = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(target);

			writer.append("<metadata>\n");

			// First write the 3TU meta data, including the DOI
			write3TUMeta(newlog, writer, fl.getName());

			// Then some easy properties
			writeLine("lifecycle_model", XLifecycleExtension.instance().extractModel(log), writer);
			writeLine("number_of_traces", XMetaDataGeneralExtension.instance().extractTracesTotal(newlog), writer);
			writeLine("number_of_events", XMetaDataGeneralExtension.instance().extractEventsTotal(newlog), writer);
			writeLine("events_per_trace", XMetaDataGeneralExtension.instance().extractEventsAverage(newlog), writer);
			writeLine("min_events_per_trace", XMetaDataGeneralExtension.instance().extractEventsMin(newlog), writer);
			writeLine("max_events_per_trace", XMetaDataGeneralExtension.instance().extractEventsMax(newlog), writer);

			// Then, write the globals
			writeGlobals(newlog, writer);

			// Then write all other extensions
			writer.append("   <meta_extensions>\n");
			extensions.remove(XMetaData3TUExtension.instance());
			List<XExtension> toDo = getSortedList(extensions);
			Iterator<XExtension> it = toDo.iterator();
			while (it.hasNext()) {
				URI uri = it.next().getUri();
				XExtension ext = XExtensionManager.instance().getByUri(uri);
				if (metaExtensions.contains(ext)) {
					writeExtensionMeta(newlog, ext, writer);
					it.remove();
				}
			}
			writer.append("   </meta_extensions>\n");

			writer.append("   <extensions>\n");
			it = toDo.iterator();
			while (it.hasNext()) {
				XExtension ext = it.next();
				writeExtensionMeta(newlog, ext, writer);
			}
			writer.append("   </extensions>\n");

			writer.append("</metadata>\n");

			writer.close();
		}

		return newlog;

	}

	private void writeExtensionMeta(XLog log, XExtension extension, PrintWriter out) {

		out.append("      <" + extension.getName().replace(' ', '_') + " prefix=\"" + extension.getPrefix()
				+ "\" uri=\"" + extension.getUri() + "\">\n");

		for (XAttribute attribute : getSortedAttributes(extension.getLogAttributes())) {
			if (attribute != null) {
				String key = attribute.getKey().substring(extension.getPrefix().length() + 1);
				Object val = log.getAttributes().get(attribute.getKey());
				if (val != null) {
					out.append("         ");
					out.append("<" + key + ">" + val + "</" + key + ">\n");
				}
			}
		}
		out.append("      </" + extension.getName().replace(' ', '_') + ">\n");

	}

	private void write3TUMeta(XLog log, PrintWriter out, String name) {
		writeLine("doi", XMetaData3TUExtension.instance().extractDOI(log), out);
		out.append("   <name>" + name + "</name>\n");
		writeLine("description", XMetaData3TUExtension.instance().extractDescription(log), out);
		writeLine("language", XMetaData3TUExtension.instance().extractLanguage(log), out);
		writeLine("log_type", XMetaData3TUExtension.instance().extractLogType(log), out);
		writeLine("process_type", XMetaData3TUExtension.instance().extractProcessType(log), out);

		out.append("   <creation>\n");
		writeLine("insitute", XMetaData3TUExtension.instance().extractCreationInstitute(log), out);
		writeLine("person", XMetaData3TUExtension.instance().extractCreationPerson(log), out);
		writeLine("place", XMetaData3TUExtension.instance().extractCreationPlace(log), out);
		writeLine("time", XMetaData3TUExtension.instance().extractCreationTime(log), out);
		out.append("   </creation>\n");

		out.append("   <source>\n");
		writeLine("insitute", XMetaData3TUExtension.instance().extractSourceInstitute(log), out);
		writeLine("institute_type", XMetaData3TUExtension.instance().extractSourceInstituteType(log), out);
		writeLine("model", XMetaData3TUExtension.instance().extractSourceModel(log), out);
		writeLine("program", XMetaData3TUExtension.instance().extractSourceProgram(log), out);
		out.append("   </source>\n");

		Rights type = XMetaData3TUExtension.instance().extractRights(log);
		out.append("   <rights type=\"" + type.toString() + "\">\n");
		if (type.equals(Rights.NON_DISCLOSURE_AGREEMENT)) {
			writeLine("nda_contact", XMetaData3TUExtension.instance().extractNDAContacts(log), out);
			writeLine("nda_end_time", XMetaData3TUExtension.instance().extractNDAEndTime(log), out);
		}
		out.append("   </rights>\n");

	}

	private void writeLine(String tag, Object val, PrintWriter out) {
		if (val != null) {
			out.append("   <" + tag + ">" + val.toString() + "</" + tag + ">\n");
		} else {
			System.err.println("cannot write tag: " + tag);
		}
	}

	private void writeGlobals(XLog log, PrintWriter out) {
		out.append("   <global_attributes>\n");
		out.append("      <trace_level>\n");
		for (XAttribute attribute : getSortedAttributes(log.getGlobalTraceAttributes())) {
			String key = attribute.getKey();
			String value = attribute.toString();
			out.append("         <attribute key=\"" + key + "\" default=\"" + value + "\"/>\n");
		}
		out.append("      </trace_level>\n");
		out.append("      <event_level>\n");
		for (XAttribute attribute : getSortedAttributes(log.getGlobalEventAttributes())) {
			String key = attribute.getKey();
			String value = attribute.toString();
			out.append("         <attribute key=\"" + key + "\" default=\"" + value + "\"/>\n");
		}
		out.append("      </event_level>\n");

		out.append("   </global_attributes>\n");

	}

	private static <T extends XAttribute> List<XAttribute> getSortedAttributes(Collection<T> toSort) {
		ArrayList<XAttribute> list = new ArrayList<XAttribute>(toSort);

		Collections.sort(list, new Comparator<XAttribute>() {

			public int compare(XAttribute o1, XAttribute o2) {
				int c = o1.getKey().compareTo(o2.getKey());
				if (c != 0) {
					return c;
				}
				return System.identityHashCode(o1) - System.identityHashCode(o2);
			}
		});
		return list;
	}

	private static <T> List<T> getSortedList(Collection<T> toSortByToString) {
		ArrayList<T> list = new ArrayList<T>(toSortByToString);

		Collections.sort(list, new Comparator<T>() {

			public int compare(T o1, T o2) {
				int c = o1.toString().compareTo(o2.toString());
				if (c != 0) {
					return c;
				}
				return System.identityHashCode(o1) - System.identityHashCode(o2);
			}
		});
		return list;
	}

	private File getFileLocation(UIPluginContext context) {
		String folder = Preferences.userNodeForPackage(this.getClass()).get("folder", null);

		JFileChooser fileChooser = new JFileChooser(folder);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		selectfile: if (fileChooser.showSaveDialog(context.getGlobalContext().getUI()) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();

			if (!f.exists()) {
				if (JOptionPane.showConfirmDialog(context.getGlobalContext().getUI(), "Create folder?",
						"Create folder", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					break selectfile;
				} else {
					f.mkdirs();
				}
			} else {
				if ((f.listFiles().length > 0)
						&& JOptionPane.showConfirmDialog(context.getGlobalContext().getUI(),
								"Overwrite existing files?", "Overwrite existing files?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					break selectfile;
				} else {
					f.mkdirs();
				}
			}
			Preferences.userNodeForPackage(this.getClass()).put("folder", f.getAbsolutePath());
			return f;
		}
		return null;
	}

	private void saveCSVFile(UIPluginContext context, XLog log, File location) throws HeadlessException, IOException {
		assert (location.isDirectory());
		// Check field values and return
		XsDateTimeFormat xsDateTimeFormat = new XsDateTimeFormat();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		File f = new File(location.getAbsolutePath() + File.separator + XConceptExtension.instance().extractName(log)
				+ ".csv");
		f.createNewFile();

		if (f != null) {

			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f)));

			writer.append("about date/time from;");
			writer.append("about date/time to;");
			writer.append("contributor;");
			writer.append("creator;");
			writer.append("date;");
			writer.append("description;");
			writer.append("extent;");
			writer.append("identifier;");
			writer.append("language;");
			writer.append("publisher;");
			writer.append("rights;");
			writer.append("subject;");
			writer.append("title;");

			writer.println();

			// from date
			writer.append(xsDateTimeFormat.format(XMetaDataTimeExtension.instance().extractLogStartTime(log)) + ";");
			// to date
			writer.append(xsDateTimeFormat.format(XMetaDataTimeExtension.instance().extractLogEndTime(log)) + ";");
			// contributor
			writer.append(contributerTextField.getText() + ";");
			// creator
			writer.append(creatorTextField.getText() + ";");
			// date
			writer.append(simpleDateFormat.format(creationDateChooser.getDate()) + ";");
			// description
			writer.append(descriptionTextField.getText() + ";");
			// extent
			writer.append(extentTextField.getText() + ";");
			// identifier
			writer.append("UUID;");
			// language
			writer.append(((LocaleItem) languageComboBox.getSelectedItem()).getISO639Code() + ";");
			// publisher
			writer.append(publisherTextField.getText() + ";");
			// rights
			writer.append(rightsComboBox.getSelectedItem() + ";");
			// subject
			writer.append(subjectTextField.getText() + ";");
			// title
			writer.append(titleTextField.getText() + ";");

			writer.println();
			writer.flush();
			writer.close();

		}

	}

	private void initComponents(XLog log) {
		SlickerFactory sf = SlickerFactory.instance();

		if (log.getExtensions().contains(XConceptExtension.instance())) {
			titleTextField.setText(XConceptExtension.instance().extractName(log));
		}
		extentTextField.setText("Traces: " + XMetaDataGeneralExtension.instance().extractTracesTotal(log)
				+ ", Events: " + XMetaDataGeneralExtension.instance().extractEventsTotal(log));

		Locale[] locales = Locale.getAvailableLocales();
		TreeSet<LocaleItem> languages = new TreeSet<LocaleItem>(new LocaleItemComparator());
		languages.add(new LocaleItem(Locale.ROOT));
		LocaleItem currentLocale = null;
		for (int i = 0; i < locales.length; i++) {
			LocaleItem currentLanguage = new LocaleItem(locales[i]);
			languages.add(currentLanguage);
			if (currentLanguage.locale.equals(Locale.getDefault())) {
				currentLocale = currentLanguage;
			}
		}
		languageComboBox = sf.createComboBox(languages.toArray());
		languageComboBox.setSelectedItem(currentLocale != null ? currentLocale : languages.first());

		subjectTextField.setText("000 Computer science, knowledge & systems");
		collectionTextField.setText("IEEE Task Force Process Mining");
		creatorTextField.setText(System.getProperty("user.name"));
		creationDateChooser.setDate(new Date(System.currentTimeMillis()));
		rightsComboBox = sf.createComboBox(Rights.values());
		rightsComboBox.setSelectedItem(Rights.NON_DISCLOSURE_AGREEMENT);
		processTypeComboBox = sf.createComboBox(ProcessType.values());
		logTypeComboBox = sf.createComboBox(LogType.values());

		rightsComboBox.addActionListener(this);
	}

	private JComponent initDublinCoreComponents(XLog log) {
		SlickerFactory sf = SlickerFactory.instance();

		panel = sf.createRoundedPanel();
		panel.setLayout(new TableLayout(new double[][] { { 200, 30, 200, TableLayout.FILL },
				{ 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, TableLayout.FILL } }));

		// Create label for log metric method implementation.
		panel.add(sf.createLabel("Please enter the meta data in the fields below"), "0,0,3,0");

		panel.add(sf.createLabel("Title:"), "0, 1");
		titleTextField.setEditable(true);
		if (log.getExtensions().contains(XConceptExtension.instance())) {
			String name = XConceptExtension.instance().extractName(log);
			// strip extensions from filename
			int i = name.lastIndexOf('.');
			if (i > 0) {
				name = name.substring(0, i);
				i = name.lastIndexOf('.');
			}
			if (i > 0) {
				name = name.substring(0, i);
			}
			titleTextField.setText(name);
		}
		panel.add(titleTextField, "1,1,3,1");

		panel.add(sf.createLabel("Contributor (institute):"), "0, 2");
		contributerTextField.setEditable(true);
		panel.add(contributerTextField, "1,2,3,2");

		panel.add(sf.createLabel("Creator (person):"), "0, 3");
		creatorTextField.setEditable(true);
		panel.add(creatorTextField, "1,3,3,3");

		panel.add(sf.createLabel("Creation date:"), "0, 4");
		creationDateChooser.setEnabled(true);
		panel.add(creationDateChooser, "1,4,3,4");

		panel.add(sf.createLabel("Description:"), "0, 5");
		descriptionTextField.setEditable(true);
		panel.add(descriptionTextField, "1,5,3,5");

		panel.add(sf.createLabel("Extent:"), "0, 6");
		extentTextField.setEditable(false);
		panel.add(extentTextField, "1,6,3,6");

		panel.add(sf.createLabel("Language:"), "0, 7");
		languageComboBox.setEditable(true);
		panel.add(languageComboBox, "1,7,3,7");

		panel.add(sf.createLabel("Publisher (institute):"), "0, 8");
		publisherTextField.setEditable(true);
		panel.add(publisherTextField, "1,8,3,8");

		panel.add(sf.createLabel("Rights:"), "0, 9");
		rightsComboBox.setEditable(true);
		panel.add(rightsComboBox, "1,9,3,9");

		panel.add(sf.createLabel("Subject:"), "0, 10");
		subjectTextField.setEditable(true);
		panel.add(subjectTextField, "1,10,3,10");

		// TODO: Let user choose collection, instead of typing one. Required
		// webservice
		panel.add(sf.createLabel("Collection:"), "0, 11");
		collectionTextField.setEditable(true);
		panel.add(collectionTextField, "1,11,3,11");

		return panel;
	}

	private JComponent init3TUComponents(XLog log) {
		SlickerFactory sf = SlickerFactory.instance();

		panel = sf.createRoundedPanel();
		panel.setLayout(new TableLayout(new double[][] { { 200, 30, 200, TableLayout.FILL },
				{ 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, TableLayout.FILL } }));

		// Create label for log metric method implementation.
		panel.add(sf.createLabel("Please enter additional meta " + "data in the fields below"), "0,0,3,0");

		// TODO: Retrieve DOI through webservice
		panel.add(sf.createLabel("DOI:"), "0, 2");
		doiTextField.setEditable(true);
		panel.add(doiTextField, "1,2,3,2");

		panel.add(sf.createLabel("Creation place:"), "0, 3");
		creationPlaceTextField.setEditable(true);
		panel.add(creationPlaceTextField, "1,3,3,3");

		panel.add(sf.createLabel("Source institute type:"), "0, 4");
		sourceInstituteTypeTextField.setEditable(true);
		panel.add(sourceInstituteTypeTextField, "1,4,3,4");

		panel.add(sf.createLabel("Source program:"), "0, 5");
		sourceProgramTextField.setEditable(true);
		panel.add(sourceProgramTextField, "1,5,3,5");

		panel.add(sf.createLabel("Source model:"), "0, 6");
		sourceModelTextField.setEditable(true);
		panel.add(sourceModelTextField, "1,6,3,6");

		panel.add(sf.createLabel("Process type:"), "0, 7");
		processTypeComboBox.setEditable(true);
		panel.add(processTypeComboBox, "1,7,3,7");

		panel.add(sf.createLabel("Log type:"), "0, 8");
		logTypeComboBox.setEditable(true);
		panel.add(logTypeComboBox, "1,8,3,8");

		if (rightsComboBox.getSelectedItem().equals(Rights.NON_DISCLOSURE_AGREEMENT)) {

			ndaHasEndTimeLabel = sf.createLabel("NDA has end time:");
			panel.add(ndaHasEndTimeLabel, "0, 9");
			ndaHasEndTimeCheckBox = sf.createCheckBox("", true);
			ndaEndTimeDateChooser.setDate(Calendar.getInstance().getTime());
			panel.add(ndaHasEndTimeCheckBox, "1,9", 0);
			ndaHasEndTimeCheckBox.addActionListener(this);
			panel.add(ndaEndTimeDateChooser, "2,9,3,9", 1);

			ndaContactsLabel = sf.createLabel("NDA contacts:");
			panel.add(ndaContactsLabel, "0, 10");
			ndaContactsOrganizationLabel = sf.createLabel("Organization");
			ndaContactsOrganizationLabel.setFont(ndaContactsLabel.getFont().deriveFont(Font.ITALIC));
			panel.add(ndaContactsOrganizationLabel, "1, 10,2,10");
			ndaContactsContactPersonLabel = sf.createLabel("Contact person");
			ndaContactsContactPersonLabel.setFont(ndaContactsLabel.getFont().deriveFont(Font.ITALIC));
			panel.add(ndaContactsContactPersonLabel, "3, 10");

			addNdaContactButton = sf.createButton("Add contact");
			addNdaContactButton.addActionListener(this);
			panel.add(addNdaContactButton, "0, 11");

		}

		return panel;
	}

	private void setAttributes(XLog log) {

		doiTextField.setText(String.format("%1$s", XMetaData3TUExtension.instance().extractDOI(log)).replace("null",
				doiTextField.getText()));
		descriptionTextField.setText(String.format("%1$s", XMetaData3TUExtension.instance().extractDescription(log))
				.replace("null", descriptionTextField.getText()));

		String languageCode = XMetaData3TUExtension.instance().extractLanguageCode(log);
		if (languageCode != null) {
			for (int i = 0; i < languageComboBox.getItemCount(); i++) {
				LocaleItem language = (LocaleItem) languageComboBox.getItemAt(i);
				if (language.getISO639Code().equals(languageCode))
					languageComboBox.setSelectedIndex(i);
			}
		}

		Date creationTime = XMetaData3TUExtension.instance().extractCreationTime(log);
		if (creationTime != null)
			creationDateChooser.setDate(creationTime);

		creationPlaceTextField.setText(String
				.format("%1$s", XMetaData3TUExtension.instance().extractCreationPlace(log)).replace("null",
						creationPlaceTextField.getText()));
		publisherTextField.setText(String
				.format("%1$s", XMetaData3TUExtension.instance().extractCreationInstitute(log)).replace("null",
						publisherTextField.getText()));
		creatorTextField.setText(String.format("%1$s", XMetaData3TUExtension.instance().extractCreationPerson(log))
				.replace("null", creatorTextField.getText()));
		contributerTextField.setText(String
				.format("%1$s", XMetaData3TUExtension.instance().extractSourceInstitute(log)).replace("null",
						contributerTextField.getText()));
		sourceInstituteTypeTextField.setText(String.format("%1$s",
				XMetaData3TUExtension.instance().extractSourceInstituteType(log)).replace("null",
				sourceInstituteTypeTextField.getText()));
		sourceProgramTextField.setText(String
				.format("%1$s", XMetaData3TUExtension.instance().extractSourceProgram(log)).replace("null",
						sourceProgramTextField.getText()));
		sourceModelTextField.setText(String.format("%1$s", XMetaData3TUExtension.instance().extractSourceModel(log))
				.replace("null", sourceModelTextField.getText()));

		processTypeComboBox.setSelectedItem(XMetaData3TUExtension.instance().extractProcessType(log));
		logTypeComboBox.setSelectedItem(XMetaData3TUExtension.instance().extractLogType(log));

		Date ndaEndTime = XMetaData3TUExtension.instance().extractNDAEndTime(log);
		if (ndaEndTime != null) {
			ndaHasEndTimeCheckBox.setSelected(true);
			ndaEndTimeDateChooser.setDate(ndaEndTime);
		} else {
			ndaHasEndTimeCheckBox.setSelected(false);
		}

		List<NDAContact> ndaContacts = XMetaData3TUExtension.instance().extractNDAContacts(log);
		if (ndaContacts.size() == 0)
			addNdaContactLine();
		for (NDAContact contact : ndaContacts)
			addNdaContactLine(contact.organisation, contact.contactPerson);

		// Set at the latest time to correctly visualize nda components
		if (XMetaData3TUExtension.instance().extractRights(log) != null) {
			rightsComboBox.setSelectedItem(XMetaData3TUExtension.instance().extractRights(log));
		}
	}

	private void addNdaContactLine() {
		addNdaContactLine("", "");
	}

	private void addNdaContactLine(String organization, String contactPerson) {
		TableLayout layout = (TableLayout) panel.getLayout();

		if (layout.getNumRow() <= (11 + ndaContactOrganizationTextFields.size())) {
			layout.insertRow(11 + ndaContactOrganizationTextFields.size(), 30);
			panel.setLayout(layout);
		}

		JTextField ndaContactOrganizationTextField = new JTextField();
		ndaContactOrganizationTextField.setText(organization);
		ndaContactOrganizationTextFields.add(ndaContactOrganizationTextField);
		panel.add(ndaContactOrganizationTextField,
				String.format("1, %1$s, 2, %1$s", 10 + ndaContactOrganizationTextFields.size()));

		JTextField ndaContactPersonTextField = new JTextField();
		ndaContactPersonTextField.setText(contactPerson);
		ndaContactPersonTextFields.add(ndaContactPersonTextField);
		panel.add(ndaContactPersonTextField, String.format("3, %1$s", 10 + ndaContactPersonTextFields.size()));

		if (panel.getParent() != null)
			panel.getParent().validate();
		else
			panel.validate();
	}

	private Set<XAttribute> getAttributes() {
		// Write attributes to dummy log (used as temporary storage)
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog dummyLog = factory.createLog();

		if (doiTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignDOI(dummyLog, doiTextField.getText());

		if (descriptionTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignDescription(dummyLog, descriptionTextField.getText());

		XMetaData3TUExtension.instance().assignLanguageCode(dummyLog,
				((LocaleItem) languageComboBox.getSelectedItem()).getISO639Code());

		if (creationDateChooser.getDate() != null)
			XMetaData3TUExtension.instance().assignCreationTime(dummyLog, creationDateChooser.getDate());

		if (creatorTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignCreationPerson(dummyLog, creatorTextField.getText());

		if (publisherTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignCreationInstitute(dummyLog, publisherTextField.getText());

		if (creationPlaceTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignCreationPlace(dummyLog, creationPlaceTextField.getText());

		if (contributerTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignSourceInstitute(dummyLog, contributerTextField.getText());

		if (sourceInstituteTypeTextField.getText().length() > 0)
			XMetaData3TUExtension.instance()
					.assignSourceInstituteType(dummyLog, sourceInstituteTypeTextField.getText());

		if (sourceProgramTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignSourceProgram(dummyLog, sourceProgramTextField.getText());

		if (sourceModelTextField.getText().length() > 0)
			XMetaData3TUExtension.instance().assignSourceModel(dummyLog, sourceModelTextField.getText());

		XMetaData3TUExtension.instance().assignProcessType(dummyLog,
				(ProcessType) processTypeComboBox.getSelectedItem());
		XMetaData3TUExtension.instance().assignLogType(dummyLog, (LogType) logTypeComboBox.getSelectedItem());
		XMetaData3TUExtension.instance().assignRights(dummyLog, (Rights) rightsComboBox.getSelectedItem());

		if (ndaHasEndTimeCheckBox.isSelected() && (ndaEndTimeDateChooser.getDate() != null))
			XMetaData3TUExtension.instance().assignNDAEndTime(dummyLog, ndaEndTimeDateChooser.getDate());

		for (int i = 0; i < ndaContactOrganizationTextFields.size(); i++) {
			String ndaContactOrganization = ndaContactOrganizationTextFields.get(i).getText();
			String ndaContactPerson = ndaContactPersonTextFields.get(i).getText();

			if ((ndaContactOrganization.length() > 0) && (ndaContactPerson.length() > 0))
				XMetaData3TUExtension.instance().assignNDAContact(dummyLog, ndaContactOrganization, ndaContactPerson);
			else if (ndaContactOrganization.length() > 0)
				XMetaData3TUExtension.instance().assignNDAContact(dummyLog, ndaContactOrganization,
						XMetaData3TUExtension.LITERAL_NOT_DEFINED_VALUE);
		}

		// Return found attributes
		return XMetaData3TUExtension.instance().extractAttributes(dummyLog);
	}

	private void validateFields() throws ValidationException {
		ArrayList<String> contactOrganizations = new ArrayList<String>();
		for (int i = 0; i < ndaContactOrganizationTextFields.size(); i++) {
			String ndaContactOrganization = ndaContactOrganizationTextFields.get(i).getText();
			String ndaContactPerson = ndaContactPersonTextFields.get(i).getText();

			if (contactOrganizations.contains(ndaContactOrganization))
				throw new ValidationException("More than one contact person is defined for organization '"
						+ ndaContactOrganization + "'. Please define only one contact person per organization.");
			else if ((ndaContactOrganization.length() == 0) && (ndaContactPerson.length() > 0))
				throw new ValidationException(
						"Contact person '"
								+ ndaContactPerson
								+ "' is defined without an organization. Please define an organization for this contact person.");
			else if (ndaContactOrganization.length() > 0)
				contactOrganizations.add(ndaContactOrganization);
		}
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getSource() == rightsComboBox) {
			boolean hasNDA = (((Rights) rightsComboBox.getSelectedItem()) == Rights.NON_DISCLOSURE_AGREEMENT);
			ndaHasEndTimeLabel.setVisible(hasNDA);
			ndaHasEndTimeCheckBox.setVisible(hasNDA);
			ndaEndTimeDateChooser.setVisible(hasNDA && ndaHasEndTimeCheckBox.isSelected());
			ndaContactsLabel.setVisible(hasNDA);
			ndaContactsOrganizationLabel.setVisible(hasNDA);
			ndaContactsContactPersonLabel.setVisible(hasNDA);
			addNdaContactButton.setVisible(hasNDA);
			for (JTextField organizationTextField : ndaContactOrganizationTextFields)
				organizationTextField.setVisible(hasNDA);
			for (JTextField contactPersonTextField : ndaContactPersonTextFields)
				contactPersonTextField.setVisible(hasNDA);
		} else if (ev.getSource() == ndaHasEndTimeCheckBox) {
			ndaEndTimeDateChooser.setVisible(ndaHasEndTimeCheckBox.isSelected());
		} else if (ev.getSource() == addNdaContactButton)
			addNdaContactLine();
	}

	private class LocaleItem {
		private Locale locale;

		public LocaleItem(Locale locale) {
			if (locale == null)
				this.locale = Locale.ROOT;
			else
				this.locale = locale;
		}

		public String getISO639Code() {
			return locale.getISO3Language();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;

			if (obj.getClass() != LocaleItem.class)
				return false;

			return getISO639Code().equals(((LocaleItem) obj).getISO639Code());
		}

		public String toString() {
			if ((locale == null) || (locale == Locale.ROOT))
				return "Unknown";
			return locale.getDisplayLanguage() + " (" + getISO639Code() + ")";
		}
	}

	private class LocaleItemComparator implements Comparator<LocaleItem> {

		/**
		 * Compare on display name
		 */
		public int compare(LocaleItem item1, LocaleItem item2) {
			return item1.toString().compareTo(item2.toString());
		}
	}
}
