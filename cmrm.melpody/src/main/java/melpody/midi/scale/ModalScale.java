package melpody.midi.scale;

/**
 * 
 * @author Giorgio De Luca
 *
 * Enum to define all the modes applicable to scales. 
 * Each mode is defined with 
 * the grade, 
 * the offset respect to the tonic, 
 * the progressive index of the starting grade
 * and a boolean true is the mode contains a major third, false otherwise
 */
public enum ModalScale {
	
	IONIAN("I", 0, 0, true),
	DORIAN("II", 2, 1, false),
	PHRYGIAN("III", 4, 2, false),
	LYDIAN("IV", 5, 3, true),
	MIXOLYDIAN("V", 7, 4, true),
	AEOLIAN("VI", 9, 5, false),
	LOCRIAN("VII", 11, 6, false);

	private String tonicRelative;
	private int tonicSemitoneOffset;
	private int progressiveIndex;
	private boolean isMajor;

	ModalScale(String tonicRelative, int tonicSemitoneOffset, int progressiveIndex, boolean isMajor){
		this.tonicRelative = tonicRelative;
		this.tonicSemitoneOffset = tonicSemitoneOffset;
		this.progressiveIndex = progressiveIndex;
		this.isMajor = isMajor;
	}
	
	public String getTonicRelative() {
		return tonicRelative;
	}

	public int getTonicSemitoneOffset() {
		return tonicSemitoneOffset;
	}

	public int getProgressiveIndex() {
		return progressiveIndex;
	}

	public boolean isMajor() {
		return isMajor;
	}

}
