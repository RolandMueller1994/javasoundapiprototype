package main;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.plaf.SliderUI;

/**
 * 
 * @author Kone
 *
 */
public class Main {

	public static Mixer playBackMixer, recordMixer;
	public static Clip clip;
	public static TargetDataLine targetLine;
	public static SourceDataLine sourceLine;
	public static File recordedFilePath = new File("Test.wav");
	public static int recordingLength;
	public static boolean stopped;

	public static boolean startedRecord = false;
	public static long startRecordTime = 0;

	public static long latency = 10;

	public static void main(String[] args) {

		getSoundDevices();
		recordSound();
		playClipSound();
		//playRecordWithLatency();
	}

	public static void getSoundDevices() {
		Mixer mixer;
		int counter = 0;
		Scanner scanner = new Scanner(System.in);
		
		// Gets all available sound devices
		Mixer.Info[] soundDevices = AudioSystem.getMixerInfo();
		
		// Checks which devices are suitable for recording
		System.out.println("Available sound devices for recording:");
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
		for (Mixer.Info i : soundDevices) {
			mixer = AudioSystem.getMixer(i);
			if(mixer.isLineSupported(info)){
				System.out.println(counter + 1 + ". " + i);	
			}
			counter++;
		}
		
		// Gets the mixers for recording by user selection
		System.out.println("");
		System.out.print("Enter the device number for sound recording: ");
		recordMixer = AudioSystem.getMixer(soundDevices[scanner.nextInt() - 1]);
		System.out.println("Record device: " + recordMixer.getMixerInfo());
		

		// Checks which devices are suitable for play back
		counter = 0;
		System.out.println("");
		System.out.println("Available sound devices for play back:");
		info = new DataLine.Info(SourceDataLine.class, null);
		for (Mixer.Info i : soundDevices) {
			mixer = AudioSystem.getMixer(i);
			if(mixer.isLineSupported(info)){
				System.out.println(counter + 1 + ". " + i);	
			}
			counter++;
		}
		
		System.out.println("");
		System.out.print("Enter the device number for play back: ");
		playBackMixer = AudioSystem.getMixer(soundDevices[scanner.nextInt() - 1]);
		System.out.println("Record device: " + recordMixer.getMixerInfo());

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
			targetLine = (TargetDataLine) recordMixer.getLine(info);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
		try {
			// open() reserves the line to the mixer
			targetLine.open();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		targetLine.start();
		System.out.println("Recording started");
		Thread recorder = new Thread(new Runnable() {
			public void run() {
				AudioInputStream audioInputStream = new AudioInputStream(targetLine);
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
		targetLine.stop();
		targetLine.close();
		System.out.println("Recording stopped");
	}

	public static void playClipSound() {

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
			 * thread won't terminate until the play back has finished.
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

	public static void playRecordWithLatency() {

		List<Byte> buffer = new LinkedList<>();

		// TargetDataLine is a line, which receives audio data from the mixer in
		// real time (e.g. microphone). SourceDataLine sends the recorded data
		// to a mixer (e.g. speaker)
		DataLine.Info infoTarget = new DataLine.Info(TargetDataLine.class, null);
		DataLine.Info infoSource = new DataLine.Info(SourceDataLine.class, null);

		try {
			// getLine() obtains a line to the mixer for references, the line
			// isn't actually reserved yet
			targetLine = (TargetDataLine) recordMixer.getLine(infoTarget);
			sourceLine = (SourceDataLine) playBackMixer.getLine(infoSource);

		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}

		Thread playThread = new Thread(new Runnable() {

			@Override
			public void run() {

				while (!(startedRecord && System.currentTimeMillis() > startRecordTime + latency)) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				try {
					sourceLine.open();
				} catch (LineUnavailableException e) {

					e.printStackTrace();
					return;
				}
				sourceLine.start();
				sourceLine.flush();
				System.out.println(System.currentTimeMillis());
				byte[] data;
				while (!stopped) {
					// reads data from the targetLine and plays it instantly

					synchronized (buffer) {
						data = new byte[buffer.size()];

						int i = 0;
						for (byte act : buffer) {
							data[i] = act;
							i++;
						}

						buffer.clear();
					}
					sourceLine.write(data, 0, data.length);
					try {
						Thread.sleep(latency / 6);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		Thread recordThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					targetLine.open();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				targetLine.start();
				targetLine.flush();

				while (!stopped) {
					if (!startedRecord) {
						startRecordTime = System.currentTimeMillis();
						System.out.println(startRecordTime);
						startedRecord = true;
					}

					// We will only read the current available data. If a fixed
					// size is read, the read()-call will block until the
					// requested data are available. This would cause latency.
					byte[] data = new byte[targetLine.available()];
					int bytesRead = targetLine.read(data, 0, data.length);

					synchronized (buffer) {
						for (int i = 0; i < bytesRead; i++) {
							buffer.add(data[i]);
						}
					}

					try {
						Thread.sleep(latency / 6);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		playThread.start();
		recordThread.start();

		try {
			Thread.sleep(recordingLength);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stopped = true;
		targetLine.stop();
		targetLine.close();
		sourceLine.stop();
		sourceLine.close();
		System.out.println("Recording stopped");
	}

}
