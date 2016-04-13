package org.processmining.framework.plugin.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageDescriptor;

public class PluginCacheEntry {

	private static final String CURRENT_VERSION = "currentVersion";

	private static final String FILE_PROTOCOL = "file";

	private static final Set<String> STANDARD_JRE_DIRS = new HashSet<String>(
			Arrays.asList(new String[] { "jdk", "jre", }));
	private static final String STANDARD_JRE_LIB_DIR = "lib";
	private static final String STANDARD_JRE_EXT_DIR = "ext";
	private static final Set<String> STANDARD_JAR_FILES = new HashSet<String>(Arrays.asList(new String[] {
			"resources.jar", "rt.jar", "jsse.jar", "jce.jar", "charsets.jar", "dnsns.jar", "localedata.jar",
			"qtjava.jar", "sunjce_provider.jar", "sunmscapi.jar", "sunpkcs11.jar" }));

	private final URL url;
	private boolean inCache;
	private Set<String> classNames;
	private String key;
	private Boot.Level verbose;

	private final PackageDescriptor packageDescriptor;

	/**
	 * Deprecated. Use the version with the package descriptor for a
	 * significantly faster cache lookup
	 * 
	 * @param url
	 * @param verbose
	 */
	@Deprecated
	public PluginCacheEntry(URL url, Boot.Level verbose) {
		this(url, verbose, null);
	}

	public PluginCacheEntry(URL url, Boot.Level verbose, PackageDescriptor packageDescriptor) {
		this.url = url;
		this.verbose = verbose;
		this.packageDescriptor = packageDescriptor;
		reset();

		try {
			init();
		} catch (Throwable e) {
			System.err.println("Error caching JAR file: " + e.getMessage());
			reset();
		}
	}

	public String getKey() {
		return key;
	}

	private void reset() {
		inCache = false;
		classNames = new HashSet<String>();
		key = null;
	}

	public boolean isInCache() {
		return inCache;
	}

	public Set<String> getCachedClassNames() {
		return classNames;
	}

	public void removeFromCache() {
		if (key != null) {
			getSettings().remove(key);
		}
	}

	private void init() throws URISyntaxException {
		if (!url.getProtocol().equals(FILE_PROTOCOL)) {
			return;
		}

		if (isStandardJar()) {
			inCache = true;
			return;
		}

		if (packageDescriptor == null) {
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// no MD5 available, so we cannot reliably detect whether the JAR is
				// cached or not
				return;
			}

			InputStream is = null;
			try {
				int numRead = 0;
				byte[] buffer = new byte[4096];

				is = url.openStream();
				while ((numRead = is.read(buffer)) > 0) {
					digest.update(buffer, 0, numRead);
				}
			} catch (IOException e) {
				return;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						return;
					}
				}
			}

			key = "";
			for (byte b : digest.digest()) {
				// append the signed byte as an unsigned hex number
				key += Integer.toString(0xFF & b, 16);
			}
			key += " " + new File(new URI(url.toString())).getName();
			if (key.length() > 80) {
				// make sure they is not too long for the preferences API
				key = key.substring(0, 80);
			}
			//System.out.println("URL: " + url);
			//System.out.println("  -> " + key);
			//System.out.println("  -> len=" + key.length());
		} else {
			key = createPackageBasedKey();
		}

		String names = getSettings().get(key, null);

		if (names == null) {
			return;
		}

		if (verbose == Level.ALL) {
			System.out.println("Plugins found in cache. ");
		}

		try {
			int subkeys = Integer.parseInt(names);
			for (int i = 0; i < subkeys; i++) {
				parseKey(key + "-" + i);
			}

		} catch (NumberFormatException e) {
			parseKey(key);
		}
		inCache = true;
	}

	private String createPackageBasedKey() {
		assert packageDescriptor != null;
		String key = packageDescriptor.getName();
		key += " ";
		key += packageDescriptor.getVersion();
		return key;
	}

	private void parseKey(String key) {
		String names = getSettings().get(key, null);

		//System.out.println("  -> FOUND IN CACHE");
		for (String className : names.split("/")) {
			if (className.length() > 0) {
				//System.out.println("     - " + className);
				classNames.add(className);
			}
		}
	}

	private boolean isStandardJar() throws URISyntaxException {
		try {
			File file = new File(new URI(url.toString()));
			String filename = file.getName().toLowerCase();

			if (STANDARD_JAR_FILES.contains(filename)) {
				String libDir = file.getParentFile().getName().toLowerCase();
				String jreDir = removeNonAlphaChars(file.getParentFile().getParentFile().getName().toLowerCase());

				if (libDir.equals(STANDARD_JRE_EXT_DIR)) {
					libDir = file.getParentFile().getParentFile().getName().toLowerCase();
					jreDir = removeNonAlphaChars(file.getParentFile().getParentFile().getParentFile().getName()
							.toLowerCase());
				}
				if (libDir.equals(STANDARD_JRE_LIB_DIR)) {
					return STANDARD_JRE_DIRS.contains(jreDir);
				}
			}
		} catch (NullPointerException e) {
			// probably the file doesn't have enough parent paths
		}
		return false;
	}

	private String removeNonAlphaChars(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			if (('a' <= s.charAt(i)) && (s.charAt(i) <= 'z')) {
				result += s.substring(i, i + 1);
			}
		}
		return result;
	}

	public void update(List<String> classes) {
		if (key != null) {
			if (verbose == Level.ALL) {
				System.out.println("UPDATING CACHE: " + key);
			}

			// updating. Remove the previpous version if present and add the new classes
			if (packageDescriptor != null) {
				String previous = getSettings().get(CURRENT_VERSION, null);
				if (previous != null) {
					TreeSet<String> installed = new TreeSet<>(Arrays.asList(previous.split("/")));
					Iterator<String> it = installed.iterator();
					if (installed.size() >= 5) {
						// already keeping 5 versions alive. Remove one if
						// current not already present.
						if (!installed.contains(createPackageBasedKey())) {
							String toRemove = it.next();
							getSettings().remove(toRemove);

						}
					}
					previous = createPackageBasedKey();
					while (it.hasNext()) {
						previous += '/';
						previous += it.next();
					}
					getSettings().put(CURRENT_VERSION, previous);
				} else {
					getSettings().put(CURRENT_VERSION, createPackageBasedKey());
				}
			}

			classNames.clear();
			for (String name : classes) {
				if ((name != null) && (name.length() > 0)) {
					classNames.add(name);
				}
			}

			StringBuffer value = new StringBuffer("");
			for (String name : classNames) {
				if (verbose == Level.ALL) {
					System.out.println("               : " + name);
				}
				value.append(name);
				value.append("/");
			}

			if (value.length() > Preferences.MAX_VALUE_LENGTH) {
				int subkeys = (value.length() / Preferences.MAX_VALUE_LENGTH) + 1;
				getSettings().put(key, "" + subkeys);
				for (int i = 0; i < subkeys; i++) {
					getSettings().put(
							key + "-" + i,
							value.substring(i * Preferences.MAX_VALUE_LENGTH,
									Math.min((i + 1) * Preferences.MAX_VALUE_LENGTH, value.length())));
				}
			} else {

				getSettings().put(key, value.toString());
			}
		}
	}

	/**
	 * If a package descriptor is given, we use that to build the cache. The
	 * version number is increased automatically now with every build/release,
	 * hence we can use that to determine the cache.
	 * 
	 * @return
	 */
	private Preferences getSettings() {
		String className = getClass().getName();
		int pkgEndIndex = className.lastIndexOf('.');
		if (pkgEndIndex < 0) {
			className = "/<unnamed>";
		} else {
			String packageName = className.substring(0, pkgEndIndex);
			className = "/" + packageName.replace('.', '/');
		}
		if (packageDescriptor == null) {
			return Preferences.userRoot().node(className + "_old");
		} else {
			return Preferences.userRoot().node(className + '/' + packageDescriptor.getName());
		}
	}
}
