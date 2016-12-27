package mobi.boilr.boilr.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;


public abstract class ListAdapter<T> extends BaseAdapter implements Filterable {

	protected List<T> mList;
	protected List<T> mOriginalList;
	private final Context mContext;
	private final LayoutInflater mInflater;
	private ListFilter<T> mFilter;
	protected final Object mLock = new Object();

	public class ListFilter<P> extends Filter {
		private CharSequence currentConstraint;
		private String[] filterStrings;

		@SuppressWarnings("unchecked")
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			this.currentConstraint = constraint;
			this.filterStrings = constraint.toString().toLowerCase().split("\\s+");

			FilterResults results = new FilterResults();
			if(mOriginalList==null) {
				synchronized (mLock) {
					mOriginalList = (List<T>) new ArrayList<P>((List<P>)mList);
				}
			}
			if(constraint == null || constraint.length() == 0) {
				ArrayList<T> list;
				synchronized (mLock) {
					list = (ArrayList<T>) new ArrayList<P>((List<P>) mOriginalList);
				}
				results.values = list;
				results.count = list.size();
			} else {
				List<T> originalList;
				synchronized (mLock) {
					originalList = (List<T>) new ArrayList<P>((List<P>)mOriginalList);
				}
				List<T> newList = new ArrayList<T>();
				T filterable;
				int count = originalList.size();
				for (int i = 0; i < count; i++) {
					filterable = originalList.get(i);
					if(belongs2Filter(filterable.toString()))
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
				ListAdapter.this.notifyDataSetChanged();
			} else {
				ListAdapter.this.notifyDataSetInvalidated();
			}
		}

		public boolean belongs2Filter(String filterable){
			if( currentConstraint != null ){
				int filtersCount = filterStrings.length;
				boolean containsAllFilters = true;
				for (int j = 0; j < filtersCount; j++) {
					if(!filterable.toString().toLowerCase().contains(filterStrings[j])) {
						containsAllFilters = false;
						break;
					}
				}
				return containsAllFilters;
			}
			return true;
		}

		public void filter() {
			if(currentConstraint != null){
				this.filter(currentConstraint);
			}	
		}
	}

	public ListAdapter(Context mContext, List<T> list) {
		this.mContext = mContext;
		this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mList = new ArrayList<T>(list);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public T getItem(int pos) {
		return mList.get(pos);
	}

	public int originalIndexOf(T obj) {
		if(mOriginalList != null) {
			return mOriginalList.indexOf(obj);
		} else {
			return -1;
		}
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public Filter getFilter() {
		if(mFilter == null) {
			mFilter = new ListFilter<T>();
		}
		return mFilter;
	}

	protected final LayoutInflater getInflater() {
		return mInflater;
	}

	public void add(T obj) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.add(obj);
			}
			if(mFilter.belongs2Filter(obj.toString())) {
				mList.add(obj);
			}
		}
		notifyDataSetChanged();
	}

	public void add(T obj, int filteredPos, int originalPos) {
		synchronized(mLock) {
			if(mOriginalList != null) {
				mOriginalList.add(originalPos, obj);
			}
			if(mFilter.belongs2Filter(obj.toString())) {
				mList.add(filteredPos, obj);
			}
		}
		notifyDataSetChanged();
	}

	public void addAll(Collection<? extends T> collection) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.addAll(collection);
			} 

			mList.addAll(collection);
			if(mFilter == null)
				mFilter = new ListFilter<T>();
			mFilter.filter();
		}
		notifyDataSetChanged();
	}

	public void remove(T item) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.remove(item);
			} 
			mList.remove(item);
		}
		notifyDataSetChanged();
	}

	public void clear() {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.clear();
			}
			mList.clear();
		}
		notifyDataSetChanged();
	}

	public Context getContext(){ 
		return mContext;
	}

	public void moveTo(T obj1, T obj2) {
		synchronized(mLock) {
			int pos1 = mList.indexOf(obj1);
			int pos2 = mList.indexOf(obj2);
			if(pos1 < pos2) {
				mList.add(pos2 + 1, obj1);
				mList.remove(pos1);
			} else {
				mList.add(pos2, obj1);
				mList.remove(pos1 + 1);
			}
			if(mOriginalList != null) {
				pos1 = mOriginalList.indexOf(obj1);
				pos2 = mOriginalList.indexOf(obj2);
				if(pos1 < pos2) {
					mOriginalList.add(pos2 + 1, obj1);
					mOriginalList.remove(pos1);
				} else {
					mOriginalList.add(pos2, obj1);
					mOriginalList.remove(pos1 + 1);
				}
			}
		}
		notifyDataSetChanged();
	}
}