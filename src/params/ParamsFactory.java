package params;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;
import util.JsonReader;

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
	
	public ParamsFactory(String jsonPath) {
		this.jsonDir = new File(jsonPath);
	}
	
	public Collection<Params> make() {

		List<Params> paramsList = new ArrayList<Params>();
		
		if (jsonDir.isDirectory()) {
			for (final File jsonFile : jsonDir.listFiles()) {
				log.info(String.format("Reading json %s from %s", jsonFile, jsonDir));
				Params params = JsonReader.readJson(jsonFile, Params.class);
				paramsList.add(params);
			}
		} else {
			log.info(String.format("Reading json %s", jsonDir));
			Params params = JsonReader.readJson(jsonDir, Params.class);
			paramsList.add(params);
		}
		
		return paramsList;
	}
	
}


