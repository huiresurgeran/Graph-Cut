package com.example.graphcut;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity implements OnClickListener{
	
	private int nowpic = 1;
	private imageV imageView;
	private Button button_read;
	private Button button_reset;
	private Button button_grabcut;
	public static RadioGroup radio;
	private static int RESULT_LOAD_IMAGE = 2;
	private String picturePath;
	private Bitmap bitmap;
	
	public static GCApplication gc;
	
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
        	Log.e("s","FUCK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e("s", "OpenCV loaded successfully!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            		BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            		gc = new GCApplication();
            		gc.setImage(bitmapDrawable.getBitmap());
            		imageView.setImageBitmap(gc.showImage());
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imageView = (imageV) findViewById(R.id.imageView1);
		button_read = (Button) findViewById(R.id.button3);
		button_reset = (Button) findViewById(R.id.button1);
		button_grabcut = (Button) findViewById(R.id.button2);
		radio = (RadioGroup) findViewById(R.id.radioGroup1);	
		
		button_read.setOnClickListener(this);
		button_reset.setOnClickListener(this);
		button_grabcut.setOnClickListener(this);
//		imageView.setOnTouchListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Button button = (Button) v;
		if (button.getId() == button_read.getId()){
			/*nowpic++;
			if (nowpic > 3) 
				nowpic = 1;
			switch (nowpic){
			case 1:
				imageView.setImageResource(R.drawable.pic1);
				break;
			case 2:
				imageView.setImageResource(R.drawable.pic2);
				break;
			case 3:
				imageView.setImageResource(R.drawable.pic3);
				break;
			}
			BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
    		gc.setImage(bitmapDrawable.getBitmap());
    		imageView.setImageBitmap(gc.showBitmap);*/
			Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        	i.setType( "image/*");
            startActivityForResult(i,RESULT_LOAD_IMAGE);
		}else
			if (button.getId() == button_reset.getId()){
				gc.reset();
				imageView.setImageBitmap(gc.showImage());
			}else
				if (button.getId() == button_grabcut.getId()){
					int iterCount = gc.getIterCount();
					int newIterCount = gc.nextIter();
					if (newIterCount > iterCount)
						imageView.setImageBitmap(gc.showImage());
					else
						Log.e("error","rect must be determined");
				}
	}
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	    {
	    	
	    	super.onActivityResult(requestCode, resultCode, data);
	    	
	    	
	    	if(requestCode==RESULT_LOAD_IMAGE&&resultCode==RESULT_OK&&null!=data)
	    	{
	    		Uri selectedImage=data.getData();
	    		String[] filePathColumn={MediaStore.Images.Media.DATA};
	    		
	    		Cursor cursor=getContentResolver().query(selectedImage,filePathColumn,null,null,null);
	    		cursor.moveToFirst();
	    		
	    		int columnIndex=cursor.getColumnIndex(filePathColumn[0]);
	    		picturePath=cursor.getString(columnIndex);
	    		cursor.close();
	            
	            bitmap=BitmapFactory.decodeFile(picturePath);
	            //imageView.setImageResource(bitmap);
	    		imageView.setImageBitmap(bitmap);
	    		
				BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
	    		gc.setImage(bitmapDrawable.getBitmap());
	    		imageView.setImageBitmap(gc.showBitmap);
	    		
	    	}
	    }

}
