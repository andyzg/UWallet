package com.enghack.uwallet;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.enghack.watcard.Transaction;

public class TransactionFragment extends Fragment implements OnClickListener {

	// To be changed depending on settings
	private static int dateFilter = 0;
	
	private Listener mListener;
	private TableLayout table;
	private ArrayList<Transaction> list;
	private int textSize=13;
	
	public interface Listener {
	}

	public TransactionFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list = MainActivity.getList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_transaction, container,
				true);
		ScrollView contain = (ScrollView)v.getRootView().findViewById(R.id.history_contain);
		
		final int width = getActivity().getWindowManager().getDefaultDisplay().getWidth(); 
		
		TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT, 
				TableLayout.LayoutParams.WRAP_CONTENT);
		table = new TableLayout(getActivity());
		table.setLayoutParams(tableParams);
		TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT);
		lparams.width = width/3;
		lparams.gravity = Gravity.LEFT;
		lparams.bottomMargin = 5;
		lparams.topMargin = 5;
		
		for (Transaction trans:list)
		{
			if (filterDate(trans))
			{	
				System.out.println("Printing "+trans.getAmount());
				TableRow row = new TableRow(getActivity());
				TextView price, date, terminal;
				price = new TextView(getActivity());
				date = new TextView(getActivity());
				terminal = new TextView(getActivity());
				
				price.setText(String.format("%.2f", trans.getAmount()));
				date.setText(trans.getDate());
				terminal.setText(trans.getTerminal());
				
				// terminal.setWidth(width/2);
				
				price.setTextSize(textSize);
				date.setTextSize(textSize);
				terminal.setTextSize(textSize);
				
				row.addView(price, lparams);
				row.addView(date, lparams);
				row.addView(terminal, lparams);
				table.addView(row, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
		}
		contain.addView(table);
		return (ScrollView)inflater.inflate(R.layout.fragment_transaction, container,
				false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}
	
	private static boolean filterDate(Transaction trans) {
		switch (dateFilter)
		{
			default:
				return true;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onClick(View view) {
	}
}
