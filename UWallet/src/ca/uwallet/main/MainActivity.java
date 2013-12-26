package ca.uwallet.main;

import ca.uwallet.main.util.ProviderUtils;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;

/**
 * Activity that is launched from the launcher. We switch between screens using fragments.
 * Does not handle login.
 * 
 * @author Gabriel
 *
 */
public class MainActivity extends ActionBarActivity implements
	TransactionFragment.Listener, MenuFragment.Listener{
	
	private static final String TAG = "MainActivity";
	
	private static final int RC_LOGIN = 17; // Response code for LoginActivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Login if no account registered
		int numAccounts = LoginActivity.numAccounts(this);
		Log.v(TAG, numAccounts + " accounts registered");
		if (numAccounts == 0){
			doLogin();
		}
				
		switchToFragment(new MenuFragment(), false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data){
		Log.i(TAG, "onActivityResult, requestCode: " + requestCode + " responseCode: " + responseCode);
		switch(requestCode){
		// From LoginActivity
		case RC_LOGIN:
			Log.i(TAG, "Received login");
			// Close the app if the user aborted login. Can't do anything without login.
			if (responseCode != RESULT_OK){
				Log.i(TAG, "User cancelled login. Closing down.");
				finish();
			} else{
				Account account = data.getParcelableExtra(LoginActivity.KEY_ACCOUNT);
				ProviderUtils.onRefresh(account);
			}
			break;
		}
	}

	/**
	 * Convenience method. Switch to fragment and add to back stack.
	 * @param newFrag The fragment to switch to.
	 */
	private void switchToFragment(Fragment newFrag) {
		switchToFragment(newFrag, true);
	}

	/**
	 * Switch to fragment.
	 * @param newFrag The fragment to switch to.
	 * @param addToBackStack Whether the transaction should be added to back stack.
	 */
	private void switchToFragment(Fragment newFrag, boolean addToBackStack) {
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
		switchToFragment(new BalanceFragment());
	}

	@Override
	public void onTransactionsButtonClicked() {
		switchToFragment(new TransactionFragment());
	}

	@Override
	public void onLogOutButtonClicked() {		
		// Remove account from AccountManager
		removeAllAccounts();
		ProviderUtils.clearData(this);
		
		doLogin();
	}

	@Override
	public void onStatsButtonClicked() {
		switchToFragment(new StatsFragment(), true);
	}
	
	/**
	 * Returns the account manager for this activity.
	 * @return
	 */
	private AccountManager getAccountManager(){
		return (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
	}
	
	/**
	 * Removes all accounts from the account manager.
	 */
	private void removeAllAccounts(){
		AccountManager accountManager = getAccountManager();
		Account[] accounts = accountManager.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
		for (Account account : accounts){
			accountManager.removeAccount(account, null, null);
		}
	}
	
	/**
	 * Launches the LoginActivity.
	 */
	private void doLogin(){
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(LoginActivity.EXTRA_IS_ADDING_NEW_ACCOUNT, true);
		Log.v(TAG, "Starting login activity");
		startActivityForResult(intent, RC_LOGIN);
	}
	
	@Override
	public void onBackPressed(){
		// TODO MAKE FASTER
		// Slow returning from balances and transactions (transactions not yet with content) to menu
		// Fast returning from statistics to menu
		// When about button is removed, there's an increase in speed
		// DDMS indicates that there are 9 MB bitmap iamges
		// Possibly because of large drawables for ImageButton
		FragmentManager manager = getSupportFragmentManager();
		if (manager.getBackStackEntryCount() == 0)
			super.onBackPressed();
		else
			manager.popBackStack();
	}
}
