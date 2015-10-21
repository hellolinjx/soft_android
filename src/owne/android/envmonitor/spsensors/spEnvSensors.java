package owne.android.envmonitor.spsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//get smart phone's magnetic field,light and ambient temperature;
public class spEnvSensors implements SensorEventListener {
	  private SensorManager mSensorManager;
	  private Context context;
	  public float[] magValues;
	  public float lightValues;
	  public float ambientTemValues;

	  public final void onCreate() {

	    // Get an instance of the sensor service, and use that to get an instance of
	    // a particular sensor.
	    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	    //mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
	  }

	 public void getContext(Context context){
		 this.context=context;
	 }
	 
	@Override
	  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
	    
	  }

	
	//get sensor values;
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		float[] values=event.values;
		int sensorType=event.sensor.getType();
		switch(sensorType){
		case Sensor.TYPE_MAGNETIC_FIELD:
			magValues[0]=values[0];   //Geomagnetic field strength along the x axis.
			magValues[1]=values[1];   //Geomagnetic field strength along the y axis.
			magValues[2]=values[2];   //Geomagnetic field strength along the z axis.
			break;
		case Sensor.TYPE_LIGHT:
			lightValues=values[0];
			break;
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			ambientTemValues=values[0];
		}
		//unregister to save battery
		sensorUnregister();
	}

	//register a listener 
	public void sensorRegister(){
		mSensorManager.registerListener(this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	//unregister listener
	public void sensorUnregister(){
		mSensorManager.unregisterListener(this);
	}

	  
	}
