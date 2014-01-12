package ca.uwallet.main;



import ca.uwallet.main.util.ProviderUtils;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
	private String[] mMenuOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
	
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
		
		switchToFragment(new BalanceFragment(), false);
		
		
		// Sets up the drawer options
		mMenuOptions = getResources().getStringArray(R.array.options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.fragment_container);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuOptions));
        
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
     
        //Action bar icon tap to open drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        
     // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    	
	}
	
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	selectItem(position);
	    	
	        
	    }
	}
	
	
	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		switch(position) {
		case 0:
			switchToFragment (new BalanceFragment());
			break;
		case 1:
			switchToFragment (new TransactionFragment());
			break;
		case 2:
			switchToFragment (new StatsFragment());
			break;
		case 3:
			onLogOutButtonClicked();
			break;
		}

		
	    // Highlight the selected item, close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}

	/**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		return false;
        
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
			// Continue to home page
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
		transaction.replace(R.id.content_frame, newFrag);
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
		switchToFragment(new BalanceFragment(), false);
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
