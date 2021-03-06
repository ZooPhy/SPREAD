package templates;

import gui.InteractiveTableModel;

import java.util.ArrayList;
import java.util.List;

import utils.Holder;
import utils.ReadLog;
import utils.Utils;
import utils.Utils.PoissonPriorEnum;

/**
 * Pass by reference: EASY: meanPoissonPrior, poissonPriorOffset HARDER:
 * bayesFactors, combin
 * */

public class BayesFactorTest {

	private Holder meanPoissonPrior;
	private Holder poissonPriorOffset;

	private InteractiveTableModel table;
	private PoissonPriorEnum meanPoissonPriorSwitcher;
	private PoissonPriorEnum poissonPriorOffsetSwitcher;
	private ReadLog indicators;
	private ArrayList<String> combin;
	private ArrayList<Double> bayesFactors;

	public BayesFactorTest(InteractiveTableModel table,
			PoissonPriorEnum meanPoissonPriorSwitcher,
			Holder meanPoissonPriorHolder,
			PoissonPriorEnum poissonPriorOffsetSwitcher,
			Holder poissonPriorOffsetHolder, ReadLog indicators,
			ArrayList<String> combin, ArrayList<Double> bayesFactors) {

		this.table = table;
		this.meanPoissonPriorSwitcher = meanPoissonPriorSwitcher;
		this.meanPoissonPrior = meanPoissonPriorHolder;
		this.poissonPriorOffsetSwitcher = poissonPriorOffsetSwitcher;
		this.poissonPriorOffset = poissonPriorOffsetHolder;
		this.indicators = indicators;
		this.combin = combin;
		this.bayesFactors = bayesFactors;

	}

	public void ComputeBFTest() {

		int n = table.getRowCount();

		switch (meanPoissonPriorSwitcher) {
		case DEFAULT:
			meanPoissonPrior.value = Math.log(2);
			break;
		case USER:
			break;
		}

		switch (poissonPriorOffsetSwitcher) {
		case DEFAULT:
			poissonPriorOffset.value = (double) (n - 1);
			break;
		case USER:
			break;
		}

		boolean symmetrical = false;
		if (indicators.ncol == n * (n - 1)) {
			symmetrical = false;
		} else if (indicators.ncol == (n * (n - 1)) / 2) {
			symmetrical = true;
		} else {
			throw new RuntimeException(
					"the number of rate indicators does not match the number of locations!");
		}

		String[] locations = table.getColumn(0);

		for (int row = 0; row < n - 1; row++) {

			String[] subset = Utils.subset(locations, row, n - row);

			for (int i = 1; i < subset.length; i++) {
				combin.add(locations[row] + ":" + subset[i]);
			}
		}

		if (symmetrical == false) {

			List<String> combinReverse = new ArrayList<String>();
			for (int i = 0; i < combin.size(); i++) {
				String state = combin.get(i).split(":")[1];
				String parentState = combin.get(i).split(":")[0];
				combinReverse.add(state + ":" + parentState);
			}

			combin.addAll(combinReverse);
		}

		double qk = Double.NaN;
		if (symmetrical) {
			qk = (meanPoissonPrior.value + poissonPriorOffset.value)
					/ ((n * (n - 1)) / 2);
		} else {
			qk = (meanPoissonPrior.value + poissonPriorOffset.value)
					/ ((n * (n - 1)) / 1);
		}

		double[] pk = Utils.colMeans(indicators.indicators);

		double denominator = qk / (1 - qk);

		for (int row = 0; row < pk.length; row++) {
			double bf = (pk[row] / (1 - pk[row])) / denominator;

			if (bf == Double.POSITIVE_INFINITY) {

				bf = ((pk[row] - (double) (1.0 / indicators.nrow)) / (1 - (pk[row] - (double) (1.0 / indicators.nrow))))
						/ denominator;

				System.out.println("Correcting for infinite bf: " + bf);
			}

			bayesFactors.add(bf);
		}

	}// END: ComputeBFTest

}// END: BayesFactorTest
