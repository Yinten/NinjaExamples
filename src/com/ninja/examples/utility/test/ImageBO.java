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
		
		images.add(new ImageBO("http://www.twistedcop.com/images/4.jpg", "Manic Focus")); 
		images.add(new ImageBO("http://www.twistedcop.com/images/1.jpg", "IS"));
		images.add(new ImageBO("http://www.twistedcop.com/images/2.jpg", "Cool"));
		images.add(new ImageBO("http://www.twistedcop.com/images/3.jpg", "http://www.manicfocus.com"));
		
		images.add(new ImageBO("http://www.localhost.com/notthere.png", "This image will not load."));
		images.add(new ImageBO("http://www.twistedcop.com/images/5.jpg", "The album is free.")); 
		images.add(new ImageBO("http://www.twistedcop.com/images/6.jpg", "Click the cover"));
		images.add(new ImageBO("http://www.twistedcop.com/images/2.jpg", "To Download"));
		images.add(new ImageBO("http://www.twistedcop.com/images/3.jpg", "Check it out"));
		
		
		images.add(new ImageBO("http://www.twistedcop.com/images/3.jpg", "Take not: My web server")); 
		images.add(new ImageBO("http://www.twistedcop.com/images/4.jpg", "Is very"));
		images.add(new ImageBO("http://www.twistedcop.com/images/2.jpg", "SLOW!!!!"));
		
		
		return images; 
	}
	

}
