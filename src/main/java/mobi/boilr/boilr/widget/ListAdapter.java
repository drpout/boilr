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

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			this.currentConstraint = constraint;
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
				this.filterStrings = constraint.toString().toLowerCase().split("\\s+");
				//int filtersCount = filterStrings.length;
				List<T> newList = new ArrayList<T>();
				T filterable;
				//boolean containsAllFilters;
				int count = originalList.size();
				for (int i = 0; i < count; i++) {
					//containsAllFilters = true;
					filterable = originalList.get(i);
				
//					for (int j = 0; j < filtersCount; j++) {
//						if(!filterable.toString().toLowerCase().contains(filterStrings[j])) {
//							containsAllFilters = false;
//							break;
//						}
//					}
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

		public void addWithFilter(P item){
			if(belongs2Filter(item.toString())){
				mList.add((T)item);
			}
		}

		public boolean belongs2Filter(String filterable){
			if( currentConstraint != null ){
				//String[] filterStrings = currentConstraint.toString().toLowerCase().split("\\s+");
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
	public T getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
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

	public void add(T pair) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.add(pair);
			} 
			mFilter.addWithFilter(pair);
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

	public void remove(int position) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.remove(position);
			} 
			mList.remove(position);
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
}