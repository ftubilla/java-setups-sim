package util;

import java.io.File;

import lombok.extern.apachecommons.CommonsLog;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

@CommonsLog
public class JsonReader {
	
	private JsonReader(){
		/*Do not instantiate*/
	}
			
	/**
	 * Reads the json file in the json directory.
	 *  
	 * @param fileName
	 * @param clazz
	 * @return
	 */
	public static <T> T readJsonRelativePath(String fileName, Class<T> clazz){		
		return readJsonAbsolutePath("inputs/" + fileName, clazz);
	}
	
	public static <T> T readJsonAbsolutePath(String fileName, Class<T> clazz) {
		return readJson(new File(fileName), clazz);
	}
		
	public static <T> T readJson(File file, Class<T> clazz) {
		T object = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule((Module) new GuavaModule());	//This is needed for deserializing Guava data types
			mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
			object = mapper.readValue(file, clazz);
		} catch (Exception e) {
			log.fatal("Problem reading json file " + file);
			e.printStackTrace();
			System.exit(-1);
		}
		return object;
	}
	
	
}


