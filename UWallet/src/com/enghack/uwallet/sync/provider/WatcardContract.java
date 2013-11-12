package com.enghack.uwallet.sync.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for 
 * {@link com.enghack.uwallet.sync.provider.WatcardProvider}.
 * @author Gabriel
 *
 */
public final class WatcardContract {
	private WatcardContract(){}
	
	/**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.enghack.uwallet.sync.watcardsyncadapter";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    /**
     * Path component for "transactions"-type resources..
     */
    public static final String PATH_TRANSACTIONS = "transaction";
    
    /**
     * Columns supported by "transaction" records.
     */
    public static final class Transaction implements BaseColumns {
    	// TODO change first two
        /**
         * MIME type for lists of transactions.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.enghack.transaction";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.enghack.transaction";

        /**
         * Fully qualified URI for "transaction" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTIONS).build();

        /**
         * Table name where records are stored for "transaction" resources.
         */
        public static final String TABLE_NAME = "transaction";
        
        /**
         * Transaction amount in cents.
         */
        public static final String COLUMN_NAME_AMOUNT = "amount";
        /**
         * Date transaction occured in Unix time.
         */
        public static final String COLUMN_NAME_DATE = "date";
        /**
         * The transaction type.
         */
        public static final String COLUMN_NAME_TYPE = "type";
        /**
         * The transaction's terminal (vendor / description).
         */
        public static final String COLUMN_NAME_TERMINAL = "terminal";
    }
}
