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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
//import org.apache.commons.codec.binary.Base64;

import org.apache.http.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.VideoView;

public class CameraActivity extends Activity {

	// Activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	// directory name to store captured images and videos
	private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

	private Uri fileUri; // file url to store image/video

	private ImageView imgPreview;
	private VideoView videoPreview;
	private Button btnCapturePicture, btnRecordVideo, btnSendtoServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		videoPreview = (VideoView) findViewById(R.id.videoPreview);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		btnSendtoServer = (Button) findViewById(R.id.btnSendtoServer);
		btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);

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
		 * Record video button click event
		 */
		btnRecordVideo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// record video
				recordVideo();
			}
		});

		btnSendtoServer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// post to server
				new ImageUploadTask().execute();
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
	 * Capturing Camera Image will lauch camera app requrest image capture
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

	/*
	 * Recording video
	 */
	private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

		// set video quality
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
															// name

		// start the video capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
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
		} else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// video successfully recorded
				// preview the recorded video
				previewVideo();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled recording
				Toast.makeText(getApplicationContext(),
						"User cancelled video recording", Toast.LENGTH_SHORT)
						.show();
			} else {
				// failed to record video
				Toast.makeText(getApplicationContext(),
						"Sorry! Failed to record video", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/*
	 * Display image from a path to ImageView
	 */
	private void previewCapturedImage() {
		try {
			// hide video preview
			videoPreview.setVisibility(View.GONE);

			imgPreview.setVisibility(View.VISIBLE);

			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
					options);

			imgPreview.setImageBitmap(bitmap);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Previewing recorded video
	 */
	private void previewVideo() {
		try {
			// hide image preview
			imgPreview.setVisibility(View.GONE);

			videoPreview.setVisibility(View.VISIBLE);
			videoPreview.setVideoPath(fileUri.getPath());
			// start playing
			videoPreview.start();
		} catch (Exception e) {
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
	* 
	* 
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
}
