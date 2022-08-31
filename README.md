# BurdenSharing v3.0 - 2022-04-15 burdenSharingPrediction

This is the version of burdenSharing v3.0 that is based on burdenSharing_v2.0 data46

* 2022-06-10: v1- B_i = utility + detection * punishment + emulation + enemy burdenSharing + alliance duration
  based on security coop v46
* 2022-07-10: add "actual ally" as input, get the simulated network. But calculate the burden sharing values
  based on simulated network annually. data_v9
* 2022-08-02: adjust the parameter of need, prior 1945 and after 1945. data_v10
* 2022-08-30: 2022-08-29: BS calculations were based on simulated networks. The currentU was also based on simulation.
   we changed the cost term and consequently equation (1) in the security cooperation paper. 
   The cost term is now defined for each prospective ally as c_j = sum e_j - ee_ij. 
   This changes equation (4) in the security cooperation paper accordingly. (data_v14)