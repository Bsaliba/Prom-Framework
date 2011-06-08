package test.meta3TU.standalone;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class DataCiteUtility {

	public static final String DEFAULT_DATACITE_XSLT_FILE = "xes2datacite-csv.xsl";
	public static final String DEFAULT_METADATA_XSLT_FILE = "xes2metadata-xml.xsl";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			//Help function
			if ((args.length < 1) || (args.length > 3) || args[0].endsWith("?")){
				System.out.println("Usage: java -jar 'DataCite utility.jar' <source xes file> <DataCite xslt file> <MetaData xslt file>");
				System.out.println();
				System.out.println("Arguments:");
				System.out.println("<source xes file>: The Xes file to transform (required)");
				System.out.println("<DataCite xslt file>: The xslt file to use to create the DataCite csv file (default '" + DEFAULT_DATACITE_XSLT_FILE + "')");
				System.out.println("<MetaData xslt file>: The xslt file to use to create the friendly xml metadata file (default '" + DEFAULT_METADATA_XSLT_FILE + "')");
				System.out.println();
				System.out.println("Output:");
				System.out.println("'DataCite_MetaData.csv': A csv file with DataCite metadata");
				System.out.println("'MetaData.xml': An xml file with Xes metadata in a friendly format");
				return;			
			}
			
			//Fill in parameters
			String sourceXesFile = args[0];
			String dataCiteXsltFile = DEFAULT_DATACITE_XSLT_FILE;
			if (args.length > 1)
				dataCiteXsltFile = args[1];
			String metaDataXsltFile = DEFAULT_METADATA_XSLT_FILE;
			if (args.length > 2)
				metaDataXsltFile = args[2];
			
			//Verify parameters
			if (!FileUtils.checkFileExists(sourceXesFile))
				throw new FileNotFoundException("Could not find source xes file (" + new File(sourceXesFile).getAbsolutePath() + ")");
			if (!FileUtils.checkFileExists(dataCiteXsltFile))
				throw new FileNotFoundException("Could not find DataCite xslt file (" + new File(dataCiteXsltFile).getAbsolutePath() + ")");
			if (!FileUtils.checkFileExists(metaDataXsltFile))
				throw new FileNotFoundException("Could not find MetaData xslt file (" + new File(metaDataXsltFile).getAbsolutePath() + ")");				
			
			//Execute transformation
			transform(sourceXesFile, "DataCite_MetaData.csv", dataCiteXsltFile);
			System.out.println("Csv file 'DataCite_MetaData.csv' with DataCite metadata created succesfully!");
			
			transform(sourceXesFile, "MetaData.xml", metaDataXsltFile);
			System.out.println("Xml file 'MetaData.xml' with friendly metadata representation created succesfully!");
		}
		catch(Throwable thrown){
			System.err.print(thrown);
		}
	}

	public static void transform(String sourceXmlFile, String targetFile, String xsltFile) {
		TransformerFactory tFactory = TransformerFactory.newInstance();

		try {
			Transformer trans = tFactory.newTransformer(new StreamSource(new File(xsltFile)));
			trans.transform(new StreamSource(new File(sourceXmlFile)), new StreamResult(new File(targetFile)));
		} catch (TransformerException te) {
			te.printStackTrace();
		}
	}
}
