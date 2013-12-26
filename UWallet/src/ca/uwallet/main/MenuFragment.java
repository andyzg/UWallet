package ca.uwallet.main;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * Main hub fragment, activates once a username and password have been verified
 * @author Seikun
 */

public class MenuFragment extends Fragment implements OnClickListener {

	
	private Listener mListener;

	public interface Listener {
		public void onBalanceButtonClicked();
		public void onTransactionsButtonClicked();
		public void onStatsButtonClicked();
		public void onLogOutButtonClicked();
	}

	public MenuFragment() {
		// Required empty public constructor

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_menu, container, false);

		v.findViewById(R.id.balance_button).setOnClickListener(this);
		v.findViewById(R.id.transactions_button).setOnClickListener(this);
		v.findViewById(R.id.statistics_button).setOnClickListener(this);
		v.findViewById(R.id.logout_button).setOnClickListener(this);

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
		switch (view.getId()) {
		case R.id.balance_button:
			mListener.onBalanceButtonClicked();
			break;
		case R.id.transactions_button:
			mListener.onTransactionsButtonClicked();
			break;
		case R.id.statistics_button:
			mListener.onStatsButtonClicked();
			break;
		case R.id.logout_button:
			mListener.onLogOutButtonClicked();
			break;
		}
	}

}
