// Helper-library for BJsyn, objectifying the engine.
// Feb 15 04.
// by: Amit Pitaru (c) 2003-2004
// You may use this code at will. If you include all/parts of it in your project, please mention where you got it: "Processing-Jsyn tutorial by Amit Pitaru, http://pitaru.com"
// If you modify/expand the code and make it better, please send me improved versions: amit@pitaru.com

package pitaru.sonia;

import com.softsynth.jsyn.*;

public class LiveInput {
	
	static ChannelIn[] chIn = new ChannelIn[Sonia.inDevChNum];
	static SynthSample sample;
	static SampleWriter sampleWriter;
	static BusReader myBusReader;
	static BusWriter[] myBusWriter = new BusWriter[4 + Sonia.MAX_SAMPLES];
	static PeakFollower followerL, followerR;
	static SampleWriter[] recorder = new SampleWriter[Sonia.MAX_SAMPLES];

	static FFTutils _FFTutils;
	public static float[] spectrum;
	static short[] _signal;
	public static float[] signal;

	static boolean doOnce;
	public static boolean micFlag = true;

	static int state = 0;

	public LiveInput() {}

	public static void start(int rate, int nSamples) {

		// System.out.println("LiveInput.start count:" + Synth.openCount);

		if (micFlag) {
			if (Synth.openCount == 1) {
				Synth.stop();
				int i = 0;
				while (i < 5) {
					i++;
				}
				Synth.start(0, rate, Sonia.inDevID, Sonia.inDevChNum, 
						Sonia.outDevID, Sonia.outDevChNum);
				BJSyn.startEngine();

			} else if (Synth.openCount == 0) {
				
				Sonia.initJsyn(rate, Synth.FLAG_ENABLE_INPUT);
			}
		}

		doOnce = true;
		state = 1;

		_FFTutils = new FFTutils(nSamples * 2);

		signal = new float[nSamples * 2];
		_signal = new short[nSamples * 2];

		sample = new SynthSample(nSamples * 2 + 1);
		sampleWriter = new SampleWriter_16F1();

		myBusReader = new BusReader();

		myBusWriter[Sonia.MAX_SAMPLES] = new BusWriter();
		myBusWriter[Sonia.MAX_SAMPLES].start();

		myBusWriter[Sonia.MAX_SAMPLES].busOutput.connect(myBusReader.busInput);

		chIn[0] = new ChannelIn(0);
		chIn[0].output.connect(myBusWriter[Sonia.MAX_SAMPLES].input);
		chIn[0].start();

		myBusReader.output.connect(sampleWriter.input);

		followerL = new PeakFollower();
		followerR = new PeakFollower();

		chIn[0].output.connect(followerL.input);

		myBusReader.start();

		followerL.start();
		followerR.start();

		sampleWriter.start();

		sampleWriter.samplePort.queueLoop(sample);
	}

	public static void start() {
		start(1024);
	}

	public static void start(int nSamples) {
		if (Synth.openCount >= 1) {
			start((int) Synth.getFrameRate(), nSamples);
		} else if (Synth.openCount == 0) {
			start(44100, nSamples);
		}
	}

	public static void start(boolean _micFlag) {
		micFlag = _micFlag;
	}

	public static void start(int nSamples, boolean _micFlag) {
		micFlag = _micFlag;
		start(nSamples);
	}

	public static void start(boolean _micFlag, int nSamples) {
		micFlag = _micFlag;
		start(nSamples);
	}

	public static void stop() {
		state = 0;
		sampleWriter.stop();
		sampleWriter.delete();
		sampleWriter = null;
		sample.delete();
		sampleWriter = null;

		followerR.stop();
		followerR.delete();
		followerR = null;
		followerL.stop();
		followerL.delete();
		followerL = null;
	}

	public static int startframe = 0;

	public static float[] getSignal() {

		if (Sonia.EXITING) return new float[0];
		
		sample.read(0, _signal, 0, _signal.length);
		int framesMoved = sampleWriter.samplePort.getNumFramesMoved();
		startframe = ((framesMoved) % (_signal.length));
		for (int i = 0; i < _signal.length; i++) {
			signal[i] = (_signal[(i + startframe) % (_signal.length)]);
		}
		spectrum = new float[_signal.length / 2];

		return signal;
	}

	public static float[] getSpectrum() {
		
		if (Sonia.EXITING) return new float[0];
		
		spectrum = _FFTutils.computeFFT(getSignal());

		return spectrum;
	}

	public static float[] getSpectrum(boolean flag) {
		
		useEqualizer(flag);
		useEnvelope(flag);
		return getSpectrum();
	}

	public static float[] getSpectrum(boolean flag, float env) {

		useEqualizer(flag);
		useEnvelope(flag, env);
		return getSpectrum();
	}

	public static void useEqualizer(boolean flag) {
		_FFTutils.useEqualizer(flag);
	}

	public static void useEnvelope(boolean flag, float env) {
		if (flag) {
			_FFTutils.useEnvelope(true, env);
		} else {
			_FFTutils.useEnvelope(false, env);
		}
	}

	public static void useEnvelope(boolean flag) {
		useEnvelope(flag, 1.5f);
	}

	public static float getLevel() {
		return Sonia.EXITING ? 0 : (float) ((followerL.output.get() + followerR.output.get()) / 2d);
	}

	public static float getLevelLeft() {
		return Sonia.EXITING ? 0 : (float) (followerL.output.get());
	}

	public static float getLevelRight() {
		return Sonia.EXITING ? 0 : (float) (followerR.output.get());
	}

	public static float getLevel(int pan) {
		if (pan == 0) {
			return (float) (followerL.output.get());
		} else if (pan == 1) {
			return (float) (followerR.output.get());
		} else {
			return 0; // really?
		}
	}

	public static void prepareRecorder(Sample s, int id) {

		recorder[id] = new SampleWriter_16F1();
		myBusReader.output.connect(recorder[id].input);
	}

	public static void startRec(Sample s) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			prepareRecorder(s, id + i);
			recorder[id + i].start();
			recorder[id + i].samplePort.queue(BJSyn.mySamp[id + i]);
		}

	}

	public static void startRec(Sample s, int start, int end) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			prepareRecorder(s, id + i);
			recorder[id + i].start();
			recorder[id + i].samplePort.queue(BJSyn.mySamp[id + i], start, end
					- start);
		}

	}

	public static void startRecLoop(Sample s) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			prepareRecorder(s, id + i);
			recorder[id + i].start();
			recorder[id + i].samplePort.queueLoop(BJSyn.mySamp[id + i]);
		}
	}

	public static void startRecLoop(Sample s, int start) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			prepareRecorder(s, id + i);
			recorder[id + i].start();
			recorder[id + i].samplePort.queue(BJSyn.mySamp[id + i], start,
					BJSyn.mySamp[id + i].getNumFrames() - start);
			recorder[id + i].samplePort.queueLoop(BJSyn.mySamp[id + i]);
		}
	}

	public static void startRecLoop(Sample s, int start, int end) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			prepareRecorder(s, id + i);
			recorder[id + i].start();
			recorder[id + i].samplePort.queueLoop(BJSyn.mySamp[id + i], start, end
					- start);
		}
	}

	public static void stopRec(Sample s) {
		int id = s.getID();
		for (int i = 0; i < s.getNumChannels(); i++) {
			recorder[id + i].samplePort.queueOff(BJSyn.mySamp[id + i]);
			recorder[id + i].samplePort.clear();
			recorder[id + i].stop();
		}
	}

}
