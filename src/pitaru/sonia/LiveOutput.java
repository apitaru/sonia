package pitaru.sonia;

import java.lang.reflect.Method;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.util.SampleQueueOutputStream;

public class LiveOutput implements Runnable {

	public static SampleReader_16F1 mySampler;
	public static LineOut myOut;

	public static int FRAMES_IN_BUFFER;// = 8*1024;
	public static int NUM_CHANNELS = 1;
	public static int SAMPLES_PER_BLOCK;// = FRAMES_PER_BLOCK * NUM_CHANNELS;
	public static int FRAMES_PER_BLOCK;// = 1024; // number of frames to synthesize at one time

	public static short[] directData;// = new short[SAMPLES_PER_BLOCK];
	public static float[] data;
	
	public static SampleQueueOutputStream outStream;
	public static Thread t1;
	
	public static int SLEEP = 5;
	public static int state = 0;

	public LiveOutput() {}

	public static void start(int block) {
		start(block, block);
	}

	public static void start(int block, int buffer) {
		try {

			FRAMES_PER_BLOCK = block;
			FRAMES_IN_BUFFER = buffer;
			SAMPLES_PER_BLOCK = FRAMES_PER_BLOCK * NUM_CHANNELS;
			directData = new short[SAMPLES_PER_BLOCK];
			data = new float[SAMPLES_PER_BLOCK];

			// Create SynthUnits to play sample data.
			if (NUM_CHANNELS == 1) {
				mySampler = new SampleReader_16F1();
			} else {
				throw new RuntimeException("This example only support mono or stereo!");
			}
			
			myOut = new LineOut();

			// if(valueMod == 0) mySampler.amplitude.set(1);
			mySampler.amplitude.set(1);
			// Create a stream that we can write to.
			outStream = new SampleQueueOutputStream(mySampler.samplePort,
					FRAMES_IN_BUFFER, NUM_CHANNELS);

			// Connect SynthUnits to output.
			mySampler.output.connect(0, myOut.input, 0);
			mySampler.output.connect(NUM_CHANNELS - 1, myOut.input, 1);

			// Start execution of units.
			myOut.start();
			mySampler.start();
		} catch (SynthException e) {
			
			throw new SoniaException(e);
		}
	}

	public static void setSleep(int s) {
		SLEEP = s;
	}

	public void run() {
		
		try {
			
			while (t1 != null) {

				while (outStream.available() >= FRAMES_PER_BLOCK) {
					fireEvent();
					if (!Sonia.DIRECT_DATA)
						convertData();
					sendBuffer();
				}
				Thread.sleep(SLEEP);
			}
		} catch (Throwable e) {
			
			System.err.println("WARN] LiveOutput.run() caught " + e);
		}
	}

	/**
	 * Generate an audio signal and send it to the stream to be heard.
	 */
	public static void sendBuffer() {

		// Write data to the stream.
		// Will block if there is not enough room so run in a thread.
		outStream.write(directData, 0, FRAMES_PER_BLOCK);
	}

	public static void convertData() {

		for (int i = 0; i < data.length; i++) {
			directData[i] = (short) ((data[i] * Sonia.valueMod));
		}
	}

	public static void stop() {
		
		try {
			stopStream();
			t1 = null;

			// Delete unit peers.
			mySampler.delete();
			mySampler = null;
			myOut.delete();
			myOut = null;

		} catch (SynthException e) {
			throw new SoniaException(e);
		}
	}

	public static void startStream() {

		state = 1;

		// Prefill output stream buffer so that it starts out full.
		while (outStream.available() > FRAMES_PER_BLOCK) {
			fireEvent();
			if (!Sonia.DIRECT_DATA)
				convertData();
			sendBuffer();
		}

		// Start slightly in the future so everything is synced.
		int time = Synth.getTickCount() + 4;
		mySampler.start(time);
		outStream.start(time);

		// launch a thread to keep stream supplied with data
		LiveOutput lo = new LiveOutput();

		t1 = new Thread(lo);
		t1.start();

	}

	public static void stopStream() {

		state = 0;
		t1 = null;
		int time = Synth.getTickCount();
		mySampler.stop(time);
		outStream.stop(time);
	}

	public static void fireEvent() { // do we need this?
//		try {
//			
//			Method m = Sonia.host.getClass().getDeclaredMethod("liveOutputEvent");
//			m.invoke(Sonia.host);
//		} catch (Throwable e) {
//			System.err.println("[WARN] "+e.getMessage());
//		}
	}

	public static void connectLiveInput(boolean flag) {
		
		int id = LiveInput.maxSamples + 2;
		
		if (flag) {
			
			LiveInput.myBusWriter[id] = new BusWriter();
			LiveInput.myBusWriter[id].start();
			mySampler.output.connect(LiveInput.myBusWriter[id].input);
			LiveInput.myBusWriter[id].busOutput
					.connect(LiveInput.myBusReader.busInput);

		} else {
			
			LiveInput.myBusWriter[id].stop();
			LiveInput.myBusWriter[id].input.disconnect();
			LiveInput.myBusWriter[id].busOutput.disconnect();
		}
	}

}
