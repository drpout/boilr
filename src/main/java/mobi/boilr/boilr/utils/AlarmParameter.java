package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class AlarmParameter {

	private final int id;
	private final Exchange exchange;
	private final Pair pair;
	private final long period;
	private final AndroidNotify notify;

	public AlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotify notify) {
		this.id = id;
		this.exchange = exchange;
		this.pair = pair;
		this.period = period;
		this.notify = notify;
	}

	public int getId() {
		return id;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public Pair getPair() {
		return pair;
	}

	public long getPeriod() {
		return period;
	}

	public AndroidNotify getNotify() {
		return notify;
	}
}
