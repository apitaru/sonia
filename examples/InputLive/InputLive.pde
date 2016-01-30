import pitaru.sonia.*;

void setup() {
  
  size(512, 200);
  
  Sonia.start(this); 
  
  // Start LiveInput w' 256 FFT frequency bands.
  LiveInput.start(256);
}

void draw() {

  background(0, 30, 0);
  getMeterLevel(); // Show meter-readings
  getSpectrum(); // Show FFT reading
}

void getSpectrum() {

  strokeWeight(0);
  stroke(0, 230, 0);
  
  // populate the spectrum array with FFT values.
  LiveInput.getSpectrum();
  
  // Draw a bar for each of the elements in the spectrum array.
  for (int i = 0; i < LiveInput.spectrum.length; i++) {
    line(i * 2, height, i * 2, height - LiveInput.spectrum[i] / 10);
  }
}

void getMeterLevel() {

  // Get Peak level (0-1) for each channel (0 -> Left , 1 -> Right)
  // Note: use LiveInput.getLevel() to combine both channels into one
  float meterDataLeft = LiveInput.getLevelLeft();
  float meterDataRight = LiveInput.getLevelRight();

  // Draw a volume-meter for each channel.
  fill(0, 100, 0);
  float left = meterDataLeft * height;
  float right = meterDataRight * height;
  rect(width / 2 - 100, height, 100, left * -1);
  rect(width / 2, height, 100, right * -1);
}