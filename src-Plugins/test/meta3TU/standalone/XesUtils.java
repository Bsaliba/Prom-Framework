package test.meta3TU.standalone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

public class XesUtils {
	public static XAttribute assignAttribute(XAttributable element, String key, String value) {
		XAttributeLiteral attr = new XAttributeLiteralImpl(key, value);
		element.getAttributes().put(key, attr);
		return element.getAttributes().get(key);
	}

	public static XAttribute assignAttribute(XAttributable element, String key, double value) {
		XAttributeContinuous attr = new XAttributeContinuousImpl(key, value);
		element.getAttributes().put(key, attr);
		return element.getAttributes().get(key);
	}

	public static XAttribute assignAttribute(XAttributable element, String key, long value) {
		XAttributeDiscrete attr = new XAttributeDiscreteImpl(key, value);
		element.getAttributes().put(key, attr);
		return element.getAttributes().get(key);
	}

	public static XAttribute assignAttribute(XAttributable element, String key, Date value) {
		XAttributeTimestamp attr = new XAttributeTimestampImpl(key, value);
		element.getAttributes().put(key, attr);
		return element.getAttributes().get(key);
	}

	public static XAttribute assignAttribute(XAttributable element, String key, boolean value) {
		XAttributeBoolean attr = new XAttributeBooleanImpl(key, value);
		element.getAttributes().put(key, attr);
		return element.getAttributes().get(key);
	}
	
	public static List<XLog> ParseXesFile(String filePath) throws Exception {
		return ParseXesFile(new File(filePath));
	}

	static List<XLog> ParseXesFile(File file) throws Exception {
		return ParseXesFile(new FileInputStream(file));	
	}

	static List<XLog> ParseXesFile(ZipFile zipFile, ZipEntry entry) throws Exception {
		return ParseXesFile(zipFile.getInputStream(entry));		
	}

	static List<XLog> ParseXesFile(InputStream iStream) throws Exception {
		return ParseXesFile(iStream, true);
	}
	
	static List<XLog> ParseXesFile(InputStream iStream, boolean closeStream) throws Exception {
		BufferedInputStream xesFile = null;
		try {
			// Use a 50MB input buffer since we assume large files
			xesFile = new BufferedInputStream(iStream,
					50 * 1024 * 1024);

			XesXmlParser parser = new XesXmlParser();
			return parser.parse(xesFile);
		} catch (FileNotFoundException e) {
			throw e;
		} finally {
			if ((xesFile != null) && closeStream)
				xesFile.close();
		}
	}

	static List<XLog> ParseGZippedXesFile(String filePath) throws Exception {
		return ParseGZippedXesFile(new File(filePath));
	}

	static List<XLog> ParseGZippedXesFile(File file) throws Exception {
		return ParseGZippedXesFile(new FileInputStream(file));	
	}

	static List<XLog> ParseGZippedXesFile(ZipFile zipFile, ZipEntry entry) throws Exception {
		return ParseGZippedXesFile(zipFile.getInputStream(entry));	
	}

	static List<XLog> ParseGZippedXesFile(InputStream iStream) throws Exception {
		return ParseGZippedXesFile(iStream, true);
	}
	
	static List<XLog> ParseGZippedXesFile(InputStream iStream, boolean closeStream) throws Exception {
		BufferedInputStream xesFile = null;
		try {
			// Use a 50MB input buffer since we assume large files
			xesFile = new BufferedInputStream(iStream,
					50 * 1024 * 1024);

			XesXmlGZIPParser parser = new XesXmlGZIPParser();
			return parser.parse(xesFile);
		} catch (FileNotFoundException e) {
			throw e;
		} finally {
			if ((xesFile != null) && closeStream)
				xesFile.close();
		}
	}

	static List<XLog> ParseMxmlFile(String filePath) throws Exception {
		return ParseMxmlFile(new File(filePath));
	}

	static List<XLog> ParseMxmlFile(File file) throws Exception {
		return ParseMxmlFile(new FileInputStream(file));
	}

	static List<XLog> ParseMxmlFile(ZipFile zipFile, ZipEntry entry) throws Exception {
		return ParseMxmlFile(zipFile.getInputStream(entry));		
	}

	static List<XLog> ParseMxmlFile(InputStream iStream) throws Exception {
		return ParseMxmlFile(iStream, true);
	}
	
	static List<XLog> ParseMxmlFile(InputStream iStream, boolean closeStream) throws Exception {
		BufferedInputStream mxmlFile = null;
		try {
			// Use a 50MB input buffer since we assume large files
			mxmlFile = new BufferedInputStream(iStream,
					50 * 1024 * 1024);

			XMxmlParser parser = new XMxmlParser();
			return parser.parse(mxmlFile);
		} catch (FileNotFoundException e) {
			throw e;
		} finally {
			if ((mxmlFile != null) && closeStream)
				mxmlFile.close();
		}
	}

	static List<XLog> ParseGZippedMxmlFile(String filePath) throws Exception {
		return ParseMxmlFile(new File(filePath));
	}

	static List<XLog> ParseGZippedMxmlFile(File file) throws Exception {
		return ParseGZippedMxmlFile(new FileInputStream(file));	
	}

	static List<XLog> ParseGZippedMxmlFile(ZipFile zipFile, ZipEntry entry) throws Exception {
		return ParseGZippedMxmlFile(zipFile.getInputStream(entry));	
	}

	static List<XLog> ParseGZippedMxmlFile(InputStream iStream) throws Exception {
		return ParseGZippedMxmlFile(iStream, true);
	}
	static List<XLog> ParseGZippedMxmlFile(InputStream iStream, boolean closeStream) throws Exception {
		BufferedInputStream mxmlFile = null;
		try {
			// Use a 50MB input buffer since we assume large files
			mxmlFile = new BufferedInputStream(iStream,
					50 * 1024 * 1024);

			XMxmlGZIPParser parser = new XMxmlGZIPParser();
			return parser.parse(mxmlFile);
		} catch (FileNotFoundException e) {
			throw e;
		} finally {
			if ((mxmlFile != null) && closeStream)
				mxmlFile.close();
		}
	}

	static void SerializeXesFile(String filePath, XLog log, boolean zipSerializedFile) throws IOException {
		PrintStream exportFile = null;
		try {
			//Create target directory if it does not exist yet
			File targetDirectory = new File(filePath).getParentFile();
			targetDirectory.mkdirs();
			
			// Use a 20MB output buffer to speed up writing process
			exportFile = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(filePath), 20 * 1024 * 1024));

			if (zipSerializedFile){
				XesXmlGZIPSerializer serializer = new XesXmlGZIPSerializer();
				serializer.serialize(log, exportFile);
			}
			else{
				XesXmlSerializer serializer = new XesXmlSerializer();
				serializer.serialize(log, exportFile);				
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (exportFile != null)
				exportFile.flush();
			exportFile.close();
		}
	}
}
