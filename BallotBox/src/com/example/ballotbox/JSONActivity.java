package com.example.ballotbox;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;


public class JSONActivity extends Activity {
	
	//Used to set up imageView
	protected int imageCount;
	protected ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	protected ProgressDialog simpleWaitDialog;
	protected String imgUrl;	
	protected String url;
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
    private Context context;
    private MyAdapter imageList;
    private Button btnBack;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_json);
		
		url = "http://people.ucsc.edu/~mscorca/HW1/images/image0.jpg";
		imgUrl = url;
		
		btnBack = (Button) findViewById(R.id.backButton);
		
		/*
		 * Moves user back to CameraActivity
		 */
		btnBack.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
            	Intent intent = new Intent(context, CameraActivity.class);
            	startActivity(intent);
            }
        });
		
		new DownloadImageTask().execute(imgUrl);
		
	}
    
    /*
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
    
    
    //Downloads img from imgURL. Asynctask so the app does not stall while downloading
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
		
		/*begins downloading image from server*/
		@Override
        protected Bitmap doInBackground(String... param) {
			Bitmap bitmap = DownloadImage(param[0]);
			Log.w("TAG", param[0].toString());
    		//Log.w("bitmap", bitmap.toString());
			return bitmap;
		}
		
		/*mandatory override of Async task*/
		@Override
		protected void onPostExecute(Bitmap result){
			
			bitmaps.add(result);
			++imageCount;
			imgUrl = "http://people.ucsc.edu/~mscorca/HW1/images/image" + Integer.toString(imageCount) + ".jpg";
			
			try {
				/*checks if next img is valid url
				 * if not then it procedes with creating the listView using MyAdapter
				 * Else it continues with a new DownloadImageTask to grab next img
				 */
				if(!CheckHttpConnection(imgUrl)){
				    imageList = new MyAdapter(context, R.layout.list_vote_element, bitmaps);
				    ListView myListView = (ListView) findViewById(R.id.listView1);
				    myListView.setAdapter(imageList);
				    imageList.notifyDataSetChanged();
				}
				else{
					new DownloadImageTask().execute(imgUrl);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		/*creates input stream to connect and download image*/
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
		        		Log.w("Check", "HTTP connection not okay");
	
		        }
		        catch (Exception ex){            
		        }
		        return in;     
		    }
		
		/*tests connection to make sure no invalid urls are connected*/
		private Boolean CheckHttpConnection(String urlString) 
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
			                return true;                                
			            }
			            else{
			        		Log.w("Check", "HTTP connection not okay");
			        		return false;
			            }
		
			        }
			        catch (Exception ex){
			        	 throw new IOException(ex.toString());
			        } 
			    }
		
	    private Bitmap DownloadImage(String URL){        
	        Bitmap bitmap = null;
	        InputStream in = null;        
	        try {
	            in = OpenHttpConnection(URL);
	            bitmap = BitmapFactory.decodeStream(new FlushedInputStream(in));
	    		if (bitmap == null) { Log.w("", "Bitmap is null"); }
	            in.close();
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
	        return bitmap;                
	    }
	}
	
	/*code fixes bug with decodeStream*/
	static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
	
	
	//Code from Luca De Alfaro. Sets up list view with all downloaded pictures also adds voting buttons
		private class MyAdapter extends ArrayAdapter<Bitmap>{

			protected static final int LENGTH_SHORT = 3;
			int resource;
			Context context;
			
			public MyAdapter(Context _context, int _resource, List<Bitmap> items) {
				super(_context, _resource, items);
				resource = _resource;
				context = _context;
				this.context = _context;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LinearLayout newView;
				Bitmap image = getItem(position);
				ImageView img;
			    Button btnVotePos;			
				
				// Inflate a new view if necessary.
				if (convertView == null) {
					newView = new LinearLayout(getContext());
					String inflater = Context.LAYOUT_INFLATER_SERVICE;
					LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
					vi.inflate(resource,  newView, true);
				} else {
					newView = (LinearLayout) convertView;
				}
				
				
				//finds image view in list_vote_element
				img = (ImageView) newView.findViewById(R.id.imagefromserver);
				
				if(img == null){
					Log.w("NULL", "img is null");
				}
				
				//sets img to first downloaded
				img.setImageBitmap(image);
				
				//sets up voting button
				btnVotePos = (Button) newView.findViewById(R.id.votePosButton);
				
				/*
				 * Votes positively for displayed picture
				 */
				btnVotePos.setOnClickListener(new OnClickListener() {
					
					public void onClick(View view) {
						/*TODO*/
					}
				});
				
				return newView;
			}		
		}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	
	/*
	 * ensures back button goes back to last activity
	 */
	@Override
	public void onBackPressed() 
	{
	    Intent myIntent = new Intent(context, CameraActivity.class);
	    startActivity(myIntent);
	    super.onBackPressed();
	}
}
