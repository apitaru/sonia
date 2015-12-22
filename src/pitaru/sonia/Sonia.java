package pitaru.sonia;

import java.lang.reflect.Method;

import processing.core.PApplet;

import com.softsynth.jsyn.*;

public class Sonia {
	
	// Start API ----------------------------------------------------
	
	public static boolean SILENT, DIRECT_DATA, EXITING;
	public static int MAX_SAMPLES = 2048;
	
	public static void start(PApplet p) {
		
		start(p, 44100);
	}

	public static void start(PApplet p, int rate) {
		
		start(p, rate, 0);
	}
	
	public static void start(PApplet p, int rate, int flag) {
		
		if (instance == null)
			instance = new Sonia(p, rate, flag);
	}
	
	public static boolean getStatus() {

		try {
			
			return Class.forName("com.softsynth.jsyn.CuePoint") != null;
			
		} catch (Throwable thr) {
			
			return false;
		}
	}

	// End API ------------------------------------------------------
	
	static float valueMod = 32767f;
	static int inDevID, outDevID, inDevChNum, outDevChNum;
	static PApplet host;
	
	private static Sonia instance;
	
	private Sonia(PApplet p, int rate, int flag) {
		
		Sonia.host = p;
		
		if (p != null) { // for Processing >= 3.0
			
			try {
				Method method = PApplet.class.getDeclaredMethod("registerMethod", 
						new Class[] { String.class, Object.class });
				method.invoke(p, new Object[] { "dispose", this });

			} catch (Throwable e) {
				
				System.err.println("[WARN] Unable to register dispose (ignore if not using Processing 3.x)");
				//e.printStackTrace();
			}
		}
		
		Sonia.initJsyn(rate, flag);
	}
	
	static void initJsyn(int rate, int flag) {

		try {
			startSynth(rate, flag);

			inDevID = AudioDevice.getDefaultInputDeviceID();
			outDevID = AudioDevice.getDefaultOutputDeviceID();
			inDevChNum = AudioDevice.getMaxInputChannels(inDevID);
			outDevChNum = AudioDevice.getMaxOutputChannels(outDevID);
			
			if (!SILENT) System.out.println("[SONIA] Input #" + inDevID + ": '" + 
					AudioDevice.getName(inDevID) + "' has " + inDevChNum + " channel(s)");
			
			if (!SILENT) System.out.println("[SONIA] Output #" + outDevID + ": '" + 
					AudioDevice.getName(outDevID) + "' has " + outDevChNum + " channel(s)");

		} catch (SynthException e) {
			
			throw new SoniaException(e);
		}
	}

	private static void startSynth(int rate, int flag) {
		
		if (Synth.openCount > 0) {
			try {
				Synth.stopEngine();
				int i = 0;
				while (i < 15) { i++; }  // why?
				if (!SILENT)
					System.out.println("[SONIA] Engine stopped with count=" + Synth.openCount);
			
			} catch (SynthException e) {
				throw new SoniaException(e);
			}
		}
		
		Synth.startEngine(flag, rate);
		Synth.setTrace(Synth.SILENT);
	}

	public static void setValueRange(int range) {
		DIRECT_DATA = false;
		valueMod = (float) (32767.0 / range);
	}

	public void dispose() {
		
		if (Sonia.EXITING) return;
		
		EXITING = true;
		
		if (!SILENT) 
				System.out.println("[SONIA] Exiting...");
		
		if (LiveInput.state == 1)
			LiveInput.stop();
		if (LiveOutput.state == 1)
			LiveOutput.stop();
		if (BJSyn.count > 0)
			BJSyn.stop();
		
		Synth.stopEngine();
	}

	public static void stop() {
		
		if (instance != null) instance.dispose();
	}

}