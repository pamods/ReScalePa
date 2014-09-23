package info.nanodesu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class ReScaleJson {
	
	public static void main(String[] args) throws IOException {
		ReScaleJson s = new ReScaleJson(.5f, new File("E:\\Games\\PA\\Planetary Annihilation\\stable\\media"));
		s.readFile(new File("E:\\Games\\PA\\Planetary Annihilation\\stable\\media\\pa\\units\\land\\assault_bot\\assault_bot.json"));
		s.process();
		s.writeOutput(new File("C:\\Users\\ColaColin\\AppData\\Local\\Uber Entertainment\\Planetary Annihilation\\server_mods\\rescale\\pa\\units\\land\\assault_bot\\assault_bot.json"), new File("C:\\Users\\ColaColin\\AppData\\Local\\Uber Entertainment\\Planetary Annihilation\\server_mods\\rescale"));
	}
	
	private JsonObject json = null;
	
	private float factor;
	
	private String unitFile;
	
	private Set<String> papas;
	private Map<String, String> skirts;
	
	private File mediaPaFolder;
	
	public ReScaleJson(float f, File mPFolder) {
		factor = f;
		papas = new HashSet<>();
		skirts = new HashMap<>();
		mediaPaFolder = mPFolder;
	}
	
	public void readFile(File f) throws IOException {
		unitFile = f.getAbsolutePath();
		String str = FileUtils.readFileToString(f);
		json = JsonObject.readFrom(str);
	}
	
	public void process() throws IOException {
		scaleMesh();
		scalePlacementSize();
		scaleEvents();
		scaleFxOffsets();
		scaleLights();
		scalePhysicsRadius();
		scaleAttachable();
		
		// the point of this is to rename all models and animations and return a list of the model/animation names before the rename
		JsonValue m = json.get("model");
		
		if (m != null) {
			if (m.isObject()) {
				scaleSkirt(m);
				xModel(m, "filename");
				JsonValue anim = m.asObject().get("animations");
				if (anim != null) {
					xAnimations(anim.asObject());
				}
			} else if (m.isArray()) {
				JsonArray ja = m.asArray();
				for (int i = 0; i < ja.size(); i++) {
					xModel(ja.get(i), "filename");
					scaleSkirt(ja.get(i));
					JsonValue anim = ja.get(i).asObject().get("animations");
					if (anim != null) {
						xAnimations(anim.asObject());
					}
				}
			}
		}
	}
	
	private void scaleEvents() {
		JsonValue events = json.get("events");
		if (events != null) {
			for (String key: events.asObject().names()) {
				JsonValue jv = events.asObject().get(key);
				JsonValue scale = jv.asObject().get("effect_scale");
				if (scale != null) {
					jv.asObject().set("effect_scale", scale.asFloat() * factor);
				} else {
					jv.asObject().set("effect_scale", factor);
				}
			}
		}
	}
	
	private void scaleAttachable() {
		JsonValue attach = json.get("attachable");
		if (attach != null) {
			JsonValue o = attach.asObject().get("offsets");
			if (o != null) {
				for (String key: o.asObject().names()) {
					scaleArray(o.asObject().get(key));
				}
			}
		}
	}
	
	private void scaleFxOffsets() {
		baseKeyOffsetScale("fx_offsets");
	}
	
	private void scaleLights() {
		baseKeyOffsetScale("headlights");
		baseKeyOffsetScale("lamps");
	}
	
	private void scalePhysicsRadius() {
		JsonValue phy = json.get("physics");
		if (phy != null) {
			JsonValue v = phy.asObject().get("radius");
			if (v != null && v.isNumber()) {
				phy.asObject().set("radius", v.asFloat() * factor);
			}
		}
	}
	
	private void baseKeyOffsetScale(String baseKey) {
		JsonValue fx = json.get(baseKey);
		if (fx != null) {
			JsonArray array = fx.asArray();
			for (int i = 0; i < array.size(); i++) {
				JsonObject obj = array.get(i).asObject();
				JsonValue v = obj.get("offset");
				if (v != null) {
					scaleArray(v);
				}
			}
		}
	}
	
	private void scaleSkirt(JsonValue modelNode) throws IOException {
		JsonValue skirtDecals = modelNode.asObject().get("skirt_decal");
		if (skirtDecals != null) {
			String skirtPath = mediaPaFolder.getAbsolutePath() + skirtDecals.asString();
			scaleSkirt(skirtPath, skirtDecals.asString());
		}
	}
	
	private void scaleSkirt(String file, String name) throws IOException {
		JsonObject skirt = JsonObject.readFrom(FileUtils.readFileToString(new File(file)));
		scaleArray(skirt.get("scale"));
		skirts.put(name, skirt.toString());
	}
	
	private void xAnimations(JsonObject obj) {
		for (String key: obj.names()) {
			xModel(obj, key);
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

	public Set<String> getPapas() {
		return papas;
	}
	
	private void xModel(JsonValue m, String key) {
		final JsonValue jsonValue2 = m.asObject().get(key);
		if (jsonValue2 != null) {
			final String n = jsonValue2.asString();
			papas.add(n);
			m.asObject().set(key, n.replace(".papa", "")+"X.papa");
		}
	}
	
	public void writeOutput(File f, File mediaFolder) throws IOException {
		String txt = json.toString();
		FileUtils.writeStringToFile(f, txt);
		
		for (String skirtName: skirts.keySet()) {
			String skirtContenbt = skirts.get(skirtName);
			File foo = new File(mediaFolder, skirtName);
			FileUtils.writeStringToFile(foo, skirtContenbt);
		}
	}
}