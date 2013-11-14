package ca.uwallet.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.uwallet.main.util.ProviderUtils;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment implements OnClickListener {

	private Listener mListener;

	public interface Listener {
	}

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
		
		int[] amounts = ProviderUtils.getBalanceAmounts(getActivity());
		int mealAmount = ProviderUtils.getMealBalance(amounts);
		int flexAmount = ProviderUtils.getFlexBalance(amounts);
		String mealLabel = getResources().getString(R.string.meal_plan) + " " + ProviderUtils.balanceToString(mealAmount);
		String flexLabel = getResources().getString(R.string.flex_dollars) + " " +ProviderUtils.balanceToString(flexAmount);
		String total = getResources().getString(R.string.total) + " " +ProviderUtils.balanceToString(mealAmount + flexAmount);
		((TextView)v.findViewById(R.id.meal_plan_label)).setText(mealLabel);
		((TextView)v.findViewById(R.id.flex_dollars_label)).setText(flexLabel);
		((TextView)v.findViewById(R.id.total_label)).setText(total);
		return v;
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
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public void onClick(View view) {}
	
	
}
