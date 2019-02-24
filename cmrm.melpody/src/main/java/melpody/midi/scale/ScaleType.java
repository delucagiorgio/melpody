package melpody.midi.scale;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Giorgio De Luca
 * 
 * Enum to define the possible types of scale (heptatonic scale). 
 * The list of integer represents the distance from the current note to the next, starting from the root note,
 * the boolean is true if the scale type contains a major third, false otherwise
 *
 */
public enum ScaleType {

	MAJOR(Arrays.asList(2,2,1,2,2,2,1), true),
	MINOR(Arrays.asList(2,1,2,2,2,2,1), false),
	BEPOP(Arrays.asList(2,2,1,2,2,1,1), true),
	ACOUSTIC(Arrays.asList(2,2,2,1,2,1,2), true),
	BLUES(Arrays.asList(2,1,2,1,1,3,2), false);

	public List<Integer> intervalNoteScale;
	public boolean isMajor;
	
	ScaleType(List<Integer> intervalNoteScale, boolean isMajor){
		this.intervalNoteScale = intervalNoteScale;
		this.isMajor = isMajor;
	}
}
