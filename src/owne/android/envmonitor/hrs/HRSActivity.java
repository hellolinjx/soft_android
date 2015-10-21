/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package owne.android.envmonitor.hrs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;



import org.achartengine.GraphicalView;

import owne.android.envmonitor.R;
import owne.android.envmonitor.profile.BleManager;
import owne.android.envmonitor.profile.BleProfileActivity;
import owne.android.envmonitor.spsensors.spEnvSensors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Matrix; 
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * HRSActivity is the main Heart rate activity. It implements HRSManagerCallbacks to receive callbacks from HRSManager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of HR values.
 */
public class HRSActivity extends BleProfileActivity implements HRSManagerCallbacks {
	@SuppressWarnings("unused")
	private final String TAG = "HRSActivity";

	private static final String GRAPH_STATUS = "graph_status";
	private static final String GRAPH_COUNTER = "graph_counter";
	private static final String HR_VALUE = "hr_value";

	private final int MAX_HR_VALUE = 65535;
	private final int MIN_POSITIVE_VALUE = 0;

	private Handler mHandler = new Handler();

	private boolean isGraphInProgress = false;

	private GraphicalView mGraphView;
	private LineGraphView mLineGraph;
	private TextView mHRSValue, mHRSPosition;

	private int mInterval = 10000; // 1 second interval
	private int mHrmValue = 0;
	private int mCounter = 0;
	private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Camera mCamera;
    //private spEnvSensors spSensors;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hrs);
		mSurfaceView = (SurfaceView)this.findViewById(R.id.surfaceview);
	        mSurfaceHolder = mSurfaceView.getHolder();
	        mSurfaceHolder.addCallback(new SurfaceHolderCallback());
	    
	        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
		setGUI();
		restoreSavedState(savedInstanceState);
		//spSensors.getContext(this);
	}
	 private class SurfaceHolderCallback implements SurfaceHolder.Callback
	    {

	        @Override
	        public void surfaceChanged(SurfaceHolder holder, int format, int width,
	                int height) {
	            // TODO Auto-generated method stub
	            
	        }

	        @Override
	        public void surfaceCreated(SurfaceHolder holder) 
	        {
	            // TODO Auto-generated method stub
	            //������ͷ
	            mCamera = Camera.open();
	            try {
	                mCamera.setPreviewDisplay(mSurfaceHolder);
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	            //��ȡ����ͷ����
	            Camera.Parameters mParameters = mCamera.getParameters();
	            //����ͼƬ��ʽ
	            mParameters.setPictureFormat(PixelFormat.JPEG);
	          //设置大小和方向等参数  
	            mParameters.setPictureSize(1280, 960);  
	            mParameters.setPreviewSize(960, 720);  
	            mCamera.setParameters(mParameters); 
	            mCamera.setDisplayOrientation(90);
	            //��ʼԤ��
	            mCamera.startPreview();
	        }

	        @Override
	        public void surfaceDestroyed(SurfaceHolder holder) 
	        {
	            // TODO Auto-generated method stub
	            if(mCamera!=null)
	            {
	                //ֹͣԤ��
	                mCamera.stopPreview();
	                //�ͷ�����ͷ
	                mCamera.release();
	                mCamera = null;
	            }
	        }
	        
	    }
	    
	    //���ջص�
	    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback()
	    {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) 
	        {
	            // TODO Auto-generated method stub
	            //ֹͣԤ��
	            mCamera.stopPreview();
	            Bitmap mBitmap;
	            mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	            //imvi.setImageBitmap(mBitmap);
	            //�ļ�·�����ļ���
	            Matrix matrix = new Matrix();  
	            matrix.postRotate((float)90.0);  
	            Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);  
	            if(Environment.getExternalStorageState()
						.equals(Environment.MEDIA_MOUNTED) ){
	     
	            String spath=Environment.getExternalStorageDirectory()+"/CameraOn";
	            File dir=new File(spath);
	            if(!dir.exists())
	            	 if(dir.mkdirs()||dir.isDirectory()) 
	            		 displayToast("����Ŀ¼�ɹ���");
	            	 else  displayToast("����Ŀ¼ʧ�ܣ���");
	            File pictureFile = new File(dir,Integer.toString(10)+".jpg");
	            //spath=spath+"/"+Integer.toString(whichPicture)+".jpg";
	            try 
	            {
	                FileOutputStream mFileOutputStream = new FileOutputStream(pictureFile);
	                //��ͼ������ѹ���ļ�
	                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, mFileOutputStream);
	                try {
	                    //�ر������
	                	mFileOutputStream.flush();
	                    mFileOutputStream.close();
	                } catch (IOException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	            } 
	            catch (FileNotFoundException e) 
	            {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }  
	            }//end of IF;
	           
	            mCamera.startPreview();
	        }
	        
	    }; 
	    private void displayToast(String s)
	    {
	        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	    }
	private void setGUI() {
		mLineGraph = LineGraphView.getLineGraphView();
		mHRSValue = (TextView) findViewById(R.id.text_hrs_value);
		mHRSPosition = (TextView) findViewById(R.id.text_hrs_position);
		showGraph();
	}

	private void showGraph() {
		mGraphView = mLineGraph.getView(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_hrs);
		layout.addView(mGraphView);
	}

	private void restoreSavedState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
			mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
			mHrmValue = savedInstanceState.getInt(HR_VALUE);
			if (isGraphInProgress) {
				startShowGraph();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
		outState.putInt(GRAPH_COUNTER, mCounter);
		outState.putInt(HR_VALUE, mHrmValue);
		stopShowGraph();
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hrs_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hrs_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HRSManager.HR_SERVICE_UUID;
	}

	private void updateGraph(final int hrmValue) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//spSensors.sensorRegister();
				mCounter++;
				Point point = new Point(mCounter, hrmValue);
				mLineGraph.addValue(point);
				mGraphView.repaint();
				mCamera.autoFocus(null);
               	mCamera.takePicture(null, null, pictureCallback);
              // 	displayToast("Take phto!\nMagnetic x "+spSensors.magValues[0]+" y "+spSensors.magValues[1]
               	//		+" Z " +spSensors.magValues[2]+"\n"+"light "+spSensors.lightValues);
			}
		});
	}

	private Runnable mRepeatTask = new Runnable() {
		@Override
		public void run() {
			if (mHrmValue > 0)
				updateGraph(mHrmValue);
			if (isGraphInProgress)
				mHandler.postDelayed(mRepeatTask, mInterval);
		}
	};

	void startShowGraph() {
		isGraphInProgress = true;
		mRepeatTask.run();
	}

	void stopShowGraph() {
		isGraphInProgress = false;
		mHandler.removeCallbacks(mRepeatTask);
	}

	@Override
	protected BleManager<HRSManagerCallbacks> initializeManager() {
		HRSManager manager = HRSManager.getHRSManager();
		manager.setGattCallbacks(this);
		return manager;
	}

	private void setHRSValueOnView(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (value >= MIN_POSITIVE_VALUE && value <= MAX_HR_VALUE) {
					mHRSValue.setText(Integer.toString(value));
				} else {
					mHRSValue.setText(R.string.not_available_value);
				}
			}
		});
	}

	private void setHRSPositionOnView(final String position) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (position != null) {
					mHRSPosition.setText(position);
				} else {
					mHRSPosition.setText(R.string.not_available);
				}
			}
		});
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onHRSensorPositionFound(String position) {
		setHRSPositionOnView(position);
	}

	@Override
	public void onHRNotificationEnabled() {
		startShowGraph();
	}

	@Override
	public void onHRValueReceived(int value) {
		mHrmValue = value;
		setHRSValueOnView(mHrmValue);
	}

	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mHRSValue.setText(R.string.not_available_value);
				mHRSPosition.setText(R.string.not_available);
				stopShowGraph();
			}
		});
	}

	@Override
	protected void setDefaultUI() {
		mHRSValue.setText(R.string.not_available_value);
		mHRSPosition.setText(R.string.not_available);
		clearGraph();
	}

	private void clearGraph() {
		mLineGraph.clearGraph();
		mGraphView.repaint();
		mCounter = 0;
		mHrmValue = 0;
	}
}
