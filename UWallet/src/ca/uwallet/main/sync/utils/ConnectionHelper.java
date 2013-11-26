package ca.uwallet.main.sync.utils;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

public class ConnectionHelper {
	private static final String TAG = "ConnectionHelper";
	private static final String WAT_BASE_URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private static final String USERNAME_NAME = "acnt_1";
	private static final String PASSWORD_NAME = "acnt_2";
	
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
}
