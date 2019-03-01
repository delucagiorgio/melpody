package melpody.midi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Track;

import classifier.NoteRank;
import converter.MidiInputOutputConverter;
import exception.MelpodySelectionProcessException;
import melpody.MelpodyMain;
import melpody.midi.chord.PossibleScale;
import melpody.midi.note.MidiNote;
import melpody.midi.note.Note;
import melpody.midi.scale.ModalScaleInitProcess;
import melpody.midi.scale.ScaleTypeInitProcess;
import melpody.midi.track.HarmonyMidiTrack;
import melpody.midi.track.MelodyAbstraction;

/**
 * 
 * @author Giorgio De Luca
 * 
 * Class used to create the output melody based on the harmony and melody abstraction sequences.
 *
 */
public final class OutputMidiNoteSelector {

	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	private double noveltyThreshold;
	
	public OutputMidiNoteSelector() {
		this.noveltyThreshold = new Double(MelpodyMain.properties.getProperty("NOVELTY_THRESHOLD"));
	}
	
	/**
	 * Use this method to create the output melody passing as parameter the harmony and the melody abstraction files 
	 * and the track of the sequence for the output file
	 * 
	 * @param melodyAbstraction the melody abstraction 
	 * @param harmony the harmony
	 * @param track the track of the output sequence
	 * @param modalScaleSelector the set of modal scale accepted
	 * @param scaleTypeSelector the set of types of scale accepted
	 * @throws InvalidMidiDataException 
	 */
	public void createOutputTrack(MelodyAbstraction melodyAbstraction, HarmonyMidiTrack harmony, Track track, ModalScaleInitProcess modalScaleSelector, ScaleTypeInitProcess scaleTypeSelector) throws InvalidMidiDataException {
		
		log.fine("Novelty value threshold " + noveltyThreshold);

		MidiNote previousNoteGenerated = null;
		MidiNote previousNoteAbMel = null;

		Long previousTick = null;
		List<MidiNote> noteOutputList = new ArrayList<MidiNote>();
		
		List<Long> tickSet = new ArrayList<Long>(harmony.getEventTimeList());
		
		Map<Long, PossibleScale> chordScaleMap = new TreeMap<Long, PossibleScale>();
		
		List<MidiNote> melodyAbstractionNotes = melodyAbstraction.getMidiInformation().getMidiNotes();
		
		for(int idx = 0; idx < melodyAbstractionNotes.size(); idx++) {
			log.fine("Note numer " + idx);
			MidiNote abMelNote = melodyAbstractionNotes.get(idx);
			long nowTick = abMelNote.getTimestampEventEnd();
			Long tickMapChord = tickSet.get(0);
			List<MidiNote> noteHarmony = harmony.getPressedNoteAtTick(nowTick);
			if(noteHarmony.isEmpty() && previousTick != null) {
				noteHarmony = harmony.getPressedNoteAtTick(previousTick);
				nowTick = previousTick;
				log.fine("Note not found: checkpoint note used");
			}
			
			for(Long tickChord : tickSet) {
				if(tickChord <= nowTick) {
					tickMapChord = tickChord;
				}
			}
			boolean exceptionRaised;
			
			Map<Long, List<PossibleScale>> timePossibileScaleMap = harmony.getScalesMap(modalScaleSelector, scaleTypeSelector);
			
			do {
				List<PossibleScale> possibileScaleList = null;
				exceptionRaised = false;
				PossibleScale possibileScale = chordScaleMap.get(tickMapChord);
				if(possibileScale == null) {
					possibileScaleList = getPossibleScaleFromInputNote(abMelNote, timePossibileScaleMap);
					if(possibileScaleList != null && !possibileScaleList.isEmpty()) {
						possibileScale = getPossibleScaleFromNotes(noteHarmony, possibileScaleList);
						log.info("Scale selected " + possibileScale.getFundamentalNote() + ", " + possibileScale.getScaleType() + ", " + possibileScale.getScale());
						chordScaleMap.put(tickMapChord, possibileScale);
					}
				}
				
				if(possibileScale != null) {
					try {	
						MidiNote outputNote = getOutputNote(possibileScale, abMelNote, noteHarmony, previousNoteGenerated, previousNoteAbMel);
						
						track = MidiInputOutputConverter
								.createNote(outputNote, track, abMelNote.getTimestampEventStart(), 
										abMelNote.getTimestampEventEnd());
						previousNoteGenerated = outputNote;
						previousNoteAbMel = abMelNote;
						
						noteOutputList.add(previousNoteGenerated);
					}catch(MelpodySelectionProcessException e) {
						log.log(Level.SEVERE, e.getMessage(), e);
						log.info("Removing scale " + possibileScale.getFundamentalNote() + ", " + possibileScale.getScaleType() + ", " + possibileScale.getScale() );
						chordScaleMap.remove(tickMapChord);
						log.info("Scale removed from possible scale list = " + removeScaleFromPossibleScaleList(abMelNote, timePossibileScaleMap, possibileScale));
						exceptionRaised = true;
					}
				}else {
					log.severe("No possible scales/notes relationship found according to melody abstraction and harmony");
					System.exit(-1);
				}
			}while(exceptionRaised);
		}
		
		String text = "";
		for(MidiNote mn : noteOutputList) {
			text = text + mn.getNote().getNote() + ",";
		}
		log.info("Note written in output: " + text );

		
		log.info("Scale selected:");
		int i = 0;
		for(Long tick : chordScaleMap.keySet()) {
			i++;
			log.info("Scale " + i + " " + chordScaleMap.get(tick).getFundamentalNote() + ", " + chordScaleMap.get(tick).getScaleType() +", " + chordScaleMap.get(tick).getScale());
		}
	}
	
	/**
	 * Use this method to select a scale randomly starting from the harmony and the possible list of scales
	 * @param noteHarmony the harmony notes
	 * @param possibileScaleList the possible scales related to the specific harmomy notes
	 * @return the selected scale
	 */
	private PossibleScale getPossibleScaleFromNotes(List<MidiNote> noteHarmony, List<PossibleScale> possibileScaleList) {
		
		MidiNote fundamentalChordNote = getLowerFundamentalNote(noteHarmony);
		
		boolean isMajorChord = false;
		boolean isMinorChord = false;
		MidiNote previousNote = fundamentalChordNote;
		for(MidiNote mn : noteHarmony) {
			if(previousNote.getNote().equals(fundamentalChordNote.getNote())) {
				int difference = (Math.max(mn.getMidiKeyId(), previousNote.getMidiKeyId()) - Math.min(mn.getMidiKeyId(), previousNote.getMidiKeyId())) % 12;
				if((difference % 12) == 4) {
					isMajorChord = true;
				}else if((difference % 12) == 3) {
					isMinorChord = true;
				}
			}
			previousNote = mn;
		}
		
		boolean thirdNotPresent = !isMajorChord && !isMinorChord;
		
		List<PossibleScale> possibleScalesMaj = new ArrayList<PossibleScale>(); 
		List<PossibleScale> possibleScalesMin = new ArrayList<PossibleScale>();
		List<PossibleScale> possibleScalesSameRoot = new ArrayList<PossibleScale>();

		for(PossibleScale ps : possibileScaleList) {
			if(isMajorChord && ps.getScale().isMajor() 
					&& (!(new Boolean(MelpodyMain.properties.getProperty("SAME_ROOT"))) || ps.getFundamentalNote().equals(fundamentalChordNote.getNote()))
					) {
				possibleScalesMaj.add(ps);
			}else if(isMinorChord && !ps.getScale().isMajor() 
					&&(!(new Boolean(MelpodyMain.properties.getProperty("SAME_ROOT"))) || ps.getFundamentalNote().equals(fundamentalChordNote.getNote()))
					) {
				possibleScalesMin.add(ps);
			}else if(thirdNotPresent 
					&& ps.getFundamentalNote().equals(fundamentalChordNote.getNote())
					) {
				possibleScalesSameRoot.add(ps);
			}
		}
		
		PossibleScale returnScale = null;
		
		if(isMajorChord && !possibleScalesMaj.isEmpty()) {
			returnScale = possibleScalesMaj.get(Math.min((int)(Math.round(Math.random() * possibleScalesMaj.size())), possibleScalesMaj.size() - 1));
		}else if(isMinorChord && !possibleScalesMin.isEmpty()) {
			returnScale = possibleScalesMin.get(Math.min((int)(Math.round(Math.random() * possibleScalesMin.size())), possibleScalesMin.size() - 1));
		}else if(thirdNotPresent && !possibleScalesSameRoot.isEmpty()){
			returnScale = possibleScalesSameRoot.get(Math.min((int)(Math.round(Math.random() * possibleScalesSameRoot.size())), possibleScalesSameRoot.size() - 1));
		}else {
			log.warning("Impossible to find the compatible scale with type and mode of the scale: the 3rd of the chord is missing or the modal scales selected are few");
			log.warning("Fundamental : " + fundamentalChordNote);
			int index = (int) Math.round((Math.random() * (possibileScaleList.size())));
			returnScale = possibileScaleList.get(Math.min(index, possibileScaleList.size() - 1));
		}
		
		return returnScale;
	}

	/**
	 * Returns the midi note to be written in output
	 *   
	 * @param possibileScale the compatible scale
	 * @param abMelNote the note of melody abstraction
	 * @param chordNotes the notes of the harmony
	 * @param melodyAbstraction the melody abstraction
	 * @param previousNote the last generated note
	 * @param currentNote current index note of the melody abstraction
	 * @return note to be written in output
	 * @throws MelpodySelectionProcessException 
	 */
	private MidiNote getOutputNote(PossibleScale possibileScale, MidiNote abMelNote, List<MidiNote> chordNotes, MidiNote previousNote, MidiNote previousNoteAbMel) throws MelpodySelectionProcessException {
		
		MidiNote returnNote = null;
		double noveltyValue = Math.random();

		MidiNote fundamentalChordNote = getLowerFundamentalNote(chordNotes);
		
		int indexChordNoteInScale = -1;
			
		//Trovo l'indice della nota fondamentale
		for(int i = 0; i < possibileScale.getNoteList().size(); i++) {
			MidiNote note = possibileScale.getNoteList().get(i);
			if(note.getNote().equals(fundamentalChordNote.getNote())) {
				indexChordNoteInScale = i;
				break;
			}
		}
		
		if(indexChordNoteInScale >= 0) {
			//Mi calcolo le note accettabli dalla scala scelta rispetto all'astrazione della melodia
			List<MidiNote> possibleNoteList = getPossibleNoteListFromMelodyAbstraction(possibileScale, previousNote, abMelNote, previousNoteAbMel);

			log.fine("Scale selected: " + possibileScale.getFundamentalNote() + ", " + possibileScale.getScaleType() +", " + possibileScale.getScale());
			
			String text = "";
			for(MidiNote n : possibileScale.getNoteList()) {
				text = text + n.getNote() + ",";
			}
			
			log.fine(text);
			
			log.fine("Chords note");
			text = "";
			for(MidiNote n : chordNotes) {
				text = text + n.getNote() + ",";
			}
			
			log.fine(text);
			
			log.fine("Index offset: " + indexChordNoteInScale);
			returnNote = NoteRank.getNoteFromParameterAndRanking
					(abMelNote.getNoteLength(), noveltyValue >= noveltyThreshold, indexChordNoteInScale, possibleNoteList, possibileScale.getNoteList());
		}
		
		return returnNote;
	}

	/**
	 * This method defines which are the notes belonging to a specific scale (root note, type of scale, mode of the scale) 
	 * can be selected as compatible notes according with the melody abstraction step distance and contour.
	 * The first call of this method returns all the notes of the scale.
	 * 
	 * @param possibileScale the scale selected 
	 * @param previousMidiNote the previous note generated
	 * @param abMelNote the current note of the melody abstraction
	 * @param previousNoteAbMel the previous note of the melody abstraction
	 * @return the list of notes in the range defined by the melody abstraction
	 */
	private List<MidiNote> getPossibleNoteListFromMelodyAbstraction(PossibleScale possibileScale, MidiNote previousMidiNote, MidiNote abMelNote, MidiNote previousNoteAbMel) {

		List<MidiNote> possibleNotesList = new ArrayList<MidiNote>();
		List<MidiNote> tempList = new ArrayList<MidiNote>();

		//Ramo iterazione > 1
		if(previousMidiNote != null) {

			int prevMidiKeyMA = previousNoteAbMel.getMidiKeyId();
			int nowMidiKeyMA = abMelNote.getMidiKeyId();
			
			int rangeDifference = nowMidiKeyMA - prevMidiKeyMA;
			int maxRange = previousMidiNote.getMidiKeyId() + rangeDifference;
			
			log.fine("Delta Range: " + rangeDifference + ", Max MIDI ID: " + maxRange);

			if(maxRange != 0) {
				
				int lowerBound = previousMidiNote.getMidiKeyId() + 1;
				int upperBound = maxRange;
				
				if(lowerBound > upperBound) {
					lowerBound = maxRange;
					upperBound = previousMidiNote.getMidiKeyId() - 1;
				}
				
				for(int midiKeyId = lowerBound; midiKeyId <= upperBound; midiKeyId++) {
					MidiNote possibileMidiNote = new MidiNote();
					
					possibileMidiNote.setNote(Note.getNote(midiKeyId % 12));
					possibileMidiNote.setOctave(midiKeyId / 12);
					tempList.add(possibileMidiNote);
				}
				
				Collections.reverse(tempList);
				
				for(MidiNote n : possibileScale.getNoteList()) {
					for(MidiNote mn : tempList) {
						if(mn.getNote().equals(n.getNote())) {
							mn.setNoteLength(abMelNote.getNoteLength());
							mn.setTimestampEventStart(abMelNote.getTimestampEventStart());
							mn.setTimestampEventEnd(abMelNote.getTimestampEventEnd());
							mn.setVelocity(abMelNote.getVelocity());
							possibleNotesList.add(mn);
						}
					}
				}
			}
			
			if(possibleNotesList.isEmpty()) {
				log.warning("Default note inserted - previous note");

				possibleNotesList.add(previousMidiNote);
			}

			
			String text = "";
			for(MidiNote m : possibleNotesList) {
				text = text + m.getNote() + ",";
			}
			
			log.info("Possible note list: " + text);
			
			//Prima esecuzione
		}else {
			
			for(MidiNote n : possibileScale.getNoteList()) {
				n.setOctave(abMelNote.getOctave());
				n.setNoteLength(abMelNote.getNoteLength());
				n.setTimestampEventEnd(abMelNote.getTimestampEventEnd());
				n.setTimestampEventStart(abMelNote.getTimestampEventStart());
				n.setVelocity(abMelNote.getVelocity());
				
				possibleNotesList.add(n);
			}
		}
		
		return possibleNotesList;
	}

	/**
	 * This method returns the list of possible scales usable in a specific time instant
	 * @param mn the note, containing the information about the time instant
	 * @param chordMap the map time-list of scales
	 * @return the list of possible scales
	 */
	private List<PossibleScale> getPossibleScaleFromInputNote(MidiNote mn, Map<Long, List<PossibleScale>> chordMap) {
		
		Long lastEventCloseNote = null;
		
		if(mn.getTimestampEventEnd() != null) {
			for(Long eventTimestamp : chordMap.keySet()) {
				if(mn.getTimestampEventEnd().compareTo(eventTimestamp) >= 0) {
					if(lastEventCloseNote == null || lastEventCloseNote.compareTo(eventTimestamp) < 0) {
						lastEventCloseNote = eventTimestamp;
					}
				}
			}
		}
		
		return lastEventCloseNote != null ? chordMap.get(lastEventCloseNote) : null;
	}
	
	/**
	 * This method removes the scale in the list of possible scale available for the instant time defined by the melody abstraction note
	 * @param mn the note, containing the information about the time instant
	 * @param chordMap the map time-list of scales
	 * @param scale the scale to be deleted
	 * @return the list of possible scales
	 */
	private boolean removeScaleFromPossibleScaleList(MidiNote mn, Map<Long, List<PossibleScale>> chordMap, PossibleScale scale) {
		
		Long lastEventCloseNote = null;
		
		if(mn.getTimestampEventEnd() != null) {
			for(Long eventTimestamp : chordMap.keySet()) {
				if(mn.getTimestampEventEnd().compareTo(eventTimestamp) >= 0) {
					if(lastEventCloseNote == null || lastEventCloseNote.compareTo(eventTimestamp) < 0) {
						lastEventCloseNote = eventTimestamp;
					}
				}
			}
		}
		
		return lastEventCloseNote != null ? chordMap.get(lastEventCloseNote).remove(scale) : false;
	}
	
	/**
	 * Given a set of notes, the method returns the lower note
	 * @param midiNotes the set of notes
	 * @return the lower note
	 */
	private MidiNote getLowerFundamentalNote(List<MidiNote> midiNotes) {
		
		MidiNote lower = null;
		
		for(MidiNote mn : midiNotes) {
			if(lower == null 
					|| mn.getOctave() < lower.getOctave() 
					|| (mn.getOctave() == lower.getOctave() 
						&& mn.getNote().getOffsetFromC() < lower.getNote().getOffsetFromC())) {
				lower = mn;
			}
		}
		
		return lower;
	}
	
}
