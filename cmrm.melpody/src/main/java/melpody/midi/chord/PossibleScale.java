package melpody.midi.chord;

import java.util.List;

import melpody.midi.note.MidiNote;
import melpody.midi.note.Note;
import melpody.midi.scale.ModalScale;
import melpody.midi.scale.ScaleType;
/**
 * 
 * @author Giorgio De Luca
 * Class to define a scale. It is defined by:
 * - a fundamental note
 * - a mode
 * - a type of scale
 * - a list of notes
 */
public class PossibleScale {

	private Note fundamentalNote;
	private ModalScale scale;
	private ScaleType scaleType;
	private List<MidiNote> noteList;
	
	public PossibleScale(Note fundamentalNote, List<MidiNote> noteList, ModalScale scale, ScaleType scaleType) {
		this.fundamentalNote = fundamentalNote;
		this.scale = scale;
		this.scaleType = scaleType;
		this.noteList = noteList;
	}

	public Note getFundamentalNote() {
		return fundamentalNote;
	}

	public void setFundamentalNote(Note fundamentalNote) {
		this.fundamentalNote = fundamentalNote;
	}

	public ModalScale getScale() {
		return scale;
	}

	public void setScale(ModalScale scale) {
		this.scale = scale;
	}

	public List<MidiNote> getNoteList() {
		return noteList;
	}

	public void setNoteList(List<MidiNote> noteList) {
		this.noteList = noteList;
	}

	public ScaleType getScaleType() {
		return scaleType;
	}

	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}
	
}
