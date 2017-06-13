package main;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * @author Kone
 *
 */
public class Main {

	public static Mixer mixer;
	public static Clip clip;
	public static TargetDataLine line;
	public static File recordedFilePath = new File("Test.wav");
	public static int recordingLength = 5000; //in milliseconds
	
	public static void main(String[] args){
		
		recordSound();
		playSound();				
	}
	
	public static void recordSound(){
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
		
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		try {
			line.open();
			System.out.println("Recording started");
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		line.start();
		Thread recorder = new Thread(new Runnable(){
			public void run(){
				AudioInputStream audioInputStream = new AudioInputStream(line);
				try {
					AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, recordedFilePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		recorder.start();
		try {
			Thread.sleep(recordingLength);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		line.stop();
		line.close();
		System.out.println("Recording stopped");
	}
	
	public static void playSound(){
		mixer = AudioSystem.getMixer(null); //NULL gets the system default mixer		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
					
		try {
			clip = (Clip) mixer.getLine(dataInfo); //obtains a line to the mixer for references, the line isn't actually reserved yet
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		try {			
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(recordedFilePath);
			clip.open(audioStream); //reserves the line to the mixer 
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
	    } catch (LineUnavailableException e) {
	        e.printStackTrace();
	    }
		
		clip.start();
		do{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(clip.isActive()); /*as long as the clip plays sound, the clip is active.	
		 						   the playback is executed in an additional thread beside the main thread.
		 						   main thread won't terminate until playback has finished*/
		clip.stop();
		clip.close();
	}
}
