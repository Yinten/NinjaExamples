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
package com.ninja.examples.utility.test;

import java.util.ArrayList;
import java.util.List;

public class ImageBO
{
	private String imageUrl;
	private String title;
	
	public ImageBO(String imageUrl, String titile)
	{
		super();
		this.imageUrl = imageUrl;
		this.title = titile;
	}
	
	public String getImageUrl()
	{
		return imageUrl;
	}
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String titile)
	{
		this.title = titile;
	}
	
	@Override
	public String toString()
	{
		return "ImageBO [imageUrl=" + imageUrl + ", titile=" + title + "]";
	}
	
	/**
	 * Generates test data for this object. 
	 * @return List<ImageBO>
	 */
	
	public static List<ImageBO> getTestData()
	{
		ArrayList<ImageBO> images = new ArrayList<ImageBO>(); 
		
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/rss.png", "A RSS Icon")); 
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/facebook.png", "A Facebook Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/googleplus.png", "A Google Plus Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/twitter.png", "A Twitter Icon"));
		
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/rss.png", "2 A RSS Icon")); 
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/facebook.png", "2 A Facebook Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/googleplus.png", "2 A Google Plus Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/twitter.png", "2 A Twitter Icon"));
		
		
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/rss.png", "2 A RSS Icon")); 
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/facebook.png", "2 A Facebook Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/googleplus.png", "2 A Google Plus Icon"));
		images.add(new ImageBO("http://www.ryangmattison.com/themes/Standard/img/social/twitter.png", "2 A Twitter Icon"));
		
		return images; 
	}
	

}
