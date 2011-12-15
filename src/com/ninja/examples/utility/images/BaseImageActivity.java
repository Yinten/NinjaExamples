/*
Copyright 2011 Ryan Mattison

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ninja.examples.utility.images;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ninja.examples.utility.images.ImageNotifyHandler.OnImageUpdateListener;

public abstract class BaseImageActivity extends Activity {

	protected static final String TAG = "BaseImageActivity";

	private ImageNotifyHandler _imageNotifyHandler;
	protected Context _context;
	protected LayoutInflater _loInflater;

	private boolean _allImageLoaded = false;

	/**
	 * Returns the ListView
	 */
	protected abstract ListView getListView();

	/**
	 * Return the Adapter
	 */
	protected abstract BaseAdapter getAdapter();

	/**
	 * Set up the image provider, generally the ImageNotifyHandler
	 */
	protected ImageNotifyHandler getNotifyHandler() {
		if (_imageNotifyHandler == null) {
			_imageNotifyHandler = new ImageNotifyHandler(
					new OnImageUpdateListener() {
						public void onImageUpdate(String remoteImageUrl) {
							if (!isFinishing()) {
								getAdapter().notifyDataSetChanged();
							}
						}
					});
		}

		return _imageNotifyHandler;
	}

	/**
	 * Returns the layoutinflater
	 */
	public LayoutInflater getLayoutInflater() {
		return _loInflater;
	}

	/**
	 * Gets the List of data
	 */
	protected abstract List<?> getList();

	/**
	 * In case all the images didn't load previously, we don't want to keep them
	 * in memory, or do web services to find them, but we do want them to load
	 * when the user comes back to the activity.
	 */
	protected void invalidate() {
		if (getAdapter() != null && getListView().getAdapter() != null) {
			getAdapter().notifyDataSetChanged();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!getAllImagesLoaded()) {
			Log.i(TAG,
					"Image Provider: Not all images loaded previously, invalidating views");
			invalidate();
		} else {
			Log.i(TAG,
					"Image Provider: must have loaded all visible images or it would have invalidated");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		setAllImagesLoaded();
		ImageProvider.clear();
	}

	private boolean getAllImagesLoaded() {
		return _allImageLoaded;
	}

	private void setAllImagesLoaded() {
		_allImageLoaded = (ImageProvider.getUnresolvedCount() == 0);
		Log.i(TAG, "Image Provider: Set All Images Loaded Too: "
				+ _allImageLoaded);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		_context = this;

		_loInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

}
