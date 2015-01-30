package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public class AlarmParameter {

	private final int id;
	private final Exchange exchange;
	private final Pair pair;
	private final long period;
	private final AndroidNotifier notifier;

	public AlarmParameter(int id, Exchange exchange, Pair pair, long period,
			AndroidNotifier notifier) {
		this.id = id;
		this.exchange = exchange;
		this.pair = pair;
		this.period = period;
		this.notifier = notifier;
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

	public AndroidNotifier getNotifier() {
		return notifier;
	}
}
