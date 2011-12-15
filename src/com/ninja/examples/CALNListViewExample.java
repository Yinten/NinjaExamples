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

import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ninja.examples.R;
import com.ninja.examples.utility.images.BaseImageActivity;
import com.ninja.examples.utility.images.ImageProvider;
import com.ninja.examples.utility.test.ImageBO;

public class CALNListViewExample extends BaseImageActivity
{

	private List<ImageBO> _images;
	private TestImageAdapter _imageAdapter;
	private ListView _imageListView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.image_list);

		_imageListView = (ListView) findViewById(R.id.imageListView);
		_imageAdapter = new TestImageAdapter();
		_images = ImageBO.getTestData();

		getListView().setAdapter(getAdapter());
	}

	public class TestImageAdapter extends BaseAdapter
	{
		public class ViewHolder
		{
			ImageView imageRowImage;
			TextView imageRowTitle;
		}

		public int getCount()
		{

			return getList().size();
		}

		public Object getItem(int position)
		{
			return getList().get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{

			ViewHolder viewHolder;
			if (convertView == null || convertView.getTag() == null)
			{
				viewHolder = new ViewHolder();

				convertView = (View) getLayoutInflater().inflate(
						R.layout.image_list_row, null);

				viewHolder.imageRowImage = (ImageView) convertView
						.findViewById(R.id.imageRowImage);
				viewHolder.imageRowTitle = (TextView) convertView
						.findViewById(R.id.imageRowTitle);

				convertView.setTag(viewHolder);
			} else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Bitmap bitmap = solveBitmapCrisis(position);

			viewHolder.imageRowImage.setImageBitmap(bitmap);
			viewHolder.imageRowTitle
					.setText(getList().get(position).getTitle());

			return convertView;
		}

		/** 
		 * if its already downloaded it'll return the bitmap
		 * if it isn't yet downloaded it'll queue the bitmap up to be downloaded, 
		 * if there isn't a proper url it'll return a coming soon bitmap
		 * @param position
		 * @return
		 */
		private Bitmap solveBitmapCrisis(int position)
		{
			Bitmap bitmap;
			if (getList().get(position).getImageUrl() != null
					&& !getList().get(position).getImageUrl().equals(""))
			{
				bitmap = ImageProvider.getBitmap(getList().get(position)
						.getImageUrl(), getNotifyHandler(), TAG);
			} else
			{
				bitmap = ImageProvider.getComingSoonImage();
			}
			if (bitmap == null)
			{
				bitmap = ImageProvider.getLoadingImage();
			}
			return bitmap;
		}
	}

	@Override
	protected ListView getListView()
	{
		return _imageListView;
	}

	@Override
	protected BaseAdapter getAdapter()
	{
		return _imageAdapter;
	}

	@Override
	protected List<ImageBO> getList()
	{
		return _images;
	}
}
