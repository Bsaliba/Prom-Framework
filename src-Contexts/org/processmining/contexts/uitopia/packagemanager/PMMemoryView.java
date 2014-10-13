package org.processmining.contexts.uitopia.packagemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.util.OsUtil;

import com.fluxicon.slickerbox.components.RoundedPanel;

public class PMMemoryView extends RoundedPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2483996283422006055L;
	
	private static boolean is64bit = OsUtil.is64Bit();
	private static long mem = OsUtil.getPhysicalMemory() / (1024 * 1024);
	
	private final static String XMX1G = "1G";
	private final static String XMX1300M = "1300M";
	private final static String XMX2G = "2G";
	private final static String XMX4G = "4G";
	private final static String XMX8G = "8G";
	private final static String XMX16G = "16G";

	private static String selectedMem = XMX1G;
	private static String oldSelectedMem = XMX1G;

	private ImageLozengeButton button1gbSelected;
	private ImageLozengeButton button1300mbSelected;
	private ImageLozengeButton button2gbSelected;
	private ImageLozengeButton button4gbSelected;
	private ImageLozengeButton button8gbSelected;
	private ImageLozengeButton button16gbSelected;
	private ImageLozengeButton button1gbNotSelected;
	private ImageLozengeButton button1300mbNotSelected;
	private ImageLozengeButton button2gbNotSelected;
	private ImageLozengeButton button4gbNotSelected;
	private ImageLozengeButton button8gbNotSelected;
	private ImageLozengeButton button16gbNotSelected;
	
	public PMMemoryView() {
		super(20, 5, 0);
		setBackground(new Color(160, 160, 160));
		setLayout(new BorderLayout());
		try {
			FileReader reader = new FileReader("ProM641.l4j.ini");
			char[] a = new char[10];
			reader.read(a);
			reader.close();
			String b = String.valueOf(a);
			if (b.startsWith("-Xmx1G")) {
				selectedMem = XMX1G;
			} else if (b.startsWith("-Xmx1300M")) {
				selectedMem = XMX1300M; // About as much as a 32-bit VM can handle.
			} else if (b.startsWith("-Xmx2G")) {
				selectedMem = XMX2G;
			} else if (b.startsWith("-Xmx4G")) {
				selectedMem = XMX4G;
			} else if (b.startsWith("-Xmx8G")) {
				selectedMem = XMX8G;
			} else if (b.startsWith("-Xmx16G")) {
				selectedMem = XMX16G;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		setupUI();
		update();
	}
	
	private void setupUI() {
		button1gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "1 GB");
		button1300mbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "1300 MB");
		button2gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "2 GB");
		button4gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "4 GB");
		button8gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "8 GB");
		button16gbSelected = new ImageLozengeButton(ImageLoader.load("remove_30x30_black.png"), "16 GB");
		button1gbSelected.setEnabled(false);
		button1300mbSelected.setEnabled(false);
		button2gbSelected.setEnabled(false);
		button4gbSelected.setEnabled(false);
		button8gbSelected.setEnabled(false);
		button16gbSelected.setEnabled(false);
		button1gbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "1 GB");
		button1300mbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "1300 MB");
		button2gbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "2 GB");
		button4gbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "4 GB");
		button8gbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "8 GB");
		button16gbNotSelected = new ImageLozengeButton(ImageLoader.load("action_30x30_black.png"), "16 GB");
		button1gbNotSelected.setToolTipText("Use 1 GB of memory when running ProM");
		button1gbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX1G;
				update();
			}
		});
		button1300mbNotSelected.setToolTipText("Use 1300 MB of memory (limit on 32-bit VMs) when running ProM");
		button1300mbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX1300M;
				update();
			}
		});
		button2gbNotSelected.setToolTipText("Use 2 GB of memory when running ProM");
		button2gbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX2G;
				update();
			}
		});
		button4gbNotSelected.setToolTipText("Use 4 GB of memory when running ProM");
		button4gbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX4G;
				update();
			}
		});
		button8gbNotSelected.setToolTipText("Use 8 GB of memory when running ProM");
		button8gbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX8G;
				update();
			}
		});
		button16gbNotSelected.setToolTipText("Use 16 GB of memory when running ProM");
		button16gbNotSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedMem = XMX16G;
				update();
			}
		});
		
	}

	private void update() {
		updateFiles();
		JPanel buttonPanel = new RoundedPanel(20, 5, 0);
		buttonPanel.setBackground(new Color(80, 80, 80));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(selectedMem.equals(XMX1G) ? button1gbSelected : button1gbNotSelected);
		buttonPanel.add(selectedMem.equals(XMX1300M) ? button1300mbSelected : button1300mbNotSelected);
		if (is64bit && mem >= 2000) {
			buttonPanel.add(selectedMem.equals(XMX2G) ? button2gbSelected : button2gbNotSelected);
		}
		if (is64bit && mem >= 4000) {
			buttonPanel.add(selectedMem.equals(XMX4G) ? button4gbSelected : button4gbNotSelected);
		}
		if (is64bit && mem >= 8000) {
			buttonPanel.add(selectedMem.equals(XMX8G) ? button8gbSelected : button8gbNotSelected);
		}
		if (is64bit && mem >= 16000) {
			buttonPanel.add(selectedMem.equals(XMX16G) ? button16gbSelected : button16gbNotSelected);
		}
		removeAll();
		add(new JLabel("Select the amount of memory ProM may use:"), BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);
		revalidate();
	}

	private void updateFiles() {
		if (oldSelectedMem == selectedMem) {
			return;
		}
		if (OsUtil.isRunningWindows()) {
			if (!updateIniFile() || !updateBatFile()) {
				JOptionPane.showMessageDialog(null, "Unable to set memory limit (-Xmx"  + selectedMem + ") in ProM.l4j.ini and/or ProM641.bat file.\nPlease run the Package Manager as administrator, or set the memory limit manually.");
				selectedMem = oldSelectedMem;
				return;
			}
		} else if (OsUtil.isRunningLinux() || OsUtil.isRunningUnix()) {
			if (!updateIniFile() || !updateShFile()) {
				JOptionPane.showMessageDialog(null, "Unable to set memory limit (-Xmx"  + selectedMem + ") in ProM.l4j.ini and/or ProM641.sh file.\nPlease set the memory limit manually.");
				selectedMem = oldSelectedMem;
				return;
			}
		}
		oldSelectedMem = selectedMem;
	}
	
	private boolean updateIniFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter("ProM641.l4j.ini", "UTF-8");
			writer.println("-Xmx" + selectedMem + " -XX:MaxPermSize=256m");
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private boolean updateBatFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter("ProM641.bat", "UTF-8");
			writer.println("java -ea -Xmx" + selectedMem + " -XX:MaxPermSize=256m -classpath ProM641.jar -Djava.util.Arrays.useLegacyMergeSort=true org.processmining.contexts.uitopia.UI");
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private boolean updateShFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter("ProM641.sh", "UTF-8");
			writer.println("#!/bin/sh");
			writer.println("CP=./ProM641.jar");
			writer.println("MEM=" + selectedMem);
			writer.println("add() {");
			writer.println("\tCP=$(CP}:$1");
			writer.println("}");
			writer.println("for lib in ./lib/*.jar");
			writer.println("do");
			writer.println("\tadd $lib");
			writer.println("done");
			writer.println("java -classpath ${CP} -Djava.library.path=./lib -ea -Xmx${MEM} -XX:MaxPermSize=256m -XX:+UseCompressedOops -Djava.util.Arrays.useLegacyMergeSort=true org.processmining.context.uitopia.UI");
			writer.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}		
	}
	
	
}
