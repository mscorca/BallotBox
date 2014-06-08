package com.example.ballotbox;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.BaseAdapter;

public abstract class BaseFriendElement {
	private Drawable icon;
	private String text1;
	private String text2;
	
	private BaseAdapter adapter;
	
	private int requestCode;
	
	public BaseFriendElement(Drawable icon, String text1, String text2, int requestCode) {
	    super();
	    this.icon = icon;
	    this.setText1(text1);
	    this.setText2(text2);
	    this.requestCode = requestCode;
	}
	
	public int getRequestCode(){
		return requestCode;
	}
	
	public void setAdapter(BaseAdapter adapter) {
	    this.adapter = adapter;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
		
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		
		this.text2 = text2;
		
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	
	protected abstract View.OnClickListener getOnClickListener();
}
