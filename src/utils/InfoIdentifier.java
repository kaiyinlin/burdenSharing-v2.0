package utils;

import java.util.Set;
import java.util.Map;

public class InfoIdentifier {
    private final Integer id;
    private final double capability;
    private final Set<Integer> enemy;
    private final Set<Integer> secondaryEnemy;
    private final Integer democracy;
    private final Set<Integer> neighbor;
    private final Map<Integer, Integer> culture;
    private final Set<Integer> alliance;
    private final Map<Integer, Integer> allianceDuration;
    private final Set<Integer> actualAlliance;

    public InfoIdentifier(Integer id, double capability, Integer democracy, Set<Integer> enemy, Set<Integer> secondaryEnemy,
                          Set<Integer> neighbor, Map<Integer, Integer> culture, Set<Integer> alliance,
                          Map<Integer, Integer> allianceDuration, Set<Integer> actualAlliance) {
        this.id = id;
        this.capability = capability;
        this.enemy = enemy;
        this.secondaryEnemy = secondaryEnemy;
        this.democracy = democracy;
        this.neighbor = neighbor;
        this.culture = culture;
        this.alliance = alliance;
        this.allianceDuration = allianceDuration;
        this.actualAlliance = actualAlliance;
    }

    public Integer getId() {
        return id;
    }

    public double getCapability() {
        return capability;
    }

    public Integer getDemocracy() {
        return democracy;
    }

    public Set<Integer> getEnemy() {
        return enemy;
    }

    public Set<Integer> getSecondaryEnemy(){
        return secondaryEnemy;
    }

    public Set<Integer> getNeighbor() {
        return neighbor;
    }

    public Set<Integer> getAlliance() {
        return alliance;
    }

    public Set<Integer> getActualAlliance() { return actualAlliance; }

    public Map<Integer, Integer> getCulture() {
        return culture;
    }

    public Map<Integer, Integer> getAllianceDuration(){
        return allianceDuration;
    }

    public void updateEnemy(Integer agentId) {
        this.enemy.add(agentId);
    }

    public void updateSecondaryEnemy(Integer agentId) {
        this.secondaryEnemy.add(agentId);
    }

    public void updateNeighbor(Integer agentId) {
        this.neighbor.add(agentId);
    }

    public void updateAlliance(Integer agentId) { this.alliance.add(agentId); }

    public void updateActualAlliance(Integer agentId) { this.actualAlliance.add(agentId);}

    public void updateCulture(Integer agentId, Integer sameCulture) { //agentID indicates j's id and see if i and j has same culture
        if (this.culture.containsKey(agentId)) {
            this.culture.replace(agentId, sameCulture);
        } else {
            this.culture.put(agentId, sameCulture);
        }
    }

    public void updateAllianceDuration(Integer agentId, Integer duration){ //agentID indicates j's id and put their alliance duration
        this.allianceDuration.put(agentId, duration);
    }

}
