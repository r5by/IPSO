package cse.uta.edu.IPSO;

/**
 * NTYPE: # of tasks in this stage is N, purely parallel computing stage, mainly contribute to Wp
 * MTYPE: # of tasks in this stage is m ( 1 < m < N), related to the available executors, mainly contribute to two parts: Wp and Wo.
 * 			-> The computing time of this stage contribute to Wp
 * 			-> Task deserialization time of this contribute to Ws
 * 			-> All other time contribute to Wo
 *
 * STYPE: # of tasks in this stage is 1, purely sequentially executed stage, contributes to Ws
 * UNSET: if NP of current experiment set is equal to 1 or NP == MP, this experiment set can't be used to identify the IPSO stage type, more experiment profile will be required.
 */
public enum IPSOStageTypes {
	NTYPE, MTYPE, STYPE, UNSET;
}
