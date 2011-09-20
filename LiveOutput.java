package pitaru.sonia_v29b;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

import java.awt.event.*;
import java.applet.Applet;
import com.softsynth.jsyn.*;
import com.softsynth.jsyn.util.*;
//import com.softsynth.jsyn.view11x.*;

import java.lang.reflect.*;


public class LiveOutput implements Runnable
{
	public static SampleReader_16F1       mySampler;
	public static LineOut            myOut;
	public static int   FRAMES_PER_BLOCK;// = 1024; // number of frames to synthesize at one time
	public static int   FRAMES_IN_BUFFER;// = 8*1024;
	public static int   NUM_CHANNELS = 1;
	public static int   SAMPLES_PER_BLOCK;// = FRAMES_PER_BLOCK * NUM_CHANNELS;
	public static short[]            directData;// = new short[SAMPLES_PER_BLOCK];
	public static float[]			  data;
	public static SampleQueueOutputStream  outStream;
	public static Thread             t1;
	public static int   SLEEP = 5;

	public static int state = 0;



	//static Object host;


/*
 * Setup synthesis.
 */

	public LiveOutput()
	{


	}

	public static void setSleep(int s){
		SLEEP = s;
	}

	public static void start(int block){
		start(block,block);
	}

	public static void start(int block,int buffer){
		try
				{



				//host = _host;
				FRAMES_PER_BLOCK = block;
				FRAMES_IN_BUFFER = buffer;
				SAMPLES_PER_BLOCK = FRAMES_PER_BLOCK * NUM_CHANNELS;
				directData = new short[SAMPLES_PER_BLOCK];
				data = new float[SAMPLES_PER_BLOCK];

		        //Sonia.initJsyn(44000,0);

		// Create SynthUnits to play sample data.
				if( NUM_CHANNELS == 1 )
				{
					mySampler = new SampleReader_16F1();
				}
				else if( NUM_CHANNELS == 2 )
				{
					//mySampler = new SampleReader_16V1();
				}
				else
				{
					throw new RuntimeException("This example only support mono or stereo!");
				}
				myOut = new LineOut();

				//if(valueMod == 0) mySampler.amplitude.set(1);
				mySampler.amplitude.set(1);
		// Create a stream that we can write to.
				outStream = new SampleQueueOutputStream( mySampler.samplePort, FRAMES_IN_BUFFER, NUM_CHANNELS );

		// Connect SynthUnits to output.
				mySampler.output.connect( 0, myOut.input, 0 );
				mySampler.output.connect( NUM_CHANNELS - 1, myOut.input, 1 );

		// Start execution of units.
				myOut.start();
				mySampler.start();
				} catch (SynthException e) {
					System.err.println(e);

				}

	}

	public void run()
	{
		try
		{
			while( t1 != null )
			{

			while (outStream.available() >= FRAMES_PER_BLOCK )	{
				fireEvent();
				if(!Sonia.directData) convertData();
					sendBuffer();

				}
				t1.sleep(SLEEP);
			}
		} catch( SynthException e )	{
			System.out.println("run() caught " + e );
		} catch( InterruptedException e )	{
			System.out.println("run() caught " + e );
		}
	}

/** Generate an audio signal and send it to the stream to be heard.
 */

	public static void sendBuffer()
	{


	// Write data to the stream.
	// Will block if there is not enough room so run in a thread.
		outStream.write( directData, 0, FRAMES_PER_BLOCK );

	}

/*
	static public void setValueRange(int range){
		valueMod= (float)(32767.0/range);
	}
	*/

	public static void convertData(){

		for ( int i = 0; i < data.length; i++){
			directData[i] = (short)((data[i]*Sonia.valueMod));
		}

	}

	public static void stop()
	{
		try
		{
			stopStream();
			t1 = null;

// Delete unit peers.
			mySampler.delete();
			mySampler = null;
			myOut.delete();
			myOut = null;

		} catch (SynthException e) {
			System.out.println(e);
		}
	}

	public static void startStream()
	{

		state = 1;

	// Prefill output stream buffer so that it starts out full.
		while( outStream.available() > FRAMES_PER_BLOCK ){
				fireEvent();
				if(!Sonia.directData) convertData();
					sendBuffer();

		}

	// Start slightly in the future so everything is synced.
		int time = Synth.getTickCount() + 4;
		mySampler.start( time );
		outStream.start( time );

	// launch a thread to keep stream supplied with data
		LiveOutput lo = new LiveOutput();

		t1 = new Thread(lo);
		t1.start();

	}

	public static void stopStream()
	{

		state = 0;

		//outStream.flush(); // wait for all data already written to stream to be played

		t1 = null;
		int time = Synth.getTickCount();
		mySampler.stop( time );
		outStream.stop( time );
	}



	 public static void fireEvent()
	  {
	    try
	    {
		// Class[] argTypes = { LiveOutput.class};


	      //Method m = host.getClass().getDeclaredMethod("soundEvent", argTypes);
	      Method m = Sonia.host.getClass().getDeclaredMethod("liveOutputEvent", null);

	      try
	      {
			     // LiveOutput[] args = new LiveOutput[1];
			     // args[0] = this;


	       // m.invoke(host, args);// the oneEvent() method will be invoked only if it exists
	       m.invoke(Sonia.host, null);
	      }
	      catch (InvocationTargetException e)
	      {}
	      catch (IllegalAccessException e)
	      {}
	    }
	    catch (NoSuchMethodException e)
	    {}
	    // it's also possible to invoke methods that have parameters,
	    // (just a matter of replacing the 2 null's that you can see above)
      }


     public static void connectLiveInput(boolean flag){
	  		int id = LiveInput.maxSamples + 2;
	  		if(flag){
	  		LiveInput.myBusWriter[id] = new BusWriter();
	  		LiveInput.myBusWriter[id].start();
	  		mySampler.output.connect(LiveInput.myBusWriter[id].input);
	  		LiveInput.myBusWriter[id].busOutput.connect( LiveInput.myBusReader.busInput );
	  /*
	  		if(channels == 2){
	  			LiveInput.myBusWriter[id+1] = new BusWriter();
	  			LiveInput.myBusWriter[id+1].start();
	  			mySampler.output.connect(LiveInput.myBusWriter[id+1].input);
	  			LiveInput.myBusWriter[id+1].busOutput.connect( LiveInput.myBusReader.busInput );
	  		}
	  		*/

	  		} else {
	  			LiveInput.myBusWriter[id].stop();
	  			LiveInput.myBusWriter[id].input.disconnect();
	  			LiveInput.myBusWriter[id].busOutput.disconnect();
	  		/*
	  		if(channels == 2){
	  			LiveInput.myBusWriter[id+1].stop();
	  			LiveInput.myBusWriter[id+1].input.disconnect();
	  			LiveInput.myBusWriter[id+1].busOutput.disconnect();
	  		}
	  		*/

	  		}
	}

}
