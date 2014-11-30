package params;

import java.io.File;
import java.util.Collection;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import util.JsonReader;

import com.google.common.collect.Lists;

/**
 * Generates a series of <code>Params</code> instances based
 * on a json file or directory containing a series of json files.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class ParamsFactory {
	
	final private File jsonDir;
	final private List<Params> paramsList = Lists.newArrayList();
	
	public ParamsFactory(String jsonPath) {
		this.jsonDir = new File(jsonPath);
	}
	
	public Collection<Params> make() {

		if (jsonDir.isDirectory()) {
			for (final File jsonFile : jsonDir.listFiles()) {
				if (isJson(jsonFile)) {					
					log.info(String.format("Reading json %s from %s", jsonFile, jsonDir));
					readFile(jsonFile);
				} else {
					log.info(String.format("Ignoring file %s from %s", jsonFile, jsonDir));
				}
			}
		} else {
			//The given "dir" is actually a file
			log.info(String.format("Reading json %s", jsonDir));
			readFile(jsonDir);
		}
		
		return paramsList;
	}
	
	private boolean isJson(File file) {
		String[] splitByTermination = file.getName().split("\\.");
		int len = splitByTermination.length;
		//Check that the last termination is json (there could be more than one . in the file name)
		return splitByTermination[len - 1].equals("json");
	}
	
	private void readFile(File jsonFile) {
		Params params = JsonReader.readJson(jsonFile, Params.class);
		params.setFile(jsonFile.getPath());
		paramsList.add(params);
	}
	
}


