package sim;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonReader {
	private static Logger logger = Logger.getLogger(JsonReader.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	public static <T> T read(String fileName, Class<T> clazz){
		
		T data = null;
		// Read data
		try {
			ObjectMapper mapper = new ObjectMapper();
			data = mapper.readValue(new File("json/" + fileName), clazz);
		} catch (Exception e) {
			logger.fatal("Problem reading input json file!");
			e.printStackTrace();
			System.exit(-1);
		}	
		
		return data;
	}
	
}


