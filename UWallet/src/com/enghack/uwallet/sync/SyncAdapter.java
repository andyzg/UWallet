package com.enghack.uwallet.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class SyncAdapter extends AbstractThreadedSyncAdapter{
	
	private ContentResolver mContentResolver = null;

	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
	}
	
	/**
	 * Set up sync adapter.
	 * @param context
	 * @param autoInitialize
	 * @param allowParallelSyncs
	 */
	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs){
		super(context, autoInitialize, allowParallelSyncs);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		// TODO Auto-generated method stub
		
	}

}
