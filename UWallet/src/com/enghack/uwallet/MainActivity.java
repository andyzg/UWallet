package com.enghack.uwallet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity implements BalanceFragment.Listener,
		TransactionFragment.Listener, AboutFragment.Listener,
		LoginFragment.Listener, MenuFragment.Listener {

	BalanceFragment mBalanceFragment = null;
	TransactionFragment mTransactionFragment = null;
	AboutFragment mAboutFragment = null;
	LoginFragment mLoginFragment = null;
	MenuFragment mMenuFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBalanceFragment = new BalanceFragment();
		mTransactionFragment = new TransactionFragment();
		mAboutFragment = new AboutFragment();
		mLoginFragment = new LoginFragment();
		mMenuFragment = new MenuFragment();

		switchToFragment(mMenuFragment);
	}
	
	void switchToFragment(Fragment newFrag){
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, newFrag).addToBackStack(null).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
