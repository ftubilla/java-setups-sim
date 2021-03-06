package util;

import org.apache.log4j.Logger;

public class AlgorithmLoader {

	private static Logger logger = Logger.getLogger(AlgorithmLoader.class);

	private AlgorithmLoader(){
		/*
		 * Not meant to be instantiated
		 */
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T load(String packageName, String className, Class<T> clazz){
		
		logger.info("Loading class " + packageName + "." + className);
		Class<?> loadedClass;
		T instance = null;		
		
		try {
			loadedClass = Class.forName(packageName + "." + className);
			instance = (T) loadedClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return instance;
	}
	
	
	
}
