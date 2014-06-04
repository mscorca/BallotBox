package com.example.ballotbox;

import java.io.*;
import java.net.*;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;


public class JSONActivity extends ActionBarActivity {

	protected ImageView img;
	protected ProgressDialog simpleWaitDialog;
	protected String imgUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_json);
		img = (ImageView) findViewById(R.id.imagefromserver);
		imgUrl = "http://people.ucsc.edu/~mscorca/HW1/images/skybox-zpos.jpg";
		new DownloadImageTask().execute(imgUrl);
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
		
		@Override
        protected Bitmap doInBackground(String... param) {
			Bitmap bitmap = DownloadImage(param[0]);  
    		Log.w("bitmap", bitmap.toString());
			return bitmap;
		}
		
        @Override
        protected void onPreExecute() {
            Log.i("Async-Example", "onPreExecute Called");
            simpleWaitDialog = ProgressDialog.show(JSONActivity.this, "Wait", "Downloading Image");
        }
		
		
		@Override
		protected void onPostExecute(Bitmap result){
			if(img == null) 
				Log.w("", "img is null");
			else 
				img.setImageBitmap(result);
			simpleWaitDialog.dismiss();

		}
		
		private InputStream OpenHttpConnection(String urlString) 
		    throws IOException {
	
				InputStream in = null;
	
		        int response = -1;
	
		        URL url = new URL(urlString); 
		        URLConnection conn = url.openConnection();
	
		        if (!(conn instanceof HttpURLConnection))                     
		            throw new IOException("Not an HTTP connection");
	
		        try{
		            HttpURLConnection httpConn = (HttpURLConnection) conn;
		            httpConn.setAllowUserInteraction(false);
		            httpConn.setInstanceFollowRedirects(true);
		            httpConn.setRequestMethod("GET");
		            httpConn.connect(); 
	
		            response = httpConn.getResponseCode();                 
		            if (response == HttpURLConnection.HTTP_OK) {
		                in = httpConn.getInputStream();                                 
		            }
		            else
		        		Log.w("", "HTTP connection not okay");
	
		        }
		        catch (Exception ex){
		            throw new IOException(ex.toString());            
		        }
		        return in;     
		    }
	    private Bitmap DownloadImage(String URL){        
	        Bitmap bitmap = null;
	        InputStream in = null;        
	        try {
	            in = OpenHttpConnection(URL);
	            bitmap = BitmapFactory.decodeStream(in);
	    		if (bitmap == null) { Log.w("", "Bitmap is null"); }
	            in.close();
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
	        return bitmap;                
	    }
	}
}
