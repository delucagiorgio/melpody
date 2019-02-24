package classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import exception.MelpodySelectionProcessException;
import melpody.MelpodyMain;
import melpody.midi.note.MidiNote;
import melpody.midi.note.NoteLength;

/**
 * @author Giorgio De Luca
 * Class to select the final output note from a context restricted to harmony, novelty and length of the note. 
 * 
 * Selection is based on the grade stability explained into the slides of the project.
 * 
 */
public final class NoteRank {

	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	private static Map<Double, Set<Integer>> noteProbabilityMap = new TreeMap<Double, Set<Integer>>();
	
	private static final double LOW_PROB = 1D/17D;
	private static final double MED_PROB = 3D/17D;
	private static final double HIGH_PROB = 5D/17D;

	private NoteRank() {}
	
	public static void initialize() {
		Set<Integer> set15 = new TreeSet<Integer>();
		set15.add(0);
		set15.add(4);
		noteProbabilityMap.put(5D/17D, set15);
		
		Set<Integer> set73 = new TreeSet<Integer>();
		set73.add(2);
		set73.add(6);
		noteProbabilityMap.put(3D/17D, set73);
		
		Set<Integer> set246 = new TreeSet<Integer>();
		set246.add(1);
		set246.add(3);
		set246.add(5);
		noteProbabilityMap.put(1D/17D, set246);
	}

	/**
	 * Use this method to select a note among possible notes in input.
	 * 
	 * Novelty boolean is used and apply a total different algorithm based on the lowest probability notes found the note list.
	 * 
	 * @param noteLength the length of the output note
	 * @param novelty the novelty boolean 
	 * @param indexOffset the offset between the root fundamental of the harmony and the root of the scale selected
	 * @param possibleNoteList the possible note list
	 * @param scaleNotes the set of notes in the selected scale
	 * @return the midi note to be written in output
	 * @throws Exception
	 */
	public static MidiNote getNoteFromParameterAndRanking(NoteLength noteLength, boolean novelty, int indexOffset, List<MidiNote> possibleNoteList, List<MidiNote> scaleNotes) throws MelpodySelectionProcessException {
		MidiNote returnNote = null;
		
		List<MidiNote>  subSetSelection = null;
		List<Double> probabilityList = Arrays.asList(HIGH_PROB, MED_PROB, LOW_PROB);
		List<Integer> gradesCompatibleWithScale = getAllGradesFromPossibleNotes(possibleNoteList, scaleNotes, indexOffset);
		Map<Double, List<MidiNote>> mapCompatibleGradeProb = getMapCompatibility(gradesCompatibleWithScale, possibleNoteList);
		
		if(gradesCompatibleWithScale == null || 
				gradesCompatibleWithScale.isEmpty() || 
				mapCompatibleGradeProb == null || 
				mapCompatibleGradeProb.isEmpty()) {
			throw new MelpodySelectionProcessException("ATTENTION... NO grades found in the list");
		}
		
		//Se voglio una nota che crei tensione
		if(novelty) {
			
			log.info("Novelty activated");
			Collections.reverse(probabilityList);
			
			int i = 0;
			do {
				Double prob = probabilityList.get(i);
				subSetSelection = mapCompatibleGradeProb.get(prob);
				i++;
				log.fine("novelty index " + i);
			}while(subSetSelection == null && i < probabilityList.size());
			
		}else { // se voglio una nota che crei stabilità
			
			if(NoteLength.getSlowNoteLengths().contains(noteLength) && mapCompatibleGradeProb.get(HIGH_PROB) != null) {
				subSetSelection = mapCompatibleGradeProb.get(HIGH_PROB);
				log.info("High prob: 1, 5");

			}else if(NoteLength.getMediumNoteLengths().contains(noteLength) && mapCompatibleGradeProb.get(MED_PROB) != null) {
				subSetSelection = mapCompatibleGradeProb.get(MED_PROB);
				log.info("Med prob: 3,7");
			
			}else if(NoteLength.getFastNoteLengths().contains(noteLength) && mapCompatibleGradeProb.get(LOW_PROB) != null) {
				subSetSelection = mapCompatibleGradeProb.get(LOW_PROB);
				log.info("Low prob: 2,4,6");
				
			}
		}
		
		if(subSetSelection == null) {
			log.severe("No grade for that prob");
			for(Double prob : probabilityList) {
				if(mapCompatibleGradeProb.get(prob) != null) {
					subSetSelection = mapCompatibleGradeProb.get(prob);
					log.info(prob + " used");
					break;
				}
			}
		}
		
		int indexSubSelection = (int) (Math.round(Math.random() * subSetSelection.size()));
		
		returnNote = subSetSelection.get(Math.min(indexSubSelection, subSetSelection.size() - 1));
		
		return returnNote;
	}

	/**
	 * This method creates a map where the key is the probability assigned to the grades, and the values are the notes with that probability
	 * @param gradesCompatibleWithScale the grades of the scale compatible with the possible notes 
	 * @param possibleNoteList possible notes
	 * @return the map probability-list of notes with that probability
	 */
	private static Map<Double, List<MidiNote>> getMapCompatibility(List<Integer> gradesCompatibleWithScale, List<MidiNote> possibleNoteList) {
		
		Map<Double, List<MidiNote>> returnMap = new TreeMap<Double, List<MidiNote>>();
		
		int idx = 0;
		for(Integer grade : gradesCompatibleWithScale) {
			for(Double prob : noteProbabilityMap.keySet()) {
				for(Integer i : noteProbabilityMap.get(prob)) {
					if(i.intValue() == grade.intValue()) {
						List<MidiNote> grades = returnMap.get(prob);
						if(grades == null) {
							grades = new ArrayList<MidiNote>();
						}
						grades.add(possibleNoteList.get(idx));
						returnMap.put(prob, grades);
					}
				}
			}
			idx++;
		}
		
		return returnMap;
	}

	/**
	 * This method creates a list of integer representing the list of grades of a given scale compatible to a set of notes
	 * @param possibleNoteList the set of notes
	 * @param scaleNotes the notes of the scale
	 * @param indexOffset the offset between the root fundamental of the harmony and the root of the scale selected 
	 * @return the list of compatible grades of the scale
	 */
	private static List<Integer> getAllGradesFromPossibleNotes(List<MidiNote> possibleNoteList, List<MidiNote> scaleNotes, int indexOffset) {
		List<Integer> returnList = null;
		if(possibleNoteList != null && !possibleNoteList.isEmpty()) {
			returnList = new ArrayList<Integer>();
			
			for(MidiNote pn : possibleNoteList) {
				
				for(int i = 0; i < scaleNotes.size(); i++) {
					int idx = (i + indexOffset) % scaleNotes.size();
					if(scaleNotes.get(idx).getNote().equals(pn.getNote())) {
						returnList.add(i);
					}
				}
			}
		}
		
		return returnList;
	}
	
}
