package burdensharing;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.File;
import java.nio.file.Files;

import utils.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SimEnvironment extends SimState {

    private static final Logger logger = Logger.getLogger(SimEnvironment.class);

    String inputDataFile;
    public String nextInputData;
    public String outputDataFile;
    public String variableCheckingFile;
    public long year;

    public List<Integer> agentIdList;
    public Map<Integer, Agent> allAgents = new HashMap<Integer, Agent>();
    public Map<Integer, Double> allUtilities = new HashMap<>();
    public Map<Integer, Double> allCapability = new HashMap<>();

    // parameters
    public double offerUpperBound = 1;
//    public double offerLowerBound = -1;
    public int MaxIteration = 20;
    public double costPenalty = 0.2;
    public double costPowerBefore = 2.0;
    public double costPowerAfter = 0.5;
    public double uij_alpha = 0.6;
    public double uij_beta = 0.1;
    public double uij_gamma = 0.3;


    // set initial value
    public boolean endSimulation = false;
    public int OfferChange = 0;
    public int stableIter = 0;

    public SimEnvironment(long seed, String inputDataFile, long year, String nextInputData) {
        super(seed);
        this.inputDataFile = inputDataFile;
        this.year = year;
        this.nextInputData = nextInputData;

        // get outputDataDirectory
        File file = new File(this.inputDataFile);
        String parent = file.getAbsoluteFile().getParent();
        outputDataFile = Paths.get(parent, String.format("%s_output.csv", year)).toString();
        variableCheckingFile = Paths.get(parent, String.format("%s_variable_checking.csv", year)).toString();

        logger.info(String.format("HyperParameter: offerUpperBound=%s, maxIter=%s, costPenalty=%s , costPowerBefore=%s, costPowerAfter=%s",
                offerUpperBound, MaxIteration, costPenalty, costPowerBefore, costPowerAfter));
    }

    public static void main(String[] args) {
        // take in json file (args.json) as arguments
        String inputJsonFile = args[0];
        logger.info(String.format("Input Json File %s", inputJsonFile));

        // readin parameters
        JSONObject parameters = InputJsonParser.parseInput(inputJsonFile);

        // start the simulation
        String inputData = (String) parameters.get("inputData");
        long year = (long) parameters.get("year");
        String nextInputData = (String) parameters.get("nextInputData");

        SimEnvironment state = new SimEnvironment(200, inputData, year, nextInputData);
        state.start();
        do
            if (!state.schedule.step(state)) break;
        while (state.schedule.getSteps() < (state.MaxIteration + 1) && !state.endSimulation);

        state.finish();
        System.exit(0);
    }

    public void start() {
        super.start();

        // readin files
        Map<Integer, InfoIdentifier> dataInformation = getDataInformation();
        agentIdList = new ArrayList<>(dataInformation.keySet());
        logger.info(String.format("Number of agents(country) in the simulation: %s", agentIdList.size()));
        logger.debug("Agents = " + Arrays.toString(new List[]{agentIdList}));

        // set agents
        makeAgents(dataInformation);
        for (int agentId : agentIdList) {
            allCapability.put(agentId, getAgent(agentId).capability);
        }

        updateUtilities();
        scheduleAgents();

        // update the initial uij
        for (Agent agent : allAgents.values()) {
            agent.updateUij(this);
            logger.debug(String.format("Update uij map for %s", agent.id));
        }

        Observer obs = new Observer();
        schedule.scheduleRepeating(obs);

    }


    private Map<Integer, InfoIdentifier> getDataInformation() {
        InputDataParser inputData = new InputDataParser(this.inputDataFile);
        return inputData.getDataInformation();
    }

    public void makeAgents(Map<Integer, InfoIdentifier> dataInformation) {
        for (Integer agentId : agentIdList) {
            InfoIdentifier agentInfo = dataInformation.get(agentId);
            Agent a = new Agent(agentInfo.getId(), agentInfo.getCapability(), agentInfo.getDemocracy(),
                    agentInfo.getEnemy(), agentInfo.getAlliance(), agentInfo.getNeighbor(),
                    agentInfo.getCulture());
            this.allAgents.put(a.id, a);
            logger.debug(String.format("Make Agent id = %s", a.id));
        }
    }

    public Agent getAgent(int agentId) {
        return allAgents.get(agentId);
    }

    public void updateAgent(Agent agent) {
        this.allAgents.put(agent.id, agent);
    }

    public void updateUtilities() {
        for (Agent a : allAgents.values()) {
            updateUtility(a);
        }
    }

    public void updateUtility(Agent a) {
        double u = UtilityCalculator.getCurrentUtility(this, a);
        allUtilities.put(a.id, u);
        a.setUtility(u);
    }

    public List<Integer> getScheduleOrder() {
//        Map<Integer, Double> sorted =
//                allUtilities.entrySet().stream().sorted(Map.Entry.comparingByValue())
//                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Map<Integer, Double> sorted =
                allCapability.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return new ArrayList<>(sorted.keySet());
    }

    public void scheduleAgents() {
        List<Integer> scheduleList = getScheduleOrder();
        int o = 1;
        for (int s : scheduleList) {
            Agent a = this.getAgent(s);
            // only Schedule those which needs more alliance
            schedule.scheduleOnce(a, o);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("schedule %s with utility %s", a.id, a.capability));
            }
        }
    }
}
