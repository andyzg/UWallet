package com.enghack.uwallet.sync;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Handle syncing of WatCard data.
 * @author Gabriel
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
	
	private ContentResolver mContentResolver = null;
	private static final String TAG = "SyncAdapter";
	
	private static final String WAT_BASE_URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private static final String USERNAME_NAME = "acnt_1";
	private static final String PASSWORD_NAME = "acnt_2";
	
	private static final String TRANSACTION_TABLE_ID = "oneweb_financial_history_table";
	private static final String BALANCE_TABLE_ID = "oneweb_balance_information_table";
	private static final String TRANSACTION_TABLE_SELECTOR = "tr:gt(1)";
	private static final String BALANCE_TABLE_SELECTOR = "tr:gt(1)";
	
	private static final String COLUMN_TRANSACTION_AMOUNT = "oneweb_financial_history_td_amount";
	private static final String COLUMN_TRANSACTION_DATE = "oneweb_financial_history_td_date";
	private static final String COLUMN_TRANSACTION_TIME = "oneweb_financial_history_td_time";
	private static final String COLUMN_TRANSACTION_TYPE = "oneweb_financial_history_td_transtype";
	private static final String COLUMN_TRANSACTION_TERMINAL = "oneweb_financial_history_td_terminal";
	
	private static final String DATE_FORMAT_STRING = "MM/dd/yyyyHH:mm:ss";
	@SuppressLint("SimpleDateFormat") // Parse the format given by the WatCard server. Not generating a String.
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	
	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
	}
	
	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 * @param allowParallelSyncs
	 */
	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs){
		super(context, autoInitialize, allowParallelSyncs);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		// Get login details
		AccountManager accountManager = AccountManager.get(getContext());
		String username = account.name;
		String password = accountManager.getPassword(account);
		
		ArrayList<Transaction> transactions;
		try{
			transactions = getTransactions(username, password);
		} catch(IOException e){
			// TODO sync failed
			Log.e(TAG, "IOException", e);
			return;
		} catch(ParseException e){
			// TODO sync failed
			Log.e(TAG, "ParseException", e);
			return;
		}
	}
	
	/**
	 * POST a request for transaction history and returns the Jsoup document.
	 * @param username The WatCard number.
	 * @param password The WatCard pin.
	 * @return The document of transaction history.
	 * @throws IOException
	 */
	private Document getTransactionDocument(String username, String password) throws IOException{
		return Jsoup.connect(WAT_BASE_URL)
				.data(USERNAME_NAME, username)
				.data(PASSWORD_NAME, password)
				.data("PASS", "PASS")
				.data("STATUS", "HIST") // Specifiy transaction request
				.data("DBDATE", "01/01/0001") // Start date for transactions
				.data("DEDATE", "01/01/2111") // End date for transactions
				.post();
	}

	/**
	 * POST 
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	private Document getBalanceDocument(String username, String password) throws IOException{
		return Jsoup.connect(WAT_BASE_URL)
				.data(USERNAME_NAME, username)
				.data(PASSWORD_NAME, password)
				.data("FINDATAREP", "ON")
				.data("MESSAGEREP", "ON")
				.data("STATUS", "STATUS") // Specify balance request
				.data("watgopher_title", "WatCard Account Status")
				.data("watgopher_regex", "/<hr>([\\s\\S]*)<hr>/;")
				.data("watgopher_style", "onecard_regular")
				.post();
	}
	
	/**
	 * Returns the transaction history for the given user.
	 * @param username The WatCard ID.
	 * @param password The WatCard PIN.
	 * @return The user's complete transaction history.
	 * @throws IOException
	 */
	private ArrayList<Transaction> getTransactions(String username, String password) throws IOException, ParseException{
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		
		Document doc = getTransactionDocument(username, password);
		Element table = doc.getElementById(TRANSACTION_TABLE_ID);
		
		
		for (Element row : table.select(TRANSACTION_TABLE_SELECTOR)){
			transactions.add(parseTransactionFromRow(row));
		}
		
		return transactions;
	}
	
	private Transaction parseTransactionFromRow(Element row) throws ParseException{
		return new Transaction(
				parseAmount(row.getElementById(COLUMN_TRANSACTION_AMOUNT).text()),
				parseDateAndTime(row.getElementById(COLUMN_TRANSACTION_DATE).text(),
								 row.getElementById(COLUMN_TRANSACTION_TIME).text(),
								 DATE_FORMAT),
				parseTransactionType(row.getElementById(COLUMN_TRANSACTION_TYPE).text()),
				parseTerminal(row.getElementById(COLUMN_TRANSACTION_TERMINAL).text()));
	}
	
	private int parseAmount(String s){
		s = s.trim().replace(".", "");
		return Integer.parseInt(s);
	}
	
	private long parseDateAndTime(String date, String time, DateFormat dateFormat) throws ParseException{
		return dateFormat.parse(date + time).getTime();
	}
	
	private int parseTransactionType(String s){
		return Integer.parseInt(filterNonNumerical(s));
	}
	
	private String parseTerminal(String s){
		return s;
	}
	
	private String filterNonNumerical(String s)
	{
		return s.replaceAll("[^0-9]", "");
	}
}
