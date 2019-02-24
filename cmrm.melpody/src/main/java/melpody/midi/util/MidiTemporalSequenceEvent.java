package melpody.midi.util;

import java.util.ArrayList;
import java.util.List;

import melpody.midi.note.MidiNote;
import melpody.midi.note.NoteLengthHelper;
/**
 * 
 * @author Giorgio De Luca
 * Class to collect all the notes of a midi file
 *
 */
public class MidiTemporalSequenceEvent {
	
	private ArrayList<MidiNote> midiNotes;
	
	public MidiTemporalSequenceEvent() {
		midiNotes = new ArrayList<MidiNote>(); 
	}
	
	/**
	 * Use this method to set the end of a previous note
	 * @param mn the midi note
	 * @param noteTick the time instant
	 * @return the midi note
	 */
	public MidiNote setNoteOff(MidiNote mn, long noteTick) {
		MidiNote returnOffNote = null;
		if(mn != null) {
			List<MidiNote> notReleasedNotes = getMidiNoteNotYetReleased();
			
			for(MidiNote nrn : notReleasedNotes) {
				if(nrn.getNote().equals(mn.getNote()) && nrn.getOctave() == mn.getOctave()) {
					nrn.setTimestampEventEnd(noteTick);
					NoteLengthHelper.setNoteLengthToMidiNote(nrn);
					returnOffNote = nrn;
					break;
				}
			}
		}
		
		return returnOffNote;
	}
	
	/**
	 * This method returns the notes not yet released (timestamp end == null)
	 * @return the notes not yet released
	 */
	private List<MidiNote> getMidiNoteNotYetReleased(){
		
		List<MidiNote> notReleasedNote = new ArrayList<MidiNote>();
		
		for(MidiNote mn : midiNotes) {
			if(mn.getTimestampEventEnd() == null && mn.getNoteLength() == null) {
				notReleasedNote.add(mn);
			}
		}
		
		return notReleasedNote;
	}

	public void add(MidiNote midiNote) {
		midiNotes.add(midiNote);
	}
	
	public List<MidiNote> getMidiNotes(){
		return midiNotes;
	}
	
}
