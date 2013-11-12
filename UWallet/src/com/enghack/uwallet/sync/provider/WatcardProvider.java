package com.enghack.uwallet.sync.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.enghack.uwallet.sync.util.SelectionBuilder;

public class WatcardProvider extends ContentProvider{

	private TransactionDatabaseHelper mDatabaseHelper = null;
	
	/** Database variables **/
	private static final int DATABASE_VERSION = 1; 
	
	// Create the UriMatcher
	private static final String AUTHORITY = WatcardContract.CONTENT_AUTHORITY;
	private static final UriMatcher sUriMatcher;
	private static final int ROUTE_TRANSACTIONS = 1,
							 ROUTE_TRANSACTIONS_ID = 2;
	static{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, WatcardContract.PATH_TRANSACTIONS , ROUTE_TRANSACTIONS);
		sUriMatcher.addURI("com.enghack.uwallet.provider", WatcardContract.PATH_TRANSACTIONS + "/#", ROUTE_TRANSACTIONS_ID);
	}
	
	@Override
	public boolean onCreate() {
		// Create database helper
		mDatabaseHelper = new TransactionDatabaseHelper(getContext());
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
            case ROUTE_TRANSACTIONS_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(WatcardContract.Transaction._ID + "=?", id);
            case ROUTE_TRANSACTIONS:
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
            case ROUTE_TRANSACTIONS_ID:
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
    * Update an etry in the database by URI.
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
           default:
               throw new UnsupportedOperationException("Unknown uri: " + uri);
       }
       Context ctx = getContext();
       assert ctx != null;
       ctx.getContentResolver().notifyChange(uri, null, false);
       return count;
   }
	
	public class TransactionDatabaseHelper extends SQLiteOpenHelper{
		
		private static final String DATABASE_NAME = "watcard.db";
		
		private static final String TYPE_TEXT = " TEXT";
		private static final String TYPE_INTEGER = " INTEGER";
		private static final String COMMA_SEP = ",";
		
		private static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + WatcardContract.Transaction.TABLE_NAME + "(" +
				"_id" + " INTEGER PRIMARY KEY, " +
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
