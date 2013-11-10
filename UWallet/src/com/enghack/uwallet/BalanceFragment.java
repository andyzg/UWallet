package com.enghack.uwallet;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Balance fragment displays watcard balances in a really simple format
 * By changing TextView values 
 * @author Seikun
 */

public class BalanceFragment extends Fragment implements OnClickListener {
	
	private static final String[] labels = {"Meal Plan", "Flex Dollars"};

	@SuppressWarnings("unused")
	private Listener mListener;

	public interface Listener {
	}

	public BalanceFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_balance, container,
				false);
		
		double mealBalance = MainActivity.getMealBalance();
		double flexBalance = MainActivity.getFlexBalance();
		String meal = getResources().getString(R.string.meal_plan) + mealBalance;
		String flex = getResources().getString(R.string.flex_dollars) + flexBalance;
		String total = getResources().getString(R.string.total)+ (mealBalance + flexBalance);
		((TextView)v.findViewById(R.id.meal_plan_label)).setText(meal);
		((TextView)v.findViewById(R.id.flex_dollars_label)).setText(flex);
		((TextView)v.findViewById(R.id.total_label)).setText(total);
		((LinearLayout)v).addView(getChartView());
		return v;
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
	
	private DefaultRenderer buildRenderer(int[] colors){
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
	        SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	        r.setColor(color);
	        renderer.addSeriesRenderer(r);
	    }
		renderer.setBackgroundColor(0x00000000);
	    return renderer;
	}
	
	private GraphicalView getChartView(){
		Context context = getActivity();
		CategorySeries series = new CategorySeries("Balance");
		
		double[] values = {MainActivity.getMealBalance(), MainActivity.getFlexBalance()};
		for (double v : values){
			series.add(v);
		}
		
		int[] colors = {0xFF00FF00, 0xFFFFFF00};
		DefaultRenderer renderer = buildRenderer(colors);
		return ChartFactory.getPieChartView(context, series, renderer);
	}

}
