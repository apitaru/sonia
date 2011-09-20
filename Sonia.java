// Helper-library for BJsyn, objectifying the engine.
// Oct 5 03.
// by: Amit Pitaru (c) 2003
// You may use this code at will. If you include all/parts of it in your project, please mention where you got it: "Processing-Jsyn tutorial by Amit Pitaru, http://pitaru.com"
// If you modify/expand the code and make it better, please send me improved versions: amit@pitaru.com

package pitaru.sonia_v29b;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.applet.Applet;
import com.softsynth.jsyn.*;
import processing.core.*;

public class Sonia{
	public static final boolean DIRECT = true;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final boolean NO_MIC = false;
	public static final boolean MIC = true;
	public static float valueMod = 32767.0f;
	public static boolean directData = false;
	static PApplet host;
	//static AudioDevice ad;
	static int  inDevID, outDevID, inDevChNum, outDevChNum;

	public final static int AVAILABLE = 0;
	public final static int CLASSES_MISSING = -1;
	public final static int NATIVE_LIBRARY_MISSING = -2;
    public final static int OBSOLETE = -3;


///////**********  BIG CHANGE Object to PApplet
	public static void start(PApplet _host, int rate, int flag){
		host = _host;
    	//host.registerDispose();
		initJsyn(rate, flag);
	}

	public static void start(PApplet _host){
			host = _host;
			//host.registerDispose();
			//System.out.println("SONIA.start count:" + Synth.openCount);
			initJsyn(44100, 0);

	}

	public static void start(PApplet _host, int rate){
			host = _host;
			//host.registerDispose();
			initJsyn(rate,0);
	}



	public static void initJsyn(int rate, int flag){
		// Only start if not already in use (specific to this code - not usually needed).
		 // System.out.println("SONIA.initJsyn count:" + Synth.openCount);

			if(Synth.openCount == 1){
				try{
				//System.out.println("STOPPING ENGINE count:" + Synth.openCount);

				//BJSyn.myBusReader.stop();
      		   // BJSyn.mainOut.stop();

				Synth.stopEngine();
				int i = 0;
				while(i < 15){
					 i++;
				}
				System.out.println("ENGINE STOPPED count:" + Synth.openCount);
				} catch (SynthException e) {
						//System.out.println("ENGINE STOPPED - failed count:" + Synth.openCount);
						System.err.println(e);
		     	}

			}


		 //  if (Synth.openCount < 2){
		     try {
		       Synth.startEngine(flag,rate);
      		   Synth.setTrace(Synth.SILENT);

      		   //BJSyn.myBusReader.output.connect(0, BJSyn.mainOut.input,0 );
      		   //BJSyn.myBusReader.output.connect(0, BJSyn.mainOut.input,1 );
      		  // BJSyn.myBusReader.start();
      		   //BJSyn.mainOut.start();

				//ad = new AudioDevice();
				inDevID = AudioDevice.getDefaultInputDeviceID();
				outDevID = AudioDevice.getDefaultOutputDeviceID();
				inDevChNum = AudioDevice.getMaxInputChannels(inDevID);
				outDevChNum = AudioDevice.getMaxOutputChannels(outDevID );
				System.out.println("Input Device #" + inDevID + ": " + AudioDevice.getName(inDevID) + " has " + inDevChNum + " channels");
				System.out.println("Input Device #" + outDevID + ": " + AudioDevice.getName(outDevID) + " has " + outDevChNum + " channels");

		     } catch (SynthException e) {
		       System.err.println(e);
		     }
    		//}
	}



	public static void setValueRange(boolean flag){
		directData = true;
	}

	public static void setValueRange(int range){
			directData = false;
			valueMod= (float)(32767.0/range);
	}

	public static void stop(){
		//System.out.println("SONIA.STOP()");
		if (LiveInput.state == 1 ) LiveInput.stop();
		if (LiveOutput.state == 1 ) LiveOutput.stop();
		if (BJSyn.count > 0 ) BJSyn.stop();
		Synth.stopEngine();

	}


 	 public void dispose() {
		if (LiveInput.state == 1 ) LiveInput.stop();
		if (LiveOutput.state == 1 ) LiveOutput.stop();
		if (BJSyn.count > 0 ) BJSyn.stop();
		Synth.stopEngine();

	}


	public static boolean getStatus(){
		int status = _getStatus();
		if(status == 0) {return true;} else {return false;}
/*
			Class cl = null;
			try
			{
				cl = Class.forName( "com.softsynth.jsyn.CuePoint" );
			} catch (ClassNotFoundException e) {
				cl = null;
			} catch (Throwable thr) {
				cl = null;
			}

			if(cl == null) return false;
			return true;
			*/



	}






public static int _getStatus()
	{
	// Try just creating a JSyn class that does not depend on the DLL.
        try
        {
		    Class.forName("com.softsynth.jsyn.CuePoint");

    	// Try calling a class that requires the native DLL.
            try
            {
                Class cl = Class.forName("com.softsynth.jsyn.SynthContext");

                try
                {
                    Object obj = cl.newInstance();
                } catch( Throwable thr )
    {
                    return NATIVE_LIBRARY_MISSING;
                }

            } catch (Throwable thr) {
			    return OBSOLETE;
            }

		} catch (Throwable thr) {
            return CLASSES_MISSING;
		}
		return 0;
	}


	public static void setMaxSamples(int ms){
		BJSyn.maxSamples = ms;
	}

	public static int getMaxSamples(){
			return BJSyn.maxSamples;
	}

/*
	    public static String getStatusText(  )
	    {
			int status = getStatus();

	        String text = null;
	        switch( status )
	        {
	            case AVAILABLE:
	                text = "JSyn is installed.";
	                break;
	             case CLASSES_MISSING:
	                text = "JSyn classes missing. Plugin not installed. " +
	                    "Please download the JSyn plugin from \"http://www.softsynth.com/jsyn/\".";
	                break;
	            case NATIVE_LIBRARY_MISSING:
	                text = "JSyn native library missing. Plugin partially installed.\n" +
	                    "Please reinstall the JSyn plugin from \"http://www.softsynth.com/jsyn/\".\n" +
	                    "If installation is unsuccessful visit \"http://www.softsynth.com/jsyn/support/\".";
	                break;
	            case OBSOLETE:
	                text = "A very old version of JSyn is installed and needs to be upgraded.\n" +
	                    "Please reinstall the JSyn plugin from \"http://www.softsynth.com/jsyn/\".";
	                break;
	            default:
	                text = "CheckForJSyn.getStatus() returned unrecognized result = " + status;
	        }
	        return text;
    }
    */

/*
	static long getLatency(){
		Date d = new Date();
		long l = 0;
  		Sample s1 = new Sample(1024);
  		s1.setVolume(1);
 		float[] sine = new float[1024];
 		for(int i = 0; i < 1024; i++){
 		  sine[i] = host.sin((float)((i*host.TWO_PI/1024.0)*4.0));
		}
		s1.write(sine);

		PeakFollower pk = new PeakFollower();
		BJSyn.mySampler[s1.getID()].output.connect(pk.input);
		pk.start();
		l = d.getTime();
		s1.play();
		while (pk.output.get() == 0){}
		d = new Date();
		l = d.getTime() - l;

		return l;

	}
	*/


}



