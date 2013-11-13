package ca.uwallet.main.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Async task to do all of the network operations to connect
 * onto the Watcard server.
 * Fetches all of the data and returns an element containing
 * all of the transaction information
 * @author Andy
 *
 */
public class LoginTask extends AsyncTask<String, Void, Boolean>{
	
	public ResponseListener mListener;
	Document histDoc;
	Document statusDoc;
	
	double flexBalance;
	double mealBalance;
	
	ProgressDialog dialog;
	
	private static final String TAG = "LoginTask";
	
	public interface ResponseListener
	{
		public void onResponseFinish(Element histDoc, Element statusDoc, boolean valid);
	}

    public LoginTask(Context context,ResponseListener listener){
        this.mListener = listener;
        dialog = new ProgressDialog(context);
    }
	
	/**
	 * Run network operations in the back
	 */
	@Override
	protected Boolean doInBackground(String... string) {
	    	
        // Set all of the input values to connect to Transaction History
        List <NameValuePair> histList = new ArrayList <NameValuePair>();
        histList.add(new BasicNameValuePair("acnt_1", string[1]));
        histList.add(new BasicNameValuePair("acnt_2", string[2]));
        histList.add(new BasicNameValuePair("PASS", "PASS"));
        histList.add(new BasicNameValuePair("STATUS", "HIST"));
        histList.add(new BasicNameValuePair("DBDATE", "01/01/0001"));
        histList.add(new BasicNameValuePair("DEDATE", "01/01/2111"));
        
        // Set all of the input values to connect to status
        List <NameValuePair> statusList = new ArrayList <NameValuePair>();
        statusList.add(new BasicNameValuePair("acnt_1", string[1]));
        statusList.add(new BasicNameValuePair("acnt_2", string[2]));
        statusList.add(new BasicNameValuePair("FINDATAREP", "ON"));
        statusList.add(new BasicNameValuePair("MESSAGEREP", "ON"));
        statusList.add(new BasicNameValuePair("STATUS", "STATUS"));
        statusList.add(new BasicNameValuePair("watgopher_title", "WatCard Account Status"));
        statusList.add(new BasicNameValuePair("watgopher_regex", "/<hr>([\\s\\S]*)<hr>/;"));
        statusList.add(new BasicNameValuePair("watgopher_style", "onecard_regular"));
        
        histDoc = connection(histList);
        statusDoc = connection(statusList);
        if (histDoc.getElementById("oneweb_message_invalid_login") != null)
        {
        	Log.e(TAG, histDoc.getElementById("oneweb_message_invalid_login").toString());
        	return false;
        }
        return true;
	}
	

	/**
	 * When the network operation is done, this will be sent
	 */
	@Override
	protected void onPostExecute(Boolean a) {
		dialog.dismiss();
		if (!a) // If there there was an error in background
		{
			mListener.onResponseFinish(null, null, false);
			return;
		}
		// Getting the table with all of the information
        Element histTable = histDoc.getElementById("oneweb_financial_history_table");
        Element statusTable = statusDoc.getElementById("oneweb_balance_information_table");
		
		// Send back the table to LoginFragment for parsing
		mListener.onResponseFinish(histTable,statusTable,true);
		return;
	}

	// Preparation for the network operations
	@Override
	protected void onPreExecute() {
		// Exception if called after Activity exited
		dialog.setTitle("Loading");
		dialog.show();
	}

	// Any progress update needed
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}
	
	private Document connection(List<NameValuePair> list)
	{
		String endResult = null;
		Document doc = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        HttpPost httpost = new HttpPost("https://account.watcard.uwaterloo.ca/watgopher661.asp");
        
        // Obtain an HTTPResponse from the server
        try {
            httpost.setEntity(new UrlEncodedFormEntity(list, "UTF_8"));
            response = httpclient.execute(httpost);
                
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Handles all of the information. Without this, none of the information
        // will properly display
        BasicResponseHandler myHandler = new BasicResponseHandler();

        
        try {
            endResult = myHandler.handleResponse(response);
        } catch (HttpResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
        
        // Convert our string into a Document file
        // this makes obtaining information much easier
        doc = Jsoup.parseBodyFragment(endResult);
        
		return doc;
	}
}