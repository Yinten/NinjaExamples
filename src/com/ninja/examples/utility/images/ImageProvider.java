
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.ninja.examples.R;
import com.ninja.examples.utility.net.APIRequest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.os.Process;
import android.util.Log;


public final class ImageProvider {

	private static Bitmap _loadingImage;
	private static Bitmap _comingSoonImage;

	private static final String TAG = "ImageProvider";
	private static final int PRESCRIBED_DOWNLOAD_THREAD_COUNT = 2;
	private static final boolean EXIT_THREADS_UPON_APPLICATION_EXIT = true;
	private static LinkedBlockingQueue<String> _downloadQueue = new LinkedBlockingQueue<String>();
	private static ConcurrentHashMap<String, ConcurrentLinkedQueue<ImageNotifyHandler>> _downloadSubscriptions = new ConcurrentHashMap<String, ConcurrentLinkedQueue<ImageNotifyHandler>>();
	private static Map<String, Bitmap> _imageCache = (Map<String, Bitmap>) Collections
			.synchronizedMap(new WeakHashMap<String, Bitmap>(30, .75F));

	private static ArrayList<String> _timedOutCache = new ArrayList<String>();
	private static Context _applicationContext = null;
	private static GetThread[] _subscribedThreads = null;

	private static String _currentActivity = "";

	public static void initialize(Context ctx) {

		if (_applicationContext == null) {
			_applicationContext = ctx.getApplicationContext();
			Log.i(TAG, "Image Provider: Initializing");
		}

		if (_loadingImage == null) {
			_loadingImage = BitmapFactory.decodeResource(_applicationContext.getResources(), R.drawable.loading);
		}

		if (_comingSoonImage == null) {
			_comingSoonImage = BitmapFactory.decodeResource(_applicationContext.getResources(),
					R.drawable.comingsoonoff);
		}

		if (_downloadSubscriptions != null) {
			_downloadSubscriptions.clear();
		}

		if (_subscribedThreads == null) {
			_subscribedThreads = new GetThread[PRESCRIBED_DOWNLOAD_THREAD_COUNT];
			TakeThreadStackAction(new ThreadStackAction() {
				public void onThreadStack(GetThread thread, int id) {
					if (thread == null) {
						if (_subscribedThreads != null) {
							if (id < _subscribedThreads.length) {
								String title = "Subscribed Thread: " + id;
								_subscribedThreads[id] = new GetThread(APIRequest.getClient(), title);
							}
						}
					}
				}
			});
		}

		/* Fire Up The Threads */
		TakeThreadStackAction(new ThreadStackAction() {
			public void onThreadStack(GetThread thread, int id) {

				if (thread != null) {

					if (!thread.isAlive()) {
						thread.start();
					}
				}
			}
		});
	}

	public static Bitmap getLoadingImage() {
		return _loadingImage;
	}

	public static Bitmap getComingSoonImage() {
		return _comingSoonImage;
	}

	public static Bitmap getBitmapImageOffThread(String url) {
		try {
			HttpGet httpRequest = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();

			HttpResponse response = (HttpResponse) httpClient.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream instream = bufHttpEntity.getContent();
			return BitmapFactory.decodeStream(instream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return BitmapFactory.decodeResource(_applicationContext.getResources(), R.drawable.comingsoonoff);
	}

	public static Bitmap getBitmap(String remoteImageUrl, ImageNotifyHandler handler, String activityName) {

		if (_currentActivity == null || _currentActivity.equalsIgnoreCase("")) {
			_currentActivity = activityName;
		} else if (!_currentActivity.equalsIgnoreCase(activityName)) {
			clear();
			_currentActivity = activityName;
		}

		Bitmap bitmap = _imageCache.get(remoteImageUrl);
		if (bitmap != null) {
			return bitmap;
		}

		ConcurrentLinkedQueue<ImageNotifyHandler> subscriptions = _downloadSubscriptions.get(remoteImageUrl);
		if (subscriptions != null) {
			if (handler == null || subscriptions.contains(handler)) {
				return null;
			} else {
				synchronized (_downloadSubscriptions) {
					subscriptions = _downloadSubscriptions.get(remoteImageUrl);
					if (subscriptions != null) {
						subscriptions.add(handler);
						return null;
					}
				}
			}
		}

		subscriptions = new ConcurrentLinkedQueue<ImageNotifyHandler>();
		if (handler != null) {
			subscriptions.add(handler);
		}
		_downloadSubscriptions.put(remoteImageUrl, subscriptions);

		try {
			_downloadQueue.put(remoteImageUrl);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * A thread that performs a GET.
	 */
	static class GetThread extends Thread {

		private final HttpClient httpClient;
		private final HttpContext context;

		public GetThread(HttpClient httpClient, String name) {
			super();
			this.httpClient = httpClient;
			this.context = new BasicHttpContext();
			this.setName(name);
			this.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
			this.setDaemon(EXIT_THREADS_UPON_APPLICATION_EXIT);
		}

		/**
		 * Executes the GetMethod and prints some status information.
		 */
		@Override
		public void run() {

			while (true) {
				String remoteImageUrl = null;

				// this will block until there is an item in the queue
				try {
					remoteImageUrl = _downloadQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace(); 
				}

				if (remoteImageUrl != null && remoteImageUrl.length() > 0) {

					Bitmap downloadedBitmap = downloadBitmap(remoteImageUrl);
					Log.i(TAG, "Downloading: " + remoteImageUrl);
					if (downloadedBitmap != null) {
						final ConcurrentLinkedQueue<ImageNotifyHandler> subscriptions = _downloadSubscriptions
								.remove(remoteImageUrl);

						if (downloadedBitmap != null) {
							cacheBitmap(remoteImageUrl, downloadedBitmap);

							if (_downloadSubscriptions != null) {
								synchronized (_downloadSubscriptions) {
									if (subscriptions != null) {

										for (final ImageNotifyHandler handler : subscriptions) {
											if (handler != null) {
												Log.i(TAG, "Finished Downloading: " + remoteImageUrl);
												final Message secondMsg = new Message();
												secondMsg.obj = remoteImageUrl;
												handler.sendMessage(secondMsg);
											}
										}
									}
								}
							}
						}
					}
				}

			}
		}

		private Bitmap downloadBitmap(String remoteImageUrl) {

			HttpGet httpget = new HttpGet(remoteImageUrl);
			Bitmap retVal = null;
			try {

				HttpResponse response = httpClient.execute(httpget, context);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					byte[] bytes = EntityUtils.toByteArray(entity);
					retVal = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				}
			} catch (Exception e) {
				e.printStackTrace(); 
				if (!_timedOutCache.contains(remoteImageUrl)) {
					_downloadQueue.add(remoteImageUrl);
					Log.i(TAG, "Image Provider Failed ---- adding to timedoutcache");
					_timedOutCache.add(remoteImageUrl);
				} else {
					Log.i(TAG, "Image Provider ---- failed twice");
				}
				if (httpget != null) {
					httpget.abort();
				}

			}

			return retVal;
		}
	}

	private static void cacheBitmap(String key, Bitmap b) {
		if (key != null && b != null) {
			_imageCache.put(key, b);
		}
	}

	public static void TakeThreadStackAction(ThreadStackAction action) {
		for (int i = 0; i < PRESCRIBED_DOWNLOAD_THREAD_COUNT; i++) {
			GetThread t = _subscribedThreads[i];
			action.onThreadStack(t, i);
		}
	}

	public interface ThreadStackAction {
		void onThreadStack(GetThread thread, int id);
	}

	public interface SingleImageUpdate {
		void onSingleImageUpdate(Bitmap b);
	}

	public static void clear() {

		if (_timedOutCache != null) {
			_timedOutCache.clear();
		}
		if (_downloadSubscriptions != null) {
			_downloadSubscriptions.clear();
		}
		if (_downloadQueue != null) {
			_downloadQueue.clear();
		}
		if (_imageCache != null) {
			_imageCache.clear();
		}
	}

	public static void setCurrentActivity(String currentActivity) {
		_currentActivity = currentActivity;
	}

	public static int getUnresolvedCount() {
		return _downloadQueue.size();
	}

	public static int getTimeoutCount() {
		return _timedOutCache.size();
	}

	public static Bitmap getBitmapFromCache(String url)
	{
		return _imageCache.get(url);
	}
}
