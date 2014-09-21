package info.nanodesu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class ReScaleJson {
	
	public static void main(String[] args) throws IOException {
		ReScaleJson s = new ReScaleJson(.5f);
		s.readFile(new File("E:\\Games\\PA\\Planetary Annihilation\\stable\\media\\pa\\units\\land\\assault_bot\\assault_bot.json"));
		s.process();
		s.writeOutput(new File("C:\\Users\\ColaColin\\AppData\\Local\\Uber Entertainment\\Planetary Annihilation\\server_mods\\rescale\\pa\\units\\land\\assault_bot\\assault_bot.json"));
	}
	
	private JsonObject json = null;
	
	private float factor;
	
	private String unitFile;
	
	private Set<String> models;
	
	public ReScaleJson(float f) {
		factor = f;
		models = new HashSet<>();
	}
	
	public void readFile(File f) throws IOException {
		unitFile = f.getAbsolutePath();
		String str = FileUtils.readFileToString(f);
		json = JsonObject.readFrom(str);
	}
	
	public void process() {
		scaleMesh();
		scalePlacementSize();
		
		
		// the point of this is to rename all models and return a list of the model names before the rename
		JsonValue m = json.get("model");
		if (m != null) {
			if (m.isObject()) {
				xModel(m);
			} else if (m.isArray()) {
				JsonArray ja = m.asArray();
				for (int i = 0; i < ja.size(); i++) {
					xModel(ja.get(i));
				}
			}
		}
	}
	
	private void scalePlacementSize() {
		JsonValue jsonvalue = json.get("placement_size");
		if (jsonvalue != null) {
			scaleArray(jsonvalue);
		}
	}
	
	private void scaleMesh() {
		JsonValue jsonValue = json.get("mesh_bounds");
		if (jsonValue != null) {
			scaleArray(jsonValue);
		} else {
			System.out.println("unit has no mesh_bounds: "+unitFile);
		}
	}

	private void scaleArray(JsonValue jsonValue) {
		JsonArray asArray = jsonValue.asArray();
		for (int i = 0; i < asArray.size(); i++) {
			JsonValue value = asArray.get(i);
			float m = factor * value.asFloat();
			asArray.set(i, m);
		}
	}

	public Set<String> getModels() {
		return models;
	}
	
	private void xModel(JsonValue m) {
		final JsonValue jsonValue2 = m.asObject().get("filename");
		if (jsonValue2 != null) {
			final String n = jsonValue2.asString();
			models.add(n);
			m.asObject().set("filename", n.replace(".papa", "")+"X.papa");
		}
	}
	
	public void writeOutput(File f) throws IOException {
		String txt = json.toString();
		FileUtils.writeStringToFile(f, txt);
	}
}