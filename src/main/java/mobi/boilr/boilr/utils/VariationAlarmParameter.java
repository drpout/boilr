package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class VariationAlarmParameter extends AlarmParameter {

	private final double variation;

	public VariationAlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotify notify, double variation) {
		super(id, exchange, pair, period, notify);
		this.variation = variation;
	}

	public double getVariation() {
		return variation;
	}

}
