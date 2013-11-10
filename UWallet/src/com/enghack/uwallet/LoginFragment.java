package com.enghack.uwallet;

import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.enghack.uwallet.login.HTMLParser;
import com.enghack.uwallet.login.LoginTask;
import com.enghack.uwallet.login.LoginTask.ResponseListener;
import com.enghack.watcard.WatcardInfo;
import com.example.testreadwrite.R;

public class LoginFragment extends Fragment implements ResponseListener, OnClickListener {

	private String URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private Button submit;
	private HTMLParser Parser;
	private int studentID;
	private int PIN;
	private LinearLayout view;
	private Context context;
	
	private EditText ViewID;
	private EditText ViewPIN;
	
	private Listener mListener;
	
	public interface Listener {
		public void onTestButtonClicked();
	}

	public LoginFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		Parser = new HTMLParser();
		
		View v = inflater.inflate(R.layout.fragment_login, container,
				false);
        
        view = (LinearLayout)getActivity().findViewById(R.id.login_view);
        submit = (Button)getActivity().findViewById(R.id.login_button);

		ViewID = (EditText)getActivity().findViewById(R.id.username_input);
		ViewPIN = (EditText)getActivity().findViewById(R.id.password_input);
       
        submit.setOnClickListener(this);
        
        return view;
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
	
	private void executeLogin(String URL, String ID, String PIN)
	{
		try
		{
			LoginTask login = new LoginTask();
			login.mListener = this;
			login.execute(URL,ID,PIN);
		} catch (Exception e) {
        e.printStackTrace();
		}
	}
	@Override
	public void onResponseFinish(Element histDoc, Element statusDoc) {
		WatcardInfo person = new WatcardInfo(
				Parser.parseHist(histDoc),
				// Indexes of each type of balance based on the website
				Parser.parseBalance(statusDoc,2,5),
				Parser.parseBalance(statusDoc,5,8),
				Parser.parseBalance(statusDoc, 8, 14),
				studentID,
				PIN);
		person.printData(); // for testing purposes
	return;	
	}



	// Currently needs validating in case bad input or bad connection
	@Override
	public void onClick(View view) {
		studentID = Integer.parseInt(ViewID.getText().toString());
		PIN = Integer.parseInt(ViewPIN.getText().toString());
		executeLogin(URL, ViewID.getText().toString(), ViewPIN.getText().toString());
	}
}
