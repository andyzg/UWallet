package ca.uwallet.main;

import java.util.ArrayList;

import org.jsoup.nodes.Element;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import ca.uwallet.main.login.HTMLParser;
import ca.uwallet.main.login.LoginTask;
import ca.uwallet.main.login.LoginTask.ResponseListener;
import ca.uwallet.main.watcard.Transaction;
import ca.uwallet.main.watcard.WatcardInfo;

/**
 * MainActivity, contains all fragment objects, listeners get methods for
 * ArrayList, and card balances
 * 
 * @author Andy, Seikun
 * 
 */

public class MainActivity extends ActionBarActivity implements ResponseListener,
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
	private String studentID = null;
	private String studentPIN = null;

	private static WatcardInfo person;
	private Context context = this;

	private static final String PREFERENCE_KEY = "Preferences";
	public static final String STUDENT_ID_KEY = "studentID";
	public static final String STUDENT_PIN_KEY = "studentPIN";
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// TODO Set up sync

		mBalanceFragment = new BalanceFragment();
		mTransactionFragment = new TransactionFragment();
		mStatsFragment = new StatsFragment();
		mAboutFragment = new AboutFragment();
		mLoginFragment = new LoginFragment();
		mMenuFragment = new MenuFragment();
		
		switchToFragment(mLoginFragment, false);
		tryLoginFromPreferences();
	}

	private void setSharedPreferences(String name, String value) {

		SharedPreferences preferences = getSharedPreferences(PREFERENCE_KEY,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}

	private String getSharedPreferences(String name) {
		SharedPreferences myPrefs = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
		return myPrefs.getString(name, null);
	}
	
	private void clearSharedPreferences(String name){
		SharedPreferences.Editor edit = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE).edit();
		edit.remove(name);
		edit.commit();
	}
	
	private void tryLoginFromPreferences(){
		studentID = getSharedPreferences(STUDENT_ID_KEY);
		studentPIN = getSharedPreferences(STUDENT_PIN_KEY);

		if (studentID != null && studentPIN != null) {
			executeLogin(URL, studentID, studentPIN);
		}
	}

	void switchToFragment(Fragment newFrag) {
		switchToFragment(newFrag, true);
	}

	void switchToFragment(Fragment newFrag, boolean addToBackStack) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, newFrag);
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
		studentID = null;
		studentPIN = null;
		clearSharedPreferences(STUDENT_ID_KEY);
		clearSharedPreferences(STUDENT_PIN_KEY);
		
		switchToFragment(mLoginFragment, false);
	}

	@Override
	public void onLogInButtonClicked(String id, String pin) {
	
		studentID = id;
		studentPIN = pin;

		executeLogin(URL, studentID, studentPIN);
	}

	private boolean executeLogin(String URL, String ID, String PIN) {
		if (!isNetworkAvailable()){
			showToast(getResources().getString(R.string.no_connection_message));
			return false;
		}
		LoginTask login = new LoginTask(context, this);
		login.mListener = this;
		
		login.execute(URL, ID, PIN);
				
		return true;
	}

	@Override
	public void onResponseFinish(Element histDoc, Element statusDoc, boolean valid) {
		if (histDoc == null)
			Log.e(TAG, "histDoc null");
		if (statusDoc == null)
			Log.e(TAG, "statusDoc null");
		if (!valid || histDoc == null || statusDoc == null) {
			showToast(getResources().getString(R.string.invalid_credentials_message));
			return;
		}
		
		

		person = new WatcardInfo(parser.parseHist(histDoc),
				parser.parseBalance(statusDoc, 2, 5),
				parser.parseBalance(statusDoc, 5, 8),
				parser.parseBalance(statusDoc, 8, 14), studentID, studentPIN);
		// person.printData(); // for testing purposes}
			
		setSharedPreferences(STUDENT_ID_KEY, studentID);
		setSharedPreferences(STUDENT_PIN_KEY, studentPIN);

		switchToFragment(mMenuFragment, false);
		return;
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	public static double getMealBalance() {
		return person.getMealBalance();
	}

	public static double getFlexBalance() {
		return person.getFlexBalance();
	}

	public boolean onTouchEvent(MotionEvent event) {
		// Dismiss keyboard by tapping
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
