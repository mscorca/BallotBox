package com.example.ballotbox;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
//import org.apache.commons.codec.binary.Base64;

import org.apache.http.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity {

	// Activity request codes
	public static final String SHARED_PREFERENCES = "File Keeper";
	public static final String PREFS_FILE = "File Location";
	private static final String TAG = "CAMERA_ACTIVITY";
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

	private Uri fileUri; // file url to store image/video

	private ImageView imgPreview;
	private Button btnCapturePicture, btnSendtoServer, btnGetPhotos;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;

		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		btnSendtoServer = (Button) findViewById(R.id.btnSendtoServer);
		btnGetPhotos = (Button) findViewById(R.id.btnGetPhotos);

		/*
		 * Capture image button click event
		 */
		btnCapturePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// capture picture
				captureImage();
			}
		});

		/*
		 * Sends current photo to server
		 */
		btnSendtoServer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// post to server
				new ImageUploadTask().execute();
			}
		});
		
		/*
		 * Launches activity to view current posted photos
		 */
		btnGetPhotos.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, JSONActivity.class);
				startActivity(intent);
			}
		});
		
		// Checking camera availability
		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(),
					"Sorry! Your device doesn't support camera",
					Toast.LENGTH_LONG).show();
			// will close the app if the device does't have camera
			finish();
		}
		
		fileUri = Uri.parse(getFile(context));
		previewCapturedImage();
	}

	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() {
		if (getApplicationContext().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/*
	 * Capturing Camera Image will launch camera app requrest image capture
	 */
	private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	/*
	 * Here we store the file url as it will be null after returning from camera
	 * app
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on scren orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}

	/**
	 * Receiving activity result method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// successfully captured the image
				// display it in image view
				setFile(fileUri.toString());
				previewCapturedImage();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
						"User cancelled image capture", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to capture image
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to capture image", Toast.LENGTH_SHORT)
						.show();
			}
		} 
	}

	/*
	 * Display image from a path to ImageView
	 */
	private void previewCapturedImage() {
		try {
			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;
			
			Log.d(TAG, fileUri.toString());

			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
					options);

			imgPreview.setImageBitmap(bitmap);
			imgPreview.setVisibility(View.VISIBLE);
			
		} catch (NullPointerException e) {
			Log.d(TAG, "Error in image preview");
			e.printStackTrace();
		}
	}
	
	/**
	 * ------------ Helper Methods ---------------------- 
	 * */

	/*
	 * Creating file uri to store image/video
	 */
	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/*
	 * returning image / video
	 */
	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
						+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	/**
	* The class connects with server and uploads the photo 
	*/
	class ImageUploadTask extends AsyncTask<Void, Void, String> {
	 private String webAddressToPost = "http://people.ucsc.edu/~mscorca/HW1/images";

	 // private ProgressDialog dialog;
	 private ProgressDialog dialog = new ProgressDialog(CameraActivity.this);

	 @Override
	 protected void onPreExecute() {
	  dialog.setMessage("Uploading...");
	  dialog.show();
	 }

	 @Override
	 protected String doInBackground(Void... params) {
	  try {
	   HttpClient httpClient = new DefaultHttpClient();
	   HttpContext localContext = new BasicHttpContext();
	   HttpPost httpPost = new HttpPost(webAddressToPost);

	   MultipartEntityBuilder entity = MultipartEntityBuilder.create();
	   entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	   
	   Bitmap bitmap = ((BitmapDrawable)imgPreview.getDrawable()).getBitmap();
	   
	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	   bitmap.compress(CompressFormat.JPEG, 100, bos);
	   byte[] data = bos.toByteArray();
	   String fileString = Base64.encodeToString(data, Base64.DEFAULT);

       File file = new File(fileString);
       FileBody fileBody = new FileBody(file);
	   
	   entity.addPart("uploaded", fileBody);
	   //entity.addPart("someOtherStringToSend", new ContentBody("your string here"));

	   httpPost.setEntity(entity.build());
	   HttpResponse response = httpClient.execute(httpPost,localContext);
	   BufferedReader reader = new BufferedReader(new InputStreamReader(
	     response.getEntity().getContent(), "UTF-8"));

	   String sResponse = reader.readLine();
	   return sResponse;
	  } catch (Exception e) {
	   // something went wrong. connection with the server error
	  }
	  return null;
	 }

	 @Override
	 protected void onPostExecute(String result) {
	  dialog.dismiss();
	  Toast.makeText(getApplicationContext(), "file uploaded",Toast.LENGTH_LONG).show();
	 }
	}
	
	/*
	 * Saves fileURI to shared prefs so picture preview always displays
	 */
	public void setFile(String secret){
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_FILE, secret);
		editor.commit();
	}
	
	//Retrieves stored URL from shared prefs
	public String getFile(Context context){
		SharedPreferences prefs = context.getSharedPreferences(CameraActivity.SHARED_PREFERENCES, 0);
		return prefs.getString(CameraActivity.PREFS_FILE, "");
	}
	
	/*
	 * ensures back button goes back to last activity
	 */
	@Override
	public void onBackPressed() 
	{
	    Intent myIntent = new Intent(context, LoginActivity.class);
	    startActivity(myIntent);
	    super.onBackPressed();
	}
}
