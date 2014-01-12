package ca.uwallet.main.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for 
 * {@link ca.uwallet.main.provider.WatcardProvider}.
 * @author Gabriel
 *
 */
public final class WatcardContract {
	private WatcardContract(){}
	
	/**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "ca.uwallet.main.provider";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    /**
     * Path component for "transactions"-type resources..
     */
    public static final String PATH_TRANSACTION = "transaction";
    
    /**
     * Path component for balance
     */
    public static final String PATH_BALANCE = "balance";
    
    /**
     * Path component for terminal.
     */
    public static final String PATH_TERMINAL = "terminal";
    
    /**
     * Path component for category.
     */
    public static final String PATH_CATEGORY = "category";
    
    /**
     * Columns supported by "transaction" records.
     */
    public static final class Transaction implements BaseColumns {
        /**
         * MIME type for lists of transactions.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ca.uwallet.main.provider.transaction";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ca.uwallet.main.provider.transaction";

        /**
         * Fully qualified URI for "transaction" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSACTION).build();

        /**
         * Table name where records are stored for "transaction" resources.
         * Transaction is a sql reserved word
         */
        public static final String TABLE_NAME = "trans";
        
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
        public static final String COLUMN_NAME_MONEY_TYPE = "moneyType";
        /**
         * The transaction's terminal (vendor / description).
         */
        public static final String COLUMN_NAME_TERMINAL = "terminal";
    }
    
    /**
     * Gives info on how to access balance data.
     */
    public static final class Balance implements BaseColumns {
        /**
         * MIME type for lists of transactions.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ca.uwallet.main.provider.balance";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ca.uwallet.main.provider.balance";
        
        /**
         * Table name where records are stored for "balance" resources.
         */
        public static final String TABLE_NAME = "balance";

        /**
         * Fully qualified URI for "transaction" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BALANCE).build();
        
        /**
         * The balance amount for the category.
         */
        public static final String COLUMN_NAME_AMOUNT = "amount";
    }
    
    /**
     * Information on how to access Terminal data.
     */
    public static final class Terminal implements BaseColumns {
    	/**
         * MIME type for lists of terminals.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ca.uwallet.main.provider.terminal";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ca.uwallet.main.provider.terminal";
        
        /**
         * Table name where records are stored for "terminal" resources.
         */
        public static final String TABLE_NAME = "terminal";

        /**
         * Fully qualified URI for "terminal" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TERMINAL).build();
        
        /**
         * The description of the terminal.
         */
        public static final String COLUMN_NAME_TEXT = "terminalText";
        
        /**
         * Whether the description is from Watcard site, synced from the central list or by user.
         */
        public static final String COLUMN_NAME_TEXT_PRIORITY = "textPriority";
        
        /**
         * The category of the terminal.
         */
        public static final String COLUMN_NAME_CATEGORY = "category";
        
        /**
         * Whether the category is the default, synced or given by user.
         */
        public static final String COLUMN_NAME_CATEGORY_PRIORITY = "categoryPriority";
    }
    
    /**
     * Information on how to access Terminal data.
     */
    public static final class Category implements BaseColumns {
    	/**
         * MIME type for lists of terminals.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ca.uwallet.main.provider.category";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ca.uwallet.main.provider.category";
        
        /**
         * Table name where records are stored for "category" resources.
         */
        public static final String TABLE_NAME = "category";

        /**
         * Fully qualified URI for "category" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();
        
        /**
         * The description of the category.
         */
        public static final String COLUMN_NAME_CATEGORY_TEXT = "categoryText";
    }
}
