package ca.uwallet.main.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import ca.uwallet.main.provider.WatcardContract;

public class ProviderUtils {
	
	// Indices in array for each balance type
	public static final int[] MEAL_PLAN_INDICES = {0, 1, 2};
	public static final int[] FLEX_DOLLAR_INDICES = {3, 4, 5, 6};
	public static DecimalFormat CURRENCY_FORMAT;
	public static DecimalFormat CURRENCY_FORMAT_NO_SYMBOL;
	public static DateFormat DATE_FORMAT;
	static{
		CURRENCY_FORMAT = (DecimalFormat)NumberFormat.getCurrencyInstance(Locale.CANADA);
		CURRENCY_FORMAT.setNegativePrefix("$-");
		CURRENCY_FORMAT.setNegativeSuffix("");
		CURRENCY_FORMAT_NO_SYMBOL = (DecimalFormat)NumberFormat.getCurrencyInstance(Locale.CANADA);
		CURRENCY_FORMAT_NO_SYMBOL.setPositivePrefix("");
		CURRENCY_FORMAT_NO_SYMBOL.setNegativePrefix("-");
		CURRENCY_FORMAT_NO_SYMBOL.setNegativeSuffix("");
		
		DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
	}
	
	private ProviderUtils(){}
	
	/**
	 * Formats the balance into a String for display.
	 * @param amount The amount in cents.
	 * @return A string representation of the amount.
	 */
	public static String amountToString(int amount){
		return CURRENCY_FORMAT.format(amount/100.);
	}
	
	public static String formatCurrencyNoSymbol(int amount){
		return CURRENCY_FORMAT_NO_SYMBOL.format(amount/100.);
	}
	
	public static String formatDate(long time){
		return DATE_FORMAT.format(new Date(time));
	}
	
	/**
	 * Returns the meal plan balance given an array of balances.
	 * @param balanceAmounts
	 * @return The meal plan balance.
	 */
	public static int getMealBalance(int[] balanceAmounts){
		int sum = 0;
		for (int i : MEAL_PLAN_INDICES){
			sum += balanceAmounts[i];
		}
		return sum;
	}
	
	/**
	 * Return the flex dollar balance given an array of balances.
	 * @param balanceAmounts
	 * @return The flex dollar balance.
	 */
	public static int getFlexBalance(int[] balanceAmounts){
		int sum = 0;
		for (int i : FLEX_DOLLAR_INDICES){
			sum += balanceAmounts[i];
		}
		return sum;
	}
	
	/**
	 * Returns an array of the balance amounts. The index of the array is the _id of the amount.
	 * @param context
	 * @return The balance amounts.
	 */
	@Deprecated
	public static int[] getBalanceAmounts(Context context){
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(WatcardContract.Balance.CONTENT_URI, null, null, null, null);
		assert (cursor != null);
		
		int columnIndex = cursor.getColumnIndex(WatcardContract.Balance.COLUMN_NAME_AMOUNT);
		int[] amounts = new int[cursor.getCount()];
		int i = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			amounts[i] = cursor.getInt(columnIndex);
			i++;
		}
		cursor.close();
		return amounts;
	}
	
	public static int[] getBalanceAmounts(Cursor cursor){
		int columnIndex = cursor.getColumnIndex(WatcardContract.Balance.COLUMN_NAME_AMOUNT);
		int[] amounts = new int[cursor.getCount()];
		int i = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			amounts[i] = cursor.getInt(columnIndex);
			i++;
		}
		return amounts;
	}
	
	/**
	 * Clears all user data from the content provider.
	 * @param context
	 */
	public static void clearData(Context context){
		ContentResolver resolver = context.getContentResolver();
		resolver.delete(WatcardContract.Transaction.CONTENT_URI, null, null);
		resolver.delete(WatcardContract.Balance.CONTENT_URI, null, null);
		resolver.delete(WatcardContract.Terminal.CONTENT_URI, null, null);
		resolver.delete(WatcardContract.Category.CONTENT_URI, null, null);
	}
	
	/**
     * Perform a sync.
     */
    public static void onRefresh(Account account) {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(account, WatcardContract.CONTENT_AUTHORITY, settingsBundle);
    }
}
