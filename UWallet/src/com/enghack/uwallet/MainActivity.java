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
	private String studentID = null;
	private String studentPIN = null;

	private static WatcardInfo person;
	private Context context = this;

	private static final String PREFERENCE_KEY = "Preferences";
	public static final String STUDENT_ID_KEY = "studentID";
	public static final String STUDENT_PIN_KEY = "studentPIN";

	// -store value———
	public void setSharedPreferences(String name, String value) {

		SharedPreferences preferences = getSharedPreferences(PREFERENCE_KEY,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}

	// -get value———-
	public String getSharedPreferences(String name) {
		SharedPreferences myPrefs = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
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
		
		studentID = getSharedPreferences(STUDENT_ID_KEY);
		studentPIN = getSharedPreferences(STUDENT_PIN_KEY);

		if (studentID == null && studentPIN == null) {
			switchToFragment(mLoginFragment, false);
		} else {
			executeLogin(URL, studentID, studentPIN);
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
		studentID = null;
		studentPIN = null;
		setSharedPreferences("studentID", "0");
		setSharedPreferences("studentPIN", "0");
		
		FragmentManager fm = getFragmentManager();
		
		switchToFragment(mLoginFragment, false);
	}

	@Override
	public void onLogInButtonClicked() {
		// DatabaseHandler db = new DatabaseHandler(this);
		viewID = (EditText) (this.findViewById(R.id.username_input));
		viewPIN = (EditText) (this.findViewById(R.id.password_input));
	
		studentID = viewID.getText().toString();
		studentPIN = viewPIN.getText().toString();
		executeLogin(URL, studentID, studentPIN);
	}

	private void executeLogin(String URL, String ID, String PIN) {
		LoginTask login = new LoginTask(context, this);
		login.mListener = this;
		try {
			login.execute(URL, ID, PIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResponseFinish(Element histDoc, Element statusDoc, boolean valid) {
		if (!valid) {
			showToast("Invalid Credentials");
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
		Log.d("STUDENT ID2", studentID);
		Log.d("STUDENT PIN2", studentPIN);
		setSharedPreferences(STUDENT_ID_KEY, studentID);
		setSharedPreferences(STUDENT_PIN_KEY, studentPIN);

		switchToFragment(mMenuFragment, false);
		return;
	}

	public void onResponseFinish(boolean valid) {
		showToast("Invalid Credentials");
		switchToFragment(mLoginFragment, false);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
