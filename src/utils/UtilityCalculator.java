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
            int n = state.allAgents.size();
            double sum_uij_minus_cij = 0;
            for (int j : agent.getAlliance()) {
                double u_ij = utilityIJ(state, agent, state.getAgent(j));
                double c_ij = (u_ij*state.getAgent(j).getAlliance().size())/(n-1);
                sum_uij_minus_cij += u_ij;
                logger.debug(String.format("i=:%s, j=%s, uij=%s, cij=%s", agent.id, j, u_ij, c_ij));
            }

            marginalUtility = agent.capability + sum_uij_minus_cij - SRGCapability;

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
        double u_ij = aj.capability * (0.6 * attractiveness(ai, aj) +
                0.1 * prevention(state, ai, aj) +
                0.3 * trust(ai, aj));
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
            A_ij = 0.2 * EE + 0.3 * Dj + 0.1 * S + 0.2 * NE + 0.2 * T;
        } else {
            A_ij = 0.3 * EE + 0.1 * Dj + 0.1 * S + 0.2 * NE + 0.2 * T;

        }
        return A_ij;
    }

    public static int isCommonEnemy(Agent ai, Agent aj) {
        Set<Integer> enemyI = ai.SRG;
        Set<Integer> enemyJ = aj.SRG;

        Set<Integer> intersection = SetUtils.intersection(enemyI, enemyJ);
        return intersection.size() > 0 ? 1 : 0;

    }

    public static int isEnemyNeighbor(Agent ai, Agent aj) {
        Set<Integer> enemyI = ai.SRG;
        Set<Integer> enemyNeighbor = SetUtils.intersection(enemyI, aj.getNeighbors());
        return enemyNeighbor.size() > 0 ? 1 : 0;
    }

    public static int commonAllianceSize(Agent ai, Agent aj) {
        Set<Integer> allianceI = ai.getAlliance();
        Set<Integer> allianceJ = aj.getAlliance();

        Set<Integer> commonAlliance = SetUtils.intersection(allianceI, allianceJ);
        return commonAlliance.size();
    }

    public static double prevention(SimEnvironment state, Agent ai, Agent aj) {
        Set<Integer> primaryEnemyI = ai.getEnemy();

        double A_kj = 0;
        for (int e : primaryEnemyI) {
            Agent ae = state.allAgents.get(e);
            ae.updateSRG(state);

            double a_ej = attractiveness(ae, aj); // attractiveness between ae and aj, ae = ai's primaryEnemy
            A_kj += a_ej;
        }
        return A_kj;
    }

    public static double trust(Agent ai, Agent aj) {
        double R_j = 0;
        Set<Integer> allianceJ = aj.getAlliance();
        if (allianceJ.size() == 0) {
            return 0;
        } else {
            for (int l : allianceJ) {
                double u_il = ai.uij.getOrDefault(l, 0.0);
                R_j += u_il;
            }
        }
        return R_j;
    }
}
