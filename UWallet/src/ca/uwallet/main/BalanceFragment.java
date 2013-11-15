package ca.uwallet.main;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.uwallet.main.util.ProviderUtils;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment{

	public BalanceFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_balance, container,
				false);
		
		String mealLabel;
		String flexLabel;
		String totalLabel;
		Resources res = getResources();
		
		int[] amounts = ProviderUtils.getBalanceAmounts(getActivity());
		if (amounts.length == 0){ // Not synced yet
			mealLabel = res.getString(R.string.meal_plan) + " " + res.getString(R.string.unknown);
			flexLabel = res.getString(R.string.flex_dollars) + " " + res.getString(R.string.unknown);
			totalLabel = res.getString(R.string.total) + " " + res.getString(R.string.unknown);
		}else{ // Show the data
			int mealAmount = ProviderUtils.getMealBalance(amounts);
			int flexAmount = ProviderUtils.getFlexBalance(amounts);
			mealLabel = res.getString(R.string.meal_plan) + " " + ProviderUtils.balanceToString(mealAmount);
			flexLabel = res.getString(R.string.flex_dollars) + " " +ProviderUtils.balanceToString(flexAmount);
			totalLabel = res.getString(R.string.total) + " " +ProviderUtils.balanceToString(mealAmount + flexAmount);
		}
		((TextView)v.findViewById(R.id.meal_plan_label)).setText(mealLabel);
		((TextView)v.findViewById(R.id.flex_dollars_label)).setText(flexLabel);
		((TextView)v.findViewById(R.id.total_label)).setText(totalLabel);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
	}	
}
