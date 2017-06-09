package main;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * @author Kone
 *
 */
public class Main {

	public static Mixer mixer;
	public static Clip clip;
	
	public static void main(String[] args){
		
			mixer = AudioSystem.getMixer(null); //NULL gets the system default mixer		
			DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
			
			try {
				clip = (Clip) mixer.getLine(dataInfo); //obtains a line to the mixer for references, the line isn't actually reserved yet
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}

			try {			
				URL soundFile = Main.class.getResource("/main/Ring01.wav");
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
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
			 						   the playback is executed in an additional thread to the main thread.
			 						   main thread won't terminate until playback has finished*/	
	}
}
