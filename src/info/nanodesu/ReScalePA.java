package info.nanodesu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class ReScalePA {
	
	private static String createModInfo(float factor) {
		return 	"{\n"+
			    "\"enabled\": true,\n" + 
			    "\"context\": \"server\",\n"+
			    "\"identifier\": \"info.nanodesu.rescale."+String.format("%s",factor)+"\",\n"+
			    "\"display_name\": \"ReScale Factor "+String.format("%s",factor)+"\",\n"+
			    "\"description\": \"Recreate the classic SupCom 'I am watching ants' feeling\",\n"+
			    "\"author\": \"Cola_Colin\",\n"+
			    "\"version\": \"0.1\",\n"+
			    "\"signature\": \"(not yet implemented)\"\n"+
			    "}\n";
	}
	
	private static final String TEXTURE_MOD_INFO = "{    \"context\": \"client\",    \"identifier\": \"info.nanodesu.rescale.textures\",    \"display_name\": \"ReScale Textures\",    \"description\": \"Textures for the use with rescale textures\",    \"author\": \"Cola_Colin\",    \"version\": \"1.0\",    \"build\": \"72996\",    \"date\": \"2014/09/30\",    \"forum\": \"https://forums.uberent.com/threads/rel-rescalepa-converter-to-create-smaller-units.64462/\",    \"signature\": \"not yet implemented\",    \"category\": [        \"textures\",		\"framework\"    ],    \"id\": \"rescale-textures\",    \"enabled\": true,    \"priority\": 100}";
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 1) {
			System.out.println("Usage: ReScalePA <path to configuration>. Trying to use my hardcoded path because I am lazy, this will crash for you if you are not Cola_Colin...");
			args = new String[]{"C:\\Users\\ColaColin\\git\\ReScalePa\\conf.txt"};
		}

		new ReScalePA(new ReScaleConf(FileUtils.readLines(new File(args[0])))).createMod();
	}
	
	private File basePath;
	private File outputPath;
	private float factor;
	
	private ReScaleConf conf;

	public ReScalePA(ReScaleConf conf) {
		this.conf = conf;
		basePath = new File(conf.getPaUnitsPath());
		outputPath = new File(conf.getModOutputPath());
		this.factor = conf.getScale();
	}
	
	private List<String> getUnitJsons() throws IOException {
		String unitList = FileUtils.readFileToString(new File(basePath, "unit_list.json"));
		JsonObject o = JsonObject.readFrom(unitList);
		JsonArray ar = o.get("units").asArray();
		List<String> lst = new ArrayList<>();
		for (int i = 0; i < ar.size(); i++) {
			lst.add(ar.get(i).asString());
		}
		return lst;
	}
	
	private List<String> getUnitsToProcess() throws IOException {
		if (conf.getUnitsToConvert().isEmpty()) {
			List<String> paUnits = getUnitJsons();
			paUnits.removeAll(conf.getUnitsToIgnore());
			return paUnits;
		} else {
			return conf.getUnitsToConvert();
		}
	}
	
	public void createMod() throws IOException {
		FileUtils.writeStringToFile(new File(outputPath, "modinfo.json"), createModInfo(factor));
		
		if (conf.getTextureModOutputPath() != null) {
			FileUtils.writeStringToFile(new File(conf.getTextureModOutputPath(), "modinfo.json"), TEXTURE_MOD_INFO);
		}
		
		List<String> units = getUnitsToProcess();
		for (String unit: units) {
			try {
				File outJson = new File(outputPath, unit);
				outJson.getParentFile().mkdirs();
				ReScaleJson jsonScale = processUnitJson(unit);
				for (String model: jsonScale.getPapas()) {
					processModel(model);
				}
				jsonScale.writeOutput(outJson, outputPath);
				System.out.println("processed "+unit);
			} catch (Exception ex) {
				System.out.println("Error processing unit: "+unit);
				ex.printStackTrace(System.out);
			}
		}
	}
	
	private void processModel(String model) throws IOException {
		ReScalePaPa papaScale = new ReScalePaPa(factor);
		File papaFile = new File(basePath, model.replace("/pa/units/", ""));
		papaScale.readFile(papaFile);
		papaScale.process();
		File out = new File(outputPath, model);
		String modelName = out.getParentFile().getName();
		
		File textureOut = out;
		if (conf.getTextureModOutputPath() != null) { 
			textureOut = new File(conf.getTextureModOutputPath(), model);
			textureOut.getParentFile().mkdirs();
		}
		copyTextures(papaFile, textureOut, modelName);
		
		papaScale.writeOutput(new File(out.getAbsolutePath().replace(".papa", "X.papa")));
	}

	private void copyTextures(File papaFile, File out, String modelName)
			throws IOException {
		String[] textures = new String[]{"_diffuse.papa", "_mask.papa", "_material.papa"};
		for (String texture: textures) {
			FileUtils.copyFile(new File(papaFile.getParentFile(), modelName+texture), new File(out.getParentFile(), modelName+"X"+texture));
		}
	}
	
	private ReScaleJson processUnitJson(String str) throws IOException {
		File json = new File(basePath, str.replace("/pa/units/", ""));
		ReScaleJson jsonScale = new ReScaleJson(factor, basePath.getParentFile().getParentFile());
		jsonScale.readFile(json);
		jsonScale.process();
		return jsonScale;
	}
}
