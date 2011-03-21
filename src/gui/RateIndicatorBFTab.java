package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import templates.RateIndicatorBFToKML;
import templates.RateIndicatorBFToProcessing;

@SuppressWarnings("serial")
public class RateIndicatorBFTab extends JPanel {

	// Icons
	private ImageIcon nuclearIcon;
	private ImageIcon logIcon;
	private ImageIcon locationsIcon;
	private ImageIcon processingIcon;
	private ImageIcon saveIcon;

	// Strings for paths
	private String logFilename;
	private String locationsFilename;

	// Text fields
	private JTextField burnInParser;
	private JTextField numberOfIntervalsParser;
	private JTextField maxAltMappingParser;
	private JTextField bfCutoffParser;
	private JTextField kmlPathParser;

	// Buttons for tab
	private JButton openLog;
	private JButton openLocations;
	private JButton generateKml;
	private JButton generateProcessing;
	private JButton saveProcessingPlot;

	// left tools pane
	private JPanel leftPanel;
	private JPanel tmpPanel;
	
	// Processing pane
	private JPanel rightPanel;
	private RateIndicatorBFToProcessing rateIndicatorBFToProcessing;

	// Progress bar
	private JProgressBar progressBar = new JProgressBar();

	public RateIndicatorBFTab() {

		// Setup miscallenous
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// Setup icons
		nuclearIcon = CreateImageIcon("/icons/nuclear.png");
		logIcon = CreateImageIcon("/icons/log.png");
		locationsIcon = CreateImageIcon("/icons/locations.png");
		processingIcon = CreateImageIcon("/icons/processing.png");
		saveIcon = CreateImageIcon("/icons/save.png");

		// Setup text fields
		burnInParser = new JTextField("0.1", 5);
		numberOfIntervalsParser = new JTextField("100", 5);
		maxAltMappingParser = new JTextField("500000", 5);
		bfCutoffParser = new JTextField("3.0", 5);
		kmlPathParser = new JTextField("/home/filip/Pulpit/output.kml", 15);

		// Setup buttons for tab
		openLog = new JButton("Open", logIcon);
		openLocations = new JButton("Open", locationsIcon);
		generateKml = new JButton("Generate", nuclearIcon);
		generateProcessing = new JButton("Plot", processingIcon);
		saveProcessingPlot = new JButton("Save", saveIcon);

		/**
		 * left tools pane
		 * */
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));// PAGE_AXIS
		leftPanel.setPreferredSize(new Dimension(230, 610));

		openLog.addActionListener(new ListenOpenLog());
		generateKml.addActionListener(new ListenGenerateKml());
		openLocations.addActionListener(new ListenOpenLocations());
		generateProcessing.addActionListener(new ListenGenerateProcessing());
		saveProcessingPlot.addActionListener(new ListenSaveProcessingPlot());

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Load log file:"));
		tmpPanel.add(openLog);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Load locations file:"));
		tmpPanel.add(openLocations);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Specify burn-in:"));
		tmpPanel.add(burnInParser);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Bayes Factor cut-off:"));
		tmpPanel.add(bfCutoffParser);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Number of intervals:"));
		tmpPanel.add(numberOfIntervalsParser);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Maximal altitude:"));
		tmpPanel.add(maxAltMappingParser);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("KML name:"));
		tmpPanel.add(kmlPathParser);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Generate KML / Plot tree:"));
		tmpPanel.setPreferredSize(new Dimension(230, 100));
		tmpPanel.add(generateKml);
		tmpPanel.add(generateProcessing);
		tmpPanel.add(progressBar);
		leftPanel.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setBorder(new TitledBorder("Save plot:"));
		tmpPanel.add(saveProcessingPlot);
		leftPanel.add(tmpPanel);

		JPanel leftPanelContainer = new JPanel();
		leftPanelContainer.setLayout(new BorderLayout());
		leftPanelContainer.add(leftPanel, BorderLayout.NORTH);
		add(leftPanelContainer);

		/**
		 * Processing pane
		 * */
		rateIndicatorBFToProcessing = new RateIndicatorBFToProcessing();
		rightPanel = new JPanel();
		rightPanel.setPreferredSize(new Dimension(2048, 1025));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.setBorder(new TitledBorder(""));
		rightPanel.setBackground(new Color(255, 255, 255));
		rightPanel.add(rateIndicatorBFToProcessing);
		add(rightPanel);

	}

	private class ListenOpenLog implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			try {

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Opening log file...");

				chooser.showOpenDialog(chooser);
				File file = chooser.getSelectedFile();
				logFilename = file.getAbsolutePath();
				System.out.println("Opened " + logFilename + "\n");

			} catch (Exception e1) {
				System.err.println("Could not Open! \n");
			}
		}
	}

	private class ListenOpenLocations implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			try {

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Loading locations file...");

				chooser.showOpenDialog(chooser);
				File file = chooser.getSelectedFile();
				locationsFilename = file.getAbsolutePath();
				System.out.println("Opened " + locationsFilename + "\n");

			} catch (Exception e1) {
				System.err.println("Could not Open! \n");
			}
		}
	}

	private class ListenGenerateKml implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				// Executed in background thread
				public Void doInBackground() {

					try {

						generateKml.setEnabled(false);
						progressBar.setIndeterminate(true);

						RateIndicatorBFToKML rateIndicatorBFToKML = new RateIndicatorBFToKML();

						rateIndicatorBFToKML.setLogFilePath(logFilename, Double
								.parseDouble(burnInParser.getText()));

						rateIndicatorBFToKML.setBfCutoff(Double
								.valueOf(bfCutoffParser.getText()));

						rateIndicatorBFToKML
								.setLocationFilePath(locationsFilename);

						rateIndicatorBFToKML.setMaxAltitudeMapping(Double
								.valueOf(maxAltMappingParser.getText()));

						rateIndicatorBFToKML.setNumberOfIntervals(Integer
								.valueOf(numberOfIntervalsParser.getText()));

						rateIndicatorBFToKML.setKmlWriterPath(kmlPathParser
								.getText());

						rateIndicatorBFToKML.GenerateKML();

						System.out.println("Finished in: "
								+ RateIndicatorBFToKML.time + " msec \n");

					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("FUBAR \n");
					}

					return null;
				}// END: doInBackground()

				// Executed in event dispatch thread
				public void done() {
					generateKml.setEnabled(true);
					progressBar.setIndeterminate(false);
				}
			};

			worker.execute();
		}
	}// END: ListenGenerateKml

	private class ListenGenerateProcessing implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				// Executed in background thread
				public Void doInBackground() {

					try {

						generateProcessing.setEnabled(false);
						progressBar.setIndeterminate(true);

						rateIndicatorBFToProcessing.setLogFilePath(logFilename,
								Double.parseDouble(burnInParser.getText()));

						rateIndicatorBFToProcessing.setBfCutoff(Double
								.valueOf(bfCutoffParser.getText()));

						rateIndicatorBFToProcessing
								.setLocationFilePath(locationsFilename);

						rateIndicatorBFToProcessing.init();

					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("FUBAR \n");
					}

					return null;
				}// END: doInBackground()

				// Executed in event dispatch thread
				public void done() {
					generateProcessing.setEnabled(true);
					progressBar.setIndeterminate(false);
				}
			};

			worker.execute();
		}
	}// END: ListenGenerateProcessing

	private class ListenSaveProcessingPlot implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			try {

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Saving as png file...");
				// System.getProperty("user.dir")

				chooser.showSaveDialog(chooser);
				File file = chooser.getSelectedFile();
				String plotToSaveFilename = file.getAbsolutePath();

				rateIndicatorBFToProcessing.save(plotToSaveFilename);
				System.out.println("Saved " + plotToSaveFilename + "\n");

			} catch (Exception e0) {
				System.err.println("Could not save! \n");
			}

		}// END: actionPerformed
	}// END: class

	private ImageIcon CreateImageIcon(String path) {
		java.net.URL imgURL = this.getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path + "\n");
			return null;
		}
	}

}
