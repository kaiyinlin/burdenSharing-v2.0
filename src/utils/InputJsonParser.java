package utils;

import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InputJsonParser {

    private static final Logger logger = Logger.getLogger(InputJsonParser.class);

    public static JSONObject parseInput(String fileDirectory) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        JSONObject inputObject = new JSONObject();

        try (FileReader reader = new FileReader(fileDirectory)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            inputObject = (JSONObject) obj;
            logger.info(String.format("Input Parameters %s", inputObject));

            //Iterate over employee array
            parseInputObject(inputObject);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return inputObject;
    }

    private static void parseInputObject(JSONObject input) {
        String inputData = (String) input.get("inputData");
        long year = (long) input.get("year");
        String nextInputData = (String) input.get("nextInputData");

    }
}
