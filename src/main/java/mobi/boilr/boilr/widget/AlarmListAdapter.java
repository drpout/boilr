package mobi.boilr.boilr.widget;

import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.listeners.SwipeAndMoveTouchListener.Reference;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.views.fragments.PriceHitAlarmSettingsFragment;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.RollingPriceChangeAlarm;
import mobi.boilr.libpricealarm.UpperLimitSmallerOrEqualLowerLimitException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmListAdapter extends ListAdapter<Alarm> {
	private boolean mStarted = false;
	private AlarmListActivity alarmListActivity;
	private TextView mExchange;
	private TextView mPair;
	private AlertDialog.Builder mAlertBuilder;
	private EditText mTextInput;
	
	public AlarmListAdapter(AlarmListActivity alarmListActivity, List<Alarm> alarms) {
		super(alarmListActivity, alarms);
		this.alarmListActivity = alarmListActivity;
	}

	@Override
	// TODO If needed optimize with http://www.piwai.info/android-adapter-good-practices
	public View getView(int position, View convertView, ViewGroup parent) {
		Alarm alarm = mList.get(position);
		AlarmLayout alarmLayout;
		// View recycling
		if(convertView == null){
			convertView = getInflater().inflate(R.layout.alarm_list_row, parent, false);
			alarmLayout = ((AlarmLayout) convertView);
			alarmLayout.start();
			View progressCircle = convertView.findViewById(R.id.progress_update_layout);
			progressCircle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alarmListActivity.getStorageAndControlService().toggleAlarm(((AlarmLayout) v.getParent().getParent()).getAlarm().getId());
				}
			});
			convertView.findViewById(R.id.upper_limit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					upperLimitClicked(v);
				}
			});
			convertView.findViewById(R.id.lower_limit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					lowerLimitClicked(v);
				}
			});
			convertView.setOnDragListener(new OnDragListener() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean onDrag(View dstView, DragEvent event) {
					Reference<View> ref = (Reference<View>) event.getLocalState();
					final View mView = ref.getReference();
					switch(event.getAction()) {
					case DragEvent.ACTION_DRAG_STARTED:
						if(!mStarted) {
							mView.setVisibility(View.INVISIBLE);
							mStarted = true;
						}
						break;
					case DragEvent.ACTION_DRAG_ENTERED:
						mView.setVisibility(View.VISIBLE);
						dstView.setVisibility(View.INVISIBLE);
						AlarmListAdapter.this.moveTo(((AlarmLayout) mView).getAlarm(), ((AlarmLayout) dstView).getAlarm());
						ref.setReference(dstView);
						break;
					case DragEvent.ACTION_DRAG_ENDED:
						mView.post(new Runnable() {
							@Override
							public void run() {
								mStarted = false;
								mView.setVisibility(View.VISIBLE);
							}
						});
						break;
					}
					return true;
				}
			});
		} else {
			// Recycled views retain the alpha and translation from when they were removed.
			if(convertView.getAlpha() != 1)
				convertView.setAlpha(1);
			if(convertView.getTranslationX() != 0)
				convertView.setTranslationX(0);
		}

		if(alarm instanceof PriceChangeAlarm) {
			convertView.findViewById(R.id.change_base_wrapper).setClickable(true);
			convertView.findViewById(R.id.change_base_wrapper).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					changeClicked(v);
				}
			});
		} else {
			convertView.findViewById(R.id.change_base_wrapper).setClickable(false);
		}
		mExchange = (TextView) convertView.findViewById(R.id.exchange);
		mExchange.setText(alarm.getExchange().getName());
		mPair = (TextView) convertView.findViewById(R.id.pair);
		mPair.setText(alarm.getPair().toString());
		alarmLayout = ((AlarmLayout) convertView);
		alarmLayout.setAlarm(alarm);
		alarmLayout.updateChildren(System.currentTimeMillis());
		return convertView;
	}

	@Override
	public void moveTo(Alarm A1, Alarm A2) {
		super.moveTo(A1, A2);
		alarmListActivity.getStorageAndControlService().updateAlarmPosition(A1, A2);
	}
	
	private void upperLimitClicked(View v) {
		Alarm alarm = ((AlarmLayout) v.getParent().getParent()).getAlarm();
		if(alarm instanceof PriceHitAlarm) {
			final PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;
			prepareDialog(priceHitAlarm.getUpperLimit());
			mAlertBuilder.setTitle(R.string.pref_title_upper_limit);
			mAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						priceHitAlarm.setUpperLimit(Double.parseDouble(mTextInput.getText().toString()));
					} catch(UpperLimitSmallerOrEqualLowerLimitException e) {
						PriceHitAlarmSettingsFragment.handleLimitsExceptions(e, getContext());
					}
				}
			});
			showDialog();
		} else if(alarm instanceof RollingPriceChangeAlarm) {
			RollingPriceChangeAlarm changeAlarm = (RollingPriceChangeAlarm) alarm;
			showLimitExplantion(changeAlarm, true);
		}
	}

	private void lowerLimitClicked(View v) {
		Alarm alarm = ((AlarmLayout) v.getParent().getParent()).getAlarm();
		if(alarm instanceof PriceHitAlarm) {
			final PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;
			prepareDialog(priceHitAlarm.getLowerLimit());
			mAlertBuilder.setTitle(R.string.pref_title_lower_limit);
			mAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						priceHitAlarm.setLowerLimit(Double.parseDouble(mTextInput.getText().toString()));
					} catch(UpperLimitSmallerOrEqualLowerLimitException e) {
						PriceHitAlarmSettingsFragment.handleLimitsExceptions(e, getContext());
					}
				}
			});
			showDialog();
		} else if(alarm instanceof RollingPriceChangeAlarm) {
			RollingPriceChangeAlarm changeAlarm = (RollingPriceChangeAlarm) alarm;
			showLimitExplantion(changeAlarm, false);
		}
	}

	private void changeClicked(View v) {
		final RollingPriceChangeAlarm changeAlarm = (RollingPriceChangeAlarm) ((AlarmLayout) v.getParent().getParent()).getAlarm();
		if(changeAlarm.isPercent())
			prepareDialog(changeAlarm.getPercent());
		else
			prepareDialog(changeAlarm.getChange());
		mAlertBuilder.setTitle(R.string.pref_title_change_value);
		mAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				double newValue = Double.parseDouble(mTextInput.getText().toString());
				if(changeAlarm.isPercent())
					changeAlarm.setPercent((float) newValue);
				else
					changeAlarm.setChange(newValue);
			}
		});
		showDialog();
	}

	private void prepareDialog(double currValue) {
		mAlertBuilder = new AlertDialog.Builder(alarmListActivity);
		mTextInput = new EditText(alarmListActivity);
		mTextInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		mAlertBuilder.setView(mTextInput);
		mAlertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled, do nothing.
			}
		});
		mTextInput.setText(Conversions.formatMaxDecimalPlaces(currValue));
	}

	private void showDialog() {
		AlertDialog dialog = mAlertBuilder.show();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void showLimitExplantion(RollingPriceChangeAlarm changeAlarm, boolean isUpperLimit) {
		String change = changeAlarm.isPercent() ? Conversions.format2DecimalPlaces(changeAlarm.getPercent()) + "%" :
			Conversions.formatMaxDecimalPlaces(changeAlarm.getChange());
		String limit = Conversions.formatMaxDecimalPlaces(isUpperLimit ? changeAlarm.getUpperLimit() : changeAlarm.getLowerLimit());
		String op = isUpperLimit ? "+" : "-";
		Toast.makeText(getContext(), getContext().getString(R.string.limit_explanation, 
				limit,
				Conversions.formatMaxDecimalPlaces(changeAlarm.getBaseValue()),
				Conversions.formatMilis(changeAlarm.getTimeFrame(), getContext()),
				op,
				change), Toast.LENGTH_LONG).show();
	}
}