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
* 2022-09-06: Since the results of data_v14 improve the correctness rate, we wonder if we can apply this version of cost term
    and implant the all three elements (attractiveness, prevention, and trust) in the utility function 
    to see if the results can even improve more (data_v15).
* 2022-10-13: Intend to allow 1st enemies to form alliance. However, in this version, I still exclude the 1st enemies in the potential-ally list.
    I only changed the u_ij calculations (0.5*u_ij), so their u_ij is not zero anymore. (data_v16)
* 2022-10-22: Correct codes. Allow 1st enemies to form alliances. from 1816-2014 (data_v17)
* 2022-10-22: Correct codes. Allow 1st enemies to form alliances, but only from 1945-2014 (data_18)
* 2022-10-25: Use actual ally dataset as first year input (1816), and then use simulated network for following years (data_v19)
* 2022-10-25: Create a series of random number for each input variable, from 1816-2014. The dataset
    was based on same prob. of real data. Random datasets were input annually (data_v20).
* 2022-11-06: modified equations for security cooperation ABM - from zeev. 
    This version is using 1816 real data as first year input and then use simulated network for following inputs (data_v21).