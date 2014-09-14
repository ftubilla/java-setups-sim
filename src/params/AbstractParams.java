package params;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public abstract class AbstractParams {

	/**
	 * Returns a collection of parameter, item, value triads.
	 */
	@SuppressWarnings("unchecked")
	public Collection<String[]> getValueTriads() {
		
		List<String[]> valueTriads = new ArrayList<String[]>();
		for (Field field : this.getClass().getDeclaredFields()){
			Object value;
			try {
				value = field.get(this);
			} catch (Exception e) {
				log.error(String.format("Could not get value triads for %s. Declare the fields as protected.", this));
				log.error(e.getMessage());
				return null;
			}
			if (value instanceof Iterable) {
				int item = 0;
				for (Object itemValue : (Iterable<Object>) value) {
					valueTriads.add(new String[]{field.getName(), item + "", itemValue.toString()});
					item++;					
				}
			} else {
				if (value instanceof AbstractParams) {
					for ( String[] triad : ( (AbstractParams) value).getValueTriads() ){
						String compositeField = field.getName() + "." + triad[0];
						valueTriads.add(new String[]{compositeField, triad[1], triad[2]});
					}
				} else {
					valueTriads.add(new String[] {field.getName(), "NA", value.toString()});
				}
			}
		}
		return valueTriads;
	}

}


