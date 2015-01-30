package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class SpikePercentageAlarmParameter extends PercentageAlarmParameter {

	private final long timeFrame;

	public SpikePercentageAlarmParameter(int id, Exchange exchange, Pair pair, long updateInterval, AndroidNotifier notifier, float percent,
			long timeFrame) {
		super(id, exchange, pair, updateInterval, notifier, percent);
		this.timeFrame = timeFrame;
	}

	public long getTimeFrame() {
		return timeFrame;
	}
}
