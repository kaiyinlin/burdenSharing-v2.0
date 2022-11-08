#! /bin/bash

# variable settings
JAR_NAME=/Users/kaiyinlin/eclipse-workspace/BurdenSharing-hsin-mac/out/artifacts/BurdenSharing_hsin_mac_jar/BurdenSharing-hsin-mac.jar
echo $JAR_LOCATION
DATA_BY_YEAR_LOCATION=/Users/kaiyinlin/Desktop/dataByYear
#DATA_BY_YEAR_LOCATION=/Users/kaiyinlin/Desktop/dataByYear_random


# 1816
printf '{"inputData":"%s/1816.csv", "year":1816, "nextInputData":"%s/1817_input.csv"}\n' $DATA_BY_YEAR_LOCATION $DATA_BY_YEAR_LOCATION > run.json
java -jar $JAR_NAME run.json

# from 1817
for ((i=1817;i<=2014;i++));
do
  echo "RUNNING YEAR="+$i
  printf '{"inputData":"%s/%s_input.csv", "year":%s, "nextInputData":"%s/%s_input.csv"}\n' $DATA_BY_YEAR_LOCATION $i $i $DATA_BY_YEAR_LOCATION $(($i+1)) > run.json
  java -jar $JAR_NAME run.json
  wait
done