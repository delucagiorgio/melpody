package melpody.midi.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import melpody.MelpodyMain;
import melpody.midi.chord.PossibleScale;
import melpody.midi.note.MidiNote;
import melpody.midi.note.Note;
import melpody.midi.scale.ModalScale;
import melpody.midi.scale.ModalScaleInitProcess;
import melpody.midi.scale.ScaleType;
import melpody.midi.scale.ScaleTypeInitProcess;

/**
 * 
 * @author Giorgio De Luca
 * 
 * Class used to retrieve information about the harmony track (notes or possible scale at a certain point)
 *
 */
public class HarmonyMidiTrack extends AbstractMidiTrack {
	
	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	
	protected HarmonyMidiTrack() {
		super();
	}
	
	/**
	 * Use this method to explore the possible scales that can be used on the harmony defined in a certain
	 * instant of time.
	 * 
	 * For each event of "NOTE_ON" are scanned the notes played at that instant of time.
	 * 
	 * The method returns a map where the id is the instant of time (as defined above) and as value
	 * all the possible scales that best fit the note played.
	 * @param scaleTypeSelector the possible types of scales for the process
	 * @param modalScaleSelector the possible modal scales for the process 
	 * @return a map where the id is the instant of time (as defined above) and as value
	 * all the possible scales that best fit the note played.
	 */
	public Map<Long, List<PossibleScale>> getScalesMap(ModalScaleInitProcess modalScaleSelector, ScaleTypeInitProcess scaleTypeSelector){
		Map<Long, List<PossibleScale>> chordsMap = new TreeMap<Long, List<PossibleScale>>();
		
		Long previousTick = null;
		
		for(Long tick : eventTimeList) {
			List<PossibleScale> scale = getPossibleScaleFromTick(tick, modalScaleSelector, scaleTypeSelector);
			if(scale != null) {
				chordsMap.put(tick, scale);
				previousTick = tick;
			}else if(previousTick != null){
				scale = getPossibleScaleFromTick(previousTick, modalScaleSelector, scaleTypeSelector);
			}else {
				log.log(Level.SEVERE, "Unexpected error");
			}
		}
		
		return chordsMap;
	}
	
	/**
	 * Use this method to retrieve which are the note "pressed" at a certain instant of time
	 * @param tick the instant of time
	 * @return the list of note pressed
	 */
	public List<MidiNote> getPressedNoteAtTick(Long tick){
		
		List<MidiNote> notePressedAtTick = new ArrayList<MidiNote>();
		
		for(MidiNote mn : midiInformation.getMidiNotes()) {
			if(mn.getTimestampEventStart() <= tick && mn.getTimestampEventEnd() >= tick) {
				notePressedAtTick.add(mn);
			}else if(!notePressedAtTick.isEmpty() && mn.getTimestampEventStart() <= tick && mn.getTimestampEventEnd() >= tick){
				break;
			}
		}
		
		return notePressedAtTick;
	}
	
	/**
	 * Given a certain instant of time, the method defines which are the best possible scales, evaluating the 
	 * pressed notes of the harmony in the same instant of time. 
	 * @param tick the instant of time
	 * @param scaleTypeSelector the possible types of scales for the process
	 * @param modalScaleSelector the possible modal scales for the process 
	 * @return the list of possible scales
	 */
	private List<PossibleScale> getPossibleScaleFromTick(Long tick, ModalScaleInitProcess modalScaleSelector, ScaleTypeInitProcess scaleTypeSelector) {
		//Prendo le note che sono suonate al tick passato come parametro
		List<MidiNote> notePressedAtTick = getPressedNoteAtTick(tick);
		
		//Faccio un controllo con tutte le scale possibili e creo l'accordo che meglio si presta alle note suonate
		return findBestChord(notePressedAtTick, modalScaleSelector, scaleTypeSelector);
	}

	/**
	 * This method finds all the possible scale containing the notes passed as parameter.
	 * 
	 * The selection of the scale is ruled by the maximum number of notes "found" respect to the number of notes passed as parameter.
	 * 
	 * @param notePressedAtTick the notes to be found
	 * @param scaleTypeSelector the possible types of scales for the process
	 * @param modalScaleSelector the possible modal scales for the process 
	 * @return the list of possible scale containing the maximum number of parameter's notes found among all possible scales
	 */
	private List<PossibleScale> findBestChord(List<MidiNote> notePressedAtTick, ModalScaleInitProcess modalScaleSelector, ScaleTypeInitProcess scaleTypeSelector) {
		List<PossibleScale> chord = null;
		
		
		if(notePressedAtTick != null && !notePressedAtTick.isEmpty()) {
			Map<Note, Map<ScaleType, Map<ModalScale, List<Note>>>> processMap = new HashMap<Note, Map<ScaleType, Map<ModalScale, List<Note>>>>(); 

			int maxCorrespondenceCount = 0;
			
			for(Note rootNote : Note.values()) {
				//considero la nota come root della scala e confronto le restanti note con l'ipotetica scala di riferimento, in tutti i suoi modi
				Map<ScaleType, Map<ModalScale, List<Note>>> scaleMap = new HashMap<ScaleType, Map<ModalScale, List<Note>>>();
				processMap.put(rootNote, scaleMap);
				
				for(ScaleType scaleType : scaleTypeSelector.getActiveScaleType()) {
					
					Map<ModalScale, List<Note>> modalScaleMap = new HashMap<ModalScale, List<Note>>();
					scaleMap.put(scaleType, modalScaleMap);
					
					for(ModalScale modalScale : modalScaleSelector.getActiveModalScale()) {
						List<Note> noteCheckList = new ArrayList<Note>();
						modalScaleMap.put(modalScale, noteCheckList);
						
						//check delle note presenti sulla scala
						List<Integer> intervalList = scaleType.intervalNoteScale;
						List<Integer> modalIntervalList = new ArrayList<Integer>(intervalList.size());
						
						for(int i = 0; i < intervalList.size(); i++) {
							int indexTrasposition = (i + modalScale.getProgressiveIndex()) % intervalList.size();
							modalIntervalList.add(intervalList.get(indexTrasposition));
						}
						
						for(int j = 0; j < notePressedAtTick.size(); j++) {
							MidiNote checkNote = notePressedAtTick.get(j);
							
							int offsetNoteCheck = checkNote.getNote().getOffsetFromC();
							//Controllo ogni possibile nota della scala se corrisponde alla nota premuta
							int count = 0;
							for(Integer step : modalIntervalList) {
								count += step;
								
								int stepOffset = (rootNote.getOffsetFromC() + count) % 12;
								
								//Appartiene alla scala
								if(stepOffset == offsetNoteCheck) {
									noteCheckList.add(checkNote.getNote());
									if(maxCorrespondenceCount < noteCheckList.size()) {
										maxCorrespondenceCount = noteCheckList.size();
									}
									break;
								}
							}
						}
					}
				}
			}
			
			if(!processMap.isEmpty()) {
				
				Set<Note> noteSet = new HashSet<Note>(processMap.keySet());
				for(Note n : noteSet) {
					Set<ScaleType> scaleTypeSet = new HashSet<ScaleType>(processMap.get(n).keySet());
					for(ScaleType st : scaleTypeSet) {
						Set<ModalScale> modalScaleSet = new HashSet<ModalScale>(processMap.get(n).get(st).keySet());
						for(ModalScale ms : modalScaleSet) {
							if(processMap.get(n).get(st).get(ms).size() < maxCorrespondenceCount) {
								processMap.get(n).get(st).remove(ms);
							}
						}
						
						if(processMap.get(n).get(st).size() == 0) {
							processMap.get(n).remove(st);
						}
					}
					if(processMap.get(n).size() == 0) {
						processMap.remove(n);
					}
				}
				
				chord = new ArrayList<PossibleScale>();
				
				noteSet = new HashSet<Note>(processMap.keySet());
				for(Note n : noteSet) {
					Set<ScaleType> scaleTypeSet = new HashSet<ScaleType>(processMap.get(n).keySet());
					for(ScaleType st : scaleTypeSet) {
						Set<ModalScale> modalScaleSet = new HashSet<ModalScale>(processMap.get(n).get(st).keySet());
						for(ModalScale ms : modalScaleSet) {
							chord.add(new PossibleScale(n, getAllNoteScaleMode(n, st, ms), ms, st));
						}
					}
				}
			}
		}
		
		return chord;
	}

	/**
	 * Use this method to retrieve the list of notes of a key value (note, type of scale, mode of the scale)
	 * @param n the note
	 * @param st the type of scale
	 * @param ms the mode of the scale
	 * @return the list of notes in the scale defined by parameters
	 */
	public static List<MidiNote> getAllNoteScaleMode(Note n, ScaleType st, ModalScale ms) {
		List<MidiNote> allNote = new ArrayList<MidiNote>();
		MidiNote previousNote = new MidiNote();
		previousNote.setNote(n);
		allNote.add(previousNote);
		List<Integer> modalIntervalList = new ArrayList<Integer>(st.intervalNoteScale.size());
		
		for(int i = 0; i < st.intervalNoteScale.size(); i++) {
			int indexTrasposition = (i + ms.getProgressiveIndex()) % (st.intervalNoteScale.size());
			modalIntervalList.add(st.intervalNoteScale.get(indexTrasposition));
		}
		
		for(Integer step : modalIntervalList) {
			Note noteToBeAdded = Note.getNote((previousNote.getNote().getOffsetFromC() + step) % 12);
			MidiNote midiToBeAdded = new MidiNote();
			midiToBeAdded.setNote(noteToBeAdded);
			
			if(!allNote.contains(midiToBeAdded)) {
				allNote.add(midiToBeAdded);
			}
			previousNote = midiToBeAdded;
		}
		
		return allNote;
	}

}
