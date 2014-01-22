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
	public static final int[] MEAL_PLAN_INDICES = {0, 1, 2, 6};
	public static final int[] FLEX_DOLLAR_INDICES = {3, 4, 5, 7, 8, 9, 10, 11};
	public static final int MEAL_PLAN = 0,
							FLEX_DOLLAR = 1,
							TOTAL = 2;
	public static final int BALANCE_CURSOR_COUNT = 12;
	private static final int BALANCE_ARRAY_LENGTH = 3;
	
	private static DecimalFormat CURRENCY_FORMAT;
	private static DecimalFormat CURRENCY_FORMAT_NO_SYMBOL;
	private static DateFormat DATE_FORMAT;
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
	 * Formats the amount to a String including the currency symbol.
	 * @param amount The amount in cents.
	 * @return A string representation of the amount.
	 */
	public static String formatCurrency(int amount){
		return CURRENCY_FORMAT.format(amount/100.);
	}
	
	/**
	 * Formats the amount into a String without the currency symbol.
	 * @param amount
	 * @return
	 */
	public static String formatCurrencyNoSymbol(int amount){
		return CURRENCY_FORMAT_NO_SYMBOL.format(amount/100.);
	}
	
	/**
	 * Formats the amount into a String.
	 * @param amount The amount to format.
	 * @param showSymbol Whether the currency symbol should be shown.
	 * @return
	 */
	public static String formatCurrency(int amount, boolean showSymbol){
		return showSymbol ? formatCurrency(amount) : formatCurrencyNoSymbol(amount);
	}
	
	/**
	 * Formats the Unix time given into a date string.
	 * @param time The time to format.
	 * @return A string representation of the date.
	 */
	public static String formatDate(long time){
		return DATE_FORMAT.format(new Date(time));
	}
	
	/**
	 * Calculates the balances of all types.
	 * @param cursor
	 * @return
	 */
	public static int[] getBalanceAmounts(Cursor cursor){
		int[] amounts = new int[BALANCE_ARRAY_LENGTH];
		
		amounts[MEAL_PLAN] = getBalanceAmount(cursor, MEAL_PLAN);
		amounts[FLEX_DOLLAR] = getBalanceAmount(cursor, FLEX_DOLLAR);
		amounts[TOTAL] = amounts[MEAL_PLAN] + amounts[FLEX_DOLLAR]; // We make this assumption
		
		return amounts;
	}
	
	/**
	 * Calculates the balance amount for a given type. 
	 * @param cursor
	 * @param balanceType
	 * @return
	 */
	public static int getBalanceAmount(Cursor cursor, int balanceType){
		int columnIndex = cursor.getColumnIndex(WatcardContract.Balance.COLUMN_NAME_AMOUNT);
		int amount = 0;
		switch(balanceType){
		case MEAL_PLAN:
			for(int i : MEAL_PLAN_INDICES){
				if (cursor.moveToPosition(i))
					amount += cursor.getInt(columnIndex);
			}
			return amount;
		case FLEX_DOLLAR:
			for (int i : FLEX_DOLLAR_INDICES){
				if (cursor.moveToPosition(i))
					amount += cursor.getInt(columnIndex);
			}
			return amount;
		case TOTAL:
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
				amount += cursor.getInt(columnIndex);
			}
			return amount;
		default: throw new IllegalArgumentException("balanceType: " + balanceType + " is not valid.");
		}
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
