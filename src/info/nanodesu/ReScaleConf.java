package info.nanodesu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReScaleConf {
	
	private String paUnitsPath;
	private String modOutputPath;
	private float scale;
	
	private List<String> unitsToConvert;
	private List<String> unitsToIgnore;

	public ReScaleConf(List<String> confLines) {
		unitsToConvert = new ArrayList<>();
		unitsToIgnore = new ArrayList<>();
		Iterator<String> iter = confLines.iterator();
		while(iter.hasNext()) {
			String line = iter.next();
			if (line.startsWith("#")) {
				// comment
			} else if (line.startsWith("scale=")) {
				scale = Float.parseFloat(line.replaceFirst("scale=", ""));
			} else if (line.startsWith("pa.units=")) {
				paUnitsPath = line.replaceFirst("pa.units=", "");
			} else if (line.startsWith("mod.output=")) {
				modOutputPath = line.replaceFirst("mod.output=", "");
			} else if (line.startsWith("convert")) {
				readLinesToList(unitsToConvert, iter);
			} else if (line.startsWith("ignore")) {
				readLinesToList(unitsToIgnore, iter);
			}
		}
	}
	
	private void readLinesToList(List<String> lst, Iterator<String> iter) {
		while(iter.hasNext()) {
			String line = iter.next();
			if (!line.startsWith("#")) {
				if (line.startsWith("endblock")) {
					break;
				} else {
					lst.add(line);
				}
			}
		}
	}

	public String getPaUnitsPath() {
		return paUnitsPath;
	}

	public void setPaUnitsPath(String paUnitsPath) {
		this.paUnitsPath = paUnitsPath;
	}

	public String getModOutputPath() {
		return modOutputPath;
	}

	public void setModOutputPath(String modOutputPath) {
		this.modOutputPath = modOutputPath;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public List<String> getUnitsToConvert() {
		return new ArrayList<String>(unitsToConvert);
	}

	public List<String> getUnitsToIgnore() {
		return new ArrayList<String>(unitsToIgnore);
	}
	
	
}
