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

import android.os.Handler;
import android.os.Message;

public class ImageNotifyHandler extends Handler {
	private final OnImageUpdateListener _onImageUpdate;

	/**
	 * Initializes a new instance of the ImageNotifyHandler class.
	 * 
	 * @param onImageUpdateListener
	 *            The onImageUpdate delegate to call back when this handler it
	 *            sent a message.
	 */
	public ImageNotifyHandler(OnImageUpdateListener onImageUpdateListener) {
		_onImageUpdate = onImageUpdateListener;
	}

	@Override
	public void handleMessage(Message msg) {
		_onImageUpdate.onImageUpdate((String) msg.obj);
	}

	public interface OnImageUpdateListener {
		void onImageUpdate(String remoteImageUrl);
	}

}