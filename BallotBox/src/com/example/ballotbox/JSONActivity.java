package com.example.ballotbox;

import java.io.*;
import java.net.*;

import android.support.v7.app.ActionBarActivity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;


public class JSONActivity extends ActionBarActivity {

	protected ImageView img;
	protected ProgressDialog simpleWaitDialog;
	protected String imgUrl;
	private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_json);
		img = (ImageView) findViewById(R.id.imagefromserver);
		imgUrl = "http://people.ucsc.edu/~mscorca/HW1/images/skybox-zpos.jpg";
		new DownloadImageTask().execute(imgUrl);
		
		
		((Button) findViewById(R.id.Button01))
		.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                // in onCreate or any event where your want the user to
                // select a file
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });
		
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
        		if(selectedImagePath == null)
        			Log.w("bitmap", "bitmap null");
        		else
        			Log.w("bitmap", imgUrl);
        		
                img.setImageBitmap(bitmap);
            }
        }

    }
	
    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
            // just some safety built in 
            if( uri == null ) {
                // TODO perform some logging or show user feedback
            	Log.w("Uri", "uri is null");
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if( cursor != null ){
                int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                Log.w("cursor", Integer.toString(column_index));
                return cursor.getString(column_index);
            }
            // this is our fallback here
            return uri.getPath();
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
	
	@Override
	public void onPause(){
		super.onPause();
		simpleWaitDialog.dismiss();
	}
}
