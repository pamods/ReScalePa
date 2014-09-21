package info.nanodesu;

import java.io.File;
import java.io.IOException;

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
	
	private boolean success = false;
	
	public ReScaleJson(float f) {
		factor = f;
	}
	
	public void readFile(File f) throws IOException {
		unitFile = f.getAbsolutePath();
		String str = FileUtils.readFileToString(f);
		json = JsonObject.readFrom(str);
	}
	
	public void process() {
		JsonValue jsonValue = json.get("mesh_bounds");
		if (jsonValue != null) {
			JsonArray asArray = jsonValue.asArray();
			for (int i = 0; i < 3; i++) {
				JsonValue value = asArray.get(i);
				float m = factor * value.asFloat();
				asArray.set(i, m);
			}
			success = true;
		} else {
			System.out.println("unit has no mesh_bounds: "+unitFile);
		}
	}
	
	public void writeOutput(File f) throws IOException {
		if (success) {
			String txt = json.toString();
			FileUtils.writeStringToFile(f, txt);
		}
	}
}