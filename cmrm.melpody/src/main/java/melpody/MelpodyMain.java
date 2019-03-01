package melpody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import classifier.NoteRank;
import converter.MidiInputOutputConverter;
import melpody.midi.scale.ModalScaleInitProcess;
import melpody.midi.scale.ScaleTypeInitProcess;
import melpody.midi.track.HarmonyMidiTrack;
import melpody.midi.track.MelodyAbstraction;
import melpody.midi.util.OutputMidiNoteSelector;

public class MelpodyMain {

	private static Sequence outSeq = null;
	private static Track trOut = null;
	public static final String MELPODY_LOGNAME = "MelpodyLogger";
	public static Properties properties = new Properties();

	public static void main(String[] args) {
		
		try {			
			initializeLogProperties();
			initializeProperties();
			
			ModalScaleInitProcess msip = new ModalScaleInitProcess();
			ScaleTypeInitProcess stip = new ScaleTypeInitProcess();
			
			msip.collectActiveModalScale(properties);
			stip.collectActiveTypesOfScale(properties);
			
			MidiInputOutputConverter.initialize();
			NoteRank.initialize();
			
			
			HarmonyMidiTrack harmonySeq = (HarmonyMidiTrack) MidiInputOutputConverter.mapMidi("../midi/harmony.mid", HarmonyMidiTrack.class);
			
			MelodyAbstraction melodyAbstraction = (MelodyAbstraction) MidiInputOutputConverter.mapMidi("../midi/abstract_melody.mid", MelodyAbstraction.class);
			
			initializeOutputTrack(melodyAbstraction);
			
			OutputMidiNoteSelector omns = new OutputMidiNoteSelector();
			
			omns.createOutputTrack(melodyAbstraction, harmonySeq, trOut, msip, stip);
			
			MidiSystem.write(outSeq, 1, new File("../output/OUTPROVA.mid"));
		} catch (Exception e) {
			Logger log = Logger.getLogger(MELPODY_LOGNAME);
			log.severe(e.getMessage());
		}
	}
	/**
	 * Method to initialize the properties related to the specific of the program functionalities
	 */
	private static void initializeProperties() {
		FileInputStream fis;
		try{
			fis = new FileInputStream("../melpody.properties");
			properties.load(fis);
			Logger.getLogger(MELPODY_LOGNAME).setLevel(Level.parse(properties.getProperty("LOGGER_LEVEL")));
			fis.close();
		}catch(Exception e) {
			Logger log = Logger.getLogger(MELPODY_LOGNAME);
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Initialize the properties of the log
	 * @throws SecurityException
	 * @throws IOException
	 */
	private static void initializeLogProperties() throws SecurityException, IOException {
		LogManager.getLogManager().reset();

		Logger mainLogger = Logger.getLogger(MELPODY_LOGNAME);
		mainLogger.setLevel(Level.ALL);
		mainLogger.setUseParentHandlers(false);
		
		FileHandler fileHandler = new FileHandler("../MelpodyLog-" + (new Date()).toString());
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.FINE);
		consoleHandler.setFormatter(new SimpleFormatter() {
			private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

			@Override
			public synchronized String format(LogRecord lr) {
				return String.format(format,
						new Date(lr.getMillis()),
						lr.getLevel().getLocalizedName(),
						lr.getMessage()
				);
			}
		});
		
		fileHandler.setLevel(Level.FINE);
		fileHandler.setFormatter(new SimpleFormatter() {
			private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

			@Override
			public synchronized String format(LogRecord lr) {
				return String.format(format,
						new Date(lr.getMillis()),
						lr.getLevel().getLocalizedName(),
						lr.getMessage()
				);
			}
		});
		mainLogger.addHandler(consoleHandler);
		mainLogger.addHandler(fileHandler);
	}

	/**
	 * Initialize the output track. Melody abstraction file required to define the end of the file.
	 * @param melodyAbstraction the melody abstraction 
	 * @throws InvalidMidiDataException
	 */
	public static void initializeOutputTrack(MelodyAbstraction melodyAbstraction) throws InvalidMidiDataException {
		outSeq = new Sequence(Sequence.PPQ, 96);
		trOut = outSeq.createTrack();
		
		//****  General MIDI sysex -- turn on General MIDI sound set  ****
		byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
		SysexMessage sm = new SysexMessage();
		sm.setMessage(b, 6);
		MidiEvent me = new MidiEvent(sm,(long)0);
		trOut.add(me);

		MetaMessage mt = new MetaMessage();

		//****  set track name (meta event)  ****
		String TrackName = new String("midifile track");
		mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
		me = new MidiEvent(mt,(long)0);
		trOut.add(me);
		
		//****  set end of track (meta event) 19 ticks later  ****
		mt = new MetaMessage();
        byte[] bet = {}; // empty array
		mt.setMessage(0x2F,bet,0);
		me = new MidiEvent(mt, melodyAbstraction.getEndOfTrack());
		trOut.add(me);
	}
	
}
