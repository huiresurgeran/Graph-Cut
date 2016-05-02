package com.example.graphcut;

import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;


class GCApplication
{
public enum State{ NOT_SET , IN_PROCESS , SET };
public static final int radius = 5;
public static final int thickness = -1;

final Scalar RED = new Scalar(255,0,0);
final Scalar PINK = new Scalar(230,130,255);
final Scalar BLUE = new Scalar(0,0,255);
final Scalar LIGHTBLUE = new Scalar(160,255,255);
final Scalar GREEN = new Scalar(0,255,0);

Mat image;
Mat mask;
Mat bgdModel = null, fgdModel = null;
Mat temp, res, binMask;
Bitmap showBitmap;

State rectState, lblsState, prLblsState;
public boolean isInitialized;

Rect rect = new Rect();
Vector<Point> fgdPxls = new Vector<Point>();
Vector<Point> bgdPxls = new Vector<Point>();
Vector<Point> prFgdPxls = new Vector<Point>();
Vector<Point> prBgdPxls = new Vector<Point>();
int iterCount;

public void reset(){
    if( !mask.empty() )
        mask.setTo(Scalar.all(Imgproc.GC_BGD));
    bgdPxls.clear(); fgdPxls.clear();
    prBgdPxls.clear();  prFgdPxls.clear();

    isInitialized = false;
    rectState = State.NOT_SET;
    lblsState = State.NOT_SET;
    prLblsState = State.NOT_SET;
    iterCount = 0;
}

public void setImage(Bitmap bitmap)
{
	if (bitmap == null)
		return;
	Log.e("bit",bitmap.getWidth()+" "+bitmap.getHeight());
	Mat tempimage = new Mat();
	
	Utils.bitmapToMat(bitmap, tempimage);
	Mat image = new Mat();
	Imgproc.cvtColor(tempimage, image, Imgproc.COLOR_RGBA2RGB);
    this.image = image;
    mask = new Mat(image.size(), CvType.CV_8UC1);
    mask.setTo(Scalar.all(Imgproc.GC_BGD));
    temp = new Mat(image.size(), CvType.CV_8UC1);
    temp.setTo(Scalar.all(1));
//    bgdModel = new Mat(1, 13*5, CvType.CV_64FC1);
//    fgdModel = new Mat(1, 13*5, CvType.CV_64FC1);
    bgdModel = new Mat(); fgdModel = new Mat();
    showBitmap = Bitmap.createBitmap(bitmap);
    
    binMask = new Mat(image.size(), CvType.CV_8UC1);
    reset();
}

private final void getBinMask( Mat comMask, Mat binMask )
{
    if( comMask.empty() || comMask.type()!=CvType.CV_8UC1 )
//      CV_Error( CV_StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)" );
        Log.e("error","comMask is empty or has incorrect type (not CV_8UC1)");
    if( binMask.empty() || binMask.rows() != comMask.rows() || binMask.cols() != comMask.cols() )
        binMask.create( comMask.size(), CvType.CV_8UC1 );
//  binMask = comMask & 1;
    Core.bitwise_and(comMask, temp, binMask);
}

public Bitmap showImage()
{
    if( image.empty() )
        return null;
    
    res = new Mat();
    if( !isInitialized )
        image.copyTo( res );
    else
    {
        getBinMask( mask, binMask );
        image.copyTo( res, binMask);
    }

    for( Point it : bgdPxls)
        Core.circle( res, it, radius, BLUE, thickness );
    for( Point it : fgdPxls)
    	Core.circle( res, it, radius, RED, thickness );
    for( Point it : prBgdPxls)
    	Core.circle( res, it, radius, LIGHTBLUE, thickness );
    for( Point it : prFgdPxls)
    	Core.circle( res, it, radius, PINK, thickness );

    if( rectState == State.IN_PROCESS || rectState == State.SET )
    	Core.rectangle( res, new Point( rect.x, rect.y ), new Point(rect.x + rect.width, rect.y + rect.height ), GREEN, 5);
    
    Utils.matToBitmap(res, showBitmap);
    return showBitmap;
 //   imshow( *winName, res );
}

 // void mouseClick( int event, int x, int y, int flags, void* param );
public int nextIter()
{
    if( isInitialized )
        Imgproc.grabCut( image, mask, rect, bgdModel, fgdModel, 1 );
    else
    {
        if( rectState != State.SET )
            return iterCount;

        if( lblsState == State.SET || prLblsState == State.SET )
        	Imgproc.grabCut( image, mask, rect, bgdModel, fgdModel, 1, Imgproc.GC_INIT_WITH_MASK );
        else
        	Imgproc. grabCut( image, mask, rect, bgdModel, fgdModel, 1, Imgproc.GC_INIT_WITH_RECT );

        isInitialized = true;
    }
    iterCount++;

    bgdPxls.clear(); fgdPxls.clear();
    prBgdPxls.clear(); prFgdPxls.clear();

    return iterCount;
}

public int getIterCount() { return iterCount; }

private void setRectInMask(){
	assert( !mask.empty() );
	mask.setTo( new Scalar(Imgproc.GC_BGD));
	rect.x = Math.max(0, rect.x);
	rect.y = Math.max(0, rect.y);
	rect.width = Math.min(rect.width, image.cols()-rect.x);
    rect.height = Math.min(rect.height, image.rows()-rect.y);
    (mask.submat(rect)).setTo( new Scalar(Imgproc.GC_PR_FGD) );
}
private void setLblsInMask( int flag, Point p, boolean isPr ){
    Vector<Point> bpxls, fpxls;
    int bvalue, fvalue;
    if( !isPr )
    {
        bpxls = bgdPxls;
        fpxls = fgdPxls;
        bvalue = Imgproc.GC_BGD;
        fvalue = Imgproc.GC_FGD;
    }
    else
    {
        bpxls = prBgdPxls;
        fpxls = prFgdPxls;
        bvalue = Imgproc.GC_PR_BGD;
        fvalue = Imgproc.GC_PR_FGD;
    }
    if( flag == 1 || flag == 3)
    {
        fpxls.add(p);
        Core.circle( mask, p, radius, new Scalar(fvalue), thickness );
    }
    if( flag == 2 || flag == 4 )
    {
        bpxls.add(p);
        Core.circle( mask, p, radius, new Scalar(bvalue), thickness );
    }
}


public void ontouch( int event, int x, int y ,int flag)
{
    switch( event )
    {
    case 0: // Touch Down
        {
            if( flag == 0 && rectState == State.NOT_SET )
            {
                rectState = State.IN_PROCESS;
                rect = new Rect( x, y, 1, 1 );
            }
            if ( (flag == 1 || flag == 2) && rectState == State.SET )
                lblsState = State.IN_PROCESS;
            
            if ( (flag == 3 || flag == 4) && rectState == State.SET )
                   prLblsState = State.IN_PROCESS;
        }
        break; 
    case 1:  //Touch Up
        if( rectState == State.IN_PROCESS )
        {
            rect = new Rect( new Point(rect.x, rect.y), new Point(x,y) );
            rectState = State.SET;
            setRectInMask();
            assert( bgdPxls.isEmpty() && fgdPxls.isEmpty() && prBgdPxls.isEmpty() && prFgdPxls.isEmpty() );
        }
        if( lblsState == State.IN_PROCESS )
        {
            setLblsInMask(flag, new Point(x,y), false);
            lblsState = State.SET;
        }
       
        if( prLblsState == State.IN_PROCESS )
        {
            setLblsInMask(flag, new Point(x,y), true);
            prLblsState = State.SET;
        }
        break;
    case 2:  //Touch Move
        if( rectState == State.IN_PROCESS )
        {
            rect = new Rect( new Point(rect.x, rect.y), new Point(x,y) );
            assert( bgdPxls.isEmpty() && fgdPxls.isEmpty() && prBgdPxls.isEmpty() && prFgdPxls.isEmpty() );
        }
        else if( lblsState == State.IN_PROCESS )
        {
            setLblsInMask(flag, new Point(x,y), false);
        }
        else if( prLblsState == State.IN_PROCESS )
        {
            setLblsInMask(flag, new Point(x,y), true);
        }
        break;
    }
}


};

//GCApplication gcapp;
//
//static void on_mouse( int event, int x, int y, int flags, void* param )
//{
//    gcapp.mouseClick( event, x, y, flags, param );
//}
//
//int main( int argc, char** argv )
//{
//    //if( argc!=2 )
//    //{
//    //    help();
//    //    return 1;
//    //}
//    //string filename = argv[1];
//    //if( filename.empty() )
//    //{
//    //    cout << "\nDurn, couldn't read in " << argv[1] << endl;
//    //    return 1;
//    //}
//	string filename = "E:\\1.jpg";
//    Mat image = imread( filename, 1 );
//    if( image.empty() )
//    {
//        cout << "\n Durn, couldn't read image filename " << filename << endl;
//        return 1;
//    }
//
//    help();
//
//    const string winName = "image";
//    namedWindow( winName, WINDOW_AUTOSIZE );
//    setMouseCallback( winName, on_mouse, 0 );
//
//    gcapp.setImageAndWinName( image, winName );
//    gcapp.showImage();
//
//    for(;;)
//    {
//        int c = waitKey(0);
//        switch( (char) c )
//        {
//        case 'r':
//            cout << endl;
//            gcapp.reset();
//            gcapp.showImage();
//            break;
//        case 'n':
//            int iterCount = gcapp.getIterCount();
//            cout << "<" << iterCount << "... ";
//            int newIterCount = gcapp.nextIter();
//            if( newIterCount > iterCount )
//            {
//                gcapp.showImage();
//                cout << iterCount << ">" << endl;
//            }
//            else
//                cout << "rect must be determined>" << endl;
//            break;
//        }
//    }
//
//exit_main:
//    destroyWindow( winName );
//    return 0;
//}
