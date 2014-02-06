package util;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonReader {
	private static Logger logger = Logger.getLogger(JsonReader.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	private JsonReader(){
		/*Do not instantiate*/
	}
	
	public static <T> T readJson(String fileName, Class<T> clazz){		
		T object = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			object = mapper.readValue(new File("json/"+fileName), clazz);
		} catch (Exception e) {
			logger.fatal("Problem reading json file at json/" + fileName);
			e.printStackTrace();
			System.exit(-1);
		}
		return object;
	}
	
	
}


