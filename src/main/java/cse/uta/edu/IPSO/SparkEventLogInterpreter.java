package cse.uta.edu.IPSO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cse.uta.edu.Utils.IPSOConfig;
import cse.uta.edu.Utils.LongDeserializer;
import cse.uta.edu.Utils.Util;
import cse.uta.edu.model.*;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import javax.naming.directory.InvalidAttributesException;
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
    private static final Logger LOG = Logger.getLogger(SparkEventLogInterpreter.class);
    //===============================
    //       Config.
    //================================
    /* Submitted as batch job */
    private boolean isBatch;
    private String batchConfigPath;

    /* File import TAGS*/
//    private int N;
//    private int m;
    private String logRootPath;
//    private String logPath;
    private String outputPath;

    /* For IPSO analysis , "stage name" -> "stage information" */
    //TODO: The index in this map can be used to form the real execution longest path
    private static HashMap<Long, IPSOStage> stages = new HashMap();

    /* print out stage*/
    private boolean showStageInfo;

    /* print out tasks info within each stage when the flag is set true */
    private boolean showTasksInfo;

    /* Write out stage info for IPSO model analysis */
    private boolean ipsoAnalysis;

    /* use the first log file in the log event directory to speedup IPSO analysis */
    private boolean quickAnalysis;

    /*application execution time stamp */
    private long appLaunchTimeMS;
    private long appFinishTimeMS;
    private long firstStageLaunchTimeMS;

    public void init(Configuration conf) {
        showStageInfo = conf.getBoolean(IPSOConfig.SHOW_STAGE_INFO, false);
        showTasksInfo = conf.getBoolean(IPSOConfig.SHOW_TASKS_INFO, false);
        ipsoAnalysis = conf.getBoolean(IPSOConfig.IPSO_ANALYSIS, false);
        quickAnalysis = conf.getBoolean(IPSOConfig.QUICK_ANALYSIS, true);

        isBatch = conf.getBoolean(IPSOConfig.BATCH_PROC, false);
        batchConfigPath = conf.getString(IPSOConfig.BATCH_CONF_PATH);

        logRootPath = conf.getString(IPSOConfig.LOG_ROOT_DIR);
//        logPath = conf.getString(IPSOConfig.LOG_DIR_FIXED_SIZE);
        outputPath = conf.getString(IPSOConfig.OUTPUT_DIR);

        //todo: set N, m from configuration file
//        N = 4;
//        m = 2;

        appLaunchTimeMS = 0;
        appFinishTimeMS = 0;
        firstStageLaunchTimeMS = 0;
    }

    public String getBatchConfigPath() {return this.batchConfigPath;}
    public boolean isBatch() { return this.isBatch; }

    public void analyzeLogsForExprSets() {
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
        LOG.debug(path);

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
            long latency = appFinishTimeMS - appLaunchTimeMS;
            LOG.info("Applicatoin execution duration (ms) | " + latency);

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
                        || line.contains("SparkListenerTaskEnd"))
                        .map(String::trim).collect(Collectors.toList());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            logList.forEach(line -> parseJSONLog(line));

            /* Process IPSO Analysis */
            if(stages.isEmpty())
                System.err.println("Requires Spark log files for IPSO performance analysis!");

            //Title
//			LOG.debug(" stageID | type | wp | ws | wo");
//			stages.forEach((k, v) -> LOG.debug(k+ " | " + v.getStageName() + " | " + v.stageType()
//															+ " | " + v.IPSO().wp()
//															+ " | " + v.IPSO().ws()
//															+ " | " + v.IPSO().wo()));
            //TODO: finish the NTYPE stage analysis and output to files based on its stage name
            stages.forEach((k, v) -> output(k, v));
        }

    }

    /**
     * MILESTONE (1): Analysis NTYPE stages
     * @param stageID
     * @param stage
     */
    public void output(Long stageID, IPSOStage stage) {
        //TITLE: N | m | stageName | wp | ws | wo
        String outString = "";

        try {
            if(stage.stageType() == IPSOStageTypes.NTYPE) {
                Path fpath=Paths.get(Util.getIpsoOutputPath(outputPath, stage.getStageName()));

                //Create file
                if(!Files.exists(fpath)) {
                    Files.createFile(fpath);
                    outString += "N | m | duration(ms) | stageName | wp | ws | wo\n";
                }

                outString +=
                        IPSOExprConfig.getInstance().NP() + " | " +
                                IPSOExprConfig.getInstance().MP() + " | " +
                                stage.getDurationInMS() + " | " +
                                stage.getStageName() + " | " +
                                stage.IPSO().wp() + " | " +
                                stage.IPSO().ws() + " | " +
                                stage.IPSO().wo() + "\n";


                BufferedWriter bfw=Files.newBufferedWriter(fpath, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                bfw.write(outString);
                bfw.flush();
                bfw.close();
            }

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
                    stages.put(stageID, new IPSOStage(stageID));
                }

                try {
                    stages.get(stageID).addStageInfo(stage);
                } catch (InvalidAttributesException e) {
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
                    stages.put(stageIDForTheTask, new IPSOStage(stageIDForTheTask));
                }

                try {
                    stages.get(stageIDForTheTask).addTaskInfo(taskMetrics);
                } catch (InvalidAttributesException e) {
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
