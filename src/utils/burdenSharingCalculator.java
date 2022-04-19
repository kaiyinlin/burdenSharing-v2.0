package utils;

import burdensharing.Agent;
import burdensharing.SimEnvironment;

import java.util.Set;

public class burdenSharingCalculator {
    public static double burdenSharing(SimEnvironment state, Agent ai){
        double f = 1;
        double alpha = -1;
        double beta = 1;
        double rho = 1;
        double Bi = 0;
        Bi = f * (alpha * ai.utility +
                beta * (detection(state, ai) + punishment(state, ai)) +
                rho * emulation(state, ai));
        //cutoff
        return Bi;
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
        D = (1.0/(n-1)) * sum;
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
        double P = 0;
        for (int j : allianceI){
            Agent aj = state.allAgents.get(j);//aj is i's alliance
            double u_ji = aj.uij.get(ai.id); //get u_ij in this round
            sum += u_ji;
        }
        P = (1.0/(n+1)) * sum;
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
        int Bj = 0;
        double E = 0;
        for(int j : allianceI){
            Agent aj = state.allAgents.get(j);//aj is i's ally
            if(state.schedule.getSteps() == 0){
                Bj = 0;
            }else {
                Bj += aj.burdenSharing.get(state.schedule.getSteps() - 1); //calculate burden sharing level of aj and sum of them
            }
        }
        E = (1.0/(n+1)) * Bj;
        return E;
    }

}
