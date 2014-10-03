package mobi.boilr.boilr.widget;

import java.util.ArrayList;
import java.util.List;

import android.widget.Filter;

public class ListFilter<T> extends Filter {

	private List<T> mList;
	private List<T> mOriginalList;
	private ListAdapter<T> mAdapter;
	private Object mLock;
	private CharSequence currentConstraint;


	public ListFilter(ListAdapter<T> adapter, List<T> list, List<T> originalList, Object lock) {
		this.mAdapter= adapter;
		this.mList = list;
		this.mOriginalList = originalList;
		this.mLock = lock;
	}

	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		this.currentConstraint = constraint;
		FilterResults results = new FilterResults();
		if(mOriginalList==null) {
			synchronized (mLock) {
				mOriginalList = new ArrayList<T>(mList);
			}
		}
		if(constraint == null || constraint.length() == 0) {
			ArrayList<T> list;
			synchronized (mLock) {
				list = new ArrayList<T>(mOriginalList);
			}
			results.values = list;
			results.count = list.size();
		} else {
			List<T> originalList;
			synchronized (mLock) {
				originalList = new ArrayList<T>(mOriginalList);
			}
			String[] filterStrings = constraint.toString().toLowerCase().split("\\s+");
			int filtersCount = filterStrings.length;
			List<T> newList = new ArrayList<T>();
			T filterable;
			boolean containsAllFilters;
			int count = originalList.size();
			for (int i = 0; i < count; i++) {
				containsAllFilters = true;
				filterable = originalList.get(i);
				for (int j = 0; j < filtersCount; j++) {
					if(!filterable.toString().toLowerCase().contains(filterStrings[j])) {
						containsAllFilters = false;
						break;
					}
				}
				if(containsAllFilters)
					newList.add(filterable);
			}
			results.values = newList;
			results.count = newList.size();
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		mList = (List<T>) results.values;
		if(results.count > 0) {
			mAdapter.notifyDataSetChanged();
		} else {
			mAdapter.notifyDataSetInvalidated();
		}
	}
	
	public void add(T item){
		
	}
	
}
