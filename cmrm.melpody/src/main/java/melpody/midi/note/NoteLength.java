package melpody.midi.note;

import java.util.Arrays;
import java.util.List;
/**
 * 
 * @author Giorgio De Luca
 * 
 * Enum to define the length of a note.
 *
 */
public enum NoteLength {
	
	WHOLE_NOTE,
	HALF_NOTE,
	QUARTER_NOTE,
	EIGHTH_NOTE,
	SIXTEENTH_NOTE,
	THIRTY_SECOND_NOTE,
	SIXTY_FOURTH_NOTE;
	
	public static List<NoteLength> getSlowNoteLengths(){
		return Arrays.asList(WHOLE_NOTE, HALF_NOTE, QUARTER_NOTE);
	}
	
	public static List<NoteLength> getMediumNoteLengths(){
		return Arrays.asList(QUARTER_NOTE, EIGHTH_NOTE);
	}
	
	public static List<NoteLength> getFastNoteLengths(){
		return Arrays.asList(EIGHTH_NOTE, SIXTEENTH_NOTE, THIRTY_SECOND_NOTE, SIXTY_FOURTH_NOTE);
	}
	
}
