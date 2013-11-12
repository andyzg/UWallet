package com.enghack.uwallet.sync;


/**
 * Data class representing a transaction.
 * @author Gabriel, Seikun
 */

public class Transaction {

	private int mId;
	private int mAmount;
	private long mDate;
	private int mTransactionType;
	private String mTerminal;
	
	private static final int DEFAULT_ID = 0;
	private static final int DEFAULT_AMOUNT = 0;
	private static final long DEFAULT_DATE = 0;
	private static final int DEFAULT_TRANSACTION_TYPE = 0;
	private static final String DEFAULT_TERMINAL = "";


	public Transaction() {
		this(DEFAULT_ID, DEFAULT_AMOUNT, DEFAULT_DATE, DEFAULT_TRANSACTION_TYPE, DEFAULT_TERMINAL);
	}

	/**
	 * Constructs a transaction object.
	 * @param id The id in the SQL table.
	 * @param amount The transaction amount in cents.
	 * @param date The date the transaction occured in Unix time.
	 * @param transactionType The transaction type (flex or meal plan, etc).
	 * @param terminal The transaction terminal (vendor / description).
	 */
	public Transaction(int id, int amount, long date, int transactionType, String terminal) {
		this.mId = id;
		this.mAmount = amount;
		this.mDate = date;
		this.mTransactionType = transactionType;
		this.mTerminal = terminal;
	}
	
	public Transaction(int amount, long date, int transactionType, String terminal) {
		this(DEFAULT_ID, amount, date, transactionType, terminal);
	}

	public int getID() {
		return this.mId;
	}

	public void setID(int id) {
		this.mId = id;
	}

	public double getAmount() {
		return this.mAmount;
	}

	public void setAmount(int amount) {
		this.mAmount = amount;
	}

	public long getDate() {
		return mDate;
	}

	public void setDate(long date) {
		this.mDate = date;
	}

	public int getTransactionType() {
		return mTransactionType;
	}

	public void setTransactionType(int transactionType) {
		this.mTransactionType = transactionType;
	}

	public String getTerminal() {
		return mTerminal;
	}

	public void setTerminal(String terminal) {
		this.mTerminal = terminal;
	}
}