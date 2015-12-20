// Sonia
// Oct 5 03.
// V 2.7
// by: Amit Pitaru (c) 2003-2004
// You may use this code at will. If you include all/parts of it in your project, please mention where you got it: "Sonia by Amit Pitaru, http://pitaru.com"
// If you modify/expand the code and make it better, please send me improved versions: sonia@pitaru.com

package pitaru.sonia;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.softsynth.jsyn.*;
import com.softsynth.jsyn.util.WAVFileWriter;

public class Sample {

	public int id = 0;
	public int channels;
	public int startF;
	public int endF;
	public int state;
	public int movedF_Buff;
	public float rate;
	public int framesWhenDone;
	
	protected int lastFrame = 0;

	public static FFTutils fft = new FFTutils(1024);
	public float[] spectrum;

	public Sample(String fileName) {

		BJSyn.loadSample(BJSyn.count, fileName);
		id = BJSyn.count;
		channels = BJSyn.channelNum;
		rate = (float) BJSyn.mySamp[id].getSampleRate();

		state = 0;
		startF = 0;
		endF = 0;
		movedF_Buff = 0;
		setVolume(1);
		setRate(rate);
		BJSyn.count += channels;
		if (BJSyn.count == BJSyn.maxSamples)
			BJSyn.count = 0;
	}

	public Sample(int frameNum, int _rate) {

		initEmptySample(frameNum, _rate, 1);
	}

	public Sample(int frameNum, int _rate, int _channels) {
		// Sonia.initJsyn(44100,0);
		initEmptySample(frameNum, _rate, _channels);
	}

	public Sample(int frameNum) {
		// Sonia.initJsyn(44100,0);
		int _rate = (int) Synth.getFrameRate();
		initEmptySample(frameNum, _rate, 1);
	}

	public void initEmptySample(int frameNum, int _rate, int _channels) {
		rate = _rate;

		id = BJSyn.count;
		channels = Math.max(Math.min(_channels, 2), 1); // channels =
																										// BJSyn.channelNum;

		for (int i = 0; i < channels; i++) {
			BJSyn.buildEmptySamp(BJSyn.count, frameNum, _rate);
			BJSyn.buildCircuit(BJSyn.count);
			BJSyn.count++;
			if (BJSyn.count == BJSyn.maxSamples)
				BJSyn.count = 0;
			setVolume(1, i);
			setRate(rate, i);
			if (channels > 1) {
				if (i == 0)
					setPan(-1, i);
				else
					setPan(1, i);
			}
		}

		state = 0;
		startF = 0;
		endF = 0;
		movedF_Buff = 0;

		// BJSyn.count += channels;
	}

	public int getID() {
		return id;
	}

	public int getNumChannels() {
		return channels;
	}

	public float[] getSpectrum(int nSamples, int frame) {

		int part = 0;

		if (fft.WS2 != nSamples * 2) {
			fft = new FFTutils(nSamples * 2);
		}
		fft.useEnvelope(true, 1.5f);
		fft.useEqualizer(true);

		short[] _signal = new short[nSamples * 2];
		float[] signal = new float[nSamples * 2];

		if (BJSyn.mySamp[id + part].getNumFrames() - nSamples * 2 >= frame) {
			BJSyn.mySamp[id + part].read(frame, _signal, 0, nSamples * 2);
			convertData(_signal, signal);
			spectrum = new float[nSamples / 2];
			spectrum = fft.computeFFT(signal);
		}

		return spectrum;
	}

	public float[] getSpectrum(int nSamples) {
		return getSpectrum(nSamples, getCurrentFrame());

	}

	public void setVolume(float v) {
		BJSyn.setVolume(id, v);
		if (channels == 2)
			BJSyn.setVolume(id + 1, v);
	}

	public void setVolume(float v, int part) {
		if (part < channels && part >= 0) {
			BJSyn.setVolume(id + part, v);

		}
	}

	public float getVolume() {
		return (float) BJSyn.getVolume(id);
	}

	public float getVolume(int part) {
		if (part < channels && part >= 0) {
			return (float) BJSyn.getVolume(id + part);
		} else {
			return 0;
		}
	}

	public int getRate() {
		return (int) BJSyn.getRate(id);
	}

	public float getRate(int part) {
		if (part < channels && part >= 0) {
			return (float) BJSyn.getRate(id + part);
		} else {
			return 0;
		}
	}

	public void setRate(float v) {
		BJSyn.setRate(id, v);
		if (channels == 2)
			BJSyn.setRate(id + 1, v);
	}

	public void setRate(float v, int part) {
		if (part < channels && part >= 0) {
			BJSyn.setRate(id + part, v);
		}
	}

	public float getSpeed() {
		return (float) (BJSyn.getRate(id) / rate);
	}

	public float getSpeed(int part) {
		if (part < channels && part >= 0) {
			return (float) (BJSyn.getRate(id + part) / rate);
		} else {
			return 0;
		}
	}

	public void setSpeed(float v) {
		BJSyn.setRate(id, (float) (v * rate));
		if (channels == 2)
			BJSyn.setRate(id + 1, (float) (v * rate));
	}

	public void setSpeed(float v, int part) {
		if (part < channels && part >= 0) {
			BJSyn.setRate(id + part, (float) (v * rate));

		}
	}

	public void setPan(float v) {
		setPan(v, 0);
	}

	public void setPan(float v, int part) {
		if (part < channels && part >= 0) {
			BJSyn.setPan(id + part, v);

		}
	}

	public float getPan() {
		return getPan(0);
	}

	public float getPan(int part) {
		if (part < channels && part >= 0) {
			return (float) BJSyn.getPan(id + part);
		} else {
			return 0;
		}
	}

	public void repeat() {
		repeat(0, getNumFrames());
	}

	public void repeat(int start, int end) {
		state = 2;
		startF = start;
		endF = end;
		movedF_Buff = getFramesMoved();
		BJSyn.loopSample(id, start, end);
		if (channels == 2)
			BJSyn.loopSample(id + 1, start, end);

	}

	public void repeatNum(int num, int start, int end) {
		state = 2;
		startF = start;
		endF = end;
		movedF_Buff = getFramesMoved();

		BJSyn.loopSampleNum(num, id, start, end);
		if (channels == 2)
			BJSyn.loopSampleNum(num, id + 1, start, end);
	}

	public void repeatNum(int num) {
		repeatNum(num, 0, getNumFrames());
	}

	public void play(int start, int end) {
		state = 1;
		startF = start;
		endF = end;
		movedF_Buff = getFramesMoved();
		framesWhenDone = movedF_Buff + end - start;

		BJSyn.playSample(id, start, end);
		if (channels == 2)
			BJSyn.playSample(id + 1, start, end);

	}

	public void play() {
		play(0, getNumFrames());

	}

	public void stop(boolean stopFlag, int stopOffset) {
		state = 0;
		BJSyn.stopSample(id, stopFlag, stopOffset);
		if (channels == 2)
			BJSyn.stopSample(id + 1, stopFlag, stopOffset);
	}

	public void stop(int stopOffset) {
		stop(true, stopOffset);
	}

	public void stop() {
		stop(true, 0); // used to be false - changed because smaple doesn't stop on
										// xp
	}

	public void delete() {
		stop(0);
		BJSyn.deleteCircuit(id);
		if (channels == 2)
			BJSyn.deleteCircuit(id + 1);
	}

	public int getNumFrames() {
		int i = 0;
		try {
			i = (int) BJSyn.getNumFrames(id);
		} catch (SynthException e) {
			System.out.print(e);
		}

		return i;
	}

	int getRangeFrames() {
		int i = 0;
		if (getNumFrames() > 0) {
			i = endF - startF;
		}
		return i;
	}

	public int getCurrentFrame() {
		int i = 0;
		try {
			if (getFramesMoved() > 0 && getRangeFrames() > 0) {
				i = startF + ((getFramesMoved() - movedF_Buff) % getRangeFrames());
			} else {
				i = 0;
			}
		} catch (SynthException e) {
			System.out.print(e);

		}

		return i;

	}

	public int getFramesMoved() {
		int i = 0;
		try {
			i = BJSyn.mySampler[id].samplePort.getNumFramesMoved();
		} catch (SynthException e) {
			System.out.print(e);

		}
		return i;

	}

	// READ
	// FLOATS

	public void readChannel(int part, float[] data, int firstDataFrame,
			int firstSampleFrame, int numFrames) {
		short[] data_s = new short[numFrames];
		BJSyn.mySamp[id + part].read(firstSampleFrame, data_s, 0, numFrames);
		convertData(data_s, data, numFrames, firstSampleFrame);
	}

	public void readChannel(int part, float[] data, int firstSampleFrame) {
		readChannel(part, data, 0, firstSampleFrame, data.length);
	}

	public void readChannel(int part, float[] data) {
		readChannel(part, data, 0, 0, data.length);
	}

	public void read(float[] data, int firstDataFrame, int firstSampleFrame,
			int numFrames) {
		readChannel(0, data, firstDataFrame, firstSampleFrame, numFrames);
	}

	public void read(float[] data, int firstSampleFrame) {
		readChannel(0, data, 0, firstSampleFrame, data.length);
	}

	public void read(float[] data) {
		readChannel(0, data, 0, 0, data.length);
	}

	// SHORT

	public void readChannel(int part, short[] data_s, int firstDataFrame,
			int firstSampleFrame, int numFrames) {
		BJSyn.mySamp[id + part].read(firstSampleFrame, data_s, 0, numFrames);
	}

	public void readChannel(int part, short[] data_s, int firstSampleFrame) {
		readChannel(part, data_s, 0, firstSampleFrame, data_s.length);
	}

	public void readChannel(int part, short[] data_s) {
		readChannel(part, data_s, 0, 0, data_s.length);
	}

	public void read(short[] data_s, int firstDataFrame, int firstSampleFrame,
			int numFrames) {
		readChannel(0, data_s, firstDataFrame, firstSampleFrame, numFrames);
	}

	public void read(short[] data_s, int firstSampleFrame) {
		readChannel(0, data_s, 0, firstSampleFrame, data_s.length);
	}

	public void read(short[] data_s) {
		readChannel(0, data_s, 0, 0, data_s.length);
	}

	// WROTE
	// FLOAT

	public void writeChannel(int part, float[] data, int firstDataFrame,
			int firstSampleFrame, int numFrames) {
		BJSyn.mySamp[id + part].write(firstSampleFrame, convertData(data), 0,
				numFrames);
	}

	public void writeChannel(int part, float[] data, int firstSampleFrame) {
		writeChannel(part, data, 0, firstSampleFrame, data.length);
	}

	public void writeChannel(int part, float[] data) {
		writeChannel(part, data, 0, 0, data.length);
	}

	public void write(float[] data, int firstDataFrame, int firstSampleFrame,
			int numFrames) {
		writeChannel(0, data, firstDataFrame, firstSampleFrame, numFrames);
	}

	public void write(float[] data, int firstSampleFrame) {
		writeChannel(0, data, 0, firstSampleFrame, data.length);
	}

	public void write(float[] data) {
		writeChannel(0, data, 0, 0, data.length);
	}

	// SHORT

	public void writeChannel(int part, short[] data_s, int firstDataFrame,
			int firstSampleFrame, int numFrames) {
		BJSyn.mySamp[id + part].write(firstSampleFrame, data_s, 0, numFrames);
	}

	public void writeChannel(int part, short[] data_s, int firstSampleFrame) {
		writeChannel(part, data_s, 0, firstSampleFrame, data_s.length);
	}

	public void writeChannel(int part, short[] data_s) {
		writeChannel(part, data_s, 0, 0, data_s.length);
	}

	public void write(short[] data_s, int firstDataFrame, int firstSampleFrame,
			int numFrames) {
		writeChannel(0, data_s, firstDataFrame, firstSampleFrame, numFrames);
	}

	public void write(short[] data_s, int firstSampleFrame) {
		writeChannel(0, data_s, 0, firstSampleFrame, data_s.length);
	}

	public void write(short[] data_s) {
		writeChannel(0, data_s, 0, 0, data_s.length);
	}

	static public short[] convertData(float[] data, int numFrames, int firstFrame) {
		short[] data_s = new short[numFrames];
		for (int i = 0; i < numFrames; i++) {
			data_s[i] = (short) ((data[i + firstFrame] * Sonia.valueMod));
		}
		return data_s;
	}

	static public short[] convertData(float[] data) {
		short[] data_s = new short[data.length];
		for (int i = 0; i < data.length; i++) {
			data_s[i] = (short) ((data[i] * Sonia.valueMod));
		}
		return data_s;
	}

	static public void convertData(short[] data_s, float[] data, int numFrames,
			int firstFrame) {
		for (int i = 0; i < numFrames; i++) {
			data[i + firstFrame] = (float) ((data_s[i] / Sonia.valueMod));
		}
	}

	static public void convertData(short[] data_s, float[] data) {
		for (int i = 0; i < data_s.length; i++) {
			data[i] = (float) ((data_s[i] / Sonia.valueMod));
		}
	}

	public void connectLiveInput(boolean flag) {

		if (flag) {
			LiveInput.myBusWriter[id] = new BusWriter();
			LiveInput.myBusWriter[id].start();
			BJSyn.mySampler[id].output.connect(LiveInput.myBusWriter[id].input);
			LiveInput.myBusWriter[id].busOutput
					.connect(LiveInput.myBusReader.busInput);

			if (channels == 2) {
				LiveInput.myBusWriter[id + 1] = new BusWriter();
				LiveInput.myBusWriter[id + 1].start();
				BJSyn.mySampler[id + 1].output
						.connect(LiveInput.myBusWriter[id + 1].input);
				LiveInput.myBusWriter[id + 1].busOutput
						.connect(LiveInput.myBusReader.busInput);
			}
		} else {
			LiveInput.myBusWriter[id].stop();
			LiveInput.myBusWriter[id].input.disconnect();
			LiveInput.myBusWriter[id].busOutput.disconnect();
			if (channels == 2) {
				LiveInput.myBusWriter[id + 1].stop();
				LiveInput.myBusWriter[id + 1].input.disconnect();
				LiveInput.myBusWriter[id + 1].busOutput.disconnect();
			}

		}
	}

	public boolean isPlaying() {
		switch (state) {
		case 0:
			return false;
		case 1:
			if (framesWhenDone - getFramesMoved() <= 1) {
				state = 0;
				return false;
			}
			return true;
		case 2:
			return true;
		default:
			return false;
		}
	}

	public void saveFile(String fileName) {
		try {
			RandomAccessFile rfile = new RandomAccessFile(fileName + ".wav", "rw");
			WAVFileWriter wavWriter = new WAVFileWriter(rfile);

			// wavWriter.setLength(0); // only supported in Java 1.2 !!!

			if (channels == 1) {
				// create an array of shorts and fill it with a sawtooth wave
				short data[] = new short[getNumFrames()];
				read(data);

				// write the data to a file
				wavWriter.write(data, 1, getRate());
				wavWriter.close();

			} else if (channels == 2) {
				short data[] = new short[getNumFrames() * 2];
				short dataL[] = new short[getNumFrames()];
				short dataR[] = new short[getNumFrames()];

				read(dataL);
				BJSyn.mySamp[id + 1].read(dataR);

				for (int i = 0; i < getNumFrames() * 2; i += 2) {
					data[i] = dataL[i / 2];
					data[i + 1] = dataR[i / 2];
				}

				// write the data to a file
				wavWriter.write(data, 2, getRate());
				wavWriter.close();
			}

			// System.out.println("Wrote testout.wav");
		} catch (IOException e) {
			throw new SoniaException(e);
		}

	}

}
