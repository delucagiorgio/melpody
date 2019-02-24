package melpody.midi.track;

/**
 * 
 * @author Giorgio De Luca
 * Class to obtain a specific type of midi track
 *
 */
public class MidiTrackFactory {

	private MidiTrackFactory() {}

	public static <T extends AbstractMidiTrack> AbstractMidiTrack getCorrectType(Class<T> type) {
		
		if(HarmonyMidiTrack.class.equals(type)) {
			return new HarmonyMidiTrack();
		}else if(MelodyAbstraction.class.equals(type)) {
			return new MelodyAbstraction();
		}
		
		return null;
	}
	
}
