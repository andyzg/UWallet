package ca.uwallet.main;

import java.io.IOException;

import org.jsoup.nodes.Document;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.sync.SyncAdapter;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_USERNAME = "ca.uwallet.main.extra.USERNAME";
	
	public static final String EXTRA_IS_ADDING_NEW_ACCOUNT = "ca.uwallet.main.extra.IS_ADDING_NEW_ACCOUNT";
	
	private static final String CONTENT_AUTHORITY = WatcardContract.CONTENT_AUTHORITY;
	public static final String ACCOUNT_TYPE = "watcard.uwaterloo.ca";
	
	private static final String TAG = "AuthenticatorActivity";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for user and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
		if (mUsername != null){
			mUsernameView = (EditText) findViewById(R.id.username_input);
			mUsernameView.setText(mUsername);
		}

		mPasswordView = (EditText) findViewById(R.id.password_input);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void showToast(String text, int duration){
		Toast.makeText(this, text, duration).show();
	}
	
	
	public enum LoginTaskResult{
		SUCCESS, INVALID_CREDENTIALS, CONNECTION_ERROR
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, LoginTaskResult> {

		
		@Override
		protected LoginTaskResult doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			Document doc;
			try {
				doc = SyncAdapter.getBalanceDocument(mUsername, mPassword);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				return LoginTaskResult.CONNECTION_ERROR;
			}
			
			boolean loginSuccessful = SyncAdapter.Parser.isLoginSuccessful(doc);
			if (!loginSuccessful)
				return LoginTaskResult.INVALID_CREDENTIALS;

			Account account = new Account(mUsername, ACCOUNT_TYPE);
			AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
			boolean addSuccess = accountManager.addAccountExplicitly(account, mPassword, null);
			if (addSuccess){
				// Inform the system that this account supports sync
		        ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
		        // Inform the system that this account is eligible for auto sync when the network is up
		        ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
		        // Recommend a schedule for automatic synchronization. The system may modify this based
		        // on other scheduled syncs and network utilization.
		        //ContentResolver.addPeriodicSync(
		        //        account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
			} else{
				// Change password if we could not add the account (error or more likely already added)
				accountManager.setPassword(account, mPassword);
			}
			
			return LoginTaskResult.SUCCESS;
		}

		@Override
		protected void onPostExecute(final LoginTaskResult result) {
			mAuthTask = null;
			showProgress(false);

			switch(result){
			case SUCCESS:
				// Send back results to AccountManager
				final Intent intent = new Intent();
				intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
				intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
				intent.putExtra(AccountManager.KEY_PASSWORD, mPassword);
				setAccountAuthenticatorResult(intent.getExtras());
				setResult(RESULT_OK);
				finish();
				break;
			case INVALID_CREDENTIALS:
				// Indicate incorrect password and prompt
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				break;
			case CONNECTION_ERROR:
				// Inform that there is a connection error
				showToast(getString(R.string.error_connection_failed), Toast.LENGTH_LONG);
				break;
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
