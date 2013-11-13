package ca.uwallet.main.sync;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import ca.uwallet.main.provider.WatcardContract;

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
	
	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.i(TAG, "Begin network synchronization");
		// Get login details
		AccountManager accountManager = AccountManager.get(getContext());
		String username = account.name;
		String password = accountManager.getPassword(account);
		
		ArrayList<Transaction> transactions;
		ArrayList<Integer> balances;
		Log.i(TAG, "Fetching from network");
		try{
			Document transactionDoc = getTransactionDocument(username, password);
			Document balanceDoc = getBalanceDocument(username, password);
			if (!Parser.isLoginSuccessful(transactionDoc) || !Parser.isLoginSuccessful(balanceDoc)){
				Log.e(TAG, "Log in unnsucessful");
				syncResult.stats.numAuthExceptions++;
				return;
			}
			transactions = Parser.parseTransactions(transactionDoc);
			balances = Parser.parseBalances(balanceDoc);
		} catch(IOException e){
			Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
		} catch(ParseException e){
			Log.e(TAG, "Error parsing data: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
		}
		// We have all transaction objects and balances
		try{
			updateTransactionData(transactions, syncResult);
			updateBalanceData(balances, syncResult);
		} catch (RemoteException e){
			Log.e(TAG, "Error updating database: " + e.toString());
			syncResult.databaseError = true;
			return;
		} catch (OperationApplicationException e){
			Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
		}
		Log.i(TAG, "Network synchronization complete");
	}
	
	/**
	 * POST a request for transaction history and returns the Jsoup document.
	 * @param username The WatCard number.
	 * @param password The WatCard pin.
	 * @return The document of transaction history.
	 * @throws IOException
	 */
	public static Document getTransactionDocument(String username, String password) throws IOException{
		Log.i(TAG, "Fetching transactions from network");
		Connection connection = Jsoup.connect(WAT_BASE_URL)
				.data(USERNAME_NAME, username)
				.data(PASSWORD_NAME, password)
				.data("PASS", "PASS")
				.data("STATUS", "HIST") // Specifiy transaction request
				.data("DBDATE", "01/01/0001") // Start date for transactions
				.data("DEDATE", "01/01/2111"); // End date for transactions
		return connection.post();
	}

	/**
	 * POST a request for balance history and returns the Jsoup document.
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public static Document getBalanceDocument(String username, String password) throws IOException{
		Log.i(TAG, "Fetching balances from network");
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
	 * Updates the transaction database with new transactions. For now it deletes all the old data and inserts the new.
	 * @param transactions
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 */
	public void updateTransactionData(ArrayList<Transaction> transactions, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
		Log.i(TAG, "Scheduling delete of old transactions");
		batch.add(ContentProviderOperation.newDelete(WatcardContract.Transaction.CONTENT_URI).build()); 
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new transactions");
		// Schedule transaction inserts
		for (Transaction t : transactions){
			batch.add(ContentProviderOperation.newInsert(WatcardContract.Transaction.CONTENT_URI)
					.withValue(WatcardContract.Transaction.COLUMN_NAME_AMOUNT, t.getAmount())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_DATE, t.getDate())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_TYPE, t.getType())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_TERMINAL, t.getTerminal())
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of transactions");
		mContentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
		mContentResolver.notifyChange(WatcardContract.BASE_CONTENT_URI, null, false);
	}
	
	/**
	 * Updates the balance data with new balances. Deletes the old balances and inserts new data.
	 * @param balances
	 * @param syncResult
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */
	public void updateBalanceData(ArrayList<Integer> balances, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
		Log.i(TAG, "Scheduling delete of old balances");
		batch.add(ContentProviderOperation.newDelete(WatcardContract.Balance.CONTENT_URI).build());
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new balances");
		for (Integer amount : balances){
			batch.add(ContentProviderOperation.newInsert(WatcardContract.Balance.CONTENT_URI)
					.withValue(WatcardContract.Balance.COLUMN_NAME_AMOUNT, amount)
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of balances");
		mContentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
		mContentResolver.notifyChange(WatcardContract.BASE_CONTENT_URI, null, false);
	}
	
	public static class Parser{
		private static final String TAG = "Parser";
		
		private static final String INVALID_LOGIN_ID = "oneweb_message_invalid_login";
		private static final String TRANSACTION_TABLE_ID = "oneweb_financial_history_table";
		private static final String BALANCE_TABLE_ID = "oneweb_balance_information_table";
		private static final String TRANSACTION_TABLE_SELECTOR = "tr:gt(1)";
		private static final String BALANCE_TABLE_SELECTOR = "tr:gt(1)";
		
		private static final String COLUMN_TRANSACTION_AMOUNT = "oneweb_financial_history_td_amount";
		private static final String COLUMN_TRANSACTION_DATE = "oneweb_financial_history_td_date";
		private static final String COLUMN_TRANSACTION_TIME = "oneweb_financial_history_td_time";
		private static final String COLUMN_TRANSACTION_TYPE = "oneweb_financial_history_td_transtype";
		private static final String COLUMN_TRANSACTION_TERMINAL = "oneweb_financial_history_td_terminal";
		
		private static final String COLUMN_BALANCE_NAME = "oneweb_balance_information_td_name";
		private static final String COLUMN_BALANCE_AMOUNT = "oneweb_balance_information_td_amount";	
		
		private static final String DATE_FORMAT_STRING = "MM/dd/yyyyHH:mm:ss";
		@SuppressLint("SimpleDateFormat") // Parse the format given by the WatCard server. Not generating a String.
		private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
		
		private Parser(){}
		
		/**
		 * Parse the transaction history document into a list of Transaction.
		 * @param doc The Jsoup transaction history.
		 * @return The parsed transactions.
		 * @throws IOException
		 * @throws ParseException
		 */
		public static ArrayList<Transaction> parseTransactions(Document doc) throws ParseException{
			Log.i(TAG, "Parsing transactions");
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			Element table = doc.getElementById(TRANSACTION_TABLE_ID);
				
			for (Element row : table.select(TRANSACTION_TABLE_SELECTOR)){
				transactions.add(parseTransactionFromRow(row));
			}
			
			return transactions;
		}
		
		private static ArrayList<ContentProviderOperation> parseTransactionsToInsertOperations(Document doc) throws ParseException{
			Log.i(TAG, "Parsing transactions to ContentProviderOperation");
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			Element table = doc.getElementById(TRANSACTION_TABLE_ID);
			
			for (Element row : table.select(TRANSACTION_TABLE_SELECTOR)){
				Transaction t = parseTransactionFromRow(row);
				operations.add(ContentProviderOperation.newInsert(WatcardContract.Transaction.CONTENT_URI)
						.withValue(WatcardContract.Transaction.COLUMN_NAME_AMOUNT, t.getAmount())
						.withValue(WatcardContract.Transaction.COLUMN_NAME_DATE, t.getDate())
						.withValue(WatcardContract.Transaction.COLUMN_NAME_TYPE, t.getType())
						.withValue(WatcardContract.Transaction.COLUMN_NAME_TERMINAL, t.getTerminal())
						.build());
			}
			
			return operations;
		}
		
		/**
		 * Parse the balance document into a list of doubles representing each balance.
		 * @param doc The Jsoup balance document.
		 * @return A list of balances.
		 * @throws IOException
		 */
		public static ArrayList<Integer> parseBalances(Document doc){
			Log.i(TAG, "Parsing balances");
			ArrayList<Integer> balances = new ArrayList<Integer>(); // TODO add expected number for performance
			Element table = doc.getElementById(BALANCE_TABLE_ID);
			
			for (Element row : table.select(BALANCE_TABLE_SELECTOR)){
				balances.add(parseBalanceFromRow(row));
			}
			return balances;
		}
		
		/**
		 * Returns the Transaction that the row describes.
		 * @param row The HTML table row.
		 * @return The transaction information.
		 * @throws ParseException
		 */
		public static Transaction parseTransactionFromRow(Element row) throws ParseException{
			return new Transaction(
					parseAmount(row.getElementById(COLUMN_TRANSACTION_AMOUNT).text()),
					parseDateAndTime(row.getElementById(COLUMN_TRANSACTION_DATE).text(),
									 row.getElementById(COLUMN_TRANSACTION_TIME).text(),
									 DATE_FORMAT),
					parseTransactionType(row.getElementById(COLUMN_TRANSACTION_TYPE).text()),
					parseTerminal(row.getElementById(COLUMN_TRANSACTION_TERMINAL).text()));
		}
		
		public static int parseBalanceFromRow(Element row){
			return parseAmount(row.getElementById(COLUMN_BALANCE_AMOUNT).text());
		}
		
		/**
		 * Parses money text to a string.
		 * @param s The string to parse.
		 * @return The monetary amount in cents.
		 */
		public static int parseAmount(String s){
			s = s.trim().replace("[^0-9-]", ""); // Remove whitespace and all non-numerical or "-" characters
			return Integer.parseInt(s);
		}
		
		public static long parseDateAndTime(String date, String time, DateFormat dateFormat) throws ParseException{
			return dateFormat.parse(date + time).getTime();
		}
		
		public static int parseTransactionType(String s){
			return Integer.parseInt(filterNonNumerical(s));
		}
		
		public static String parseTerminal(String s){
			return s;
		}
		
		public static String filterNonNumerical(String s)
		{
			return s.replaceAll("[^0-9]", "");
		}
		
		public static boolean isLoginSuccessful(Document doc){
			// Checks whether the invalid login message is displayed
			return doc.getElementById(INVALID_LOGIN_ID) == null;
		}
	}
}
