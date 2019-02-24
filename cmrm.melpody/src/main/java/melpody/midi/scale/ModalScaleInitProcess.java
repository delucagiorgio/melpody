package melpody.midi.scale;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import melpody.MelpodyMain;

/**
 * 
 * @author Giorgio De Luca
 * Class to select the modal scales that are going to be used in the process to generate all possible scales
 *
 */
public final class ModalScaleInitProcess {

	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	private List<ModalScale> activeModalScale;
	
	public ModalScaleInitProcess() {
		activeModalScale = new ArrayList<ModalScale>();
	}
	
	/**
	 * Use this method to read the properties of the program and initialize the list of active modal scales
	 * @param properties properties of the program
	 */
	public void collectActiveModalScale(Properties properties) {
		
		String text = "";
		for(ModalScale ms : ModalScale.values()) {
			String value = properties.getProperty(ms.toString());
			if("1".equals(value)) {
				activeModalScale.add(ms);
				text = text + "," + ms;
			}
		}
		
		log.info("Modal scales active = " + text);
		
	}

	public List<ModalScale> getActiveModalScale() {
		return activeModalScale;
	}
}
