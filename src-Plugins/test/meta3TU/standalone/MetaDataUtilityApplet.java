package test.meta3TU.standalone;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


//VS4E -- DO NOT REMOVE THIS LINE!
public class MetaDataUtilityApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	private JTextField metaDataFileNameTextField;
	private JTextField exportFileNameTextField;
	private JTextField importFileNameTextField;
	private JLabel importFileLabel;
	private JLabel exportFileLabel;
	private JLabel metaDataFileLabel;
	private JLabel zipResultsLabel;
	private JLabel exportToRootLabel;
	private JCheckBox zipResultsCheckbox;
	private JCheckBox exportToRootCheckbox;
	private JButton startExportButton;
	private JButton selectImportDirButton;
	private JButton selectExportDirButton;
    private JFileChooser fc;
	private static final String PREFERRED_LOOK_AND_FEEL = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	public void init() {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					initComponents();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void initComponents() {				
		setPreferredSize(new Dimension(800, 600));
		setLayout(null);
		add(getExportFileNameTextField());
		add(getImportFileNameTextField());
		add(getMetaDataFileNameTextField());
		add(getZipResultsCheckbox());
		add(getExportToRootCheckbox());
		add(getImportFileLabel());
		add(getExportFileLabel());
		add(getMetaDataFileLabel());
		add(getZipResultsLabel());
		add(getExportToRootLabel());
		add(getStartExportButton());
		add(getSelectImportDirButton());
		add(getSelectExportDirButton());
		setSize(474, 419);
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	private JButton getSelectImportDirButton() {
		if (selectImportDirButton == null) {
			selectImportDirButton = new JButton();
			selectImportDirButton.setText("...");
			selectImportDirButton.setToolTipText("Select import directory");
			selectImportDirButton.setBounds(320, 33, 40, 28);
			selectImportDirButton.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					selectImportDirButtonMouseClicked(event);
				}
			});
		}
		return selectImportDirButton;
	}

	private JButton getSelectExportDirButton() {
		if (selectExportDirButton == null) {
			selectExportDirButton = new JButton();
			selectExportDirButton.setText("...");
			selectExportDirButton.setToolTipText("Select export directory");
			selectExportDirButton.setBounds(320, 68, 40, 28);
			selectExportDirButton.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					selectExportDirButtonMouseClicked(event);
				}
			});
		}
		return selectExportDirButton;
	}

	private JButton getStartExportButton() {
		if (startExportButton == null) {
			startExportButton = new JButton();
			startExportButton.setText("Start export");
			startExportButton.setBounds(85, 192, 221, 28);
			startExportButton.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					startExportButtonMouseMouseClicked(event);
				}
			});
		}
		return startExportButton;
	}

	private JLabel getImportFileLabel() {
		if (importFileLabel == null) {
			importFileLabel = new JLabel();
			importFileLabel.setText("Import directory:");
			importFileLabel.setBounds(20, 38, 90, 16);
		}
		return importFileLabel;
	}

	private JLabel getExportFileLabel() {
		if (exportFileLabel == null) {
			exportFileLabel = new JLabel();
			exportFileLabel.setText("Export directory:");
			exportFileLabel.setBounds(20, 73, 90, 16);
		}
		return exportFileLabel;
	}

	private JLabel getMetaDataFileLabel() {
		if (metaDataFileLabel == null) {
			metaDataFileLabel = new JLabel();
			metaDataFileLabel.setText("Meta data file:");
			metaDataFileLabel.setBounds(20, 106, 90, 16);
		}
		return metaDataFileLabel;
	}

	private JLabel getZipResultsLabel() {
		if (zipResultsLabel == null) {
			zipResultsLabel = new JLabel();
			zipResultsLabel.setText("Zip results:");
			zipResultsLabel.setBounds(20, 136, 90, 16);
		}
		return zipResultsLabel;
	}

	private JLabel getExportToRootLabel() {
		if (exportToRootLabel == null) {
			exportToRootLabel = new JLabel();
			exportToRootLabel.setText("Export to root of export directory:");
			exportToRootLabel.setBounds(20, 166, 90, 16);
		}
		return exportToRootLabel;
	}

	private JTextField getImportFileNameTextField() {
		if (importFileNameTextField == null) {
			importFileNameTextField = new JTextField();
			importFileNameTextField.setText(new File("").getAbsolutePath());
			importFileNameTextField.setBounds(116, 32, 199, 28);
		}
		return importFileNameTextField;
	}

	private JTextField getExportFileNameTextField() {
		if (exportFileNameTextField == null) {
			exportFileNameTextField = new JTextField();
			exportFileNameTextField.setText(new File("").getAbsolutePath());
			exportFileNameTextField.setBounds(116, 67, 202, 28);
		}
		return exportFileNameTextField;
	}

	private JTextField getMetaDataFileNameTextField() {
		if (metaDataFileNameTextField == null) {
			metaDataFileNameTextField = new JTextField();
			metaDataFileNameTextField.setText("3TU metadata.xes");
			metaDataFileNameTextField.setBounds(116, 100, 202, 28);
		}
		return metaDataFileNameTextField;
	}

	private JCheckBox getZipResultsCheckbox() {
		if (zipResultsCheckbox == null) {
			zipResultsCheckbox = new JCheckBox();
			zipResultsCheckbox.setBounds(116, 130, 202, 28);
		}
		return zipResultsCheckbox;
	}

	private JCheckBox getExportToRootCheckbox() {
		if (exportToRootCheckbox == null) {
			exportToRootCheckbox = new JCheckBox();
			exportToRootCheckbox.setBounds(116, 160, 202, 28);
		}
		return exportToRootCheckbox;
	}

	private static void installLnF() {
		try {
			String lnfClassname = PREFERRED_LOOK_AND_FEEL;
			if (lnfClassname == null)
				lnfClassname = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lnfClassname);
		} catch (Exception e) {
			System.err.println("Cannot install " + PREFERRED_LOOK_AND_FEEL
					+ " on this platform:" + e.getMessage());
		}
	}

	/**
	 * Main entry of the class. Note: This class is only created so that you can
	 * easily preview the result at runtime. It is not expected to be managed by
	 * the designer. You can modify it as you like.
	 */
	public static void main(String[] args) {
		installLnF();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MetaDataUtilityApplet applet = new MetaDataUtilityApplet();
				JFrame frame = new JFrame();
				frame.add(applet, BorderLayout.CENTER);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setTitle("XES MetaData Utility");
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	public MetaDataUtilityApplet() {
		initComponents();
	}

	private void startExportButtonMouseMouseClicked(MouseEvent event) {
		try {
			File exportDir = new File(exportFileNameTextField.getText());
			if (!exportDir.exists())
			{
				int dialogResult = JOptionPane.showConfirmDialog(null, "Export folder does not exist. Create?", "Create export folder dialog", JOptionPane.OK_CANCEL_OPTION);
				if (dialogResult == JOptionPane.CANCEL_OPTION
						|| dialogResult == JOptionPane.CLOSED_OPTION)
					return;
				
				exportDir.mkdir();
			}
			else if (exportDir.listFiles().length > 0)
			{				
				int dialogResult = JOptionPane.showConfirmDialog(null, "Export folder not empty. Remove existing files?", "Remove existing files dialog", JOptionPane.YES_NO_CANCEL_OPTION);
				if (dialogResult == JOptionPane.CANCEL_OPTION
						|| dialogResult == JOptionPane.CLOSED_OPTION)
					return;
				else if (dialogResult == JOptionPane.YES_OPTION)
				{
					FileUtils.deleteDirectory(exportDir);
					exportDir.mkdir();
				}				
			}

			Tracer.Log(Level.INFO, "Export started");
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			List<String> messages = MetaDataCalculator.ExportMetaEnhancedLogs(importFileNameTextField.getText(),
					exportFileNameTextField.getText(), metaDataFileNameTextField.getText(), zipResultsCheckbox.isSelected(), exportToRootCheckbox.isSelected());
			this.setCursor(Cursor.getDefaultCursor());
			
			if (messages.size() == 0){
				Tracer.Log(Level.INFO, "The meta-enhanced export of event log files completed successfully.");
				JOptionPane.showMessageDialog(null, "The export of event log files completed successfully!", "Export completed", JOptionPane.INFORMATION_MESSAGE);
			}
			else{
				Tracer.Log(Level.INFO, "The meta-enhanced export of event log files completed with the following messages:");
				for(String message: messages)
					Tracer.Log(Level.INFO, message);
				JOptionPane.showMessageDialog(null, "The export of event log files completed with messages. See the log file for details.", "Export completed", JOptionPane.INFORMATION_MESSAGE);
			}			
		} catch (Throwable e) {
			this.setCursor(Cursor.getDefaultCursor());
			Tracer.Log(Level.SEVERE, "MetaDataUtilityApplet", "startExport", e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void selectImportDirButtonMouseClicked(MouseEvent event)
	{
		try {
			fc.setDialogTitle("Select import directory");
			if (importFileNameTextField.getText().length() > 0)
				fc.setCurrentDirectory(new File(importFileNameTextField
						.getText()));

			int returnVal = fc.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				importFileNameTextField.setText(fc.getSelectedFile()
						.getAbsolutePath());
			}
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"An error occurred", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void selectExportDirButtonMouseClicked(MouseEvent event) {
		try {
			fc.setDialogTitle("Select export directory");
			if (exportFileNameTextField.getText().length() > 0)
				fc.setCurrentDirectory(new File(exportFileNameTextField
						.getText()));

			int returnVal = fc.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION)
				exportFileNameTextField.setText(fc.getSelectedFile()
						.getAbsolutePath());
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"An error occurred", JOptionPane.ERROR_MESSAGE);
		}
	}

}
