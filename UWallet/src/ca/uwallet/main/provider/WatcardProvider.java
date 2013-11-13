package ca.uwallet.main.provider;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import ca.uwallet.main.util.SelectionBuilder;

public class WatcardProvider extends ContentProvider{

	private TransactionDatabaseHelper mDatabaseHelper = null;
	
	// Balance Data
	private ArrayList<Integer> mBalanceList = null;
	private static final String[] BALANCE_COLUMN_NAMES = {WatcardContract.Balance._ID,
														  WatcardContract.Balance.COLUMN_NAME_AMOUNT};
	
	/** Database variables **/
	private static final int DATABASE_VERSION = 1; 
	
	private static final String PREFS_KEY_BALANCE = "WatBalance";
	
	// Create the UriMatcher
	private static final String AUTHORITY = WatcardContract.CONTENT_AUTHORITY;
	private static final UriMatcher sUriMatcher;
	private static final int ROUTE_TRANSACTIONS = 1,
							 ROUTE_TRANSACTIONS_ID = 2,
							 ROUTE_BALANCES = 3,
							 ROUTE_BALANCES_ID = 4;
	static{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTIONS , ROUTE_TRANSACTIONS);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTIONS + "/#", ROUTE_TRANSACTIONS_ID);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_BALANCES, ROUTE_BALANCES);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_BALANCES + "/#", ROUTE_BALANCES_ID);
	}
	
	@Override
	public boolean onCreate() {
		// Create database helper
		mDatabaseHelper = new TransactionDatabaseHelper(getContext());
		mBalanceList = getIntegerArrayPref(getContext(), PREFS_KEY_BALANCE);
		return true;
	}
	
	@Override
	/**
     * Determine the mime type for entries returned by a given URI.
     */
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)){
		case ROUTE_TRANSACTIONS:
			return WatcardContract.Transaction.CONTENT_TYPE;
		case ROUTE_TRANSACTIONS_ID:
			return WatcardContract.Transaction.CONTENT_ITEM_TYPE;
		case ROUTE_BALANCES:
			return WatcardContract.Balance.CONTENT_TYPE;
		case ROUTE_BALANCES_ID:
			return WatcardContract.Transaction.CONTENT_ITEM_TYPE;
		default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
	
	/**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all transactions (/transactions) and individual transactions by ID
     * (/transactions/{ID}).
     */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_TRANSACTIONS_ID:{
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(WatcardContract.Transaction._ID + "=?", id);
            }
            case ROUTE_TRANSACTIONS:{
                // Return all known entries.
                builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            }
            case ROUTE_BALANCES:{
            	// Return all balance entries
            	// Ignore sort order
            	MatrixCursor mc = new MatrixCursor(BALANCE_COLUMN_NAMES, mBalanceList.size());
            	for (int i = 0; i < mBalanceList.size(); i++){ // Populate cursor
            		Integer[] row = {i, mBalanceList.get(i)};
            		mc.addRow(row);
            	}
            	return mc;
            }
            case ROUTE_BALANCES_ID:{
            	int id = Integer.parseInt(uri.getLastPathSegment());
            	if (id < 0 || id >= mBalanceList.size()) // If id not in list
            		return null;
            	MatrixCursor mc = new MatrixCursor(BALANCE_COLUMN_NAMES, 1);
            	Integer[] row = {id, mBalanceList.get(id)};
            	mc.addRow(row);
            	return mc;
            }
            	
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}
	
	/**
     * Insert a new entry into the database.
     */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_TRANSACTIONS:
                long id = db.insertOrThrow(WatcardContract.Transaction.TABLE_NAME, null, values);
                result = Uri.parse(WatcardContract.Transaction.CONTENT_URI + "/" + id);
                break;
            case ROUTE_BALANCES:
            	mBalanceList.add(values.getAsInteger(WatcardContract.Balance.COLUMN_NAME_AMOUNT));
            	result = Uri.parse(WatcardContract.Balance.CONTENT_URI + "/" + (mBalanceList.size() - 1));
            	setIntegerArrayPref(getContext(), PREFS_KEY_BALANCE, mBalanceList);
            	break;
            case ROUTE_TRANSACTIONS_ID:
            case ROUTE_BALANCES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
	}
	
	/**
	 * Delete an entry by database by URI.
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_TRANSACTIONS:
                count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_TRANSACTIONS_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(WatcardContract.Transaction._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            case ROUTE_BALANCES:
            	count = mBalanceList.size();
            	mBalanceList = new ArrayList<Integer>();
            	setIntegerArrayPref(getContext(), PREFS_KEY_BALANCE, mBalanceList);
            	break;
            case ROUTE_BALANCES_ID:
            	mBalanceList.remove((int) Integer.parseInt(uri.getLastPathSegment()));
            	setIntegerArrayPref(getContext(), PREFS_KEY_BALANCE, mBalanceList);
            	count = 1;
            	break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
	}

	/**
    * Update an entry in the database by URI.
    */
   @Override
   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       SelectionBuilder builder = new SelectionBuilder();
       final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
       final int match = sUriMatcher.match(uri);
       int count;
       switch (match) {
           case ROUTE_TRANSACTIONS:
               count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(selection, selectionArgs)
                       .update(db, values);
               break;
           case ROUTE_TRANSACTIONS_ID:
               String id = uri.getLastPathSegment();
               count = builder.table(WatcardContract.Transaction.TABLE_NAME)
                       .where(WatcardContract.Transaction._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .update(db, values);
               break;
           case ROUTE_BALANCES_ID:
        	   int index = Integer.parseInt(uri.getLastPathSegment());
        	   mBalanceList.set(index, values.getAsInteger(WatcardContract.Balance.COLUMN_NAME_AMOUNT));
        	   count = 1;
        	   break;
           case ROUTE_BALANCES:
        	   throw new UnsupportedOperationException("Update not support on URI: " + uri);
           default:
               throw new UnsupportedOperationException("Unknown uri: " + uri);
       }
       Context ctx = getContext();
       assert ctx != null;
       ctx.getContentResolver().notifyChange(uri, null, false);
       return count;
   }
   
   /**
    * Saves an ArrayList of Integer to SharedPreferences.
    * @param context The context.
    * @param key The key to store the list in.
    * @param values The list to store.
    */
   public static void setIntegerArrayPref(Context context, String key, ArrayList<Integer> values) {
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    SharedPreferences.Editor editor = prefs.edit();
	    JSONArray a = new JSONArray();
	    for (int i = 0; i < values.size(); i++) {
	        a.put(values.get(i));
	    }
	    if (!values.isEmpty()) {
	        editor.putString(key, a.toString());
	    } else {
	        editor.putString(key, null);
	    }
	    editor.commit();
	}

   /**
    * Loads an ArrayList of Integer from SharedPreferences.
    * @param context The context.
    * @param key The key the list is stored in.
    * @return The list.
    */
	public static ArrayList<Integer> getIntegerArrayPref(Context context, String key) {
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    String json = prefs.getString(key, null);
	    ArrayList<Integer> list = new ArrayList<Integer>();
	    if (json != null) {
	        try {
	            JSONArray a = new JSONArray(json);
	            for (int i = 0; i < a.length(); i++) {
	                int url = a.optInt(i);
	                list.add(url);
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	    }
	    return list;
	}
	
	public class TransactionDatabaseHelper extends SQLiteOpenHelper{
		
		private static final String DATABASE_NAME = "watcard.db";
		
		private static final String TYPE_TEXT = " TEXT";
		private static final String TYPE_INTEGER = " INTEGER";
		private static final String COMMA_SEP = ",";
		
		private static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + WatcardContract.Transaction.TABLE_NAME + "(" +
				WatcardContract.Transaction._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
				WatcardContract.Transaction.COLUMN_NAME_AMOUNT + TYPE_TEXT + COMMA_SEP +
				WatcardContract.Transaction.COLUMN_NAME_DATE + TYPE_INTEGER + COMMA_SEP +
				WatcardContract.Transaction.COLUMN_NAME_TYPE + TYPE_INTEGER + COMMA_SEP + 
				WatcardContract.Transaction.COLUMN_NAME_TERMINAL + TYPE_TEXT + ")";
		
		private static final String SQL_DELETE_ENTRIES = 
				"DROP TABLE IF EXISTS " + WatcardContract.Transaction.TABLE_NAME;

		public TransactionDatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		/**
		 * Build the tables
		 */
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_ENTRIES);
		}

		@Override
		/**
		 * Called with new database version. Drop the entire table and rebuild.
		 */
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
	}
}
