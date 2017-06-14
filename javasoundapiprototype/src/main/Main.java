package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
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

	public static Mixer playBackMixer, recordMixer;
	public static Clip clip;
	public static TargetDataLine line;
	public static File recordedFilePath = new File("Test.wav");
	public static int recordingLength;

	public static void main(String[] args) {

		getSoundDevices();
		recordSound();
		playSound();
	}

	public static void getSoundDevices() {
		// Gets all available I/O sound devices
		Mixer.Info[] soundDevices = AudioSystem.getMixerInfo();
		System.out.println("Available sound devices:");
		for (int i = 0; i < soundDevices.length; i++) {
			System.out.println(i + 1 + ". " + soundDevices[i]);
		}

		// Gets the mixers for recording and play back by user selection
		System.out.println("");
		System.out.print("Enter the device number for sound recording: ");
		Scanner scanner = new Scanner(System.in);
		recordMixer = AudioSystem.getMixer(soundDevices[scanner.nextInt() - 1]);
		System.out.println("Record device: " + recordMixer.getMixerInfo());

		System.out.println("");
		System.out.print("Enter the device number for play back: ");
		playBackMixer = AudioSystem.getMixer(soundDevices[scanner.nextInt() - 1]);
		System.out.println("Play back device: " + playBackMixer.getMixerInfo());

		System.out.println("");
		System.out.print("Determine recording length (in milliseconds): ");
		recordingLength = scanner.nextInt();
	}

	public static void recordSound() {

		// TargetDataLine is a line, which receives audio data from the mixer in
		// real time (e.g. microphone)
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);

		try {
			// getLine() obtains a line to the mixer for references, the line
			// isn't actually reserved yet
			line = (TargetDataLine) recordMixer.getLine(info);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		try {
			// open() reserves the line to the mixer
			line.open();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		line.start();
		System.out.println("Recording started");
		Thread recorder = new Thread(new Runnable() {
			public void run() {
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

	public static void playSound() {

		// Clip is a line, which sends stored audio data to the mixer (not in
		// real time, e.g. wave-files)
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);

		try {
			// getLine() obtains a line to the mixer for references, the line
			// isn't actually reserved yet
			clip = (Clip) playBackMixer.getLine(dataInfo);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(recordedFilePath);

			// open() reserves the line to the mixer
			clip.open(audioStream);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		clip.start();
		do {
			/*
			 * as long as the clip plays sound, the clip is active. the playback
			 * is executed in an additional thread beside the main thread. main
			 * thread won't terminate until playback has finished.
			 */
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (clip.isActive());
		clip.stop();
		clip.close();
	}
}
