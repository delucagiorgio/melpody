package melpody.midi.scale;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import melpody.MelpodyMain;

/**
 * 
 * @author Giorgio De Luca
 * Class to select the types of scales that are going to be used in the process to generate all possible scales
 *
 */
public final class ScaleTypeInitProcess {

	private static Logger log = Logger.getLogger(MelpodyMain.MELPODY_LOGNAME);
	private List<ScaleType> activeScaleType;
	
	public ScaleTypeInitProcess() {
		activeScaleType = new ArrayList<ScaleType>();
	}
	
	/**
	 * Use this method to read the properties of the program and initialize the list of active types of scales
	 * @param properties properties of the program
	 */
	public void collectActiveTypesOfScale(Properties properties) {
		
		String text = "";
		for(ScaleType st : ScaleType.values()) {
			String value = properties.getProperty(st.toString());
			if("1".equals(value)) {
				activeScaleType.add(st);
				text = text + "," + st;
			}
		}
		
		log.info("Types of scales active = " + text);
	}

	public List<ScaleType> getActiveScaleType() {
		return activeScaleType;
	}
	
}
