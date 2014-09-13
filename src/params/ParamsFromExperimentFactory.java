package params;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import util.JsonReader;

import com.google.common.collect.ImmutableList;

/**
 * Reads an experiment folder containing a base json and the instances.csv,
 * and generates a series of simulation runs with the appropriate variations
 * on the parameters as specified in the csv file.
 *  
 * @author ftubilla
 *
 */
public class ParamsFromExperimentFactory {
		
	/**
	 * Columns of the csv file
	 */
	public static enum Column {INSTANCE, PARAMETER, ITEM, VALUE};
	
	private String experimentDir;
	
	public ParamsFromExperimentFactory(String experimentDir) {
		this.experimentDir = experimentDir;
	}
	
	public Collection<Params> make() throws FileNotFoundException, IOException {
			
		//List of params
		List<Params> paramsList = new ArrayList<Params>();
		
		//Fill the headers array and read the csv
		String[] headerNames = new String[Column.values().length];
		int i=0;
		for (Column col : Column.values()) {
			headerNames[i++] = col.toString();
		}
		CSVParser parser = new CSVParser(new FileReader("experiments/" + experimentDir + "/instances.csv"), 
				CSVFormat.RFC4180.withHeader(headerNames));
		
		//Read each csv record and create a new params object every time the instance number change, and
		//record the new parameter values as they change 
		boolean isHeader = true;
		int prevInstance = -1;			
		String prevParameter = null;	
		Params params = null;
		String prevItem = null;
		List<Object> parameterValues = new ArrayList<Object>();
		for (CSVRecord record : parser){
			
			if (isHeader) {
				//skip the header
				isHeader = false;
				continue;
			}
			//Read the instance number and check if it's different than what we had before
			int thisInstance = Integer.parseInt(record.get(Column.INSTANCE));
			if (thisInstance != prevInstance) {
				//We have a new instance. Create a new Params object using the base inputs.json.
				 params = JsonReader.readJsonAbsolutePath("experiments/" + experimentDir + "/inputs.json", Params.class);
			}	
			//Read the parameter in this record and check if it's different than the previous one or if the instance changed
			String thisParameter = record.get(Column.PARAMETER);
			String thisItem = record.get(Column.ITEM);
			if (thisInstance != prevInstance || !thisParameter.contentEquals(prevParameter) ) {
				if (!parameterValues.isEmpty()) {
					//If the parameter values list is not empty, then record the previous values and clear it
					setParameter(params, prevParameter, parameterValues);
					parameterValues.clear();
				}
				// Check that instances id are nondecreasing
				if (thisInstance < prevInstance) {
					throw new Error("The instances in the file are not sorted in increasing order!");
				}
			} else {
				//The item must be increasing
				int prevItemInt = Integer.parseInt(prevItem);
				int thisItemInt = Integer.parseInt(thisItem);
				if (thisItemInt <= prevItemInt) {
					throw new Error("The items in the file are not sorted in increasing order for a given instance-parameter combination!");
				}
			}
			//Read the value for the current parameter
			Object value;
			try {
				value = Double.parseDouble(record.get(Column.VALUE));
			} catch (NumberFormatException e) {
				value = record.get(Column.VALUE);
			}
			parameterValues.add(value);
			prevParameter = thisParameter;
			prevInstance = thisInstance;
			prevItem = thisItem;
			
		}
		setParameter(params, prevParameter, parameterValues);
		
		
		return null;
	}
	
	private void setParameter(Params params, String parameter, List<Object> values) {
		Method method = null;
		String methodName = toCamelCase(parameter);
		System.out.println("Params before " + params);
		try {
			  method = params.getClass().getMethod("set"+methodName, ImmutableList.class);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			List<Double> doubleList = new ArrayList<Double>();
			for (Object val : values){
				doubleList.add((Double) val);
			}
			method.invoke(params, ImmutableList.of(doubleList));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Params after " + params);
	}

	private String toCamelCase(String s) {
		String[] parts = s.split("_");
		String camelCaseString = "";
		for (String part : parts) {
			camelCaseString = camelCaseString + toProperCase(part);
		}
		return camelCaseString;
	}

	private String toProperCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	 
	public static void main(String[] args) throws FileNotFoundException, IOException {
		ParamsFromExperimentFactory factory = new ParamsFromExperimentFactory(args[0]);
		factory.make();
	}
	
}


