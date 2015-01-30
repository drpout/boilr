package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class ChangeAlarmParameter extends AlarmParameter {

	private final double change;

	public ChangeAlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotifier notifier, double change) {
		super(id, exchange, pair, period, notifier);
		this.change = change;
	}

	public double getChange() {
		return change;
	}

}
