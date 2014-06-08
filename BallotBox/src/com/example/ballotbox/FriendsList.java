package com.example.ballotbox;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import com.google.gson.Gson;
import com.example.ballotbox.Friend;

import android.support.v4.app.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsList extends FragmentActivity{
	
	private static final String TAG = "FriendsList";

	private Session session;
	public ArrayList<Friend> fList = new ArrayList<Friend>();
	private Context context;
	private MyAdapter friendList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_friendslist);
	    
	    context = this;
	    
	    session = Session.getActiveSession();
	    
	    if(session == null){
	    	Log.d(TAG, "Session is null");
	    }
	    else{
	    	Log.d(TAG,"Active Session!");
	    }
	    
	    new Request(
	    	    session,
	    	    "/me/taggable_friends",
	    	    null,
	    	    HttpMethod.GET,
	    	    new Request.Callback() {
	    	        public void onCompleted(Response response) {
	    	            /* handle the result */
	    	        	parseUserFromFQLResponse(response);
	    	        }
	    	    }
	    	).executeAsync();    
	}
	
	public  void parseUserFromFQLResponse( Response response )
	{
	    try
	    {
	        GraphObject go  = response.getGraphObject();
	        JSONObject  jso = go.getInnerJSONObject();
	        JSONArray   arr = jso.getJSONArray( "data" );
	        
	        Log.d(TAG, "Length" + arr.length());

	        for ( int i = 0; i < ( arr.length() ); i++ )
	        {
	            JSONObject json_obj = arr.getJSONObject( i );
	            
	            Friend f = new Friend();
	            
	            //Log.d(TAG,json_obj.getString("name"));
	            

	            f.name = json_obj.getString( "name" );
	            
	            //Log.d(TAG, "f: " + f.name);
	            //Log.d(TAG, "fList: " + fList);
	            
	            fList.add(f);
	        }
	        
	        friendList = new MyAdapter(context, R.layout.list_element, fList);
	        ListView myListView = (ListView) findViewById(R.id.listView1);
	        myListView.setAdapter(friendList);
	        friendList.notifyDataSetChanged();
	    }
	    catch ( Throwable t )
	    {
	    	Log.d(TAG, "Failed parse");
	        t.printStackTrace();
	    }
	}
	
	private class MyAdapter extends ArrayAdapter<Friend>{

		protected static final int LENGTH_SHORT = 3;
		int resource;
		Context context;
		
		public MyAdapter(Context _context, int _resource, List<Friend> items) {
			super(_context, _resource, items);
			resource = _resource;
			context = _context;
			this.context = _context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout newView;
			Friend friend = getItem(position);
			
			Log.d(TAG, "f: " + friend.name);
			
			
			// Inflate a new view if necessary.
			if (convertView == null) {
				newView = new LinearLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
				vi.inflate(resource,  newView, true);
			} else {
				newView = (LinearLayout) convertView;
			}
			
			// Fill list View
			TextView tv = (TextView) newView.findViewById(R.id.listText);
			if(friend.name == null){
				tv.setText("No Title");
			}
			else{
				tv.setText(friend.name);
			}
			return newView;
		}		
	}
}
