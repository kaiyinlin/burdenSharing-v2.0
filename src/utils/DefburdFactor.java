package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class DefburdFactor {
    String fileDirectory;
    Path path;
    List<String[]> data;
    Map<Integer, Map<Integer, Double>> defburdInfo = new HashMap<>();

    public DefburdFactor(String fileDirectory) {
        // TODO Auto-generated constructor stub
        this.fileDirectory = fileDirectory;
        loadDeltaDefburd();
    }

    public void loadDeltaDefburd() {
        List<String> lines;
        path = Paths.get(fileDirectory);
        try {
            lines = Files.readAllLines(path);
            data = lines.stream().skip(1).map(line -> line.split(",")).collect(Collectors.toList());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // parse the file
        for (String[] lst : data) {
            double deltaDefburd;
            Integer year = Integer.valueOf(lst[0]);
            Integer i = Integer.valueOf(lst[1]);
            if(lst.length == 2){
                deltaDefburd = 0;
            } else{
                deltaDefburd = Double.parseDouble(lst[2]);
            }



            Map<Integer, Double> agentDeltaDefburd = defburdInfo.getOrDefault(i, new HashMap<Integer, Double>());
            agentDeltaDefburd.put(year, deltaDefburd);
            defburdInfo.put(i, agentDeltaDefburd);

        }
    }

    public double getDeltaDefburd(Integer i, Integer year){
        return defburdInfo.get(i).get(year);
    }

//    public double getDeltaDefburd(Integer i, Integer currentYear){
//        Map<Integer, Double> agentDefburd = defburdInfo.get(i);
//        double currentDefburd = agentDefburd.getOrDefault(currentYear, 0.0);
//        double lastDefburd = agentDefburd.getOrDefault((currentYear-1),0.0);
//        return currentDefburd - lastDefburd;
//    }
}
