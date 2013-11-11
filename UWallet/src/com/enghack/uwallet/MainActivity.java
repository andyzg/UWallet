package com.enghack.uwallet;

import java.util.ArrayList;

import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.enghack.uwallet.login.HTMLParser;
import com.enghack.uwallet.login.LoginTask;
import com.enghack.uwallet.login.LoginTask.ResponseListener;
import com.enghack.watcard.Transaction;
import com.enghack.watcard.WatcardInfo;

/**
 * MainActivity, contains all fragment objects, listeners get methods for
 * ArrayList, and card balances
 * 
 * @author Andy, Seikun
 * 
 */

public class MainActivity extends Activity implements ResponseListener,
		BalanceFragment.Listener, TransactionFragment.Listener,
		AboutFragment.Listener, LoginFragment.Listener, MenuFragment.Listener {

	BalanceFragment mBalanceFragment = null;
	TransactionFragment mTransactionFragment = null;
	StatsFragment mStatsFragment = null;
	AboutFragment mAboutFragment = null;
	LoginFragment mLoginFragment = null;
	MenuFragment mMenuFragment = null;

	private final String URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private HTMLParser parser = new HTMLParser();
	private EditText viewID = null;
	private EditText viewPIN = null;
	private int studentID = 0;
	private int studentPIN = 0;

	private static WatcardInfo person;
	private Context context = this;

	private String m_key = "Preferences";

	// -store value———
	public void setShared_Preferences(String name, String value) {

		SharedPreferences preferences = getSharedPreferences(m_key,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}

	// -get value———-
	public String getShared_Preferences(String name) {
		SharedPreferences myPrefs = getSharedPreferences(m_key, MODE_PRIVATE);
		String resgid = myPrefs.getString(name, "0");
		return resgid;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBalanceFragment = new BalanceFragment();
		mTransactionFragment = new TransactionFragment();
		mStatsFragment = new StatsFragment();
		mAboutFragment = new AboutFragment();
		mLoginFragment = new LoginFragment();
		mMenuFragment = new MenuFragment();
		studentID = Integer.parseInt(getShared_Preferences("studentID"));
		studentPIN = Integer.parseInt(getShared_Preferences("studentPIN"));

		if (studentID == 0 && studentPIN == 0) {
			switchToFragment(mLoginFragment, false);
		} else {
			Log.d("STUDENT ID", String.format("%08d", studentID));
			Log.d("STUDENT PIN", String.format("%04d", studentPIN));
			executeLogin(URL, String.format("%08d", studentID),
					String.format("%04d", studentPIN));
		}

	}

	void switchToFragment(Fragment newFrag) {
		switchToFragment(newFrag, true);
	}

	void switchToFragment(Fragment newFrag, boolean addToBackStack) {
/*		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.fragment_container, newFrag);
		if (addToBackStack)
			transaction.addToBackStack(null);
		transaction.commit();*/

		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.card_flip_right_in,
				R.anim.card_flip_right_out).replace(R.id.fragment_container,
				newFrag);
		if (addToBackStack)
			transaction.addToBackStack(null);
		transaction.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBalanceButtonClicked() {
		switchToFragment(mBalanceFragment);
	}

	@Override
	public void onTransactionsButtonClicked() {
		switchToFragment(mTransactionFragment);
	}

	@Override
	public void onAboutButtonClicked() {
		switchToFragment(mAboutFragment);
	}

	@Override
	public void onLogOutButtonClicked() {
		// TODO: Use cleardata base method
		studentID = 0;
		studentPIN = 0;
		setShared_Preferences("studentID", "0");
		setShared_Preferences("studentPIN", "0");
		
		FragmentManager fm = getFragmentManager();
		
		switchToFragment(mLoginFragment, false);
	}

	@Override
	public void onLogInButtonClicked() {
		// DatabaseHandler db = new DatabaseHandler(this);
		viewID = (EditText) (this.findViewById(R.id.username_input));
		viewPIN = (EditText) (this.findViewById(R.id.password_input));
		if (!authenticate(viewID.getText().toString(), viewPIN.getText()
				.toString())) {

			errorMessage("Invalid Login");
			return;
		} else {
			studentID = Integer.parseInt(viewID.getText().toString());

			studentPIN = Integer.parseInt(viewPIN.getText().toString());
			executeLogin(URL, viewID.getText().toString(), viewPIN.getText()
					.toString());
		}
	}

	private void executeLogin(String URL, String ID, String PIN) {
		try {
			LoginTask login = new LoginTask(context, this);
			login.mListener = this;
			login.execute(URL, ID, PIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResponseFinish(Element histDoc, Element statusDoc,
			boolean valid) {
		if (!valid) {
			errorMessage("Invalid Credentials");
			return;
		}

		try {
			person = new WatcardInfo(parser.parseHist(histDoc),
					parser.parseBalance(statusDoc, 2, 5), parser.parseBalance(
							statusDoc, 5, 8), parser.parseBalance(statusDoc, 8,
							14), studentID, studentPIN);
			// person.printData(); // for testing purposes}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d("STUDENT ID2", String.format("%08d", studentID));
		Log.d("STUDENT PIN2", String.format("%04d", studentPIN));
		setShared_Preferences("studentID", String.format("%08d", studentID));
		setShared_Preferences("studentPIN", String.format("%04d", studentPIN));

		switchToFragment(mMenuFragment, false);
		return;
	}

	public void onResponseFinish(boolean valid) {
		errorMessage("Invalid Credentials");
	}

	private boolean authenticate(String a, String b) {
		if (a.matches("[0-9a-zA-Z]+") && a.length() > 2 && b.matches("[0-9a-zA-Z]+")
				&& b.length() > 2 && this.isNetworkAvailable()) {
			return true;
		}
		return false;
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void errorMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	public static double getMealBalance() {
		return person.getMealBalance();
	}

	public static double getFlexBalance() {
		return person.getFlexBalance();
	}

	public boolean onTouchEvent(MotionEvent event) {
		try {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	public static ArrayList<Transaction> getList() {
		return person.getList();
	}

	@Override
	public void onStatsButtonClicked() {
		switchToFragment(mStatsFragment, true);
	}
}
