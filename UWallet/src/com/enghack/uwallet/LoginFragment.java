package com.enghack.uwallet;

import org.jsoup.nodes.Element;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.enghack.uwallet.login.HTMLParser;
import com.enghack.uwallet.login.LoginTask;
import com.enghack.uwallet.login.LoginTask.ResponseListener;
import com.enghack.watcard.WatcardInfo;

public class LoginFragment extends Activity implements ResponseListener, OnClickListener {

	private String URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private Button submit;
	private HTMLParser Parser;
	private int studentID;
	private int PIN;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parser = new HTMLParser();
        setContentView(R.layout.activity_main);
        
        submit = (Button)findViewById(R.id.login_button);
       
        submit.setOnClickListener(this);
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
		EditText ViewID = (EditText)findViewById(R.id.username_input);
		EditText ViewPIN = (EditText)findViewById(R.id.password_input);
		studentID = Integer.parseInt(ViewID.getText().toString());
		PIN = Integer.parseInt(ViewPIN.getText().toString());
		executeLogin(URL, ViewID.getText().toString(), ViewPIN.getText().toString());
	}
}