package melpody.midi.track;

import java.util.Set;
import java.util.TreeSet;

import melpody.midi.util.MidiTemporalSequenceEvent;

/**
 * 
 * @author Giorgio De Luca
 *
 * Class to define a generic midi track containing a set of event timestamps, a end instant and a set of notes
 */
public abstract class AbstractMidiTrack {

	//Set of notes
	protected MidiTemporalSequenceEvent midiInformation;
	//Set of time instants
	protected Set<Long> eventTimeList;
	//End of track time instant
	protected long endOfTrack;
	
	protected AbstractMidiTrack() {
		eventTimeList = new TreeSet<Long>();
	}
	
	public MidiTemporalSequenceEvent getMidiInformation() {
		return midiInformation;
	}

	public void setMidiInformation(MidiTemporalSequenceEvent midiInformation) {
		this.midiInformation = midiInformation;
	}

	public Set<Long> getEventTimeList() {
		return eventTimeList;
	}

	public void setEventTimeList(Set<Long> eventTimeList) {
		this.eventTimeList = eventTimeList;
	}

	public long getEndOfTrack() {
		return endOfTrack;
	}

	public void setEndOfTrack(long endOfTrack) {
		this.endOfTrack = endOfTrack;
	}
	
}
