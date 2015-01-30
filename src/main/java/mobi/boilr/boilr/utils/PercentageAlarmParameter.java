package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class PercentageAlarmParameter extends AlarmParameter{

	private final float percent;

	public PercentageAlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotifier notifier, float percent) {
		super(id, exchange, pair, period, notifier);
		this.percent = percent;
	}

	public float getPercent() {
		return percent;
	}
}
