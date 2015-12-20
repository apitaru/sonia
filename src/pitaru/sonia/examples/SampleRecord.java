package pitaru.sonia.examples;

import pitaru.sonia.*;
import processing.core.PApplet;

public class SampleRecord extends PApplet {
	
	Sample mySampleObj; // The Sonia Sample object which we'll record into

	public void settings() { 
		
	  size(400,200);
	}
	
	public void setup(){ 
		
	  background(0,50,0);
	  
	  Sonia.start(this); 
	  
	  LiveInput.start();  // start the liveInput engine (see liveInput example)
	  
	  // Create an empty Sample object with 441000 frames (ten seconds of data).
	  mySampleObj = new Sample(44100 * 10);  
	} 
	 
	public void draw() {
	    // do nothing
	} 

	public void mousePressed() {
		
	  LiveInput.startRec(mySampleObj); // Record LiveInput data into the Sample object. 
	  // The recording will automatically end when all of the Sample's frames are filled with data. 
	  print("REC");
	} 

	public void mouseReleased(){
		
	  LiveInput.stopRec(mySampleObj); 
	  mySampleObj.play();
	  print("PLAY");
	} 
	
	public static void main(String[] args) {
		PApplet.main(new String[] { SampleRecord.class.getName() });
	}

}
