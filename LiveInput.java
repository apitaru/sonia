// Helper-library for BJsyn, objectifying the engine.
// Feb 15 04.
// by: Amit Pitaru (c) 2003-2004
// You may use this code at will. If you include all/parts of it in your project, please mention where you got it: "Processing-Jsyn tutorial by Amit Pitaru, http://pitaru.com"
// If you modify/expand the code and make it better, please send me improved versions: amit@pitaru.com

package pitaru.sonia_v29b;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.applet.Applet;
import com.softsynth.jsyn.*;

public class LiveInput{

//static PeakFollower followerL,followerR ;
//static LineIn myIn;
static ChannelIn[] chIn = new ChannelIn[Sonia.inDevChNum];
static SynthSample sample;
static SampleWriter sampleWriter;
//static AddUnit bus;
static BusReader myBusReader;
static int maxSamples = BJSyn.maxSamples;
static BusWriter[] myBusWriter = new BusWriter[4 + maxSamples];
static PeakFollower followerL,followerR ;
static SampleWriter[] recorder = new SampleWriter[maxSamples];

//static fft _fft = new fft();
static FFTutils _FFTutils;// = new FFTutils();
public static float[] spectrum;
static short[] _signal; //= new short[512];
public static float[] signal; //= new float[512];
//static SynthContext inputSynthContext;

static boolean doOnce;
public static boolean micFlag = true;

static int state = 0;


public LiveInput(){
}

public static void start(int rate, int nSamples){

	//System.out.println("LiveInput.start count:" + Synth.openCount);

if(micFlag){
	if ( Synth.openCount == 1 ){
		//Sonia.stop();
		Synth.stop();
		int i = 0;
		while(i < 5){
		  i++;
		}
		//Synth.start(Synth.FLAG_ENABLE_INPUT,rate);
		Synth.start( 0, rate ,Sonia.inDevID, Sonia.inDevChNum, Sonia.outDevID, Sonia.outDevChNum);
		BJSyn.startEngine();

	} else if( Synth.openCount == 0 ){
		Sonia.initJsyn(rate, Synth.FLAG_ENABLE_INPUT);
	}
}


	doOnce= true;
	state = 1;

	//Sonia.initJsyn((int)Synth.getFrameRate(),Synth.FLAG_ENABLE_INPUT);

	// create a unique, non-static, SynthContext
	//inputSynthContext = new SynthContext();

	// create and start a unique SynthContext
	//inputSynthContext.startEngine(Synth.FLAG_ENABLE_INPUT, rate);

	// get the time in ticks for a unique SynthContext
	//int time = inputSynthContext.getTickCount();

	_FFTutils = new FFTutils(nSamples*2);


	signal = new float[nSamples*2];
	_signal = new short[nSamples*2];

	sample = new SynthSample(nSamples*2 + 1);
	sampleWriter = new SampleWriter_16F1();
	//myIn = new LineIn();
	//bus =new AddUnit();

	myBusReader = new BusReader();

	myBusWriter[0 + maxSamples] = new BusWriter();
	//myBusWriter[1 + maxSamples] = new BusWriter();
	myBusWriter[0 + maxSamples].start();
	//myBusWriter[1 + maxSamples].start();

	myBusWriter[0 + maxSamples].busOutput.connect( myBusReader.busInput );
	//myBusWriter[1 + maxSamples].busOutput.connect( myBusReader.busInput );

	chIn[0] = new ChannelIn(0);
	////chIn[1] = new ChannelIn(1);

	chIn[0].output.connect(myBusWriter[maxSamples].input);
	//chIn[1].output.connect(myBusWriter[1+ maxSamples].input);

	chIn[0].start();
	//chIn[1].start();

	////myIn.output.connect(0,bus.inputA,0);
	////myIn.output.connect(1,bus.inputB,0);
	////bus.output.connect(sampleWriter.input);


	myBusReader.output.connect( sampleWriter.input);


	followerL = new PeakFollower();
	followerR = new PeakFollower();



	chIn[0].output.connect(followerL.input);
	//chIn[1].output.connect(followerR.input);


	myBusReader.start();

	followerL.start();
    followerR.start();


	//myIn.start();
	sampleWriter.start();
	//bus.start();

	sampleWriter.samplePort.queueLoop(sample);



}


public static void start(){
	start(1024);
}

public static void start(int nSamples){
	if ( Synth.openCount >= 1 ){
		start((int)Synth.getFrameRate(), nSamples);
	} else if ( Synth.openCount == 0 ){
		start(44100, nSamples);
	}
}


public static void start(boolean _micFlag){
	micFlag = _micFlag;
}

public static void start(int nSamples, boolean _micFlag){
	micFlag = _micFlag;
	start(nSamples);
}

public static void start(boolean _micFlag, int nSamples){
	micFlag = _micFlag;
	start(nSamples);
}



public static void stop(){
	state = 0;
	sampleWriter.stop();
	sampleWriter.delete();
	sampleWriter = null;
	sample.delete();
	sampleWriter = null;
	//myIn.stop();
	//myIn.delete();
	//myIn = null;
	//bus.stop();
	//bus.delete();
	//bus = null;
	followerR.stop();
	followerR.delete();
	followerR = null;
	followerL.stop();
	followerL.delete();
	followerL = null;

	//inputSynthContext.stopEngine();

}

public static int startframe= 0;

public static float[] getSignal(){

	sample.read(0,_signal, 0, _signal.length);
	int framesMoved = sampleWriter.samplePort.getNumFramesMoved();
	startframe = ((framesMoved)%(_signal.length));
	 for(int i = 0; i < _signal.length; i++){
	      signal[i] = (float)(_signal[(i+startframe)%(_signal.length)]);
	 }
	spectrum = new float [_signal.length/2];

	return signal;
}

public static float[] getSpectrum(){

      spectrum = _FFTutils.computeFFT(getSignal());

      return spectrum;
}




public static float[] getSpectrum(boolean flag){
	useEqualizer(flag);
	useEnvelope(flag);
	return 	getSpectrum();
}

public static float[] getSpectrum(boolean flag, float env){

		useEqualizer(flag);
		useEnvelope(flag, env);
		return getSpectrum();
}



public static void useEqualizer(boolean flag){
  	_FFTutils.useEqualizer(flag);
}

public static void useEnvelope(boolean flag, float env){
	if(flag){
   	 _FFTutils.useEnvelope(true,env);
	}else {
	 _FFTutils.useEnvelope(false,env);
	}
}

public static void useEnvelope(boolean flag){
	useEnvelope(flag,1.5f);
}




public static float getLevel(){
   return (float)((followerL.output.get() + followerR.output.get() )/2d);
}

public static float getLevelLeft(){
   return (float)(followerL.output.get() );
}

public static float getLevelRight(){
   return (float)(followerR.output.get()) ;
}

public static float getLevel(int pan){
	if ( pan == 0){
   		return (float)(followerL.output.get());
	} else if ( pan == 1){
		return (float)(followerR.output.get());
	} else {
		return 0;
	}
}



public static void prepareRecorder(Sample s, int id){
	//if( s.getNumChannels() == 1){
			recorder[id] = new SampleWriter_16F1();
			myBusReader.output.connect(recorder[id].input);
   // } else if (s.getNumChannels() == 2){
			//recorder = new SampleWriter_16F2();
			//myBusReader.output.connect(0,recorder.input,0);
			//myBusReader.output.connect(0,recorder.input,1);

	//}

}




public static void startRec ( Sample s ){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
		prepareRecorder(s,id + i);
		recorder[id+i].start();
		recorder[id+i].samplePort.queue(BJSyn.mySamp[id + i]);
	}

}



public static void startRec ( Sample s, int start, int end ){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
	prepareRecorder(s,id + i);
	recorder[id+i].start();
	recorder[id+i].samplePort.queue(BJSyn.mySamp[id+ i], start, end - start);
	}

}



public static void startRecLoop ( Sample s ){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
	prepareRecorder(s,id+i);
	recorder[id+i].start();
	recorder[id+i].samplePort.queueLoop(BJSyn.mySamp[id+i]);
	}
}


public static void startRecLoop ( Sample s , int start){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
	prepareRecorder(s,id+i);
	recorder[id+i].start();
	recorder[id+i].samplePort.queue(BJSyn.mySamp[id+i], start, BJSyn.mySamp[id+i].getNumFrames() - start);
	recorder[id+i].samplePort.queueLoop(BJSyn.mySamp[id+i]);
}
}


public static void startRecLoop ( Sample s , int start, int end){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
	prepareRecorder(s,id+i);
	recorder[id+i].start();
	recorder[id+i].samplePort.queueLoop(BJSyn.mySamp[id+i], start, end - start);
}
}



public static void stopRec ( Sample s ){
	int id = s.getID();
	for(int i = 0; i < s.getNumChannels(); i++){
	recorder[id+i].samplePort.queueOff(BJSyn.mySamp[id+i]);
	recorder[id+i].samplePort.clear();
	recorder[id+i].stop();
}
}





}



