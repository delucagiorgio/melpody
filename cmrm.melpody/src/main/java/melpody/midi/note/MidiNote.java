package melpody.midi.note;

/**
 * 
 * @author Giorgio De Luca
 * Class to define the midi note
 *
 */
public class MidiNote{

	private Note note;
	private int octave;
	private int velocity;
	private Long timestampEventStart;
	private Long timestampEventEnd;
	private NoteLength noteLength;
	
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(Note note) {
		this.note = note;
	}
	
	public int getOctave() {
		return octave;
	}
	
	public void setOctave(int octave) {
		this.octave = octave;
	}
	
	public Long getTimestampEventStart() {
		return timestampEventStart;
	}
	
	public void setTimestampEventStart(Long timestampEvent) {
		this.timestampEventStart = timestampEvent;
	}
	
	public Long getTimestampEventEnd() {
		return timestampEventEnd;
	}
	
	public void setTimestampEventEnd(Long timestampEventEnd) {
		this.timestampEventEnd = timestampEventEnd;
	}

	public NoteLength getNoteLength() {
		return noteLength;
	}

	public void setNoteLength(NoteLength noteLength) {
		this.noteLength = noteLength;
	}
	
	@Override
	public String toString() {
		return "Note: " + note + "," +
				"Octave: " + octave + "," + 
				"Note length: " + noteLength;
	}	
	
	public int getMidiKeyId() {
		return note.getOffsetFromC() + octave * 12;
	}
	
	@Override
	public boolean equals(Object e) {
		return this.note.equals(((MidiNote)e).getNote());
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}
	
}
