package policies;

import system.Item;

/**
 * A silent service level controller.
 *
 */
public class InactiveServiceLevelController implements IServiceLevelController {

    @Override
    public double getControl(Item item) {
        return 0;
    }

    @Override
    public Double getLearnedServiceLevel(Item item) {
        return null;
    }

    @Override
    public Double getLearningRate(Item item) {
        return null;
    }

    @Override
    public void updateSurplus() {
    }

    @Override
    public void noteNewSetup(Item item) {
    }

}
