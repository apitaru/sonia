// BJsyn: (Bagel Jsyn) library for sample control using jSyn.
// Oct 5 03.
// V2.2
// by: Amit Pitaru (c) 2003
// You may use this code at will. If you include all/parts of it in your project, please mention where you got it: "Processing-Jsyn tutorial by Amit Pitaru, http://pitaru.com"
// If you modify/expand the code and make it better, please send me improved versions: amit@pitaru.com

package pitaru.sonia;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import processing.core.PApplet;

import com.softsynth.jsyn.*;

public class BJSyn {

	public static PApplet parent;
	static public SynthSample[] mySamp = new SynthSample[Sonia.MAX_SAMPLES];
	public static SampleReader_16V1[] mySampler = new SampleReader_16V1[Sonia.MAX_SAMPLES];
	public static LineOut[] myOut = new LineOut[Sonia.MAX_SAMPLES];
	public static PanUnit[] myPan = new PanUnit[Sonia.MAX_SAMPLES];
	public static InputStream stream;
	public static LinearLag[] myLinearLag = new LinearLag[Sonia.MAX_SAMPLES];
	public static LinearLag[] myLinearLag2 = new LinearLag[Sonia.MAX_SAMPLES];
	public static LinearLag[] myLinearLag3 = new LinearLag[Sonia.MAX_SAMPLES];
	public static MultiplyUnit[] multiplier = new MultiplyUnit[Sonia.MAX_SAMPLES];
	
	public static int count, sampleNum, channelNum;

	public BJSyn() {}

	public static int getChannels(int sampleNum) {

		return mySamp[sampleNum].getChannelsPerFrame();
	}

	// loop the sample, providing start-end points.
	public static void loopSample(int sampleNum, int start, int end) {
		
		try {
			
			startCircuit(sampleNum);
			int offset = Synth.getTickCount() + 1; // used to delay operations below -
																							// prevents 'pop' sound
			
			mySampler[sampleNum].samplePort.clear(offset); // reset the sample
																											// play-head.
			
			// in the next 'frame' (offset), start a slope from 0 to 1, over .001 sec.
			// we use these slopes all over the code to prevent 'pop' sounds - and
			// provide a clean transition.
			myLinearLag2[sampleNum].time.set(offset, 0.001);
			myLinearLag2[sampleNum].input.set(offset, 1.0);
		
			// in the next 'frame' (offset), start looping the sample between
			// start-end points.
			mySampler[sampleNum].samplePort.queueLoop(offset, mySamp[sampleNum],
					start, end - start);

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

	// loop sample, using entire sample-data.
	public static void loopSample(int sampleNum) {
		
		loopSample(sampleNum, 0, mySamp[sampleNum].getNumFrames());
	}

	// Play sample, using entire sample-data.
	public static void playSample(int sampleNum) {
		
		playSample(sampleNum, 0, mySamp[sampleNum].getNumFrames());
	}

	// Play sample once. See loopSample() for details.
	public static void playSample(int sampleNum, int start, int end) {
		
		try {
			
			startCircuit(sampleNum);
			int offset = Synth.getTickCount() + 1;
			mySampler[sampleNum].samplePort.clear(offset);
			myLinearLag2[sampleNum].time.set(offset, 0.001);
			myLinearLag2[sampleNum].input.set(offset, 1.0);
			mySampler[sampleNum].samplePort.queue(offset, mySamp[sampleNum], start,
					end - start);

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

	// Loop sample a number of times. See loopSample() for details.
	public static void loopSampleNum(int num, int sampleNum, int start, int end) {
		
		try {
			startCircuit(sampleNum);
			int offset = Synth.getTickCount() + 1;
			mySampler[sampleNum].samplePort.clear(offset);
			myLinearLag2[sampleNum].time.set(offset, 0.001);
			myLinearLag2[sampleNum].input.set(offset, 1.0);
			for (int i = 0; i < num; i++) {
				mySampler[sampleNum].samplePort.queue(offset, mySamp[sampleNum], start,
						end - start);
			}

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

	// Stop sample
	public static void stopSample(int sampleNum, boolean stopFlag, int stopOffset) {
		
		try {
			// shut off the circuit, free -cpu.
			if (stopFlag)
				stopCircuit(sampleNum, stopOffset);
			// Start a slope from current volume to 0, during 0.005 sec.
			myLinearLag2[sampleNum].time.set(0.005);
			myLinearLag2[sampleNum].input.set(0);

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

	// Start the circuit, opening up all sample units.
	public static void startCircuit(int sampleNum) {

		try {
			myPan[sampleNum].start();
			myOut[sampleNum].start();
			// myBusWriter[sampleNum].start();
			mySampler[sampleNum].start();
			multiplier[sampleNum].start();
			myLinearLag[sampleNum].start();
			myLinearLag2[sampleNum].start();
			myLinearLag3[sampleNum].start();
		} catch (SynthException e) {

			SynthAlert.showError(e);

			throw new SoniaException(e);
		}
	}

	// Start the circuit, stopping all sample units.
	public static void stopCircuit(int sampleNum, int theOffset) {
		
		try {	
			// delayed operation to shut off circuits & prevent pops
			int offset = Synth.getTickCount() + theOffset;
			if (myPan != null && myPan[sampleNum] != null)
				myPan[sampleNum].stop(offset);
			if (myOut != null && myOut[sampleNum] != null)
				myOut[sampleNum].stop(offset);
			if (mySampler != null && mySampler[sampleNum] != null)
				mySampler[sampleNum].stop(offset);
			if (multiplier != null && multiplier[sampleNum] != null)
				multiplier[sampleNum].stop(offset);
			if (myLinearLag != null && myLinearLag[sampleNum] != null)
				myLinearLag[sampleNum].stop(offset);
			if (myLinearLag2 != null && myLinearLag2[sampleNum] != null)
				myLinearLag2[sampleNum].stop(offset);
			if (myLinearLag3 != null && myLinearLag3[sampleNum] != null)
				myLinearLag3[sampleNum].stop(offset);

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

	public static void setRate(int sampleNum, float r) {
		
		if (!Sonia.EXITING) mySampler[sampleNum].rate.set(r);
	}

	public static double getRate(int sampleNum) {
		
		return Sonia.EXITING ? 0 : mySampler[sampleNum].rate.get();
	}

	public static void setVolume(int sampleNum, float a) {
		
		if (!Sonia.EXITING) {
			myLinearLag[sampleNum].input.set(a);
			myLinearLag[sampleNum].time.set(0.03);
		}
	}

	public static void setVolume(int sampleNum, double a) {
		
		if (!Sonia.EXITING) setVolume(sampleNum, (float) (a));
	}

	public static double getVolume(int sampleNum) {
		return Sonia.EXITING ? 0 : myLinearLag[sampleNum].input.get();
	}

	public static void setPan(int sampleNum, float p) {
		if (!Sonia.EXITING) {
			myLinearLag3[sampleNum].time.set(0.03);
			myLinearLag3[sampleNum].input.set(p);
		}
	}

	public static double getPan(int sampleNum) {
		return Sonia.EXITING ? 0 : myLinearLag3[sampleNum].input.get();
	}

	public static int getNumFrames(int sampleNum) {
		return Sonia.EXITING ? 0 : mySamp[sampleNum].getNumFrames();
	}

	public static void buildSamp(int sampleNum, String fileName) {

		// determines type of sample (can only read wav or aiff)
		switch (SynthSample.getFileType(fileName)) {
		case SynthSample.AIFF:
			mySamp[sampleNum] = new SynthSampleAIFF();
			break;
		case SynthSample.WAV:
			mySamp[sampleNum] = new SynthSampleWAV();
			break;
		default:
			System.err.println("Sonia: Sample must be 'wav' or 'aiff' format");
		}
	}

	public static void buildEmptySamp(int sampleNum, int len, int rate) {
		mySamp[sampleNum] = new SynthSample(len);
		mySamp[sampleNum].setSampleRate(rate);
	}

	// Load a sample and build a circuit for it.
	public static void loadSample(int sampleNum, String filename) {

		InputStream stream = null;

		if (Sonia.host != null) {
			try {

				// gets the sample file from a jar of directory.
				stream = Sonia.host.createInput(filename);

			} catch (NullPointerException e) {

				System.err
						.println("[Sonia] make sure you have entered the correct Sample filename!");
				return;
			} catch (Throwable e) {

				throw new SoniaException(e);
			}
			
		} else {
			
			stream = openStream(filename);
		}

		buildSamp(sampleNum, filename);

		// if there's data in the sample, load the sample.
		if (mySamp[sampleNum] != null) {

			try {
				mySamp[sampleNum].load(stream);

			} catch (Throwable e) {

				throw new SoniaException(e);
			}
		}
		
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				System.err.println("[WARN] error closing "+filename);
			}
			stream = null;
		}
		
		if (mySamp[sampleNum].getChannelsPerFrame() == 1) {
			// Now that the sample is ready, create a circuit for it.
			buildCircuit(sampleNum);
			channelNum = 1;
		} else if (mySamp[sampleNum].getChannelsPerFrame() == 2) {
			channelNum = 2;

			int numShorts = mySamp[sampleNum].getNumFrames()
					* mySamp[sampleNum].getChannelsPerFrame();

			short[] data = new short[numShorts];
			short[] leftSamples = new short[numShorts / 2];
			short[] rightSamples = new short[numShorts / 2];

			mySamp[sampleNum].read(data);

			int leftIndex = 0;
			int rightIndex = 0;
			int stereoIndex = 0;

			while (stereoIndex < numShorts) {
				leftSamples[leftIndex++] = data[stereoIndex++];
				rightSamples[rightIndex++] = data[stereoIndex++];
			}

			buildSamp(sampleNum + 1, filename);

			mySamp[sampleNum].clear(0, numShorts / 2);
			mySamp[sampleNum].allocate(numShorts / 2, 1);
			mySamp[sampleNum].write(0, leftSamples, 0, numShorts / 2);
			buildCircuit(sampleNum);
			setPan(sampleNum, -1f);

			mySamp[sampleNum + 1].allocate(numShorts / 2, 1);
			mySamp[sampleNum + 1].write(0, rightSamples, 0, numShorts / 2);
			buildCircuit(sampleNum + 1);
			setPan(sampleNum + 1, 1f);

		}

	}

	private static String SLASH = System.getProperty("file.separator");
			
  private static boolean isAbsolutePath(String fileName) {
    return (fileName.startsWith(SLASH) || fileName.matches("^[A-Za-z]:")); // // 'driveA:\\'?
  }

	private static InputStream openStream(String streamName) // from Processing
	{
		boolean dbug = false;

		try // check for url first (from PApplet)
		{
			URL url = new URL(streamName);
			return url.openStream();
		} catch (MalformedURLException mfue) {
			
			// not a url, that's fine
		} catch (FileNotFoundException fnfe) {
			
			// Java 1.5 likes to throw this when URL not available.
			// http://dev.processing.org/bugs/show_bug.cgi?id=403
		} catch (Throwable e) {
			
			throw new SoniaException(e);
		}

		InputStream is = null;
    String[] guesses = { "src/data", "data", "" };
		for (int i = 0; i < guesses.length; i++) {
			String guess = streamName;
			if (guesses[i].length() > 0) {
				if (isAbsolutePath(guess))
					continue;
				guess = guesses[i] + System.getProperty("file.separator") + guess;
			}

			if (dbug && !Sonia.SILENT)
				System.out.print("[INFO] Trying " + guess);

			try {
				is = new FileInputStream(guess);
				if (dbug && !Sonia.SILENT)
					System.out.println("... OK");
			} catch (FileNotFoundException e) {
				if (dbug && !Sonia.SILENT)
					System.out.println("... failed");
				if (is != null)
					try {
						is.close();
					} catch (IOException e1) {
					}
			}
			if (is != null)
				break;
		}

		if (is == null) // last try with classloader...
		{
			// Using getClassLoader() prevents java from converting dots
			// to slashes or requiring a slash at the beginning.
			// (a slash as a prefix means that it'll load from the root of
			// the jar, rather than trying to dig into the package location)
			ClassLoader cl = Sonia.class.getClassLoader();

			// by default, data files are exported to the root path of the jar.
			// (not the data folder) so check there first.
			if (dbug && !Sonia.SILENT)
				System.out.print("[INFO] Trying data/" + streamName + " as resource");

			is = cl.getResourceAsStream("data/" + streamName);
			if (is != null) {

				String cn = is.getClass().getName();

				// this is an irritation of sun's java plug-in, which will return
				// a non-null stream for an object that doesn't exist. like all good
				// things, this is probably introduced in java 1.5. awesome!
				// http://dev.processing.org/bugs/show_bug.cgi?id=359
				if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {

					if (dbug && !Sonia.SILENT)
						System.out.println("... OK");

					return is;
				}
			}
			if (dbug && !Sonia.SILENT)
				System.out.println("... failed");
		}

		if (is == null)
			throw new SoniaException("Unable to create stream for: " + streamName);

		return is;
	}

	// Overload loadSample, primarily used by function above.
	public static void loadSample(SynthSample sample, InputStream stream)
			throws IOException {
		sample.load(stream);
	}

	// Build a circuit for the sample (this is not a real jSyn circuit, but just
	// my terminology).
	// See jSyn tutorial for understanding Unit-Generator techniques used here
	public static void buildCircuit(int sampleNum) {

		mySampler[sampleNum] = new SampleReader_16V1();
		myOut[sampleNum] = new LineOut();
		myPan[sampleNum] = new PanUnit();
		multiplier[sampleNum] = new MultiplyUnit();

		myLinearLag[sampleNum] = new LinearLag();
		myLinearLag2[sampleNum] = new LinearLag();
		myLinearLag3[sampleNum] = new LinearLag();

		myLinearLag3[sampleNum].output.connect(myPan[sampleNum].pan);

		mySampler[sampleNum].output.connect(myPan[sampleNum].input);
		myPan[sampleNum].output.connect(0, myOut[sampleNum].input, 0);
		myPan[sampleNum].output.connect(1, myOut[sampleNum].input, 1);

		myLinearLag[sampleNum].output.connect(multiplier[sampleNum].inputB);
		myLinearLag2[sampleNum].output.connect(multiplier[sampleNum].inputA);

		multiplier[sampleNum].output.connect(mySampler[sampleNum].amplitude);

		startCircuit(sampleNum);
	}

	// delete a circuit.
	public static void deleteCircuit(int sampleNum) {
		if (mySampler[sampleNum] != null)
			mySampler[sampleNum].delete();
		mySampler[sampleNum] = null;

		if (myOut[sampleNum] != null)
			myOut[sampleNum].delete();
		myOut[sampleNum] = null;

		if (myPan[sampleNum] != null)
			myPan[sampleNum].delete();
		myPan[sampleNum] = null;

		if (mySamp[sampleNum] != null)
			mySamp[sampleNum].delete();
		mySamp[sampleNum] = null;

		if (multiplier[sampleNum] != null)
			multiplier[sampleNum].delete();
		multiplier[sampleNum] = null;

		if (myLinearLag[sampleNum] != null)
			myLinearLag[sampleNum].delete();
		myLinearLag[sampleNum] = null;

		if (myLinearLag2[sampleNum] != null)
			myLinearLag2[sampleNum].delete();
		myLinearLag2[sampleNum] = null;

		if (myLinearLag3[sampleNum] != null)
			myLinearLag3[sampleNum].delete();
		myLinearLag3[sampleNum] = null;

	}

	// Start all circuits.
	public static void startEngine() {
		for (int sampleNum = 0; sampleNum < count; sampleNum++) {
			startCircuit(sampleNum);
		}
	}

	// Stop all circuits
	public static void stopEngine() {
		
		// System.out.println("count: " + count);
		for (int sampleNum = 0; sampleNum < count; sampleNum++) {
			stopCircuit(sampleNum, 0);
			deleteCircuit(sampleNum);
		}
	}

	// called on exit
	public static void stop() {

		try {

			// Delete unit peers.
			stopEngine();
			
			count = 0; // ie java and mac don't do this on restart...

			// Turn off tracing.
			Synth.setTrace(Synth.SILENT);

		} catch (SynthException e) {

			throw new SoniaException(e);
		}
	}

}
