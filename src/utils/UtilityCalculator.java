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
        double cost = 0;
        Set<Integer> enemy = agent.getEnemy();
        Set<Integer> secondaryEnemy = agent.getSecondaryEnemy(state);
        for (int id : enemy) {
            SRGCapability += state.getAgent(id).capability;
        }

        for (int id : secondaryEnemy) {
            SRGCapability += (0.5 * state.getAgent(id).capability);
        }

        double allianceSize = agent.getAlliance().size();
        if (allianceSize == 0) {
            marginalUtility = agent.capability - SRGCapability;
        } else {
//            if (state.year < 1945) {
//                cost = Math.pow(allianceSize, state.costPowerBefore);
//            } else {
//                cost = Math.pow(allianceSize, state.costPowerAfter);
//            }

            double sum_uij = 0;
            for (int j : agent.getAlliance()) {
                double u_ij = utilityIJ(state, agent, state.getAgent(j));
                sum_uij += u_ij;
            }
            //logger.debug(String.format("sum_uij=%s and cost=%s", sum_uij,state.costPenalty*cost));
            marginalUtility = agent.capability + sum_uij /*- state.costPenalty * cost */ - SRGCapability;

        }
        return marginalUtility;
    }

    public static double utilityIJ(SimEnvironment state, Agent ai, Agent aj) {
        if (ai == aj) {
            return 0;
        }
        if(ai.getEnemy().contains(aj.id)){
            double u_ij = 0.5 * aj.capability* (state.uij_alpha * attractiveness(ai, aj) +
                    state.uij_beta * prevention(state, ai, aj) +
                    state.uij_gamma * trust(state, ai, aj) - state.uij_delta * cost_j(ai, aj));
            return u_ij;
        }

        // update both SRG to avoid too much for loops
        ai.updateSRG(state);
        aj.updateSRG(state);

        // calculate the u_ij
        double u_ij = aj.capability* (state.uij_alpha * attractiveness(ai, aj) +
                state.uij_beta * prevention(state, ai, aj) +
                state.uij_gamma * trust(state, ai, aj) - state.uij_delta * cost_j(ai, aj));
//        double u_ij = state.uij_alpha * attractiveness(ai, aj) +
//                state.uij_beta * prevention(state, ai, aj) +
//                state.uij_gamma * trust(state, ai, aj);
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
            A_ij = 0.426 * EE + 0.043 * Dj + 0.13 * S + 0.003 * NE + 0.565 * T;
        } else {
            A_ij = 0.42 * EE + 0.043 * Dj + 0.062 * S + 0.003 * NE + 0.595 * T;
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
        Set<Integer> allianceI = ai.getAlliance();
        Set<Integer> allianceJ = aj.getAlliance();

        Set<Integer> commonAlliance = SetUtils.intersection(allianceI, allianceJ);
        if(commonAlliance.size() < 2){
            return 0;
        } else {
            return 1;
        }
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
        Set<Integer> allianceJ = aj.getAlliance();
        if (allianceJ.size() == 0) {
            return 0;
        } else {
            double sumU_ik = 0;
            for (int l : allianceJ) {
//                Agent ak = state.allAgents.get(l); //ak is j's alliance
//                double a_kj = attractiveness(ak, aj); //attractiveness between ak and aj
                double u_il = ai.uij.getOrDefault(l, 0.0); //basically see how useful the potential ally (j's ally) is
                sumU_ik += u_il;
            }
            R_j = sumU_ik / allianceJ.size();
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
        if(c_j == 0){
            return 0;
        } else if(c_j > 0 && c_j <= 10) {
            return 1;
        } else{
            return 2;
        }
    }
}
