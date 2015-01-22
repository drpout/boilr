package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class SpikePercentageAlarmParameter extends PercentageAlarmParameter {

	private final long timeFrame;

	public SpikePercentageAlarmParameter(int id, Exchange exchange, Pair pair, long updateInterval, AndroidNotify notify, float percent,
			long timeFrame) {
		super(id, exchange, pair, updateInterval, notify, percent);
		this.timeFrame = timeFrame;
	}

	public long getTimeFrame() {
		return timeFrame;
	}
}
