package ca.uwallet.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;

/**
 * MainActivity, contains all fragment objects, listeners get methods for
 * ArrayList, and card balances
 * 
 * @author Andy, Seikun
 * 
 */

public class MainActivity extends ActionBarActivity implements
		BalanceFragment.Listener, TransactionFragment.Listener, MenuFragment.Listener, AboutFragment.Listener {

	private BalanceFragment mBalanceFragment = null;
	private TransactionFragment mTransactionFragment = null;
	private StatsFragment mStatsFragment = null;
	private AboutFragment mAboutFragment = null;
	private MenuFragment mMenuFragment = null;
	
	private static final String TAG = "MainActivity";
	
	private static final int RC_LOGIN = 17; // Response code for LoginActivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		int numAccounts = LoginActivity.numAccounts(this);
		Log.v(TAG, numAccounts + " accounts registered");
		if (numAccounts == 0){
			doLogin();
		}

		mBalanceFragment = new BalanceFragment();
		mTransactionFragment = new TransactionFragment();
		mStatsFragment = new StatsFragment();
		mAboutFragment = new AboutFragment();
		mMenuFragment = new MenuFragment();
		
		switchToFragment(mMenuFragment, false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data){
		Log.i(TAG, "onActivityResult, requestCode: " + requestCode + " responseCode: " + responseCode);
		switch(requestCode){
		case RC_LOGIN:
			Log.i(TAG, "Received login");
			if (responseCode != RESULT_OK)
				// Close the app unless the user logged in
				Log.i(TAG, "User cancelled login. Closing down.");
				finish();
			break;
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
		// Remove account from AccountManager
		removeAllAccounts();
		
		doLogin();
	}

	@Override
	public void onStatsButtonClicked() {
		switchToFragment(mStatsFragment, true);
	}
	
	private AccountManager getAccountManager(){
		return (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
	}
	
	private void removeAllAccounts(){
		AccountManager accountManager = getAccountManager();
		Account[] accounts = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
		for (Account account : accounts){
			accountManager.removeAccount(account, null, null);
		}
	}
	
	private void doLogin(){
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(LoginActivity.EXTRA_IS_ADDING_NEW_ACCOUNT, true);
		Log.v(TAG, "Starting login activity");
		startActivityForResult(intent, RC_LOGIN);
	}
	
	@Override
	public void onBackPressed(){
		getSupportFragmentManager().popBackStack();
	}
}
