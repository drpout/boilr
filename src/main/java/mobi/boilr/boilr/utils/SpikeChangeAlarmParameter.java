package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class SpikeChangeAlarmParameter extends ChangeAlarmParameter {

	private final long timeFrame;

	public SpikeChangeAlarmParameter(int id, Exchange exchange, Pair pair, long updateInterval, AndroidNotify notify, double change, long timeFrame) {
		super(id, exchange, pair, updateInterval, notify, change);
		this.timeFrame = timeFrame;
	}

	public long getTimeFrame() {
		return timeFrame;
	}
}
