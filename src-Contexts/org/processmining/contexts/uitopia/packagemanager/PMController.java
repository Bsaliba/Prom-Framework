package org.processmining.contexts.uitopia.packagemanager;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.UnknownPackageTypeException;
import org.processmining.framework.packages.impl.CancelledException;

public class PMController {

	private final PMMainView mainView;
	private final PackageManager manager;
	
	/*
	 * Maps every package descriptor to whether it is still available.
	 * This map acts as a cache to prevent us from have to access the URL over and over again.
	 * 
	 * This map is also used by PackageConfigPersiter when writing the packages to the local repo again.
	 * As a result, packages that are known to be unavailable will not be written back to the local repo.
	 */
	static public Map<PackageDescriptor, Boolean> availability;

	public PMController( Boot.Level verbose) {
		availability = new HashMap<PackageDescriptor, Boolean>();
		manager = PackageManager.getInstance();
		manager.initialize(verbose);
		try {
			manager.update(false, verbose);
		} catch (CancelledException e) {
			e.printStackTrace();
		} catch (UnknownPackageTypeException e) {
			e.printStackTrace();
		}

		mainView = new PMMainView(this);		
	}

	/**
	 * Select a package, select the right tab and return the selected PMPackage
	 * 
	 * @param packageName
	 * @return null if no package with such name exists.
	 */
	public PMPackage selectPackage(String packageName) {
		Set<PackageDescriptor> available = manager.getAvailablePackages();
		for (PackageDescriptor d : available) {
			if (d.getName().equals(packageName)) {
				// package descriptor d is available
				PMPackage pack = new PMPackage(d);
				setStatus(pack, d);
				mainView.showWorkspaceView(pack);
				return pack;
			}
		}
		return null;
	}

	public PMMainView getMainView() {
		return mainView;
	}

	public java.util.List<PMPackage> getToUninstallPackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			PMPackage pack = new PMPackage(descriptor);
			PackageDescriptor installed = manager.findInstalledVersion(descriptor);
			if ((installed != null) && installed.equals(descriptor)) {
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}
	
	/**
	 * Checks whether a package is still available.
	 * This prevents the user from installing or updating a package that cannot be installed anymore.
	 * 
	 * @param descriptor The descriptor of the package.
	 * @return Whether the URL of the package descriptor can be opened successfully.
	 */
	public boolean isAvailable(PackageDescriptor descriptor) {
		/*
		 * First check the cache.
		 */
		if (availability.containsKey(descriptor)) {
			/*
			 * In cache, return cached result.
			 */
			return availability.get(descriptor);
		}
		/*
		 * Not in cache, check whether URL still exists.
		 */
		InputStream is = null;
		try {
			URL url = new URL(descriptor.getURL());
			is = url.openStream();
		} catch (Exception e) {
			/*
			 * Something's wrong with this URL. Mark it as unavailable.
			 */
			System.err.println("Package found in local repository, but not in global repository: "+ descriptor);
			availability.put(descriptor,  false);
			return false;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
//		System.out.println("Package available: "+ descriptor);
		/*
		 * All fine, still available. Mark it as such.
		 */
		availability.put(descriptor,  true);
		return true;
	}

	public java.util.List<? extends PMPackage> getToUpdatePackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor available : descriptors) {
			PMPackage pack = new PMPackage(available);
			PackageDescriptor installed = manager.findInstalledVersion(available);
			if ((installed != null && isAvailable(available)) && //
					installed.getVersion().lessThan(available.getVersion())) {
				list.add(pack);
				setStatus(pack, available);
			}
		}
		return list;
	}

	public java.util.List<? extends PMPackage> getToInstallPackages() {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			PMPackage pack = new PMPackage(descriptor);
			PackageDescriptor installed = manager.findInstalledVersion(descriptor);
			if (installed == null && isAvailable(descriptor)) {
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public java.util.List<PMPackage> getParentPackages(PMPackage reference) {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			if (reference.getDependencies().contains(descriptor.getName())) {
				PMPackage pack = new PMPackage(descriptor);
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public java.util.List<PMPackage> getChildPackages(PMPackage reference) {
		Set<PackageDescriptor> descriptors = manager.getAvailablePackages();
		java.util.List<PMPackage> list = new ArrayList<PMPackage>();
		for (PackageDescriptor descriptor : descriptors) {
			if (descriptor.getDependencies().contains(reference.getPackageName())) {
				PMPackage pack = new PMPackage(descriptor);
				list.add(pack);
				setStatus(pack, descriptor);
			}
		}
		return list;
	}

	public void setStatus(PMPackage pack, PackageDescriptor descriptor) {
		PackageDescriptor installed = manager.findInstalledVersion(descriptor);
		if (installed != null) {
			if (isAvailable(descriptor) && !installed.equals(descriptor)) {
				pack.setStatus(PMPackage.PMStatus.TOUPDATE);
			} else {
				pack.setStatus(PMPackage.PMStatus.TOUNINSTALL);
			}
		} else {
			if (isAvailable(descriptor)) {
				pack.setStatus(PMPackage.PMStatus.TOINSTALL);
			} else {
				pack.setStatus(PMPackage.PMStatus.DEAD);
			}
		}
	}

	public void update(final PMPackage pack, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					manager.install(Arrays.asList(new PackageDescriptor[] { pack.getDescriptor() }));
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				} catch (UnknownPackageTypeException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void update(final Collection<PMPackage> packs, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					List<PackageDescriptor> pds = new ArrayList<PackageDescriptor>();
					for (PMPackage p : packs) {
						pds.add(p.getDescriptor());
					}
					manager.install(pds);
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				} catch (UnknownPackageTypeException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void remove(final PMPackage pack, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					manager.uninstall(Arrays.asList(new PackageDescriptor[] { pack.getDescriptor() }));
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}

	public void remove(final Collection<PMPackage> packs, final PMWorkspaceView view) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					List<PackageDescriptor> pds = new ArrayList<PackageDescriptor>();
					for (PMPackage p : packs) {
						pds.add(p.getDescriptor());
					}
					manager.uninstall(pds);
					view.updatePackages();
				} catch (CancelledException e) {
					e.printStackTrace();
				}
			}

		});
		t.start();
	}
}
