package com.enghack.uwallet;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Standard fragment with 2 input fields and a button with listener attached
 * @author Seikun
 */

public class LoginFragment extends Fragment implements OnClickListener {

	private Listener mListener;

	public interface Listener {
		public void onLogInButtonClicked();
	}

	public LoginFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, container,
				false);

		v.findViewById(R.id.login_button).setOnClickListener(this);

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
	
	public void clearId(){
		((TextView)getView().findViewById(R.id.username_input)).setText("");
	}
	
	public void clearPin(){
		((TextView)getView().findViewById(R.id.password_input)).setText("");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.login_button:
			clearId();
			clearPin();
			mListener.onLogInButtonClicked();
			break;
		}
	}

}
