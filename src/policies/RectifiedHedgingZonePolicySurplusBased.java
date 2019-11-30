package policies;

import com.google.common.annotations.VisibleForTesting;

import system.Item;

/**
 * A surplus-controlled version of the Dynamic Hedging Zone Policy. Note that, in the paper, we refer to this policy
 * as RHZPx.
 * 
 * @author ftubilla
 *
 */
public class RectifiedHedgingZonePolicySurplusBased extends RectifiedHedgingZonePolicyTimeBased {

    @VisibleForTesting
    protected boolean isSurplusControlled(final Item item) {
        return true;
    }

}
