package ca.uwallet.main.sync;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.jsoup.nodes.Document;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.provider.utils.ConnectionHelper;
import ca.uwallet.main.provider.utils.ParseHelper;

/**
 * Handle syncing of WatCard data.
 * @author Gabriel
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
	
	private ContentResolver mContentResolver = null;
	private static final String TAG = "SyncAdapter";
	
	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.i(TAG, "Begin network synchronization");
		// Get login details
		AccountManager accountManager = AccountManager.get(getContext());
		String username = account.name;
		String password = accountManager.getPassword(account);
		
		ArrayList<Transaction> transactions;
		ArrayList<Integer> balances;
		Log.i(TAG, "Fetching from network");
		try{
			Document transactionDoc = ConnectionHelper.getTransactionDocument(username, password);
			Document balanceDoc = ConnectionHelper.getBalanceDocument(username, password);
			if (!ParseHelper.isLoginSuccessful(transactionDoc) || !ParseHelper.isLoginSuccessful(balanceDoc)){
				Log.e(TAG, "Log in unnsucessful");
				syncResult.stats.numAuthExceptions++;
				return;
			}
			transactions = ParseHelper.parseTransactions(transactionDoc);
			balances = ParseHelper.parseBalances(balanceDoc);
		} catch(IOException e){
			Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
		} catch(ParseException e){
			Log.e(TAG, "Error parsing data: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
		}
		// We have all transaction objects and balances
		try{
			updateTransactionData(transactions, syncResult);
			updateBalanceData(balances, syncResult);
		} catch (RemoteException e){
			Log.e(TAG, "Error updating database: " + e.toString());
			syncResult.databaseError = true;
			return;
		} catch (OperationApplicationException e){
			Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
		}
		Log.i(TAG, "Network synchronization complete");
	}
	
	
	
	/**
	 * Updates the transaction database with new transactions. For now it deletes all the old data and inserts the new.
	 * @param transactions
	 * @throws OperationApplicationException 
	 * @throws RemoteException 
	 */
	public void updateTransactionData(ArrayList<Transaction> transactions, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
		Log.i(TAG, "Scheduling delete of old transactions");
		batch.add(ContentProviderOperation.newDelete(WatcardContract.Transaction.CONTENT_URI).build()); 
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new transactions");
		// Schedule transaction inserts
		for (Transaction t : transactions){
			batch.add(ContentProviderOperation.newInsert(WatcardContract.Transaction.CONTENT_URI)
					.withValue(WatcardContract.Transaction.COLUMN_NAME_AMOUNT, t.getAmount())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_DATE, t.getDate())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_MONEY_TYPE, t.getType())
					.withValue(WatcardContract.Transaction.COLUMN_NAME_TERMINAL, t.getTerminal())
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of transactions");
		mContentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
		mContentResolver.notifyChange(WatcardContract.BASE_CONTENT_URI, null, false);
	}
	
	/**
	 * Updates the balance data with new balances. Deletes the old balances and inserts new data.
	 * @param balances
	 * @param syncResult
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */
	public void updateBalanceData(ArrayList<Integer> balances, final SyncResult syncResult) throws RemoteException, OperationApplicationException{
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		
		// Delete the old data
		Log.i(TAG, "Scheduling delete of old balances");
		batch.add(ContentProviderOperation.newDelete(WatcardContract.Balance.CONTENT_URI).build());
		syncResult.stats.numDeletes++;
		
		Log.i(TAG, "Scheduling insert of new balances");
		for (Integer amount : balances){
			batch.add(ContentProviderOperation.newInsert(WatcardContract.Balance.CONTENT_URI)
					.withValue(WatcardContract.Balance.COLUMN_NAME_AMOUNT, amount)
					.build());
			syncResult.stats.numInserts++;
		}
		
		Log.i(TAG, "Applying batch update of balances");
		mContentResolver.applyBatch(WatcardContract.CONTENT_AUTHORITY, batch);
		mContentResolver.notifyChange(WatcardContract.BASE_CONTENT_URI, null, false);
	}
}
