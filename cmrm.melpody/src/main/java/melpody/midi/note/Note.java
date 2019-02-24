package melpody.midi.note;

/**
 * 
 * @author Giorgio De Luca
 *
 * Enum to define the musical notes. Each object is identified by the name of the note and its offset respect to C
 */
public enum Note {
	
	C("C", 0),
	C_DIESIS("C#", 1),
	D("D", 2),
	D_DIESIS("D#", 3),
	E("E", 4),
	F("F", 5),
	F_DIESIS("F#", 6),
	G("G", 7),
	G_DIESIS("G#", 8),
	A("A", 9),
	A_DIESIS("A#", 10),
	B("B", 11);
	
	private String note;
	private int offsetFromC;
	
	public int getOffsetFromC() {
		return offsetFromC;
	}
	
	Note(String note, int offsetFromC) {
		this.offsetFromC = offsetFromC;
		this.note = note;
	}
	
	/**
	 * Use this method to get the note with a certain offset from C
	 * @param offsetFromC offset from C
	 * @return the note
	 */
	public static Note getNote(int offsetFromC) {
		for(Note n : Note.values()) {
			if(n.getOffsetFromC() == offsetFromC) {
				return n;
			}
		}
		
		return null;
	}

	public String getNote() {
		return note;
	}
	
	@Override
	public String toString() {
		return note;
	}
	
	/**
	 * Use this method to get a note from the name of the note desired
	 * @param noteString the name of the note
	 * @return the note, null if not found
	 */
	public static Note getNoteFromString(String noteString) {
		for(Note mn : Note.values()) {
			if(mn.getNote().equalsIgnoreCase(noteString)) {
				return mn;
			}
		}
		
		return null;
	}
	
}
