/*
 *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.android;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.view.adapter.UpdateListCursorAdapter;
import org.akvo.rsr.android.xml.Downloader;

import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.database.Cursor;

public class UpdateListActivity extends ListActivity {


	private static final String TAG = "UpdateListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
	private TextView updateCountLabel;
	private String projId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//find which project we will be showing updates for
		Bundle extras = getIntent().getExtras();
		projId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (projId == null) {
			projId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.PROJECT_ID_KEY) : null;
		}


		setContentView(R.layout.activity_update_list);

		updateCountLabel = (TextView) findViewById(R.id.updatecountlabel);
 
        //Create db
        ad = new RsrDbAdapter(this);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_project_list, menu);
		return true;
	}

	
	@Override
	public void onResume() {
		super.onResume();
		ad.open();
		getData();
	}
	
	
	@Override
	protected void onDestroy() {
		if (dataCursor != null) {
			try {
				dataCursor.close();
			} catch (Exception e) {

			}
		}
		if (ad != null) {
			ad.close();
		}
		super.onDestroy();
	}



	/**
	 * show all the projects in the database for this project
	 */
	private void getData() {
		try {
			if (dataCursor != null) {
				dataCursor.close();
			} 
		} catch(Exception e) {
			Log.w(TAG, "Could not close old cursor before reloading list",e);
		}
		dataCursor = ad.findAllUpdatesFor(projId);
		//Show count
		updateCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
		//Populate list view
		UpdateListCursorAdapter updates = new UpdateListCursorAdapter(this, dataCursor);
		setListAdapter(updates);

	}

	/**
	 * when a list item is clicked, get the id of the selected
	 * item and open one-project activity.
	 */
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		Intent i = new Intent(view.getContext(), ProjectDetailActivity.class);
		i.putExtra(ConstantUtil.UPDATE_ID_KEY, ((Long) view.getTag(R.id.update_id_tag)).toString());
		startActivity(i);
	}

	
	/*
	 * Start the service fetching new update data
	 */
	private void startGetProjectsService() {
		//TODO start a real service, register a listener for a completion intent
//		Intent i = new Intent(this, GetProjecDataService.class);
//		i.putExtra(SERVER_KEY, "http://test.akvo.org");
//		i.putExtra(URL_KEY, "/api/v1/project/?format=xml"); //get first 20 by default
//		getApplicationContext().startService(i);
		//meanwhile:
		Downloader dl = new Downloader();
		//TODO THIS MIGHT HANG, no timeout defined...
		dl.FetchUpdateList(this,"http://test.akvo.org","/api/v1/update/?format=xml&partnerships__organisation=42");//Akvo updates
		dl.FetchNewThumbnails(this, "http://test.akvo.org", Environment.getExternalStorageDirectory()+"/"+ConstantUtil.IMAGECACHE_DIR);	
	}



}