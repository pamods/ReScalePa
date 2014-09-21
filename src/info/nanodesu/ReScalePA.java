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
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		args = new String[]{"E:\\Games\\PA\\Planetary Annihilation\\stable\\media\\pa\\units", "C:\\Users\\ColaColin\\AppData\\Local\\Uber Entertainment\\Planetary Annihilation\\server_mods\\rescale", "0.5"};		

		if (args.length != 3) {
			System.out.println("Usage: ReScalePA <path to media/pa> <empty output directory for the mod> <factor>");
		} else {
			new ReScalePA(new File(args[0]), new File(args[1]), Float.parseFloat(args[2])).createMod();
		}
	}
	
	private File basePath;
	private File outputPath;
	private float factor;
	
	public ReScalePA(File input, File output, float factor) {
		basePath = input;
		outputPath = output;
		this.factor = factor;
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
	
	public void createMod() throws IOException {
		FileUtils.writeStringToFile(new File(outputPath, "modinfo.json"), createModInfo(factor));
		
		List<String> units = getUnitJsons();
		for (String unit: units) {
			processUnit(unit);
		}
	}
	
	private void processUnit(String str) {
		try {
			File json = new File(basePath, str.replace("/pa/units/", ""));
			ReScaleJson jsonScale = new ReScaleJson(factor);
			jsonScale.readFile(json);
			jsonScale.process();
			
			ReScalePaPa papaScale = new ReScalePaPa(factor);
			papaScale.readFile(new File(json.getAbsolutePath().replace(".json", ".papa")));
			papaScale.process();
			
			File out = new File(outputPath, str);
			out.getParentFile().mkdirs();
			
			System.out.println("processing for "+str+" complete, writing results...");
			papaScale.writeOutput(new File(out.getAbsolutePath().replace(".json", ".papa")));
			jsonScale.writeOutput(out);
		} catch (Exception ex) {
			System.out.println("Error processing unit: "+str);
			ex.printStackTrace(System.out);
		}
	}
}
