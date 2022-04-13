// methods relate to makeing offers (aka Agent.step)
package utils;

import burdensharing.Agent;
import burdensharing.SimEnvironment;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class OfferUtils {

    private static final Logger logger = Logger.getLogger(OfferUtils.class);

    public static Set<Integer> getPotentialAllies(SimEnvironment state, Agent a) {
        Set<Integer> alliance = a.getAlliance();

        Set<Integer> potentialAllies = new HashSet<>(state.agentIdList);
        potentialAllies.removeAll(alliance);
        potentialAllies.removeAll(a.getEnemy());
        potentialAllies.remove(a.id);
        return potentialAllies;
    }

    public static boolean acceptOffer(SimEnvironment state, Agent ai, Agent aj) {
        /*
         ai make an offer to aj
         */

        // checking if aj is able to accept the offer by checking the following three items
        // 1. check if aj need friends
        // 2. if i is in j's potentialAllies
        // 3. if u_ji > 0
        Set<Integer> potentialAllieJ = getPotentialAllies(state, aj);
        if (!potentialAllieJ.contains(ai.id)) {
            logger.debug(String.format("%s not in potentialAllies", ai.id));
        }
        boolean needAllies = aj.needMorePartner(state);
        if (!needAllies) {
            logger.debug(String.format("%s does not need alliance", aj.id));
        }
        double u_ji = UtilityCalculator.utilityIJ(state, aj, ai);
        if (u_ji <= 0) {
            logger.debug(String.format("%s does not accept offer b/c u_ji > 0", aj.id));
        }
        if (potentialAllieJ.contains(ai.id) && needAllies && u_ji > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void makeOffer(Agent ai, Agent aj) {
        ai.addAlliance(aj.id);
        aj.addAlliance(ai.id);
    }

    public static void dropOffer(Agent ai, Agent aj) {
        ai.dropAlliance(aj.id);
        aj.dropAlliance(ai.id);
    }

    public static boolean validateOffer(SimEnvironment state, Agent ai, Agent aj) {
        // check if both ai and aj will benefit from the offer
        boolean valid = true;
        makeOffer(ai, aj);

        double oldAiU = ai.utility;
        double oldAjU = aj.utility;
        state.updateUtility(ai);
        state.updateUtility(aj);
        state.OfferChange += 1;
        if (ai.utility < oldAiU || aj.utility < oldAjU) {
            dropOffer(ai, aj);
            logger.debug(String.format("Decide to drop the offer between %s and %s since the utility not improved utility (old, new): (%s, %s), (%s, %s)",
                    ai.id, aj.id, oldAiU, ai.utility, oldAjU, aj.utility));

            //rollback the utilities
            state.updateUtility(ai);
            state.updateUtility(aj);
            state.OfferChange -= 1;
            valid = false;
        }
        return valid;
    }

}
