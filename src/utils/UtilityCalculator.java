package utils;

import burdensharing.Agent;
import burdensharing.SimEnvironment;
import org.apache.log4j.Logger;

import java.util.Set;

public class UtilityCalculator {

    private static final Logger logger = Logger.getLogger(SimEnvironment.class);

    public static double getCurrentUtility(SimEnvironment state, Agent agent) {

        double SRGCapability = 0;
        double marginalUtility;
//        double cost = 0;
        Set<Integer> enemy = agent.getEnemy();
        Set<Integer> secondaryEnemy = agent.getSecondaryEnemy(state);
        for (int id : enemy) {
            SRGCapability += state.getAgent(id).capability;
        }

        for (int id : secondaryEnemy) {
            SRGCapability += (0.5 * state.getAgent(id).capability);
        }

        double actualAllianceSize = agent.getActualAlliance().size();
        if (actualAllianceSize == 0) {
            marginalUtility = agent.capability - SRGCapability;
        }  else {
//            if (state.year < 1945) {
//                cost = Math.pow(actualAllianceSize, state.costPowerBefore);
//            } else {
//                cost = Math.pow(actualAllianceSize, state.costPowerAfter);
//            }
            double sum_uij = 0;
            for (int j : agent.getActualAlliance()) {
                double u_ij = utilityIJ(state, agent, state.getAgent(j));
                sum_uij += u_ij;
            }
            //logger.debug(String.format("sum_uij=%s and cost=%s", sum_uij,state.costPenalty*cost));
            marginalUtility = agent.capability + sum_uij /*- state.costPenalty * cost */ - SRGCapability;

        }
        return marginalUtility;
    }

    public static double utilityIJ(SimEnvironment state, Agent ai, Agent aj) {
        if (ai == aj || ai.getEnemy().contains(aj.id)) {
            return 0;
        }

        // update both SRG to avoid too much for loops
        ai.updateSRG(state);
        aj.updateSRG(state);

        // calculate the u_ij
        double u_ij = aj.capability* (state.uij_alpha * attractiveness(ai, aj) +
                state.uij_beta * prevention(state, ai, aj) +
                state.uij_gamma * trust(state, ai, aj) - state.uij_delta * cost_j(ai, aj));
        return u_ij;
    }

    public static double attractiveness(Agent ai, Agent aj) {
        int EE = isCommonEnemy(ai, aj);
        int Dj = aj.democracy;
        int S = ai.culture.get(aj.id);
        int NE = isEnemyNeighbor(ai, aj);
        int T = commonAllianceSize(ai, aj);

        double A_ij;
        if (ai.democracy == 1) {
            A_ij = 0.9593453 * EE + 0.5485924 * Dj + 1.477012 * S + 1.092695 * NE + 0.5274558 * T;
        } else {
            A_ij = 0.8485381 * EE + 0.0211666 * Dj + 1.412664 * S + 0.6035882 * NE + 0.5274558 * T;

        }
        return A_ij;
    }

    public static int isCommonEnemy(Agent ai, Agent aj) {
        Set<Integer> enemyI = ai.getEnemy();
        Set<Integer> enemyJ = aj.getEnemy();

        Set<Integer> intersection = SetUtils.intersection(enemyI, enemyJ);
        return intersection.size() > 0 ? 1 : 0;

    }

    public static int isEnemyNeighbor(Agent ai, Agent aj) {
        Set<Integer> enemyI = ai.getEnemy();
        Set<Integer> enemyNeighbor = SetUtils.intersection(enemyI, aj.getNeighbors());
        return enemyNeighbor.size() > 0 ? 1 : 0;
    }

    public static int commonAllianceSize(Agent ai, Agent aj) {
        Set<Integer> actualAllianceI = ai.getActualAlliance();
        Set<Integer> actualAllianceJ = aj.getActualAlliance();

        Set<Integer> commonAlliance = SetUtils.intersection(actualAllianceI, actualAllianceJ);
        return commonAlliance.size();
    }

    public static double prevention(SimEnvironment state, Agent ai, Agent aj) {
        Set<Integer> primaryEnemyI = ai.getEnemy();

        double A_kj_sum = 0;
        double A_kj = 0;
        for (int e : primaryEnemyI) {
            Agent ae = state.allAgents.get(e);
            ae.updateSRG(state);
            double a_ej = attractiveness(ae, aj); // attractiveness between ae and aj, ae = ai's primaryEnemy
            A_kj_sum += a_ej;
        }
        if(primaryEnemyI.size() > 0){
            A_kj = A_kj_sum/ primaryEnemyI.size();
        }else{
            A_kj = 0;
        }

        return A_kj;
    }

    public static double trust(SimEnvironment state, Agent ai, Agent aj) {
        double R_j = 0;
        Set<Integer> allianceJ = aj.getActualAlliance();
        if (allianceJ.size() == 0) {
            return 0;
        } else {
            for (int k : allianceJ) {
                Agent ak = state.allAgents.get(k); //ak is j's alliance
                double a_kj = attractiveness(ak,aj); //attractiveness between ak and aj
                //double u_il = ai.uij.getOrDefault(l, 0.0);
                R_j += a_kj;
            }
        }
        return R_j;
    }

    public static int cost_j(Agent ai, Agent aj){
        int c_j = 0; //The cost term is defined for each prospective ally j
        //get the number of common enemies (common interests between i and j)
        Set<Integer> enemyI = ai.getEnemy();
        Set<Integer> enemyJ = aj.getEnemy();
        Set<Integer> intersection = SetUtils.intersection(enemyI, enemyJ);
        int eeIJ = intersection.size();
        c_j = enemyJ.size() -eeIJ;
        return c_j;
    }
}
