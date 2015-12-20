package pitaru.sonia.examples;

import pitaru.sonia.*;
import processing.core.PApplet;

// Sample Control for Processing 3.x
// Description: Loads and manipulates the Pitch and Panning of a sample object
// Instructions: Move the mouse Up/Down to control the sample-pitch, and
// sideways for the panning.
// By: Amit Pitaru on July 16th 2005

public class SampleControl extends PApplet {


	Sample mySample;

	public void settings() {
		
		size(512, 200);
	}

	public void setup() {

		Sonia.start(this);
		mySample = new Sample("sine.aiff");
	}

	public void draw() {

		background(0, 30, 0);
		strokeWeight(1);
		
		// if sample is playing (or looping), do this...
		if (mySample.isPlaying()) {
			background(0, 40, 0);
		}

		setRate(); // use mouseY to control sample-rate playback
		setPan(); // use mouseX to control sample Panning
		
		drawScroller();
	}

	public void mousePressed() {
		// loop the sample
		mySample.repeat();
	}

	public void mouseReleased() {
		// Stop the sample, and unload it form memory in 1 frames (each frame is
		// about 1 ms).
		mySample.stop(1);
	}

	void setPan() {
		// set the pan of the sample object.
		// Range: float from -1 to 1 .... -1 -> left, 0 -> balanced ,1 -> right
		// notes: only works with MONO samples. Pan for Stereo support in next
		// version.
		float pan = -1f + mouseX / (width / 2f);
		mySample.setPan(pan);

	}

	void setRate() {
		// set the speed (sampling rate) of the sample.
		// Values:
		// 0 -> very low pitch (slow playback).
		// 88200 -> very high pitch (fast playback).
		float rate = (height - mouseY) * 88200 / (height);
		mySample.setRate(rate);
	}

	// Draw a scroller that shows the current sample-frame being played.
	// Notice how the sample plays faster when the Sample-Rate is
	// higher.(controlled by mouseY)

	void drawScroller() {
		strokeWeight(1);
		stroke(0, 255, 0);

		// figure out which percent of the sample has been played.
		float percent = mySample.getCurrentFrame() * 100f / mySample.getNumFrames();
		// calculate the marker position
		float marker = percent * width / 100f;
		// draw...
		line(marker, 0, marker, 20);
		line(0, 10, width, 10);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { SampleControl.class.getName() });
	}

}
