package ca.uwallet.main.provider.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.util.Log;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.sync.Transaction;

public class ParseHelper{
	private static final String TAG = "Parser";
	
	private static final String INVALID_LOGIN_ID = "oneweb_message_invalid_login";
	private static final String TRANSACTION_TABLE_ID = "oneweb_financial_history_table";
	private static final String BALANCE_TABLE_ID = "oneweb_balance_information_table";
	private static final String TRANSACTION_TABLE_SELECTOR = "tr:gt(1)";
	private static final String BALANCE_TABLE_SELECTOR = "tr:gt(1)";
	
	private static final String COLUMN_TRANSACTION_AMOUNT = "oneweb_financial_history_td_amount";
	private static final String COLUMN_TRANSACTION_DATE = "oneweb_financial_history_td_date";
	private static final String COLUMN_TRANSACTION_TIME = "oneweb_financial_history_td_time";
	private static final String COLUMN_TRANSACTION_TYPE = "oneweb_financial_history_td_trantype";
	private static final String COLUMN_TRANSACTION_TERMINAL = "oneweb_financial_history_td_terminal";
	
	private static final String COLUMN_BALANCE_NAME = "oneweb_balance_information_td_name";
	private static final String COLUMN_BALANCE_AMOUNT = "oneweb_balance_information_td_amount";	
	
	private static final String DATE_FORMAT_STRING = "MM/dd/yyyyHH:mm:ss";
	@SuppressLint("SimpleDateFormat") // Parse the format given by the WatCard server. Not generating a String.
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	
	private ParseHelper(){}
	
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
	
	public static ArrayList<ContentProviderOperation> parseTransactionsToInsertOperations(Document doc) throws ParseException{
		Log.i(TAG, "Parsing transactions to ContentProviderOperation");
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		Element table = doc.getElementById(TRANSACTION_TABLE_ID);
		
		for (Element row : table.select(TRANSACTION_TABLE_SELECTOR)){
			Transaction t = parseTransactionFromRow(row);
			operations.add(ContentProviderOperation.newInsert(WatcardContract.Transaction.CONTENT_URI)
					.withValue(WatcardContract.Transaction.COLUMN_NAME_AMOUNT, t.getAmount())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_DATE, t.getDate())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_MONEY_TYPE, t.getType())
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
				parseTerminalToId(row.getElementById(COLUMN_TRANSACTION_TERMINAL).text()));
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
		s = s.trim().replaceAll("[^0-9-]", ""); // Remove whitespace and all non-numerical or "-" characters
		return Integer.parseInt(s);
	}
	
	public static long parseDateAndTime(String date, String time, DateFormat dateFormat) throws ParseException{
		return dateFormat.parse(date + time).getTime();
	}
	
	public static int parseTransactionType(String s){
		return Integer.parseInt(filterNonNumerical(s));
	}
	
	public static int parseTerminalToId(String s){
		String[] pieces = s.split("[()]");
		return Integer.parseInt(pieces[1]);
	}
	
	public static String parseTerminalToDescription(String s){
		String[] pieces = s.split("[()]");
		return pieces[2];
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