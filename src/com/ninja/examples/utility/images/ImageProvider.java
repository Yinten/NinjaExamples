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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.ninja.examples.R;
import com.ninja.examples.utility.Globals;
import com.ninja.examples.utility.net.APIRequest;

public final class ImageProvider {

	/**
	 * The first time this static class is called, it'll load up all it needs.
	 * If it is still in memory, it won't execute this method.
	 */
	static {
		ImageProvider.initialize();
	}

	private static final String TAG = "ImageProvider";

	/**
	 * Image to load if web service isn't complete, allowed to be Garbage
	 * collected if needed.
	 */
	private static Bitmap _loadingImage;

	/**
	 * Image to load if the image doesn't exist or bad URL, allowed to be
	 * Garbage collected if needed.
	 */
	private static Bitmap _comingSoonImage;

	/**
	 * Image to load in place of large loading images, allowed to be Garbage
	 * collected if needed.
	 */
	private static Bitmap _largeLoadingImage;

	/**
	 * Amount of threads for producer / consumer
	 */
	private static final int PRESCRIBED_DOWNLOAD_THREAD_COUNT = 2;

	/**
	 * Which type of threads to run
	 */
	private static final boolean EXIT_THREADS_UPON_APPLICATION_EXIT = true;

	/**
	 * A queue of URLs to be downloaded that blocks. While in a thread looping,
	 * it'll wait until it is fed to iterate.
	 */
	private static LinkedBlockingQueue<String> _downloadQueue = new LinkedBlockingQueue<String>();

	/**
	 * An activities subscription to this image download. The action will occur
	 * when the image is finished downloading.
	 */
	private static ConcurrentHashMap<String, ConcurrentLinkedQueue<ImageNotifyHandler>> _downloadSubscriptions = new ConcurrentHashMap<String, ConcurrentLinkedQueue<ImageNotifyHandler>>();

	/**
	 * A synchronized map that contains a WeakHashMap. To simplify, it means the
	 * garbage collector will clear these as it deems. They'll never be tied
	 * into another class instantiation.
	 * 
	 * The images won't de-load because they're inside of a ImageView, but when
	 * the ImageView is no longer on screen memory will quickly be retrieved.
	 */
	private static Map<String, Bitmap> _imageCache = (Map<String, Bitmap>) Collections
			.synchronizedMap(new WeakHashMap<String, Bitmap>(30, .75F));

	/**
	 * A list of images that timed out when downloading.
	 */
	private static HashSet<String> _timedOutCache = new HashSet<String>();

	/**
	 * Threads that will be spinning attempting to grab a URL from the list to
	 * download. If one is in the middle of downloading another will grab the
	 * next available URL and began the process.
	 */
	private static GetThread[] _subscribedThreads = null;

	/**
	 * Keeps track of which group or activity is loaded. To relieve the system
	 * even further and avoid running it thin, as an activity pauses or finishes
	 * the Image Cache manually unloads.
	 * 
	 * If the onResume is called, the images will reload.
	 */
	private static String _currentActivity = "";

	/**
	 * Should be called as the application starts. Initializes the
	 * ImageProvider.
	 */
	public static void initialize() {

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
								_subscribedThreads[id] = new GetThread(
										APIRequest.getClient(), title);
							}
						}
					}
				}
			});
		}

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

	/**
	 * An outside method can decide if this will be used, but cached as it may
	 * be used a lot. Other loading images can also be used.
	 * 
	 * @return default loading image.
	 */
	public static Bitmap getLoadingImage() {

		if (_loadingImage == null) {
			_loadingImage = BitmapFactory.decodeResource(Globals.getInstance()
					.getContext().getResources(), R.drawable.loading);
		}

		return _loadingImage;
	}

	/**
	 * An outside method can decide if this will be used, but cached as it may
	 * be used a lot. Other loading images can also be used.
	 * 
	 * @return large loading image.
	 */
	public static Bitmap getLargeLoadingImage() {

		if (_largeLoadingImage == null) {
			_largeLoadingImage = BitmapFactory.decodeResource(Globals
					.getInstance().getContext().getResources(),
					R.drawable.loading_big);
		}

		return _largeLoadingImage;
	}

	/**
	 * An outside method can decide if this will be used, but cached as it may
	 * be used a lot. Other placeholder images can also be used.
	 * 
	 * @return placeholder image.
	 */
	public static Bitmap getComingSoonImage() {

		if (_comingSoonImage == null) {
			_comingSoonImage = BitmapFactory.decodeResource(Globals
					.getInstance().getContext().getResources(),
					R.drawable.comingsoonoff);
		}

		return _comingSoonImage;
	}

	/**
	 * Queues a bitmap for download if it isn't already downloaded, the threads
	 * will fire up and download. If an image is downloaded it'll return. This
	 * takes a ImageNotify handler that'll execute when the image is finished
	 * downloading.
	 * 
	 * It also takes Activity name. If one activity is still actively
	 * downloading images and another attempts. The out dated activity will be
	 * cleared out and the new activity will take preference.
	 * 
	 * @param remoteImageUrl
	 *            - URL to download.
	 * @param handler
	 *            - Action to happen once URL is downloaded.
	 * @param activityName
	 *            - Group or Activity that is currently downloading.
	 * @return Bitmap that's downloaded.
	 */
	public static Bitmap getBitmap(String remoteImageUrl,
			ImageNotifyHandler handler, String activityName) {

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

		ConcurrentLinkedQueue<ImageNotifyHandler> subscriptions = _downloadSubscriptions
				.get(remoteImageUrl);
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
	 * A thread that performs a GET, in this case on an image a lot of these can
	 * be running async.
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
					if (downloadedBitmap == null) {
						downloadedBitmap = getComingSoonImage();
					}
					final ConcurrentLinkedQueue<ImageNotifyHandler> subscriptions = _downloadSubscriptions
							.remove(remoteImageUrl);

					if (downloadedBitmap != null) {
						cacheBitmap(remoteImageUrl, downloadedBitmap);

						if (_downloadSubscriptions != null) {
							synchronized (_downloadSubscriptions) {
								if (subscriptions != null) {

									for (final ImageNotifyHandler handler : subscriptions) {
										if (handler != null) {
											Log.i(TAG, "Finished Downloading: "
													+ remoteImageUrl);
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

		private Bitmap downloadBitmap(String remoteImageUrl) {

			HttpGet httpget = new HttpGet(remoteImageUrl);
			Bitmap retVal = null;
			try {

				HttpResponse response = httpClient.execute(httpget, context);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					byte[] bytes = EntityUtils.toByteArray(entity);
					retVal = BitmapFactory.decodeByteArray(bytes, 0,
							bytes.length);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (!_timedOutCache.contains(remoteImageUrl)) {
					_downloadQueue.add(remoteImageUrl);
					Log.i(TAG,
							"Image Provider Failed ---- adding to timedoutcache");
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

	/*
	 * cache Bitmap
	 */
	private static void cacheBitmap(String key, Bitmap b) {
		if (key != null && b != null) {
			_imageCache.put(key, b);
		}
	}

	/*
	 * Take an action on a large amount of threads.
	 */
	public static void TakeThreadStackAction(ThreadStackAction action) {
		for (int i = 0; i < PRESCRIBED_DOWNLOAD_THREAD_COUNT; i++) {
			GetThread t = _subscribedThreads[i];
			action.onThreadStack(t, i);
		}
	}

	/**
	 * Thread Action to be taken
	 */
	public interface ThreadStackAction {
		void onThreadStack(GetThread thread, int id);
	}

	/**
	 * clears image provider system, allowing it to free up memory and start
	 * from scratch.
	 */
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

	/**
	 * This is used by abstract classes to manage a Group or Activity of images.
	 * 
	 * @param currentActivity
	 */
	public static void setCurrentActivity(String currentActivity) {
		_currentActivity = currentActivity;
	}

	/**
	 * Number of images in the download queue that haven't started.
	 */
	public static int getUnresolvedCount() {
		return _downloadQueue.size();
	}

	/**
	 * Number of urls that timed out in the Group or Activity Set.
	 */
	public static int getTimeoutCount() {
		return _timedOutCache.size();
	}

	/**
	 * Get a bitmap that is sure to be in the cache.
	 */
	public static Bitmap getBitmapFromCache(String url) {
		return _imageCache.get(url);
	}
}
