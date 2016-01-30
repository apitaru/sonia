package examples;

import pitaru.sonia.LiveInput;
import pitaru.sonia.Sonia;
import processing.core.PApplet;

//LiveInput example for Processing V3.x
//Description: Shows FFT spectrum and volume level for the active sound-input on your mahcine.
//For PC: use the 'Sounds & Audio Devices' menu in the control panel to choose your input; Mic, wave, etc.
//For Mac: the current microphone device will be used as input.
//By: Amit Pitaru,  July 16th 2005
public class InputLive extends PApplet {

	public void settings() {
		
		size(512, 200);
	}
	
	public void setup() {
		
		Sonia.start(this); // Start Sonia engine.
		LiveInput.start(256); // Start LiveInput and return 256 FFT frequency bands.
	}

	public void draw() {

		background(0, 30, 0);
		getMeterLevel(); // Show meter-level reading for Left/Right channels.
		getSpectrum(); // Show FFT reading

	}

	void getSpectrum() {
		
		strokeWeight(0);
		stroke(0, 230, 0);
		// populate the spectrum array with FFT values.
		LiveInput.getSpectrum();
		// draw a bar for each of the elements in the spectrum array.
		// Note - the current FFT math is done in Java and is very raw. expect
		// optimized alternative soon.
		for (int i = 0; i < LiveInput.spectrum.length; i++) {
			line(i * 2, height, i * 2, height - LiveInput.spectrum[i] / 10);
		}
	}

	void getMeterLevel() {
		
		// get Peak level for each channel (0 -> Left , 1 -> Right)
		// Value Range: float from 0.0 to 1.0
		// Note: use inputMeter.getLevel() to combine both channels (L+R) into one
		// value.
		float meterDataLeft = LiveInput.getLevel();
		float meterDataRight = LiveInput.getLevel();

		// draw a volume-meter for each channel.
		fill(0, 100, 0);
		float left = meterDataLeft * height;
		float right = meterDataRight * height;
		rect(width / 2 - 100, height, 100, left * -1);
		rect(width / 2, height, 100, right * -1);
	}

	
	public static void main(String[] args) {
		
		PApplet.main(new String[] { InputLive.class.getName() });
	}

}
