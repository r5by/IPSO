package cse.uta.edu.Utils;

import org.apache.log4j.Level;

public class IPSOConfig {
    // Values: "debug", "info", "warn", "error", "fatal"
    public final static String LOG_LEVEL = "log.level";
    public final static Level DEFAULT_LOG_LEVEL = Level.DEBUG;

    /** The root directory of all N-m sets of Spark job log profiles
     * e.g. A typical Spark log files tree structure for IPSO is:
     *  <log.root.dir>
     *      \- <1-1>
     *      \- <2-1>
     *      \- <2-2>
     *          ...
     *   where <N-m> is the directory name which keeps logs of experiment set for N=N, m=m
     */
    public final static String LOG_ROOT_DIR = "log.root.dir";
    /* The directory contains the Spark job log profiles for a fixed N experiment set (fixed workload size) given different m value*/
//    public final static String LOG_DIR_FIXED_SIZE = "log.dir.fixed.size";

    /* Boolean flags to use IPSO analyzing Spark log files */
    public final static String SHOW_STAGE_INFO = "show.stage.info";
    public final static String SHOW_TASKS_INFO = "show.tasks.info";
    public final static String QUICK_ANALYSIS = "quick.analysis";
    public final static String BATCH_PROC = "batch.process";

    /* Write out IPSO related performance metrics */
    public final static String IPSO_ANALYSIS = "ipso.analysis";
    /* Write out IPSO scaling factors*/
    public final static String IPSO_ANALYSIS_SCALING_FACTORS = "ipso.analysis.scaling.factors";
    /* Write out IPSO speedups v.s. actual system speedups*/
    public final static String IPSO_ANALYSIS_SPEEDUP = "ipso.analysis.speedup";
    /* Plotting the speedups of experimental results with IPSO prediction */
    public final static String IPSO_ANALYSIS_PLOTTING = "ipso.analysis.plotting";
    /* An output directory to write out results, if show.ipso flag is set true */
    public final static String OUTPUT_DIR = "output.dir";
    /* Configuration file path of using IPSO batch job processing function */
    public final static String BATCH_CONF_PATH = "batch.conf.path";

    /* If isBatch is off, expr N/m values need to be passed by the configuration file*/
    public final static String EXPR_CONF_N = "expr.conf.N";
    public final static String EXPR_CONF_M = "expr.conf.m";
}
