package experiment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Holds a set of different parameter values for a <em>fixed</em> item.
 * 
 * @author ftubilla
 *
 */
public class ItemParamValues {
	
	private static Logger logger = Logger.getLogger(ItemParamValues.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private int item;
	@JsonProperty private List<Object> values;
	
	public int getItem(){
		return item;
	}
	
	public Iterable<Double> getDoubles(){
		assert values.get(0) instanceof Double : "This parameter doesn't contain doubles!";
		List<Double> doubles = new ArrayList<Double>();
		for (Object value : values){
			doubles.add((Double) value);
		}
		return doubles;
	}
}


