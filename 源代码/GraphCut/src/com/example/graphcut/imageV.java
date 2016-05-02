package com.example.graphcut;

import com.example.graphcut.GCApplication.State;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;


public class imageV extends ImageView{
	private Context context;
	
	public imageV(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	 public imageV(Context context,AttributeSet attrs) {
		  super(context,attrs);
		  // TODO Auto-generated constructor stub
		  this.context = context;
		 }
	 public imageV(Context context,AttributeSet attrs,int defStyle) {
		  super(context,attrs,defStyle);
		  // TODO Auto-generated constructor stub
		  this.context = context;
		 }
	public boolean onTouchEvent(MotionEvent event) {
		int rbId = MainActivity.radio.getCheckedRadioButtonId();
		if(rbId < 0)
			return true;
		RadioButton rb = (RadioButton) ((Activity) context).findViewById(rbId);
		Log.e("button",(String) rb.getText());
		if(rb.getText().equals("�ָ�����"))
			rbId = 0;
		else if (rb.getText().equals("ǰ����"))
				rbId = 1;
			 else if (rb.getText().equals("������"))
				      rbId = 2;
			      else if (rb.getText().equals("���ÿ���ǰ����"))
			    	       rbId = 3;
			           else if (rb.getText().equals("���ÿ��ܱ�����"))
			        	       rbId = 4;
		if(rbId < 0 || rbId > 4)
			return true;
		Log.e("rbid",""+rbId);
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			MainActivity.gc.ontouch(0, (int)event.getX(), (int)event.getY(), rbId);
			this.setImageBitmap(MainActivity.gc.showImage());
		}
		if (event.getAction() == MotionEvent.ACTION_UP){
			MainActivity.gc.ontouch(1, (int)event.getX(), (int)event.getY(), rbId);
			this.setImageBitmap(MainActivity.gc.showImage());
		}
		if (event.getAction() ==MotionEvent.ACTION_MOVE){
			MainActivity.gc.ontouch(2, (int)event.getX(), (int)event.getY(), rbId);
			this.setImageBitmap(MainActivity.gc.showImage());
		}
		return true;
	}
}
