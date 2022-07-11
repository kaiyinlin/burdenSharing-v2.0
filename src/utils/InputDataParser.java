package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.List;

public class InputDataParser {
    String fileDirectory;
    Path path;
    List<String[]> data;
    Map<Integer, InfoIdentifier>  info = new HashMap<Integer, InfoIdentifier>();

    public InputDataParser(String fileDirectory) {
        // TODO Auto-generated constructor stub
        this.fileDirectory = fileDirectory;
    }

    public Map<Integer, InfoIdentifier> getDataInformation() {
        // read in files
        List<String> lines;
        path = Paths.get(fileDirectory);
        try {
            lines = Files.readAllLines(path);
            data = lines.stream().skip(1).map(line -> line.split(",")).collect(Collectors.toList());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // parse file information
        for (String[] lst : data) {
            Integer alliance;
            Integer allianceDuration;
            Integer i = Integer.valueOf(lst[1]); //key
            Integer j = Integer.valueOf(lst[2]);
            double capI = Double.valueOf(lst[3]);
            Integer cultureIndex = Integer.valueOf(lst[5]);
            // fix the cultureIndex to 0,1; for the old input data
//            double cultureRead = Double.valueOf(lst[5]);
//            Integer cultureIndex = cultureRead > 0 ? 1: 0;
            Integer democI = Integer.valueOf(lst[6]);
            Integer neighb = Integer.valueOf(lst[8]);
            Integer enemy = Integer.valueOf(lst[9]);
            //use actual ally data to calculate Burden Sharing; edit at 2022-07-06
            Integer actualAlly = Integer.valueOf(lst[10]);
            //alliance (use simulation alliance data as input)
            if (lst.length == 12) {
                alliance = Integer.valueOf(lst[11]);
            } else {
                alliance = 0;
            }
            //alliance duration
            if(lst.length ==13){
//                allianceDuration = Integer.valueOf(lst[11]);
                allianceDuration = Integer.valueOf(lst[12]); //new input at 2022-07-06
            } else{
                allianceDuration = 0;
            }

            InfoIdentifier agentInfo = info.getOrDefault(i, new InfoIdentifier(i, capI, democI, new HashSet<Integer>(),
                    new HashSet<Integer>(), new HashSet<Integer>(), new HashMap<Integer, Integer>(), new HashSet<Integer>(),
                    new HashMap<Integer, Integer>(), new HashSet<Integer>()));

            agentInfo.updateCulture(j, cultureIndex);
            if(!i.equals(j)){
                agentInfo.updateAllianceDuration(j, allianceDuration);
            }

            if (enemy == 1 && !i.equals(j)) {
                agentInfo.updateEnemy(j);
            }
            if (enemy == 2 && !i.equals(j)) {
                agentInfo.updateSecondaryEnemy(j);
            }
            if (neighb == 1) {
                agentInfo.updateNeighbor(j);
            }
            if (alliance == 1 && !i.equals(j)) {
                agentInfo.updateAlliance(j);
            }
            if (actualAlly == 1 && !i.equals(j)) {
                agentInfo.updateActualAlly(j);
            }
            info.put(i, agentInfo);

        }
        return this.info;
    }

}