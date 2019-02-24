package melpody.midi.note;

/**
 * 
 * @author Giorgio De Luca
 * Class to map the duration of a note in terms of time instant in Melpody unit of measure
 *
 */
public class NoteLengthHelper {

	public static final int RESOLUTION_LENGTH = 24;
	
	private NoteLengthHelper() {}

	public static void setNoteLengthToMidiNote(MidiNote note) {
		if(note.getTimestampEventStart() != null && note.getTimestampEventEnd() != null) {
			
			Long start = note.getTimestampEventStart();
			Long end = note.getTimestampEventEnd();
			
			Long length = end - start;
			
			if(length.intValue() <= RESOLUTION_LENGTH / 4) {
				note.setNoteLength(NoteLength.SIXTY_FOURTH_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH / 2) {
				note.setNoteLength(NoteLength.THIRTY_SECOND_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH) {
				note.setNoteLength(NoteLength.SIXTEENTH_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH * 2) {
				note.setNoteLength(NoteLength.EIGHTH_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH * 4) {
				note.setNoteLength(NoteLength.QUARTER_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH * 8) {
				note.setNoteLength(NoteLength.HALF_NOTE);
			}else if(length.intValue() <= RESOLUTION_LENGTH * 16) {
				note.setNoteLength(NoteLength.WHOLE_NOTE);
			}
		}
	}
	
}
