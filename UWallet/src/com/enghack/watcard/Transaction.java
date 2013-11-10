package com.enghack.watcard;

import com.enghack.uwallet.login.HTMLParser;

/**
 * Template for each transaction object, standard get/set methods
 * @author Seikun
 */

public class Transaction {

	// private variables
	int _id;
	double _amount;
	int _date;
	int _trans_type;
	int _terminal;

	// Empty constructor
	public Transaction() {

	}

	// constructor
	public Transaction(int id, double amount, int date, int trans_type,
			int terminal) {
		this._id = id;
		this._amount = amount;
		this._date = date;
		this._trans_type = trans_type;
		this._terminal = terminal;
	}

	// constructor
	public Transaction(double amount, int date, int trans_type,
			int terminal) {
		this._amount = amount;
		this._date = date;
		this._trans_type = trans_type;
		this._terminal = terminal;
	}

	// getting id
	public int getID() {
		return this._id;
	}

	// setting id
	public void setID(int id) {
		this._id = id;
	}

	// getting amount
	public double getAmount() {
		return this._amount;
	}

	// setting amount
	public void setAmount(double amount) {
		this._amount = amount;
	}

	// getting date
	public String getDate() {
		return getMonth(this._date/1000000)+" "+(this._date/10000 - (this._date/1000000)*100)+" "+(this._date%100)+"'";
	}

	// setting date
	public void setDate(int date) {
		this._date = date;
	}

	// getting trans-type
	public String getTransType() {
		return HTMLParser.map.get(this._trans_type);
	}

	// setting trans-type
	public void setTransType(int trans_type) {
		this._trans_type = trans_type;
	}

	// getting terminal
	public String getTerminal() {
		return HTMLParser.map.get(this._terminal);
	}

	// setting terminal
	public void setTerminal(int terminal) {
		this._terminal = terminal;
	}
	
	private String getMonth(int i)
	{
		switch (i)
		{
		case 1:return "Jan";
		case 2:return "Feb";
		case 3:return "Mar";
		case 4:return "Apr";
		case 5:return "May";
		case 6:return "Jun";
		case 7:return "Jul";
		case 8:return "Aug";
		case 9:return "Sep";
		case 10:return "Oct";
		case 11:return "Nov";
		case 12:return "Dec";
		}
		return null;
	}
}