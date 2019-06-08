package policies;

import sim.Sim;
import system.Item;
import system.Machine;

/**
 * A simple implementation of the (non-cruising) HZP using the structure from {@link GeneralizedHedgingZonePolicy}.
 * 
 * @author ftubilla
 *
 */
public class HZPImplGeneralizedHedgingZonePolicy extends GeneralizedHedgingZonePolicy {

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        return machine.getSetup().getSurplus() >= machine.getSetup().getSurplusTarget() - Sim.SURPLUS_TOLERANCE;
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        return item.getSurplusTarget() - item.getSurplus() <= deltaZ;
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        Item currentSetup = machine.getSetup();
        return currentSetup.getFluidTimeToSurplusLevel(currentSetup.getSurplusTarget());
    }

    @Override
    protected double getSurplusDeviation(Machine machine, Item item) {
        return item.getSurplusTarget() - item.getSurplus();
    }

    @Override
    public boolean isTargetBased() {
        return true;
    }

}
