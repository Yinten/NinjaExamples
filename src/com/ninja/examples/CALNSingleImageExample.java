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
package com.ninja.examples;

import com.ninja.examples.utility.images.ImageNotifyHandler;
import com.ninja.examples.utility.images.ImageNotifyHandler.OnImageUpdateListener;
import com.ninja.examples.utility.images.ImageProvider;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class CALNSingleImageExample extends Activity {

	private ImageView _testImageView;
	private static final String TEST_IMAGE_URL = "http://www.twistedcop.com/images/3.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.image_single);

		_testImageView = (ImageView) findViewById(R.id.testImageView);

		_testImageView.setImageBitmap(ImageProvider.getLargeLoadingImage());
		ImageProvider.getBitmap(TEST_IMAGE_URL, new ImageNotifyHandler(
				new OnImageUpdateListener() {
					@Override
					public void onImageUpdate(String remoteImageUrl) {
						_testImageView.setImageBitmap(ImageProvider
								.getBitmapFromCache(remoteImageUrl));
					}
				}), this.getClass().getName());
	}

}
