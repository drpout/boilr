package aunused;

import java.util.List;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.ArrayAdapter;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.services.StorageAndControlService;
import com.github.andrefbsantos.boilr.services.StorageAndControlService.StorageAndControlServiceBinder;

public class ToggleAlarmServiceConnection implements ServiceConnection {

	private StorageAndControlService mService;
	private boolean mBound;
	private ArrayAdapter<AlarmWrapper> adapter;
	private int id;

	public ToggleAlarmServiceConnection(ArrayAdapter<AlarmWrapper> adapter,
			int id) {
		this.adapter = adapter;
		this.id = id;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		mService = ((StorageAndControlServiceBinder) binder).getService();
		mBound = true;

		List<AlarmWrapper> list = null;
		if (mBound) {
			list = mService.getAlarms();
			// unbindService(this);
			// this.un
		}

		System.out.println("ID=" + id);

		for (AlarmWrapper wrapper : list) {
			if (wrapper.getAlarm().getId() == id) {
				wrapper.getAlarm().toggle();
				break;
			}
		}

		adapter.clear();
		adapter.addAll(list);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		mBound = false;
	}

}
