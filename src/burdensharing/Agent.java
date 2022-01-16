package burdensharing;

import sim.engine.SimState;
import sim.engine.Steppable;
import org.apache.log4j.Logger;
import utils.OfferUtils;
import utils.UtilityCalculator;

import java.util.*;
import java.util.stream.Collectors;

public class Agent implements Steppable {

    private final Logger logger = Logger.getLogger(Agent.class);

    // variables
    public int id;
    public int democracy;
    public double capability;
    public Map<Integer, Integer> culture;
    public double utility;
    public Map<Integer, Double> uij = new HashMap<>();


    // relationships
    Set<Integer> enemy; // original enemy
    Set<Integer> alliance;
    Set<Integer> neighbors;

    public Set<Integer> SRG;

    public Agent(int id, double capability, int democracy,
                 Set<Integer> enemy, Set<Integer> alliance, Set<Integer> neighbors,
                 Map<Integer, Integer> culture) {
        this.id = id;
        this.capability = capability;
        this.democracy = democracy;
        this.enemy = enemy;
        this.alliance = alliance;
        this.neighbors = neighbors;
        this.culture = culture;
    }

    @Override
    public void step(SimState simState) {
        logger.debug(String.format("Agent %s in step %s", this.id, simState.schedule.getSteps()));
        logger.debug("Current alliance: " + alliance);

        SimEnvironment state = (SimEnvironment) simState;
        updateUij(state); // update the agent's uij for this round

        // check if we want to switch the partners
        Map<Integer, Double> potentialAllies_uij = new HashMap<>();
        Set<Integer> potentialAllies = OfferUtils.getPotentialAllies(state, this);
        for (int p : potentialAllies) {
            potentialAllies_uij.put(p, uij.get(p));
        }

        Map<Integer, Double> currentAllies_uij = new HashMap<>();
        for (int allie : alliance) {
            currentAllies_uij.put(allie, uij.get(allie));
        }

        if (needMorePartner(state)) {
            Map<Integer, Double> sorted =
                    potentialAllies_uij.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            List<Integer> potentialOrderedList = new ArrayList<>(sorted.keySet()); // sort the potential allies by utilities
            logger.debug(String.format("Agent %s potentialAllies", this.id) + potentialOrderedList);
            while (needMorePartner(state) && potentialOrderedList.size() > 0) {
                // make the offer
                logger.debug(String.format("Agent %s making offer to %s, u_ij=%s",
                        this.id, potentialOrderedList.get(0), potentialAllies_uij.get(potentialOrderedList.get(0))));
                Agent targetAgent = state.getAgent(potentialOrderedList.get(0));
                boolean accept = OfferUtils.acceptOffer(state, this, targetAgent);
                logger.debug("Accept Offer: " + accept);
                if (accept) {
                    boolean validOffer = OfferUtils.validateOffer(state, this, targetAgent);
                    if (validOffer && logger.isDebugEnabled()) {
                        logger.debug("Offer change: f" + state.OfferChange);
                    }
                }

                potentialOrderedList.remove(0);
            }

        } else if (currentAllies_uij.keySet().size() > 0 && potentialAllies.size() > 0) {
            // check if the agent wants to switch the partner (each step only switch one)
            int potentialAllieMax = Collections.max(potentialAllies_uij.entrySet(), Map.Entry.comparingByValue()).getKey();
            int currentAllieMin = Collections.min(currentAllies_uij.entrySet(), Map.Entry.comparingByValue()).getKey();
            if (currentAllies_uij.get(currentAllieMin) < potentialAllies_uij.get(potentialAllieMax)) {
                // try to make offer to the potentialAllie with Max uij
                logger.debug(String.format("Agent %s making offer to %s (switch Ally)",
                        this.id, potentialAllieMax));
                boolean accept = OfferUtils.acceptOffer(state, this, state.getAgent(potentialAllieMax));
                if (accept) {
                    boolean validOffer = OfferUtils.validateOffer(state, this, state.getAgent(potentialAllieMax));
                    logger.debug("Validate Offer" + validOffer);
                    if (validOffer) {
                        OfferUtils.dropOffer(this, state.getAgent(currentAllieMin));
                        logger.debug("Offer change: " + state.OfferChange);
                    }
                }
            }
        }
        state.updateUtilities(); // update utilities for all agents
        logger.debug(String.format("%s has alliance %s at the end of step with utility %s",
                this.id, alliance, utility));
    }

    public boolean needMorePartner(SimEnvironment state) {
        return utility < state.offerUpperBound;
    }

    public Set<Integer> getEnemy() {
        return this.enemy;
    }

    public void updateSRG(SimEnvironment state) {
        Set<Integer> SRG = getSRG(state);
        this.SRG = SRG;
    }

    public Set<Integer> getSRG(SimEnvironment state) {
        Set<Integer> allEnemies = new HashSet<>();

        // add primary enemy
        allEnemies.addAll(this.enemy);
        // adding secondary enemy
        Set<Integer> secondaryEnemy = getSecondaryEnemy(state);
        allEnemies.addAll(secondaryEnemy);
        return allEnemies;
    }

    public Set<Integer> getSecondaryEnemy(SimEnvironment state) {
        Set<Integer> secondaryEnemy = new HashSet<>();
        for (int id : this.enemy) {
            Set<Integer> enemiesAlliance = state.allAgents.get(id).alliance;
            secondaryEnemy.addAll(enemiesAlliance);
        }
        return secondaryEnemy;
    }

    public Set<Integer> getAlliance() {
        return this.alliance;
    }

    public Set<Integer> getNeighbors() {
        return this.neighbors;
    }

    public void setUtility(double utility) {
        this.utility = utility;
    }

    public void addAlliance(int id) {
        this.alliance.add(id);
    }

    public void dropAlliance(int id) {
        this.alliance.remove(id);
    }

    public void updateUij(SimEnvironment state) {
        for (int agentId : state.agentIdList) {
            double u = UtilityCalculator.utilityIJ(state, this, state.getAgent(agentId));
            uij.put(agentId, u);
        }
    }


}
