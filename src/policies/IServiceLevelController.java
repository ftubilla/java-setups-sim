package policies;

import system.Item;

/**
 * Interface for the service level controller, which adjusts an item's target based on
 * service level tracking performance.
  */
public interface IServiceLevelController {

    /**
     * Returns the current control to apply to the item's target (as an additive term)
     * 
     * @param item
     * @return control additive term
     */
    double getControl(Item item);

    /**
     * Returns the current estimate of the service level for the item
     * 
     * @param item
     * @return service level estimate
     */
    Double getLearnedServiceLevel(Item item);

    /**
     * Returns the current rate at which service level is learned.
     * 
     * @param item
     * @return learning rate
     */
    Double getLearningRate(Item item);

    /**
     * A call to update the controller with the latest surplus data.
     *
     */
    void updateSurplus();

    /**
     * Notify the controller that a new changeover into the given item has occurred.
     * @param item
     */
    void noteNewSetup(Item item);

}