package burdensharing;

import org.apache.log4j.Logger;
import sim.engine.SimState;
import sim.engine.Steppable;
import utils.UtilityCalculator;
import utils.burdenSharingCalculator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Observer implements Steppable {

    private static final Logger logger = Logger.getLogger(Observer.class);

    @Override
    public void step(SimState simState) {
        logger.info(String.format("Observer in step %s", simState.schedule.getSteps()));
        SimEnvironment state = (SimEnvironment) simState;
        if (state.OfferChange == 0) {
            state.stableIter += 1;
        } else {
            state.stableIter = 0;
        }
        logger.info(String.format("Offer change in current step: %s", state.OfferChange));
        logger.info(String.format("Current Metrics: stable iteration %s", state.stableIter));

        // writing the step status, skip the step 0
        for (Agent agent : state.allAgents.values()) {
            agent.updateUij(state);
            agent.updateBurdenSharing(state);
        }
        try {
            collectData(state);
            burdenSharingData(state);
            variableChecking(state);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // determine if we should terminate the simulation
        if (state.stableIter == 3 || state.schedule.getSteps() == state.MaxIteration) {
            state.endSimulation = true;
            try {
                generateNextYearInput(state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            state.scheduleAgents();
        }
        state.OfferChange = 0; // reset offerChange Count
    }

    private void collectData(SimEnvironment state) throws IOException {
        String[] header_ALL = {"step", "year", "state_i", "state_j", "cap_i", "cap_j", "cultureSim",
                "democ_i", "democ_j", "neighbor", "enemy", "ally_ij", "u_ij", "currentU", "commonAllianceSize"};

        FileWriter writer;
        File csvFile = new File(state.outputDataFile);
        if (!csvFile.exists()) {
            csvFile.createNewFile();
            writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            writer.write(String.join(",", header_ALL));
            writer.write("\n");
        } else {
            if (state.schedule.getSteps() == 0) {
                csvFile.delete();
                logger.info(String.format("Delete the original file %s", state.outputDataFile));

                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
                writer.write(String.join(",", header_ALL));
                writer.write("\n");
            } else {
                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            }

        }

        // start writing
        long stp = state.schedule.getSteps();
        for (int i : state.agentIdList) {
            for (int j : state.agentIdList) {
                // write the information to csv
                Agent agentI = state.getAgent(i);
                Agent agentJ = state.getAgent(j);
                int cultureSim = agentI.culture.get(j);
                int neighbor = agentI.neighbors.contains(j) ? 1 : 0;
                int enemy = 0;
                if (agentI.getEnemy().contains(j)) {
                    enemy = 1;
                } else if (agentI.getSecondaryEnemy(state).contains(j)) {
                    enemy = 2;
                }
                int ally = agentI.alliance.contains(j) ? 1 : 0;
                double uij = agentI.uij.get(j);
                double currentU = agentI.utility;
                Set<Integer> commonAlliance = new HashSet<>(agentI.alliance);
                commonAlliance.retainAll(agentJ.alliance);
                int commonAllieSize = commonAlliance.size();

                String info = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        stp, state.year, i, j, agentI.capability, agentJ.capability, cultureSim,
                        agentI.democracy, agentJ.democracy, neighbor, enemy, ally, uij, currentU, commonAllieSize);
                writer.write(info);
                writer.write("\n");
            }
        }
        writer.flush();
        writer.close();
    }

    private void generateNextYearInput(SimEnvironment state) throws IOException {
        // original `appendInputInfo` function
        // filename as the original filename/(year + 1).csv
        int appendYear = (int) (state.year + 1);
        File file = new File(state.inputDataFile);
        String parent = file.getAbsoluteFile().getParent();
        String baseData = Paths.get(parent, String.format("%s.csv", appendYear)).toString();
        logger.info(String.format("Base file for appending results: %s", baseData));

        File baseFile = new File(baseData);
        if (!baseFile.exists()) {
            return;
        }

        List<String> lines;
        List<String[]> data = null;

        Path path = Paths.get(baseData);
        String header = null;
        String[] headerArray = null;
        try {
            lines = Files.readAllLines(path);
            data = lines.stream().skip(1).map(line -> line.split(",")).collect(Collectors.toList());
            headerArray = Files.lines(path)
                    .map(s -> s.split(","))
                    .findFirst()
                    .get();
            if (headerArray.length == 11) { //add actual_ally column at 2022-07-06
                header = String.join(",", headerArray) + ",alliance" + ",allianceDuration\n";
            } else {
                header = String.join(",", headerArray) + "\n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String result;
        FileWriter writer = new FileWriter(state.nextInputData);
        writer.write(header);
        for (String[] lst : data) {
            Integer i = Integer.valueOf(lst[1]);
            Integer j = Integer.valueOf(lst[2]); //key
            int a = 0;
            int al = 0;
            if (state.allAgents.containsKey(i) && state.allAgents.get(i).alliance.contains(j)) {
                a = 1; //i & j are allies
                al = state.allAgents.get(i).allianceDuration.get(j) + 1; //alliance durarion +1
            }
            if (headerArray.length == 11) { //edit at 2022-07-06 (if the last column is actual alliance)
                result = String.join(",", lst) + "," + a + "," + al + "\n";
            } else { //if there are already 13 columns
                lst[11] = String.valueOf(a); //edit at 2022-07-06
                lst[12] = String.valueOf(al); //edit at 2022-07-06
                result = String.join(",", lst) + "\n";
            }
            writer.write(result);

        }
        writer.flush();
        writer.close();
    }

    //add the cost scores at 2022-09-29 because we change the cost term for each potential ally
    private void variableChecking(SimEnvironment state) throws IOException {
        String[] header_variableChecking = {"step", "year", "state_i", "state_j", "EE_ij", "D_j", "NE_ij", "T_ijm",
                "i's allies", "j's alles", "commonAllies", "A_ij",
                "enemyI[]", "EE_kj[]", "S_kj[]", "NE_kj[]", "T_kjm[]", "A_kj", "R_j", "cost_j"};

        FileWriter writer;
        File csvFile = new File(state.variableCheckingFile);
        if (!csvFile.exists()) {
            csvFile.createNewFile();
            writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            writer.write(String.join(",", header_variableChecking));
            writer.write("\n");
        } else {
            if (state.schedule.getSteps() == 0) {
                csvFile.delete();
                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
                writer.write(String.join(",", header_variableChecking));
                writer.write("\n");
            } else {
                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            }
        }

        // start writing
        long stp = state.schedule.getSteps();
        for (int i : state.agentIdList) {
            for (int j : state.agentIdList) {
                // write the information to csv
                Agent agentI = state.getAgent(i);
                Agent agentJ = state.getAgent(j);
                int EE = UtilityCalculator.isCommonEnemy(agentI, agentJ);
                int Dj = agentJ.democracy;
                int NE = UtilityCalculator.isEnemyNeighbor(agentI, agentJ);
                int T = UtilityCalculator.commonAllianceSize(agentI, agentJ);

                Set<Integer> commonAlliance = new HashSet<>(agentI.alliance);
                commonAlliance.retainAll(agentJ.alliance);

                double Aij = UtilityCalculator.attractiveness(agentI, agentJ);
                Set<Integer> allEnemies = agentI.getEnemy();
                List<String> EE_kj = new ArrayList<>(); //EE_kj
                List<String> S_kj = new ArrayList<>(); //S_kj
                List<String> NE_kj = new ArrayList<>(); //NE_kj
                List<String> T_kjm = new ArrayList<>(); //T_kjm
                for (int e : allEnemies) {
                    Agent k = state.allAgents.get(e);
                    EE_kj.add(String.valueOf(UtilityCalculator.isCommonEnemy(k, agentJ)));
                    S_kj.add(String.valueOf(k.culture.get(j)));
                    NE_kj.add(String.valueOf(UtilityCalculator.isEnemyNeighbor(k, agentJ)));
                    T_kjm.add(String.valueOf(UtilityCalculator.commonAllianceSize(k, agentJ)));
                }

                double Akj = UtilityCalculator.prevention(state, agentI, agentJ);
                double Rj = UtilityCalculator.trust(state, agentI, agentJ);
                double cost = UtilityCalculator.cost_j(agentI, agentJ); //add at 2022-09-29

                String info = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s \n",
                        stp, state.year, i, j, EE, Dj, NE, T,
                        agentI.getAlliance().stream().map(String::valueOf).collect(Collectors.joining("|")),
                        agentJ.getAlliance().stream().map(String::valueOf).collect(Collectors.joining("|")),
                        commonAlliance.stream().map(String::valueOf).collect(Collectors.joining("|")),
                        Aij, allEnemies.stream().map(String::valueOf).collect(Collectors.joining("|")),
                        String.join("|", EE_kj), String.join("|", S_kj),
                        String.join("|", NE_kj), String.join("|", T_kjm),
                        Akj, Rj, cost);
                writer.write(info);
            }
        }
        writer.flush();
        writer.close();
    }

    private void burdenSharingData(SimEnvironment state) throws IOException{
        String[] header_burdenSharing = {"step", "year", "state_i", "currentU", "need",
                "detection", "punishment", "emulation", "enemy_BS", "allianceDuration", "burdenSharing"};
        FileWriter writer;
        File csvFile = new File(state.burdenSharingFile);
        if(!csvFile.exists()){
            csvFile.createNewFile();
            writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            writer.write(String.join(",", header_burdenSharing));
            writer.write("\n");
        }else{
            if (state.schedule.getSteps() == 0) {
                csvFile.delete();
                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
                writer.write(String.join(",", header_burdenSharing));
                writer.write("\n");
            } else {
                writer = new FileWriter(csvFile.getAbsoluteFile(), true);
            }
        }

        // start writing
        long stp = state.schedule.getSteps();
        for (int i : state.agentIdList){
            // write the information to csv
            Agent agentI = state.getAgent(i);
            double currentU = agentI.utility;
            double burdenSharing = agentI.burdenSharing.get(stp);
            int need = burdenSharingCalculator.need(agentI);
            double detection = burdenSharingCalculator.detection(state, agentI);
            double punishment = burdenSharingCalculator.punishment(state, agentI);
            double emulation = burdenSharingCalculator.emulation(state, agentI);
            double enemy_BS = burdenSharingCalculator.BS_enemy(state, agentI);
            double allianceDuration = burdenSharingCalculator.allianceDuration(state, agentI);
            String info = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    stp, state.year, i, currentU, need, detection, punishment, emulation,
                    enemy_BS, allianceDuration, burdenSharing);
            writer.write(info);
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }

}
