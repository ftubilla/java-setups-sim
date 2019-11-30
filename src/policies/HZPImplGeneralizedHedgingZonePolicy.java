package policies;

import sim.Sim;
import system.Item;
import system.Machine;

/**
 * A simple implementation of the (non-cruising) HZP using the structure from {@link GeneralizedHedgingZonePolicy}.
 * 
 * Deprecated. Use {@link GeneralizedHedgingZonePolicyV2} instead.
 * 
 * @author ftubilla
 *
 */
@Deprecated
public class HZPImplGeneralizedHedgingZonePolicy extends GeneralizedHedgingZonePolicy {

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        return machine.getSetup().getSurplus() >= getTarget(machine.getSetup()) - Sim.SURPLUS_TOLERANCE;
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        return getTarget(item) - item.getSurplus() <= deltaZ;
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        Item currentSetup = machine.getSetup();
        return currentSetup.getFluidTimeToSurplusLevel(getTarget(currentSetup));
    }

    @Override
    protected double getSurplusDeviation(Machine machine, Item item) {
        return getTarget(item) - item.getSurplus();
    }

    @Override
    public boolean isTargetBased() {
        return true;
    }

    protected double getTarget(final Item item) {
        return item.getSurplusTarget() + this.serviceLevelController.getControl(item);
    }

}
