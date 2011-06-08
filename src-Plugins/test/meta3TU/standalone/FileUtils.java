package test.meta3TU.standalone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	public final static char DEFAULT_PATH_SEPARATOR = File.separatorChar;
	public final static char DEFAULT_EXTENSION_SEPARATOR = '.';

	public static String combinePath(String... partialPaths) {
		StringBuilder pathBuilder = new StringBuilder(partialPaths.length*5);		
		for (String path : partialPaths) {
			if (path != null && path.length() > 0) {
				if ((pathBuilder.length() == 0)
						|| (pathBuilder.charAt(pathBuilder.length() - 1) == DEFAULT_PATH_SEPARATOR))
					pathBuilder.append(path);
				else
				{
					pathBuilder.append(DEFAULT_PATH_SEPARATOR);
					pathBuilder.append(path);
				}
			}
		}
		return pathBuilder.toString();
	}

	public static boolean checkFileExists(String path) {
		File f = new File(path);
		return f.exists();
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
	
	public static void extractFile(ZipFile zipFile, ZipEntry entryToExtract, File outZipFile, String outFileName) throws IOException {
		extractFile(zipFile.getInputStream(entryToExtract), outZipFile, outFileName, true);		}

	/**
	 * Extracts the current file from the zip input stream into the destination zip file
	 * */
	public static void extractFile(InputStream iStream, File outZipFile, String outFileName, boolean closeInput) throws IOException {
		// Create target directory if it does not exist yet
		File targetDirectory = outZipFile.getParentFile();
		targetDirectory.mkdirs();

		int buffer = 2048;
		BufferedInputStream input = null;
		ZipOutputStream destination = null;
		try {
			input = new BufferedInputStream(iStream);
			destination = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outZipFile), buffer));
			
			//Add entry information
			ZipEntry entry = new ZipEntry(outFileName);
			destination.putNextEntry(entry);
			
			//Add actual data
			int count;
			byte data[] = new byte[buffer];
			count = input.read(data, 0, buffer);
			while (count != -1) {
				destination.write(data, 0, count);
				count = input.read(data, 0, buffer);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if ((input != null) && closeInput)
				input.close();
			if (destination != null) {
				destination.flush();
				destination.close();
			}
		}
	}
	
	public static void extractFile(ZipFile zipFile, ZipEntry entryToExtract, File out) throws IOException {
		extractFile(zipFile.getInputStream(entryToExtract), out, true);	
	}

	/**Extracts the current file from the zip input stream
	 * */
	public static void extractFile(InputStream iStream, File out, boolean closeInput) throws IOException {
		//Create target directory if it does not exist yet
		File targetDirectory = out.getParentFile();
		targetDirectory.mkdirs();

		int buffer = 2048;
		BufferedInputStream input = null;
		BufferedOutputStream destination = null;
		try {
			input = new BufferedInputStream(iStream);
			destination = new BufferedOutputStream(new FileOutputStream(out), buffer);

			int count;
			byte data[] = new byte[buffer];
			count = input.read(data, 0, buffer);
			while (count != -1) {
				destination.write(data, 0, count);
				count = input.read(data, 0, buffer);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if ((input != null) && closeInput)
				input.close();
			if (destination != null) {
				destination.flush();
				destination.close();
			}
		}
	}

	public static void copyFile(File in, File out) throws IOException {
		//Create target directory if it does not exist yet
		File targetDirectory = out.getParentFile();
		targetDirectory.mkdirs();
		
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel
						.transferTo(position, maxCount, outChannel);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
}
