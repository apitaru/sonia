import pitaru.sonia.*;

// The Sample object we'll record into
Sample mySampleObj; 

void settings() { 

  size(400, 200);
}

void setup() { 

  background(0, 50, 0);

  Sonia.start(this); 

  LiveInput.start();  // start the liveInput engine

  // Empty Sample object with 441000 frames (10 seconds)
  mySampleObj = new Sample(44100 * 10);
} 

void draw() {
  // do nothing
} 

void mousePressed() {

  // Record LiveInput data into the Sample object
  LiveInput.startRec(mySampleObj);
  
  // The recording will end when the Sample is full 
  print("REC");
} 

void mouseReleased() {

  LiveInput.stopRec(mySampleObj); 
  mySampleObj.play();
  print("PLAY");
} 