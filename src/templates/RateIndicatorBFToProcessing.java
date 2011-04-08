package templates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import utils.Setter;
import utils.ReadLocations;
import utils.ReadLog;
import utils.Utils;

@SuppressWarnings("serial")
public class RateIndicatorBFToProcessing extends PApplet {

	private ReadLocations locations;
	private ReadLog indicators;
	private double bfCutoff;
	private List<Double> bayesFactors;
	private List<String> combin;
	private PImage mapImage;
	private double maxBranchRedMapping;
	private double maxBranchGreenMapping;
	private double maxBranchBlueMapping;
	private double maxBranchOpacityMapping;

	// min/max longitude
	private float minX, maxX;
	// min/max latitude
	private float minY, maxY;

	public RateIndicatorBFToProcessing() {

	}// END: RateIndicatorBFToProcessing()

	public void setBfCutoff(double cutoff) {
		bfCutoff = cutoff;
	}

	public void setLocationFilePath(String path) throws ParseException {

		locations = new ReadLocations(path);
	}

	public void setLogFilePath(String path, double burnIn) {

		indicators = new ReadLog(path, burnIn);
	}

	public void setMaxBranchRedMapping(double max) {
		maxBranchRedMapping = max;
	}

	public void setMaxBranchGreenMapping(double max) {
		maxBranchGreenMapping = max;
	}

	public void setMaxBranchBlueMapping(double max) {
		maxBranchBlueMapping = max;
	}

	public void setMaxBranchOpacityMapping(double max) {
		maxBranchOpacityMapping = max;
	}

	public void setup() {

		minX = -180;
		maxX = 180;

		minY = -90;
		maxY = 90;

		// will improve font rendering speed with default renderer
		hint(ENABLE_NATIVE_FONTS);
		PFont plotFont = createFont("Arial", 12);
		textFont(plotFont);

	}// END: setup

	public void draw() {

		ComputeBFTest();

		noLoop();
		smooth();
		drawMapBackground();
		DrawPlaces();
		DrawRates();
		DrawPlacesLabels();

	}// END:draw

	void drawMapBackground() {

		// World map in Equirectangular projection
		mapImage = loadImage(LoadMapBackground(new Setter().getJarBoolean()));
		image(mapImage, 0, 0, width, height);

	}// END: drawMapPolygons

	// //////////////
	// ---PLACES---//
	// //////////////
	private void DrawPlaces() {

		float radius = 7;

		// White
		fill(255, 255, 255);
		noStroke();

		for (int row = 0; row < locations.nrow; row++) {

			// Equirectangular projection:
			float X = map(locations.getFloat(row, 1), minX, maxX, 0, width);
			float Y = map(locations.getFloat(row, 0), maxY, minY, 0, height);

			ellipse(X, Y, radius, radius);

		}
	}// END DrawPlaces

	private void DrawPlacesLabels() {

		textSize(7);

		// Black labels
		fill(0, 0, 0);

		for (int row = 0; row < locations.nrow; row++) {

			String name = locations.locations[row];
			float X = map(locations.getFloat(row, 1), minX, maxX, 0, width);
			float Y = map(locations.getFloat(row, 0), maxY, minY, 0, height);

			text(name, X, Y);
		}
	}// END: DrawPlacesLabels

	private void DrawRates() {

		strokeWeight(2);

		float bfMax = (float) Math.log(Utils.getListMax(bayesFactors));

		for (int i = 0; i < combin.size(); i++) {

			if (bayesFactors.get(i) > bfCutoff) {

				/**
				 * Color mapping
				 * */
				float bf = (float) Math.log(bayesFactors.get(i));

				int red = (int) Utils.map(bf, 0, bfMax, 0, maxBranchRedMapping);

				int green = (int) Utils.map(bf, 0, bfMax, 0,
						maxBranchGreenMapping);

				int blue = (int) Utils.map(bf, 0, bfMax, 0,
						maxBranchBlueMapping);

				int alpha = (int) Utils.map(bf, 0, bfMax,
						maxBranchOpacityMapping, 100);

				stroke(red, green, blue, alpha);

				String state = combin.get(i).split(":")[1];
				String parentState = combin.get(i).split(":")[0];

				float longitude = Utils.MatchStateCoordinate(locations, state,
						1);
				float latitude = Utils
						.MatchStateCoordinate(locations, state, 0);

				float parentLongitude = Utils.MatchStateCoordinate(locations,
						parentState, 1);
				float parentLatitude = Utils.MatchStateCoordinate(locations,
						parentState, 0);

				float x0 = map(parentLongitude, minX, maxX, 0, width);
				float y0 = map(parentLatitude, maxY, minY, 0, height);

				float x1 = map(longitude, minX, maxX, 0, width);
				float y1 = map(latitude, maxY, minY, 0, height);

				line(x0, y0, x1, y1);
			}
		}// END: ArrayList loop
	}// END: DrawRates

	private void ComputeBFTest() {

		int n = locations.nrow;

		boolean symmetrical = false;
		if (indicators.ncol == n * (n - 1)) {
			symmetrical = false;
		} else if (indicators.ncol == (n * (n - 1)) / 2) {
			symmetrical = true;
		} else {
			System.err.println("the number of rate indicators does not match "
					+ "the number of locations!");
		}

		combin = new ArrayList<String>();

		for (int row = 0; row < n - 1; row++) {

			String[] subset = Utils.Subset(locations.locations, row, n - row);

			for (int i = 1; i < subset.length; i++) {
				combin.add(locations.locations[row] + ":" + subset[i]);
			}
		}

		if (symmetrical == false) {
			combin.addAll(combin);
		}

		double qk = Double.NaN;
		if (symmetrical) {
			qk = (Math.log(2) + n - 1) / ((n * (n - 1)) / 2);
		} else {
			qk = (Math.log(2) + n - 1) / ((n * (n - 1)) / 1);
		}

		double[] pk = Utils.ColMeans(indicators.indicators);

		bayesFactors = new ArrayList<Double>();
		double denominator = qk / (1 - qk);

		for (int row = 0; row < pk.length; row++) {
			double bf = (pk[row] / (1 - pk[row])) / denominator;
			bayesFactors.add(bf);
		}
	}// END: ComputeBFTest

	private String LoadMapBackground(boolean fromJar) {

		String imgPathFromJar;

		if (fromJar) {
			imgPathFromJar = "jar:"
					+ this.getClass().getResource("world_map.png").getPath();
		} else {
			imgPathFromJar = this.getClass().getResource("world_map.png")
					.getPath();
		}

		return imgPathFromJar;
	}

}// END: RateIndicatorBFToProcessing class
