# BurdenSharing v2.0 - master_20220105

This is the master version of burdenSharing v2.0 that is created by hsin and based on burdenSharing v1.8

* data_v22: rewritten and organized codes. Use the old cost term setting. The results should be similar to data_v21
* data_v24: Use the old cost term function, but change the exponent of cost term (after 1945) to 0.5. The cost term after 1945 is 0.2*T^0.5
* data_v25: Use the old cost term function, but change the exponent of cost term (after 1943) to 0.8. The cost term after 1943 is 0.2*T^0.8. The threshold of currentU is -1<U<1.
* data_v27: change the cost term after 1945 to 0.2T^0.5, and limits the currentU threshold between -1 and 0.5 (-1<U<0.5).
* data_v28: change the cost term to 0.2T^0.7 and limits the currentU threshold between -1 and 1 (-1<U<1). Change the schedule order from the most powerful country.
* data_v29: change the cost term back to 0.2T^1 and limits the currentU between -2<U<0. Schedule order: from the highest capability

Create a new branch - 20220114- check-potential-utility
* data_v31: New offer rules. Accept the offer when marginal u increases. Upperbound = 0
* data_v32: New offer rules. Accept the offer when marginal u increases. Upperbound = 1 
* data_v33: New offer rules. Accept the offer when marginal u increases. Upperbound = 1
* data_v34: reduce the costPenalty = 0.005, cost term setting was like v29
* data_v35: manipulate the parameters of u_ij - 6, 0.1, 3