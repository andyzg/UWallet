package com.enghack.uwallet;

import org.jsoup.nodes.Document;

import android.app.Activity;
import android.os.Bundle;

import com.enghack.uwallet.login.LoginTask;
import com.enghack.uwallet.login.LoginTask.ResponseListener;

public class LoginFragment extends Activity implements ResponseListener{

	private String URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // INITIALIZE YOUR INFORMATION HERE TO TEST
        String studentID = "";
        String PIN = "";
        
        executeLogin(URL, studentID, PIN);
	}
	
	private void executeLogin(String URL, String ID, String PIN)
	{
		try
		{
			LoginTask login = new LoginTask();
			login.execute(URL,ID,PIN);
		} catch (Exception e) {
        e.printStackTrace();
		}
	}
	@Override
	public void onResponseFinish(Document doc) {
		System.out.println("HELLO WORLD");
	return;	
	}
}
