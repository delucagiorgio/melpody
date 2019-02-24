package converter;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import melpody.MelpodyMain;
import melpody.midi.note.MidiNote;
import melpody.midi.note.Note;
import melpody.midi.track.AbstractMidiTrack;
import melpody.midi.track.MidiTrackFactory;
import melpody.midi.util.MidiTemporalSequenceEvent;

/**
 * 
 * @author Giorgio De Luca
 *
 * Class used to manage midi files 
 */
public final class MidiInputOutputConverter {

	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static MidiInputOutputConverter instance;
	
	private MidiInputOutputConverter() {}
	
	public static synchronized void initialize() {
		if(instance == null) {
			instance = new MidiInputOutputConverter();
		}
	}
	
	/**
	 * Use this method to map a midi file in a Melpody usable object, using the absolute path of the file MIDI and the Melpody type of object desired 
	 * @param midiFilename the absolute path of midi file
	 * @param type the Melpody type of midi track
	 * @return the midi track object desired
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 */
	public static <T extends AbstractMidiTrack> AbstractMidiTrack mapMidi(String midiFilename, Class<T> type) throws InvalidMidiDataException, IOException {
		
		AbstractMidiTrack returnMappedMidi = MidiTrackFactory.getCorrectType(type);
		Set<Long> eventTimeList = returnMappedMidi.getEventTimeList();
		
		MidiTemporalSequenceEvent returnSequence = new MidiTemporalSequenceEvent();
		Sequence sequence = MidiSystem.getSequence(new File(midiFilename));

		Track[] noteArray = sequence.getTracks();

		for (Track track : noteArray) {
			for (int i = 0; i < track.size(); i++) {
				MidiMessage message = track.get(i).getMessage();
				
				long noteTick = track.get(i).getTick();
				MidiNote midiNote = new MidiNote();
				
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
					int key = sm.getData1();
					int octave = (key / 12) - 1;
					int note = key % 12;
					midiNote.setNote(Note.getNote(note));
					midiNote.setOctave(octave);
					midiNote.setTimestampEventStart(noteTick);
					
					if (sm.getCommand() == NOTE_ON) {
						returnSequence.add(midiNote);
						midiNote.setVelocity(sm.getData2());
						eventTimeList.add(noteTick);
						
					} else if (sm.getCommand() == NOTE_OFF) {
						returnSequence.setNoteOff(midiNote, noteTick);
						
					} 
				} else if(message instanceof MetaMessage) {
					MetaMessage mm = (MetaMessage) message; 
					if(mm.getClass().getName().contains("ImmutableEndOfTrack")) {
						returnMappedMidi.setEndOfTrack(noteTick);
					}
				}
			}
		}
		
		returnMappedMidi.setMidiInformation(returnSequence);
		returnMappedMidi.setEventTimeList(eventTimeList);
		
		return returnMappedMidi;
	}
	
	/**
	 * Use this method to insert a note in a specified track used for the output saving process
	 * 
	 * @param note the note to be inserted
	 * @param track the track where the note will be inserted
	 * @param startNoteTime start time of the note
	 * @param endNoteTime end time of the note
	 * @return the track with the note inserted
	 * @throws InvalidMidiDataException
	 */
	public static Track createNote(MidiNote note, Track track, Long startNoteTime, Long endNoteTime) throws InvalidMidiDataException {
		
		log.fine("Writing " + note);
		
		track.add(new MidiEvent(new ShortMessage(NOTE_ON, note.getMidiKeyId() + 12, note.getVelocity()), startNoteTime));
		track.add(new MidiEvent(new ShortMessage(NOTE_OFF, note.getMidiKeyId() + 12, 127), endNoteTime));

		return track;
	}
	
}
