package tru.kyle.classicgameanthology;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class ActionMenu 
{
	private static AlertDialog aboutDialog;
	private static final String ABOUT_TITLE = "About";
	private static final String ABOUT_MESSAGE = 
			"Classic Game Anthology " +
			"\nCopyright (C) Connor Kyle (2015) " +

			"\n\nThe Classic Game Anthology is free software: you can redistribute it and/or modify " +
			"it under the terms of the GNU General Public License as published by " +
			"the Free Software Foundation, either version 3 of the License, or " +
			"(at your option) any later version." +

			"\n\nTo read the full text of the GNU General Public License, see: " +
			"\n http://www.gnu.org/licenses/gpl.html" +
			
			"\n\nIf you wish to access the source code, send an e-mail to: " +
			"\n connorkyle81@gmail.com"
			;
	
	
	public static void displayAboutDialog(Activity context)
	{
		AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(context);
		aboutBuilder.setTitle(ABOUT_TITLE);
		aboutBuilder.setMessage(ABOUT_MESSAGE);
		aboutBuilder.setCancelable(true);
		aboutBuilder.setNegativeButton("Return", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				aboutDialog.dismiss();
			}
		});
		
		aboutDialog = aboutBuilder.create();
		aboutDialog.show();
	}
	
}
