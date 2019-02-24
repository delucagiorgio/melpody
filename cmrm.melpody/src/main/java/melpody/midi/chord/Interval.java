package melpody.midi.chord;

import java.util.List;

import melpody.midi.note.MidiNote;

public class Interval {
	

	private int intervalLength;
	private boolean isMinor; 
	private boolean isMajor;
	private boolean isPerfect;
	private boolean isTritone;
	private boolean isOctave;
	private List<MidiNote> listOfNote;
	
	
	public Interval(List<MidiNote> listOfNote) {
		this.listOfNote = listOfNote;
		this.intervalLength = calculateIntervalLength(listOfNote.get(0), listOfNote.get(1));
		setFlags();
	}

	private void setFlags() {
		isMinor = false;
		isMajor = false;
		isOctave = false;
		isPerfect = false;
		isTritone = false;
		
		//Distanza di 12 semitoni
		if(intervalLength % 12 == 0) {
			isOctave = true;
			isPerfect = true;
		//Distanza degli intervalli minori
		}else if(intervalLength == 1 || intervalLength == 3 || intervalLength == 8 || intervalLength == 10){
			isMinor = true;
		//Distanza degli intervalli maggiori
		}else if(intervalLength == 2 || intervalLength == 4 || intervalLength == 9 || intervalLength == 11) {
			isMajor = true;
		//Distanza degli intervalli giusti
		}else if(intervalLength == 5 || intervalLength == 7){
			isPerfect = true;
		//Distanza degli intervalli tritoni
		}else if(intervalLength == 6) {
			isTritone = true;
		}
	}

	public static int calculateIntervalLength(MidiNote midiNote1, MidiNote midiNote2) {
		
		int intervalLength = -1;

		//Se la nota 1 si trova nell'ottava inferiore rispetto alla nota 2
		if(midiNote1.getOctave() == midiNote2.getOctave() - 1) {
			intervalLength = midiNote2.getNote().getOffsetFromC() - midiNote1.getNote().getOffsetFromC();
		
		//Se la nota 1 si trova nell'ottava superiore rispetto alla nota 2
		}else if(midiNote1.getOctave() == midiNote2.getOctave() + 1) {
			intervalLength = midiNote1.getNote().getOffsetFromC() - midiNote2.getNote().getOffsetFromC();
		
		//Se le note si trovano nella stessa ottava della tastiera C-C
		}else if(midiNote1.getOctave() == midiNote2.getOctave()) {
			
			//Se la nota1 è più alta della nota 2 
			if(midiNote1.getNote().getOffsetFromC() > midiNote2.getNote().getOffsetFromC()) {
				intervalLength = midiNote1.getNote().getOffsetFromC() - midiNote2.getNote().getOffsetFromC();
				
			//Se la nota2 è più alta della nota1	
			}else if(midiNote2.getNote().getOffsetFromC() > midiNote1.getNote().getOffsetFromC()) {
				intervalLength = midiNote2.getNote().getOffsetFromC() - midiNote1.getNote().getOffsetFromC();
				
			}else {
				//non possono essere la stessa nota all'interno di un ottava
			}
		}else {
			//Intervallo oltre l'ottava
		}
		return intervalLength;
	}

	public int getIntervalLength() {
		return intervalLength;
	}

	public boolean isMinor() {
		return isMinor;
	}

	public boolean isMajor() {
		return isMajor;
	}

	public boolean isPerfect() {
		return isPerfect;
	}

	public boolean isTritone() {
		return isTritone;
	}

	public boolean isOctave() {
		return isOctave;
	}
	
	public Interval getInversionInterval() {
		Interval inversionInterval = null;
		
//		List<MidiNote> invertedListNotes = new ArrayList<MidiNote>(listOfNote.size());
		
		
		
		return inversionInterval;
	}

	public List<MidiNote> getListOfNote() {
		return listOfNote;
	}

}
