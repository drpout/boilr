package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class PercentageAlarmParameter extends AlarmParameter{

	private final float percent;

	public PercentageAlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotify notify, float percent) {
		super(id, exchange, pair, period, notify);
		// TODO Auto-generated constructor stub
		this.percent = percent;
	}

	public float getPercent() {
		return percent;
	}
}
