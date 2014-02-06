package experiment;

/**
 * Use this interface for any class that generates combinations of 
 * values for a parameter across all items. For example, for demand rates,
 * it would create points of the form (d1,d2,...,dN) for each value combo.
 * @author ftubilla
 *
 */
public interface IParameterComboGenerator {

	/**
	 * Generates combinations of values for a parameter across all items.
	 * @param numItems
	 */
	public abstract void generate(int numItems);

	/**
	 * Iterates over all parameter combos generated.
	 * @return Iterable
	 */
	public abstract Iterable<ParameterCombo> getParameterCombos();

}
