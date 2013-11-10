package com.enghack.uwallet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment implements OnClickListener {

	@SuppressWarnings("unused")
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
		
		TextView insert = (TextView) v.findViewById(R.id.meal_plan_variable);
		insert.setText("" + MainActivity.getMealBalance());
		insert = (TextView)v.findViewById(R.id.flex_dollars_variable);
		insert.setText("" + MainActivity.getFlexBalance());
		insert = (TextView)v.findViewById(R.id.total_variable);
		insert.setText("" + (MainActivity.getFlexBalance()+MainActivity.getMealBalance()));
		
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
	public void onClick(View view) {
	}

}
