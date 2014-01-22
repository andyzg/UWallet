package ca.uwallet.main;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.util.ProviderUtils;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment implements LoaderCallbacks<Cursor>{
	
	private static final int LOADER_BALANCES_ID = 501;

	public BalanceFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_BALANCES_ID, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_balance, container,
				false);
		
		//updateLabels(v, null);
		
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	private void updateLabels(View v, Cursor cursor){
		if (v == null)
			return;
		String mealLabel;
		String flexLabel;
		String totalLabel;
		Resources res = getResources();
		
		if (cursor.getCount() < ProviderUtils.BALANCE_CURSOR_COUNT){ // Not synced yet
			mealLabel = res.getString(R.string.meal_plan) + " " + res.getString(R.string.unknown);
			flexLabel = res.getString(R.string.flex_dollars) + " " + res.getString(R.string.unknown);
			totalLabel = res.getString(R.string.total) + " " + res.getString(R.string.unknown);
		}else{ // Show the data
			int[] amounts = ProviderUtils.getBalanceAmounts(cursor);
			mealLabel = res.getString(R.string.meal_plan) + " " + ProviderUtils.formatCurrency(amounts[ProviderUtils.MEAL_PLAN]);
			flexLabel = res.getString(R.string.flex_dollars) + " " +ProviderUtils.formatCurrency(amounts[ProviderUtils.FLEX_DOLLAR]);
			totalLabel = res.getString(R.string.total) + " " +ProviderUtils.formatCurrency(amounts[ProviderUtils.TOTAL]);
		}
		((TextView)v.findViewById(R.id.meal_plan_label)).setText(mealLabel);
		((TextView)v.findViewById(R.id.flex_dollars_label)).setText(flexLabel);
		((TextView)v.findViewById(R.id.total_label)).setText(totalLabel);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_BALANCES_ID)
			return null;
		return new CursorLoader(getActivity(), WatcardContract.Balance.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		updateLabels(getView(), data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		updateLabels(getView(), null);
	}	
}
