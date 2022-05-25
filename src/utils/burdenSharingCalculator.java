package utils;

import burdensharing.Agent;
import burdensharing.SimEnvironment;
import org.apache.log4j.Logger;

import java.util.Set;

public class burdenSharingCalculator {
    private static final Logger logger = Logger.getLogger(burdenSharingCalculator.class);

    public static double burdenSharing(SimEnvironment state, Agent ai){
        double f = 1;
        double alpha = 0.23; //alpha > 0
        double beta = -0.35; //beta < 0
        double gamma = 12; //gamma > 0
        double delta = 1; //delta > 0
        double eta = -1; //eta < 0
        double BS_i = 0; //burden sharing value
        BS_i =  alpha * need(ai) + beta * (detection(state, ai) + punishment(state, ai)) +
                gamma * emulation(state, ai) + delta * BS_enemy(state, ai) +
                eta * allianceDuration(state, ai);
        //cutoff
        return BS_i;
    }

    /**
     * Need should be binarized such as N_i = 1 if i's utility <0, N_i = 0 otherwise
     * @param ai
     * @return
     */
    public static int need(Agent ai){
        if(ai.utility < 0){
            return 1;
        }else{
            return 0;
        }
    }
    /**
     * D = detection score, measures as the average degree of i' allies
     * @param state
     * @param ai
     * @return
     */
    public static double detection(SimEnvironment state, Agent ai) {
        Set<Integer> allianceI = ai.getAlliance();
        int n = state.allAgents.size();
        double sum = 0;
        double D = 0;
        for (int j : allianceI) {
            Agent aj = state.allAgents.get(j); //aj is i's alliance
            sum += aj.getAlliance().size(); //get the size of allies of aj (in this round? or current?)
        }
        D = sum / (n-1);
        return D;
    }

    /**
     * P = the probability and cost of punishment measured as the average utility i's allies assign to i
     * @param state
     * @param ai
     * @return
     */
    public static double punishment(SimEnvironment state, Agent ai){
        Set<Integer> allianceI = ai.getAlliance(); //get i's allies
        int n = state.allAgents.size();
        double sum = 0;
        double P = 0; //punishment value
        for (int j : allianceI){ //get all i's allies
            Agent aj = state.allAgents.get(j);//aj is i's ally
            double u_ji = aj.uij.get(ai.id); //get u_ji in this round
            sum += u_ji;
        }
        P = sum / (n+1);
        return P;
    }

    /**
     * E = elumation factor, measured as the average burden-sharing level of i's allies
     * @param state
     * @param ai
     * @return
     */
    public static double emulation(SimEnvironment state, Agent ai){
        Set<Integer> allianceI = ai.getAlliance();//get i's allies
        int n = state.allAgents.size();
        double DBj = 0;
        double E = 0;
        for(int j : allianceI){
            Agent aj = state.allAgents.get(j);//aj is i's ally
            if(state.schedule.getSteps() < 2){
                DBj = 0; //there is no delta Bj at the first step
            }else {
                double Bj = aj.burdenSharing.get(state.schedule.getSteps()-1) - aj.burdenSharing.get(state.schedule.getSteps()-2);
                logger.debug(String.format("Bj's BS %s was calculated by last year BS %s and last two year BS %s", Bj,aj.burdenSharing.get(state.schedule.getSteps()-1), aj.burdenSharing.get(state.schedule.getSteps()-2)));
                DBj += Bj; //calculate burden sharing level of aj and sum of them
            }
        }
        E = DBj / (n+1);
        return E;
    }

    public static double BS_enemy(SimEnvironment state, Agent ai){
        Set<Integer> enemyI = ai.getEnemy();
        int n = state.allAgents.size();
        double DBk = 0;
        double Be = 0; //burden sharing of enemies
        for(int k : enemyI){
            Agent ak = state.allAgents.get(k);
            if(state.schedule.getSteps() < 2){
                DBk = 0;
            }else{
                double Bk = ak.burdenSharing.get(state.schedule.getSteps()-1) - ak.burdenSharing.get(state.schedule.getSteps()-2);
                DBk += Bk;
            }
        }
        Be = DBk / (n-1);
        return Be;
    }

    public static double allianceDuration(SimEnvironment state, Agent ai){
        Set<Integer> allianceI = ai.getAlliance();
        int n = state.allAgents.size();
        double sumAd = 0; //sum of alliance duration
        double AD; //average alliance duration
        for(int j: allianceI){
            int ad = ai.allianceDuration.get(j);
            sumAd += ad;
        }
        AD = sumAd / (n+1);
        return AD;
    }

}
