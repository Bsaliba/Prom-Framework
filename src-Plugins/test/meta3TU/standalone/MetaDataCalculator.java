package test.meta3TU.standalone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.log.meta.extensions.XMetaDataGeneralExtension;
import org.processmining.plugins.log.meta.extensions.XMetaDataTimeExtension;
import org.processmining.plugins.log.meta.plugins.StandardMetaDataPlugins;

import test.XMetaData3TUExtension;

public class MetaDataCalculator {
	static final String ORIGINAL_XES_EXPORT_DIR = "Original log";
	static final String ORIGINAL_MXML_EXPORT_DIR = "Original log";
	static final String OTHER_EXPORT_DIR = "Other files";
	static final String ERROR_EXPORT_DIR = "Error";

	static final String XES_EXTENSION = ".xes";
	static final String MXML_EXTENSION = ".mxml";
	static final String XML_EXTENSION = ".xml";
	static final String GZIP_EXTENSION = ".gz";
	static final String ZIP_EXTENSION = ".zip";
	static final String GZIPPED_XES_EXTENSION = XES_EXTENSION + GZIP_EXTENSION;
	static final String GZIPPED_MXML_EXTENSION = MXML_EXTENSION + GZIP_EXTENSION;
	
	static final String LOG_INFO_FILE_PATH = "log\\eventlog info.csv";

	static SimpleDateFormat _timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public static List<String> ExportMetaEnhancedLogs(String importDirectory, String exportDirectory, String metaLogFileName, boolean zipResults, boolean exportToRoot)
	    throws Throwable {
		if (!FileUtils.checkFileExists(importDirectory))
			throw new FileNotFoundException("Import directory not found");
		
		initLogInfo();
		return ExportMetaEnhancedLogs(importDirectory, "", exportDirectory, null, metaLogFileName, zipResults, exportToRoot);
	}

	static List<String> ExportMetaEnhancedLogs(String importDirectoryPath, String relativeImportPath, String exportDirectoryPath, XLog metaLog,
	    String metaLogFileName, boolean zipResults, boolean exportToRoot) throws Throwable {
		List<String> messages = new LinkedList<String>();

		// Read more specific meta log (if it exists)
		XLog currentMetaLog = metaLog;
		String metaLogFilePath = FileUtils.combinePath(importDirectoryPath, metaLogFileName);
		if (FileUtils.checkFileExists(metaLogFilePath)) {
			try {
				Tracer.Log(Level.INFO, "Loading 3TU meta file '" + metaLogFilePath + "'");

				List<XLog> metaLogList = XesUtils.ParseXesFile(metaLogFilePath);
				if (metaLogList.size() > 0)
					currentMetaLog = metaLogList.get(0);
			} catch (Throwable ex) {
				String message = "Could not load 3TU meta file '" + metaLogFilePath + "':" + ex.getLocalizedMessage();
				messages.add(message);
				Tracer.Log(Level.SEVERE, "", "", message, ex);
			}
		}

		// Iterate through all files in the directory --> write logs as
		// enhanced xes file and just copy other files
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		};
		File importDirectory = new File(importDirectoryPath);
		for (File file : importDirectory.listFiles(fileFilter)) {
			try {
				Tracer.Log(Level.INFO, "Processing file '" + file.getAbsolutePath() + "'");

				// Different types of files need to be deserialized differently.
				if (file.getName().equalsIgnoreCase(metaLogFileName))
					; // Skip the meta log file
				else if (file.getName().toLowerCase().endsWith(XES_EXTENSION))
					HandleXesFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
				else if (file.getName().toLowerCase().endsWith(GZIPPED_XES_EXTENSION))
					HandleGZippedXesFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
				else if (file.getName().toLowerCase().endsWith(MXML_EXTENSION))
					HandleMxmlFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
				else if (file.getName().toLowerCase().endsWith(GZIPPED_MXML_EXTENSION))
					HandleGzippedMxmlFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
				else if (file.getName().toLowerCase().endsWith(XML_EXTENSION))
					HandleXmlFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
				else if (file.getName().toLowerCase().endsWith(ZIP_EXTENSION)){
					List<String> subMessages = HandleZipFile(file, relativeImportPath, exportDirectoryPath, currentMetaLog, metaLogFileName, zipResults, exportToRoot);
					messages.addAll(subMessages);
				}
				else
					HandleOtherFile(file, relativeImportPath, exportDirectoryPath, exportToRoot);
			} catch (Throwable ex) {
				String message = "Could not process file '" + file.getAbsolutePath() + "':" + ex.getLocalizedMessage();
				messages.add(message);
				Tracer.Log(Level.SEVERE, "", "", message, ex);

				String errorFileCopy;
				if (exportToRoot)
					errorFileCopy = FileUtils.combinePath(exportDirectoryPath, ERROR_EXPORT_DIR, file.getName());
				else
					errorFileCopy = FileUtils.combinePath(exportDirectoryPath, relativeImportPath, ERROR_EXPORT_DIR, file.getName());
				FileUtils.copyFile(file, new File(errorFileCopy));
			}
		}

		// Recursively iterate through subdirectories to process material in
		// these folders
		FileFilter dirFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		for (File dir : importDirectory.listFiles(dirFilter)) {
			String relativeSubPath = FileUtils.combinePath(relativeImportPath, dir.getName());
			List<String> subMessages = ExportMetaEnhancedLogs(dir.getAbsolutePath(), relativeSubPath, exportDirectoryPath, currentMetaLog, metaLogFileName,
			    zipResults, exportToRoot);
			messages.addAll(subMessages);
		}

		return messages;
	}

	// HandleZipFile
	// Enumerate zip entries:
	// if mxml / xes / "correct" xml: parse and enhance logs
	// if at least one other: treat entire zip file as "other" file (i.e. copy
	// to export dir)
	static List<String> HandleZipFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName, boolean zipResult, boolean exportToRoot)
	    throws Exception {
		List<String> messages = new LinkedList<String>();
		String relativeExportPathForContent = FileUtils.combinePath(relativeExportPath, new Filename(file.getAbsolutePath()).filename() + " (zip)");		

		ZipFile zipFile = new ZipFile(file);
		ZipEntry entry;
		Enumeration<? extends ZipEntry> e = zipFile.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();

			// Skip directory entries, (sub-)directories will be created as required by the files
			if (entry.isDirectory())
				continue;

			String entryName = new Filename(entry.getName()).filenameWithExtension();
			String entryExportName = entryName.replace(":\\", "-ROOT\\").replace("\\\\", "\\"); //Correct errors in zip file...
			String entryParentFolder = new Filename(entry.getName()).path();
			String entryRelativeExportPath = FileUtils.combinePath(relativeExportPathForContent, entryParentFolder);
			
			boolean xesLogFound = false;
			boolean mxmlLogFound = false;
			boolean otherFileFound = false;

			try {
				Tracer.Log(Level.INFO, "Processing file '" + file.getAbsolutePath() + " --> " + entry.getName() + "'");
				// Different types of files need to be deserialized differently.
				if (entry.getName().toLowerCase().endsWith(XES_EXTENSION)) {
					xesLogFound = true;
					List<XLog> logList = XesUtils.ParseXesFile(zipFile, entry);
					exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (xes from zipfile)", metaLog, zipResult, exportToRoot, logList);
				} else if (entry.getName().toLowerCase().endsWith(GZIPPED_XES_EXTENSION)) {
					xesLogFound = true;
					List<XLog> logList = XesUtils.ParseGZippedXesFile(zipFile, entry);
					exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (gzipped xes from zipfile)", metaLog, zipResult, exportToRoot, logList);
				} else if (entry.getName().toLowerCase().endsWith(MXML_EXTENSION)) {
					mxmlLogFound = true;
					List<XLog> logList = XesUtils.ParseMxmlFile(zipFile, entry);
					exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (mxml from zipfile)", metaLog, zipResult, exportToRoot, logList);
				} else if (entry.getName().toLowerCase().endsWith(GZIPPED_MXML_EXTENSION)) {
					mxmlLogFound = true;
					List<XLog> logList = XesUtils.ParseGZippedMxmlFile(zipFile, entry);
					exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (gzipped mxml from zipfile)", metaLog, zipResult, exportToRoot, logList);
				} else if (entry.getName().toLowerCase().endsWith(ZIP_EXTENSION))	{
					//Temporarily extract zip file to destination folder to be able to handle it easily
					File tempFile = new File(FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, entryExportName));
					FileUtils.extractFile(zipFile, entry, tempFile);
					HandleZipFile(tempFile, entryRelativeExportPath, exportDirectoryPath, metaLog, metaLogFileName, zipResult, exportToRoot);					
					if (!tempFile.delete())
						tempFile.deleteOnExit();
				} else if (entry.getName().toLowerCase().endsWith(XML_EXTENSION)) {
					// Open document as plain xml file and see what it is
					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					org.w3c.dom.Document document = parser.parse(zipFile.getInputStream(entry));
					try{
						if (document.getElementsByTagName("log").getLength() > 0
						    && document.getElementsByTagName("log").item(0).getAttributes().getNamedItem("xes.version") != null) {
							xesLogFound = true;
							List<XLog> logList = XesUtils.ParseXesFile(zipFile, entry);
							exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (xes from zipfile)", metaLog, zipResult, exportToRoot, logList);
						} else if (document.getElementsByTagName("WorkflowLog").getLength() > 0) {
							mxmlLogFound = true;
							List<XLog> logList = XesUtils.ParseMxmlFile(zipFile, entry);
							exportMetaEnhancedLogs(entryExportName, entryRelativeExportPath, exportDirectoryPath, " (mxml from zipfile)", metaLog, zipResult, exportToRoot, logList);
						} else
							otherFileFound = true;
						}
					catch(Throwable ex){
						Tracer.Log(Level.INFO, null, null, "Error while verifying contents of xml file '" + file.getAbsolutePath() + " --> " + entry.getName() + "'. Assuming file is not any type of workflow log.", ex);
						otherFileFound = true;						
					}
				} else
					otherFileFound = true;
			} catch (Throwable ex) {
				String message = "Could not process file '" + file.getAbsolutePath() + " --> " + entry.getName() + "':" + ex.getLocalizedMessage();
				messages.add(message);
				Tracer.Log(Level.SEVERE, "", "", message, ex);
				
				String errorFileCopy;
				if (exportToRoot)
					errorFileCopy = FileUtils.combinePath(exportDirectoryPath, ERROR_EXPORT_DIR, entryExportName);
				else
					errorFileCopy = FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, ERROR_EXPORT_DIR, entryExportName);
				FileUtils.extractFile(zipFile, entry, new File(errorFileCopy));
			}

			if (xesLogFound) {// Make a copy of the original file to a to the "Original log" directory
				String originalFileCopy;
				if (exportToRoot)
					originalFileCopy = FileUtils.combinePath(exportDirectoryPath, ORIGINAL_XES_EXPORT_DIR, "original logs.zip");
				else
					originalFileCopy = FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, ORIGINAL_XES_EXPORT_DIR, "original logs.zip");
				FileUtils.extractFile(zipFile, entry, new File(originalFileCopy), entryExportName);
			}
			if (mxmlLogFound) {// Make a copy of the original file to a to the "Original log" directory
				String originalFileCopy;
				if (exportToRoot)
					originalFileCopy = FileUtils.combinePath(exportDirectoryPath, ORIGINAL_MXML_EXPORT_DIR, "original logs.zip");
				else
					originalFileCopy = FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, ORIGINAL_MXML_EXPORT_DIR, "original logs.zip");
				FileUtils.extractFile(zipFile, entry, new File(originalFileCopy), entryExportName);
			}			
			if ((xesLogFound || mxmlLogFound) && (metaLog != null)) {// Make a copy of the meta data file to a to the "Original log" directory
				String metaFileCopy;
				if (exportToRoot)
					metaFileCopy = FileUtils.combinePath(exportDirectoryPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
				else
					metaFileCopy = FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
				if (!FileUtils.checkFileExists(metaFileCopy))
					XesUtils.SerializeXesFile(metaFileCopy, metaLog, zipResult);//Only zip result if extension of metaLogFileName is .xes.gz!
			}
			
			if (otherFileFound)
			{
				String relatedFileCopy;
				if (exportToRoot)
					relatedFileCopy = FileUtils.combinePath(exportDirectoryPath, OTHER_EXPORT_DIR, "other files.zip");
				else
					relatedFileCopy = FileUtils.combinePath(exportDirectoryPath, entryRelativeExportPath, "other files.zip");
				
				FileUtils.extractFile(zipFile, entry, new File(relatedFileCopy), entryExportName);
			}
		}
		
		return messages;
	}

	static void HandleXesFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName, boolean zipResult, boolean exportToRoot)
	    throws Exception {
		// Make copies of the original file(s)
		String originalFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, file.getName());
		FileUtils.copyFile(file, new File(originalFileCopy));
		String metaFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
		if ((metaLog != null) && !FileUtils.checkFileExists(metaFileCopy))
			XesUtils.SerializeXesFile(metaFileCopy, metaLog, zipResult);//Only zip result if extension of metaLogFileName is .xes.gz!

		// Do the actual parsing
		List<XLog> logList = XesUtils.ParseXesFile(file);
		exportMetaEnhancedLogs(file.getName(), relativeExportPath, exportDirectoryPath, " (xes)", metaLog, zipResult, exportToRoot, logList);
	}

	static void HandleGZippedXesFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName,
	    boolean zipResult, boolean exportToRoot) throws Exception {
		// Make copies of the original file(s)
		String originalFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, file.getName());
		FileUtils.copyFile(file, new File(originalFileCopy));
		String metaFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
		if ((metaLog != null) && !FileUtils.checkFileExists(metaFileCopy))
			XesUtils.SerializeXesFile(metaFileCopy, metaLog, zipResult);//Only zip result if extension of metaLogFileName is .xes.gz!

		// Do the actual parsing
		List<XLog> logList = XesUtils.ParseGZippedXesFile(file);
		exportMetaEnhancedLogs(file.getName(), relativeExportPath, exportDirectoryPath, " (gzipped xes)", metaLog, zipResult, exportToRoot, logList);
	}

	static void HandleMxmlFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName, boolean zipResult, boolean exportToRoot)
	    throws Exception {
		// Make copies of the original file(s)
		String originalFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_MXML_EXPORT_DIR, file.getName());
		FileUtils.copyFile(file, new File(originalFileCopy));
		String metaFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
		if ((metaLog != null) && !FileUtils.checkFileExists(metaFileCopy))
			XesUtils.SerializeXesFile(metaFileCopy, metaLog, zipResult);//Only zip result if extension of metaLogFileName is .xes.gz!

		// Do the actual parsing
		List<XLog> logList = XesUtils.ParseMxmlFile(file);
		exportMetaEnhancedLogs(file.getName(), relativeExportPath, exportDirectoryPath, " (mxml)", metaLog, zipResult, exportToRoot, logList);
	}

	static void HandleGzippedMxmlFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName,
	    boolean zipResult, boolean exportToRoot) throws Exception {
		// Make copies of the original file(s)
		String originalFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_MXML_EXPORT_DIR, file.getName());
		FileUtils.copyFile(file, new File(originalFileCopy));
		String metaFileCopy = FileUtils.combinePath(exportDirectoryPath, exportToRoot ? "" : relativeExportPath, ORIGINAL_XES_EXPORT_DIR, metaLogFileName);
		if ((metaLog != null) && !FileUtils.checkFileExists(metaFileCopy))
			XesUtils.SerializeXesFile(metaFileCopy, metaLog, zipResult);//Only zip result if extension of metaLogFileName is .xes.gz!

		// Do the actual parsing
		List<XLog> logList = XesUtils.ParseGZippedMxmlFile(file);
		exportMetaEnhancedLogs(file.getName(), relativeExportPath, exportDirectoryPath, " (gzipped mxml)", metaLog, zipResult, exportToRoot, logList);
	}

	static void HandleXmlFile(File file, String relativeExportPath, String exportDirectoryPath, XLog metaLog, String metaLogFileName, boolean zipResult, boolean exportToRoot)
	    throws Exception {
		boolean isXesFile = false;
		boolean isMxmlFile = false;
		
		// Open document as plain xml file and check contents
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document document = parser.parse(file);
		try{
			if (document.getElementsByTagName("log").getLength() > 0
			    && document.getElementsByTagName("log").item(0).getAttributes().getNamedItem("xes.version") != null)
				isXesFile = true;
			else if (document.getElementsByTagName("WorkflowLog").getLength() > 0)
				isMxmlFile = true;
		}
		catch(Throwable ex){
			Tracer.Log(Level.INFO, null, null, "Error while verifying contents of xml file '" + file.getAbsolutePath() + "'. Assuming file is not any type of workflow log.", ex);
		}
		
		if (isXesFile)
			HandleXesFile(file, relativeExportPath, exportDirectoryPath, metaLog, metaLogFileName, zipResult, exportToRoot);
		else if (isMxmlFile)
			HandleMxmlFile(file, relativeExportPath, exportDirectoryPath, metaLog, metaLogFileName, zipResult, exportToRoot);
		else
			HandleOtherFile(file, relativeExportPath, exportDirectoryPath, exportToRoot);
	}

	static void HandleOtherFile(File file, String relativeExportPath, String exportDirectoryPath, boolean exportToRoot) throws IOException {
		String relatedFileCopy; 
		if (exportToRoot)
			relatedFileCopy = FileUtils.combinePath(exportDirectoryPath, OTHER_EXPORT_DIR, file.getName());
		else
			relatedFileCopy = FileUtils.combinePath(exportDirectoryPath, relativeExportPath, file.getName());
		
		FileUtils.copyFile(file, new File(relatedFileCopy));
	}

	private static void exportMetaEnhancedLogs(String fileName, String relativeExportPath, String exportDirectoryPath, String optionalPostfix,
	    XLog metaLog, boolean zipResult, boolean exportToRoot, List<XLog> logList) throws IOException {
		int currentNr = 1;
		for (XLog log : logList) {
			String exportFileName = createExportFileName(fileName, relativeExportPath, exportDirectoryPath, optionalPostfix, zipResult, exportToRoot, currentNr, logList
			    .size());
			ExportMetaEnhancedLog(log, exportFileName, metaLog, zipResult);
			currentNr++;
		}
	}

	static String createExportFileName(String importFileName, String relativeExportPath, String exportDirectoryPath, String optionalPostfix,
	    boolean zipResult, boolean exportToRoot, int sequenceNr, int totalLogCount) {
		Filename importName = new Filename(importFileName);

		StringBuilder exportNameBuilder = new StringBuilder(exportDirectoryPath.length() + 10);
		if (exportToRoot)
			exportNameBuilder.append(FileUtils.combinePath(exportDirectoryPath, importName.filename()));
		else
			exportNameBuilder.append(FileUtils.combinePath(exportDirectoryPath, relativeExportPath, importName.filename()));
		
		if (totalLogCount > 1) {
			exportNameBuilder.append('_');
			exportNameBuilder.append(sequenceNr);
		}

		String extension = XES_EXTENSION;
		if (zipResult)
			extension = GZIPPED_XES_EXTENSION;
		
		String exportFileName = exportNameBuilder.toString() + extension;
		if (FileUtils.checkFileExists(exportFileName)){
			exportNameBuilder.append(optionalPostfix);
			
			exportFileName = exportNameBuilder.toString() + extension;		
			int number = 0;
			while(FileUtils.checkFileExists(exportFileName)){
				number++;
				exportFileName = exportNameBuilder.toString() + "(" + number + ")" + extension;
			}
		}
	
		return exportFileName;
	}

	static void ExportMetaEnhancedLog(XLog log, String exportFileName, XLog metaLog, boolean zipResult) throws IOException {
		XLog metaEnhancedLog = MetaEnhanceLog(log, metaLog);

		// Create export subdir if it does not exist yet
		File exportDirectory = new File(new Filename(exportFileName).path());
		exportDirectory.mkdirs();

		writeLogInfo(metaEnhancedLog, exportFileName);
		XesUtils.SerializeXesFile(exportFileName, metaEnhancedLog, zipResult);
	}

	static XLog MetaEnhanceLog(XLog log, XLog metaLog) {
		// Copy basic meta attributes (e.g. description) from meta log
		if (metaLog != null) {
			log.getAttributes().putAll(metaLog.getAttributes());
			log.getExtensions().addAll(metaLog.getExtensions());
		}

		return StandardMetaDataPlugins.enhanceLog(log);
	}
	
	static void initLogInfo(){
		PrintWriter exportFile = null;
		try {
			exportFile = new PrintWriter(new BufferedWriter(new FileWriter(LOG_INFO_FILE_PATH, false)));
			exportFile.println("DOI;Version;Name;File name;Description;Creation time;Creator (institute);Creator (person);Creation place;Source institute;Source institute type;Source program;Source model;Language;Log type;Process type;Number of traces;Total number of events;Average number of events;Standard deviation of events;Minimum number of events;Maximum number of events;Log start time;Log end time;");
		} catch (IOException e) {
			Tracer.Log(Level.WARNING, "", "", "Error while initializing log info file", e);
		} finally {
			if (exportFile != null)
				exportFile.flush();
			exportFile.close();
		}
	}
	
	static void writeLogInfo(XLog log, String logFileName){
		StringBuilder logInfoBuilder = new StringBuilder(1000);		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractDOI(log));
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractVersion(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XConceptExtension.instance().extractName(log));
		logInfoBuilder.append(';');		
		logInfoBuilder.append(logFileName);
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractDescription(log));		
		logInfoBuilder.append(';');	
		Date creationTime = XMetaData3TUExtension.instance().extractCreationTime(log);
		if (creationTime != null) 
			logInfoBuilder.append(_timeFormatter.format(creationTime));	
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractCreationInstitute(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractCreationPerson(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractCreationPlace(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractSourceInstitute(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractSourceInstituteType(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractSourceProgram(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractSourceModel(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractLanguage(log));			
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractLogType(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaData3TUExtension.instance().extractProcessType(log));		
		logInfoBuilder.append(';');		
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractTracesTotal(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractEventsTotal(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractEventsAverage(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractEventsStandardDeviation(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractEventsMin(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataGeneralExtension.instance().extractEventsMax(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataTimeExtension.instance().extractLogStartTime(log));		
		logInfoBuilder.append(';');	
		logInfoBuilder.append(XMetaDataTimeExtension.instance().extractLogEndTime(log));		
		logInfoBuilder.append(';');	
		String logInfo = logInfoBuilder.toString().replace("null;", ";");		
		
		PrintWriter exportFile = null;
		try {
			exportFile = new PrintWriter(new BufferedWriter(new FileWriter(LOG_INFO_FILE_PATH, true)));			
			exportFile.println(logInfo);		
		} catch (IOException e) {
			Tracer.Log(Level.WARNING, "", "", "Error while writing log info '" + logInfo  + "'", e);
		} finally {
			if (exportFile != null)
				exportFile.flush();
			exportFile.close();
		}
	}
}
