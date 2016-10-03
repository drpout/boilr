package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IconToast {
	
	private static final int[] warningAttrs = new int[] { R.attr.ic_action_warning /*index 0*/};
	private static final int[] infoAttrs = new int[] { R.attr.ic_action_about /*index 0*/};

	private IconToast() {
	}

	public static void warning(Context context, CharSequence text) {
		show(context, text, warningAttrs);
	}
	
	public static void info(Context context, CharSequence text) {
		show(context, text, infoAttrs);
	}

	private static void show(Context context, CharSequence text, int[] attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.icon_toast, null);
		TextView textV = (TextView) layout.findViewById(R.id.icon_toast_text);
		textV.setText(text);
		ImageView imageV = (ImageView) layout.findViewById(R.id.icon_toast_icon);
		TypedArray ta = context.obtainStyledAttributes(attrs);
		imageV.setImageResource(ta.getResourceId(0, android.R.color.transparent));
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

}
