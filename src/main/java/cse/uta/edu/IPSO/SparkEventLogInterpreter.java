package cse.uta.edu.IPSO;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cse.uta.edu.Utils.IPSOConfig;
import cse.uta.edu.Utils.LongDeserializer;
import cse.uta.edu.Utils.Util;
import cse.uta.edu.model.*;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Scatter3DPlot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.Scatter3DTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparkEventLogInterpreter {
    private static final Logger LOG = LoggerFactory.getLogger(SparkEventLogInterpreter.class);
    //===============================
    //       Config.
    //================================
    /* Submitted as batch job */
    private boolean isBatch;
    private String batchConfigPath;

    /* File import TAGS*/
    private String logRootPath;
    private String outputPath;

    /* For IPSO analysis , "stage id" -> "stage information" */
    //TODO: The index in this map can be used to form the real execution longest path
    private static HashMap<Long, IPSOStage> stages = new HashMap();

    /* IPSO cache , "(N,m) pair" -> "stages information" */
    private static HashMap<IPSOExprID, HashMap<Long, IPSOStage>> ipsoStagesHM = new HashMap<>();
    private static HashMap<IPSOExprID, Long> ipsoDurationHM = new HashMap<IPSOExprID, Long>();
    private static HashMap<IPSOExprID, Long> ipsoOverheadHM = new HashMap<IPSOExprID, Long>();

    /* IPSO Speedup */
    private ArrayList<Integer> NArray;
    private ArrayList<Integer> mArray;
    private ArrayList<Double> latencyExprArray;
    private ArrayList<Double> latencyIPSOArray;
    private ArrayList<Long> wpArray;
    private ArrayList<Long> wsArray;
    private ArrayList<Long> woArray;
    private ArrayList<Double> speedupExprArray;
    private ArrayList<Double> speedupIPSOArray;

    /* print out stage*/
    private boolean showStageInfo;

    /* print out tasks info within each stage when the flag is set true */
    private boolean showTasksInfo;

    /* Write out stage info for IPSO model analysis */
    private boolean ipsoAnalysis;
    /* Scaling factors: Wp, Ws, Wo */
    private boolean ipsoAnalysisScalingFactors;
    /* Write out speedups */
    private boolean ipsoAnalysisSpeedup;
    private boolean ipsoAnalysisPlotting;

    /* use the first log file in the log event directory to speedup IPSO analysis */
    private boolean quickAnalysis;

    /*application execution time stamp */
    private long appLaunchTimeMS;
    private long appFinishTimeMS;
    private long firstStageLaunchTimeMS;
    /* baseline: the latency and ipso-speedup of the first profile will be used as baseline to compare the system speedups (for fixed-time) */
    private double baseLatencyMS;
    private double baseLatencyIPSOMS;
    /* baseline: the workload, taken from base expr sets: N=m=1 */
    private double baselineWs;
    private double baselineWp;

    /* If isBatch is off, the experiment configuration N/m must be obtained from configuration file */
    private int N;
    private int m;
    private static final int DEFAULT_N_M = -1;

    public void init(Configuration conf) {
        /* IPSO-realted */
        ipsoAnalysis = conf.getBoolean(IPSOConfig.IPSO_ANALYSIS, false);
        ipsoAnalysisScalingFactors = conf.getBoolean(IPSOConfig.IPSO_ANALYSIS_SCALING_FACTORS, false);
        ipsoAnalysisSpeedup = conf.getBoolean(IPSOConfig.IPSO_ANALYSIS_SPEEDUP, false);
        ipsoAnalysisPlotting = conf.getBoolean(IPSOConfig.IPSO_ANALYSIS_PLOTTING, false);

        isBatch = conf.getBoolean(IPSOConfig.BATCH_PROC, false);
        batchConfigPath = conf.getString(IPSOConfig.BATCH_CONF_PATH);

        /* Tunes */
        showStageInfo = conf.getBoolean(IPSOConfig.SHOW_STAGE_INFO, false);
        showTasksInfo = conf.getBoolean(IPSOConfig.SHOW_TASKS_INFO, false);
        quickAnalysis = conf.getBoolean(IPSOConfig.QUICK_ANALYSIS, true);

        /* Paths */
        logRootPath = conf.getString(IPSOConfig.LOG_ROOT_DIR);
        outputPath = conf.getString(IPSOConfig.OUTPUT_DIR);

        /* Others */
        N = conf.getInt(IPSOConfig.EXPR_CONF_N, DEFAULT_N_M);
        m = conf.getInt(IPSOConfig.EXPR_CONF_M, DEFAULT_N_M);

        appLaunchTimeMS = 0;
        appFinishTimeMS = 0;
        firstStageLaunchTimeMS = 0;
        baseLatencyMS = -1;
        baseLatencyIPSOMS = -1;

        if(ipsoAnalysisSpeedup) {
            NArray = new ArrayList<>();
            mArray = new ArrayList<>();
            latencyExprArray = new ArrayList<>();
            latencyIPSOArray = new ArrayList<>();
            wpArray = new ArrayList<>();
            wsArray = new ArrayList<>();
            woArray = new ArrayList<>();
            speedupExprArray = new ArrayList<>();
            speedupIPSOArray = new ArrayList<>();
        }
    }

    public String getBatchConfigPath() {return this.batchConfigPath;}
    public boolean isBatch() { return this.isBatch; }
    public boolean isIPSO() { return this.ipsoAnalysis; }
    public boolean onSpeedup() { return this.ipsoAnalysisSpeedup; }
    public boolean onScalingFactors() {return this.ipsoAnalysisScalingFactors; }

    public void analyzeLogsForExprSets() {
        IPSOExprConfig.getInstance().setNP(N);
        IPSOExprConfig.getInstance().setMP(m);
        analyzeLogsInPath(Paths.get(this.logRootPath));
    }
    private void analyzeLogsInPath(Path source) {
        /* Read all log files in dir. and process each log file */
        List<Path> fileList = new ArrayList();
        try {
            fileList = Files.walk(source).filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(quickAnalysis)
            parseLogInPath(fileList.get(0));
        else
            fileList.forEach(path -> parseLogInPath(path));
    }

    public void analyzeLogsForExprSets(int pN, int pm) {
        Path source = null;

        if(!isBatch) {
            LOG.debug("Use the passed path as Spark log directory.");
            source = Paths.get(logRootPath);
        } else {
            LOG.debug("Formatting Spark log file path for (N=" + pN + ",m=" + pm + ") set and prepare the IPSO analysis");
            /*Save the expr. config */
            IPSOExprConfig.getInstance().setNP(pN);
            IPSOExprConfig.getInstance().setMP(pm);

            source = Paths.get(logRootPath + pN + "-" + pm);
        }

        analyzeLogsInPath(source);
    }

    /**
     * Process the file line by line for 3 usages:
     * 1. Show stage info
     * 2. Show tasks info
     * 3. Write out IPSO info
     * @param path
     * @return
     */
    public void parseLogInPath(Path path) {
        //clear cache
        stages.clear();

        //Print out the log file location
        LOG.debug("Processing log file: {}", path);

        List<String> logList = new ArrayList<>();

        /* USE CASE- 1: Show stage info in the log files */
        if(showStageInfo) {
            try (Stream<String> stream = Files.lines(path)) {
                logList = stream.filter(line -> line.contains("SparkListenerStageCompleted")
                        || line.contains("SparkListenerApplicationEnd") || line.contains("SparkListenerApplicationStart"))
                        .map(String::trim).collect(Collectors.toList());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

//			LOG.debug(logList.size());
            /**
             * Title, print each stage information in format:
             *  StageID | numberOfTasks | Duration(s) | Deserialization Time(ms) | Computing Time(ms)
             */
            LOG.info("StageID | StageName | numberOfTasks | SubmissionTime | ComplitionTime | Duration(ms) | Deserialization Time(ms) | Computing Time(ms) | GC Time(ms)");
            logList.forEach(line -> parseJSONLog(line));

            /* app execution duration in seconds. */
            long duration = appFinishTimeMS - appLaunchTimeMS;
            LOG.info("Applicatoin execution duration (ms) | " + duration);

            /* app execution overhead (time spent between app_launch time and first_stage_launch time */
            long overhead = firstStageLaunchTimeMS - appLaunchTimeMS;
            LOG.info("Application launch overhead (ms) | " + overhead);
        }


        /*USE CASE- 2: Show Tasks info of each stage */
        if(showTasksInfo) {
            try (Stream<String> stream = Files.lines(path)) {
                logList = stream.filter(line -> line.contains("SparkListenerTaskEnd"))
                        .map(String::trim).collect(Collectors.toList());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            /*Title*/
            LOG.info("TaskID | StageID | ExecutorID | TaskType | Submission Time | Completion Time"
                    + " | internal.metrics.executorRunTime"
                    + " | internal.metrics.jvmGCTime"
                    + " | internal.metrics.executorDeserializeCpuTime"
                    + " | internal.metrics.shuffle.write.writeTime"
                    + " | internal.metrics.executorCpuTime"
                    + " | internal.metrics.resultSerializationTime"
                    + " | internal.metrics.executorDeserializeTime");

            logList.forEach(line -> parseJSONLog(line));
        }


        /*USE CASE- 3: IPSO related */
        if(ipsoAnalysis) {
            try (Stream<String> stream = Files.lines(path)) {
                logList = stream.filter(line -> line.contains("SparkListenerStageCompleted")
                        || line.contains("SparkListenerTaskEnd")
                        || line.contains("SparkListenerApplicationEnd")
                        || line.contains("SparkListenerApplicationStart"))
                        .map(String::trim).collect(Collectors.toList());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            logList.forEach(line -> parseJSONLog(line));

            /* Process IPSO Analysis */
            if(stages.isEmpty())
                LOG.error("Requires Spark log files for IPSO performance analysis!");

            //First iteration find the experiment set with N=m=1 to pin-point stage tags in single-processor expr;
            if(!AppStageTag.getInstance().isReady())
            {
                AppStageTag.getInstance().put(stages);
            }

            IPSOExprID key = new IPSOExprID(IPSOExprConfig.getInstance().NP(), IPSOExprConfig.getInstance().MP());

            if (onSpeedup()) {
                long duration = appFinishTimeMS - appLaunchTimeMS;
                long overhead = firstStageLaunchTimeMS - appLaunchTimeMS;
                LOG.debug("(" + IPSOExprConfig.getInstance().NP() + " , " + IPSOExprConfig.getInstance().MP() + ") duration: " + duration + ", overhead: " + overhead);
                ipsoDurationHM.put(key, duration);
                ipsoOverheadHM.put(key, overhead);
            }

            //Cache the stage info for IPSO analysis in second iteration
            ipsoStagesHM.put(key, new HashMap<>(stages));
        }
    }

    public void outputIPSO() {
        try {
            Table ipsoSpeedupTable = Table.create("IPSOSpeedupAnalysisTable")
                    .addColumns(
                            IntColumn.create("N", Ints.toArray(NArray)),
                            IntColumn.create("m", Ints.toArray(mArray)),
                            DoubleColumn.create("L_expr (ms)", Doubles.toArray(latencyExprArray)),
                            DoubleColumn.create("L_ipso (ms)", Doubles.toArray(latencyIPSOArray)),
                            LongColumn.create("External Scaling (ms)", Longs.toArray(wpArray)),
                            LongColumn.create("Internal Scaling (ms)", Longs.toArray(wsArray)),
                            LongColumn.create("Scaling-out-induced (ms)", Longs.toArray(woArray)),
                            DoubleColumn.create("Speedup_expr", Doubles.toArray(speedupExprArray)),
                            DoubleColumn.create("Speedup_ipso", Doubles.toArray(speedupIPSOArray)));

            ipsoSpeedupTable.write().csv(Util.ipsoSpeedupOutputPath(outputPath, "ipso.csv"));

            //Plotting
            if (ipsoAnalysisPlotting) {
                String title = "IPSO Speedup Analysis";
                String xCol = "N";
                String yCol = "m";
                String zCol = "S";
                Layout layout = Util.standardLayout(title, xCol, yCol, zCol, true);
                Scatter3DTrace traceSexpr = Scatter3DTrace.builder(ipsoSpeedupTable.numberColumn(xCol), ipsoSpeedupTable.numberColumn(yCol), ipsoSpeedupTable.numberColumn("Speedup_expr")).build();
                Scatter3DTrace traceSipso = Scatter3DTrace.builder(ipsoSpeedupTable.numberColumn(xCol), ipsoSpeedupTable.numberColumn(yCol), ipsoSpeedupTable.numberColumn("Speedup_ipso")).build();
                Figure fig = new Figure(layout, new Trace[]{traceSexpr, traceSipso});
                Plot.show(fig);
            }

        } catch (Exception e) {
            LOG.error("Error outputting IPSO analysis", e);
            e.printStackTrace();
        }

    }

    public void calcIPSO(int N, int m) {
        IPSOExprID key = new IPSOExprID(N, m);
        stages = ipsoStagesHM.get(key);
        final long duration = ipsoDurationHM.get(key);
        final long overhead = ipsoOverheadHM.get(key);
        long sumWp = 0;
        long sumWs = overhead;
        long sumWo = 0;

        //Before output firstly check whether the stages are all tagged
        if(!AppStageTag.getInstance().isReady())
            LOG.error("Missing Spark application profiles: IPSO requires more experiments to start-up its analysis!");

        /* IPSO option (1): Scaling factors */
        if(onScalingFactors()) {
            stages.forEach((k, v) -> output(k, v, N, m));
        }

        /* IPSO option (2): Speedups */
        if (onSpeedup()) {

            for (IPSOStage stage : stages.values()) {
                sumWp += stage.IPSO().wp();
                sumWs += stage.IPSO().ws();
                sumWo += stage.IPSO().wo();
            }

            if (N == 1 && m == 1) {
                baselineWs = sumWs;
                baselineWp = sumWp;
            }

            /* L = T/W ; the W is the workload, in IPSO model, we use IN(n)==1, EX(n)==N, thus Ws(1)* IN(n) + Wp(1) * EX(n) as the approximation of the workload */
            double workload = baselineWs + N * baselineWp;
            double latency = duration / workload;

            if (baseLatencyMS == -1)
                baseLatencyMS = latency;

            double latencyIPSO = latencyIPSO(sumWs, sumWp, sumWo, N, m);
            if (baseLatencyIPSOMS == -1)
                baseLatencyIPSOMS = latencyIPSO;

            NArray.add(N);
            mArray.add(m);
            latencyExprArray.add(latency);
            latencyIPSOArray.add(latencyIPSO);
            wpArray.add(sumWp);
            wsArray.add(sumWs);
            woArray.add(sumWo);
            speedupExprArray.add(speedup(baseLatencyMS, latency));
            speedupIPSOArray.add(speedup(baseLatencyIPSOMS, latencyIPSO));

        }
    }

    //IPSO latency (similar to expr latency definition)
    private double latencyIPSO(long ws, long wp, long wo, int N, int m) {
        return (ws + wp/m + wo)/(baselineWs + N * baselineWp);
    }
    //Speedup definition from wikipedia: https://en.wikipedia.org/wiki/Speedup
    private double speedup(double baseLatency, double latency) {
        return baseLatency/latency;
    }


    private void output(Long stageID, IPSOStage stage, int N, int m) {
        //TITLE: | N | m | stageName | wp | ws | wo | type |
        String outString = "";

        try {
                Path fpath=Paths.get(Util.ipsoScalingFactorOutputPath(outputPath, stage.getStageName()));
                if(!Files.exists(fpath)) {
                    Files.createFile(fpath);
                    outString += "| N | m | duration(ms) | stageName | wp | ws | wo | type |\n";
                }

                outString +=
                        "| " + N + " | " + m + " | " +
                                stage.getDurationInMS() + " | " +
                                stage.getStageName() + " | " +
                                stage.IPSO().wp() + " | " +
                                stage.IPSO().ws() + " | " +
                                stage.IPSO().wo() + " | " +
                                stage.stageType() + " | " +
                                "\n";


                BufferedWriter bfw=Files.newBufferedWriter(fpath, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                bfw.write(outString);
                bfw.flush();
                bfw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the string line msg with gson
     */
    private void parseJSONLog(String line) {
        /* Customize gson for parsing Accumulable class (Long value in this class causes trouble)*/
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Long.class, new LongDeserializer())
                .create();

        if (line.contains("SparkListenerApplicationEnd")) {
            EventApplicationEnd msgAppEnd = gson.fromJson(line, EventApplicationEnd.class);
//			LOG.debug("app end time: " + msgAppEnd.getTimestamp());
            appFinishTimeMS = Long.valueOf(msgAppEnd.getTimestamp());

        } else if (line.contains("SparkListenerApplicationStart")){
            EventApplicationStart msgAppStart = gson.fromJson(line, EventApplicationStart.class);
//			LOG.debug("app start time: " + msgAppStart.getTimestamp());
            appLaunchTimeMS = Long.valueOf(msgAppStart.getTimestamp());

        } else if (line.contains("SparkListenerStageCompleted")){
            EventStageEnd msgStageEnd = gson.fromJson(line, EventStageEnd.class);
            StageInfo stageInfo = msgStageEnd.getStageInfo();

            StageMetrics stage = new StageMetrics(stageInfo.getStageID(),
                    stageInfo.getStageName(),
                    stageInfo.getNumberOfTasks(),
                    stageInfo.getSubmissionTime(),
                    stageInfo.getCompletionTime(),
                    getDeserTimeFromStageInfo(stageInfo),
                    getComptTimeFromStageInfo(stageInfo),
                    getjvmGCTime(stageInfo));

            /* Print out the information as excel import for further analysis */
            if(showStageInfo)
                stage.stageInfo();
//			stage.stageInfoSignificant();

            /**
             * Cash stage information for later IPSO analysis
             */
            if(ipsoAnalysis) {
//				stages.put(stageInfo.getStageID(), new IPSOStage(stageInfo.getStageID(), stage));
                long stageID = stage.getID();
                if(stages.size() == 0 || !stages.containsKey(stageID)) {
                    stages.put(stageID, new IPSOStage(stageID, IPSOExprConfig.getInstance().NP(), IPSOExprConfig.getInstance().MP()));
                }

                try {
                    stages.get(stageID).addStageInfo(stage);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }

            /* Record the first stage launch time, to calculate the overhead
             *  overhead = stage0_launch_time - app_launch_time
             **/
            if(stageInfo.getStageID().equals(Long.valueOf(0)))
                firstStageLaunchTimeMS = stageInfo.getSubmissionTime();

        } else if(line.contains("SparkListenerTaskEnd")) {

            EventTaskEnd msgTaskEnd = gson.fromJson(line, EventTaskEnd.class);
            TaskInfo taskInfo = msgTaskEnd.getTaskInfo();
            TaskMetrics metrics = msgTaskEnd.getTaskMetrics();

            TaskMetricsInStage taskMetrics = new TaskMetricsInStage(taskInfo.getTaskID(),
                    msgTaskEnd.getStageID(),
                    taskInfo.getExecutorID(),
                    msgTaskEnd.getTaskType(),
                    taskInfo.getLaunchTime(),
                    taskInfo.getFinishTime(),
                    metrics.getExecutorRunTime(),
                    metrics.getJVMGCTime(),
                    metrics.getExecutorDeserializeCPUTime(),
                    metrics.getShuffleWriteMetrics().getShuffleWriteTime(),
                    metrics.getExecutorCPUTime(),
                    metrics.getResultSerializationTime(),
                    metrics.getExecutorDeserializeTime());

            /*print out task info*/
            if(showTasksInfo)
                taskMetrics.taskInfo();

            /*Cashed the task info per-stage for ipso analysis*/
            if(ipsoAnalysis) {
                Long stageIDForTheTask = Long.valueOf(taskMetrics.getStageID());
                if(stages.size() == 0 || !stages.containsKey(stageIDForTheTask)) {
                    stages.put(stageIDForTheTask, new IPSOStage(stageIDForTheTask, IPSOExprConfig.getInstance().NP(), IPSOExprConfig.getInstance().MP()));
                }

                try {
                    stages.get(stageIDForTheTask).addTaskInfo(taskMetrics);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } else {
            //todo: catch and parse other type msgs here
        }
    }

    /**
     * Get deserialization time from stage info
     * @param stageInfo
     * @return
     */
    private long getDeserTimeFromStageInfo(StageInfo stageInfo) {
        long deserL = 0;
        List<Accumulable> accumulables = stageInfo.getAccumulables();
        for(Accumulable accum : accumulables) {
            if(accum.getName().equals("internal.metrics.executorDeserializeTime"))
                deserL = accum.getValue();
        }

        return deserL;
    }

    /**
     * Get executor run (computing) time from stage info
     * @param stageInfo
     * @return
     */
    private long getComptTimeFromStageInfo(StageInfo stageInfo) {
        long comptL = 0;
        List<Accumulable> accumulables = stageInfo.getAccumulables();
        for(Accumulable accum : accumulables) {
            if(accum.getName().equals("internal.metrics.executorRunTime"))
                comptL = accum.getValue();
        }

        return comptL;
    }

    /**
     * Get jvm GC Time from stage info
     * @param stageInfo
     * @return
     */
    private long getjvmGCTime(StageInfo stageInfo) {
        long comptL = 0;
        List<Accumulable> accumulables = stageInfo.getAccumulables();
        for(Accumulable accum : accumulables) {
            if(accum.getName().equals("internal.metrics.jvmGCTime"))
                comptL = accum.getValue();
        }

        return comptL;
    }
}
