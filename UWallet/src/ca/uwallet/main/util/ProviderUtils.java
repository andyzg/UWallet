package ca.uwallet.main.util;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import ca.uwallet.main.provider.WatcardContract;

public class ProviderUtils {
	
	// Indices in array for each balance type
	public static final int[] MEAL_PLAN_INDICES = {0, 1, 2};
	public static final int[] FLEX_DOLLAR_INDICES = {3, 4, 5, 6};
	
	
	private ProviderUtils(){}
	
	/**
	 * Formats the balance into a String for display.
	 * @param cents The amount in cents.
	 * @return A string representation of the amount.
	 */
	public static String balanceToString(int cents){
		double dollars = cents / 100.;
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
		return currencyFormat.format(dollars);
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
}
