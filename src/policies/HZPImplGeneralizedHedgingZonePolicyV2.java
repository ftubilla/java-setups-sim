package policies;

import sim.Sim;
import system.Item;
import system.Machine;

public class HZPImplGeneralizedHedgingZonePolicyV2 extends GeneralizedHedgingZonePolicyV2 {

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        Item currentSetup = machine.getSetup();
        return currentSetup.getFluidTimeToSurplusLevel(getTarget(currentSetup));
    }

    @Override
    protected boolean isTimeToChangeOver() {
        return this.currentSetup.getSurplus() >= getTarget(this.currentSetup) - Sim.SURPLUS_TOLERANCE;
    }

    @Override
    protected double getTarget(Item item) {
        return item.getSurplusTarget() + this.serviceLevelController.getControl(item);
    }

    @Override
    public boolean isTargetBased() {
        return true;
    }

}
