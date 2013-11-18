package ca.uwallet.main;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.util.ProviderUtils;

/**
 * Transaction fragment, implements ScrollView to display information in table format
 * @author Andy, Seikun
 *
 */

public class TransactionFragment extends ListFragment implements LoaderCallbacks<Cursor>, SimpleCursorAdapter.ViewBinder{

	private Listener mListener;
	private static final int LOADER_TRANSACTION_ID = 137;
	private static final String SORT_ORDER_DESCENDING = "DESC";
	private SimpleCursorAdapter mAdapter;

	public interface Listener {
	}

	public TransactionFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		// Set text to display if there's no data
		setEmptyText(getResources().getString(R.string.empty_transaction_message));
		
		// Create the adapter
		int[] to = {R.id.date, R.id.description, R.id.amount};
		String[] from = {WatcardContract.Transaction.COLUMN_NAME_DATE,
				WatcardContract.Terminal.COLUMN_NAME_TEXT,
				WatcardContract.Transaction.COLUMN_NAME_AMOUNT};
		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.simple_list_transactions, null,
				from, to, 0);
		mAdapter.setViewBinder(this); // So that dates are displayed correctly
		setListAdapter(mAdapter);
		
		// Create the transaction loader
		getLoaderManager().initLoader(LOADER_TRANSACTION_ID, null, this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_TRANSACTION_ID)
			return null;
		String sortOrder = WatcardContract.Transaction.COLUMN_NAME_DATE + " " + SORT_ORDER_DESCENDING;
		CursorLoader loader = new CursorLoader(getActivity(), WatcardContract.Transaction.CONTENT_URI,
				null, null, null, sortOrder);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap cursor
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Remove the cursor
		mAdapter.swapCursor(null);
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// Display the formatted date
		if (cursor.getColumnName(columnIndex).equals(WatcardContract.Transaction.COLUMN_NAME_DATE)){
			long time = cursor.getLong(columnIndex);
			String s = ProviderUtils.formatDate(time);
			((TextView) view).setText(s);
			return true;
		} else if (cursor.getColumnName(columnIndex).equals(WatcardContract.Transaction.COLUMN_NAME_AMOUNT)){
			String s = ProviderUtils.formatCurrencyNoSymbol(cursor.getInt(columnIndex));
			((TextView) view).setText(s);
			return true;
		}
		return false;
	}
}
