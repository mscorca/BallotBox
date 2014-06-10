package com.example.ballotbox;

import android.support.v4.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.*;

public class MainFragment extends Fragment {
	
	private static final String TAG = "LOGIN_ACTIVITY";
	
	//This object sets the Facebook Login button status
	private UiLifecycleHelper uiHelper;
	
	//Buttons contained in fragment
	public Button btnFriends;
	public Button btnVote;
	
	//Stores active Facebook session
	public Session session;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	    ViewGroup container, 
	    Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.activity_login, container, false);

	    session = Session.getActiveSession();
	   
	   /*adds facebook login widget - currently commented because the widget breaks the app*/
	   // LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	   // authButton.setReadPermissions("user_photos", "user_friends", "read_friendlists");
	   // authButton.setFragment(this);

	    btnFriends = (Button) view.findViewById(R.id.friendsbtn);
	    btnVote = (Button) view.findViewById(R.id.btnVote);
	    
	    if(session == null){
	    	btnFriends.setEnabled(false);
	    }
	    
	    /*
	     * Creates Friends list button transition
	     */
	    btnFriends.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(session != null){
					displayFriends();
				}
				else{
					Toast.makeText(getActivity(), "No active Facebook Session Available", Toast.LENGTH_SHORT).show();
				}
			}
		});
	    
	    /*
	     * Creates button to transition app to CameraActivity
	     */
	    btnVote.setOnClickListener(new View.OnClickListener() {
	    	
	    	@Override
	    	public void onClick(View v) {
	    		Intent intent = new Intent(getActivity(), CameraActivity.class);
	    		startActivity(intent);
	    	}
	    });
	    
	    return view;
   
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	//Used for facebook integration
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
   /*
	*debug function to test for state change, also turns off friends button if logged out or session is null
	*/
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	    	btnFriends.setEnabled(true);
	        Log.i(TAG, "Logged in...");
	    } else if (state.isClosed()) {
	    	btnFriends.setEnabled(false);
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	/*
	 * Moves application to FriendsListActivity
	 */
	private void displayFriends(){
		
		Intent intent = new Intent(getActivity(), FriendsList.class);
		startActivity(intent);
		//Log.d(TAG,"DisplayFriends()");
	}
}
