package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class ChangeAlarmParameter extends AlarmParameter {

	private final double change;

	public ChangeAlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotify notify, double change) {
		super(id, exchange, pair, period, notify);
		this.change = change;
	}

	public double getChange() {
		return change;
	}

}
