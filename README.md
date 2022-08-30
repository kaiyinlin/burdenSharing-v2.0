# BurdenSharing v3.0 - 2022-04-15 burdenSharingPrediction

This is the version of burdenSharing v3.0 that is based on burdenSharing_v2.0 data46

* 2022-06-10: v1- B_i = utility + detection * punishment + emulation + enemy burdenSharing + alliance duration
  based on security coop v46
* 2022-07-10: add "actual ally" as input. The calculation of B_i is based on actual network data 
  without updating networks (data_v7)
* 2022-07-26: this calculation of B_i is based on simulated network data (data_v8)
* 2022-07-27: the calculations were based on simulated network data but the first input was actual data. 
   Use an updated input from zeev at 07-19-2022 (data_v9)
* 2022-08-05: Two simulations that adjust the need elements prior 1945 and after 1945, 
   use the updated input from zeev at 07-19-2022 (data_v10 & data_v11).
* 2022-08-10: omit D*P based on data_v7 because D*P cause the outliers (data_v12)
* 2022-08-29: I changed the cost term and consequently equation (1) in the security cooperation paper. 
  The cost term is now defined for each prospective ally as c_j = sum e_j - ee_ij. 
  This changes equation (4) in the security cooperation paper accordingly. (data_v13)