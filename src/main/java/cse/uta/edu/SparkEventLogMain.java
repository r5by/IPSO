/**
 * 
 */
package cse.uta.edu;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.directory.InvalidAttributesException;
import javax.sound.sampled.Line;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import cse.uta.edu.model.Accumulable;
import cse.uta.edu.model.EventApplicationEnd;
import cse.uta.edu.model.EventApplicationStart;
import cse.uta.edu.model.EventStageEnd;
import cse.uta.edu.model.EventTaskEnd;
import cse.uta.edu.model.StageInfo;
import cse.uta.edu.model.TaskInfo;
import cse.uta.edu.model.TaskMetrics;

/**
 * @author ruby
 * 
 *         Run this tool to read and parse Spark 2.1 event log (downloaded form
 *         Spark History Web UI)
 *         
 *         Usage: Change String value of SUB_LOG_DIR to the n_m directory that includes all experiment data for experiment n_m
 *
 */
public class SparkEventLogMain {
	private static final Logger LOG = Logger.getLogger(SparkEventLogMain.class);
	//===============================
	//       Config.
	//================================

	/* File import TAGS*/
	public static int N = 4;
	public static int m = 1;
	public static String LOG_DIR = "C:/Users/ruby/Desktop/Research/spark-lab/event_logs/bayes_2xlarge_master_no_dups/";
	public static String LOG_DIR_RESEARCH = "C:/Users/ruby/Desktop/ipso/nweight_n_4_m_v";
	public static final String OUTPUT_DIR = "C:/Users/ruby/Desktop/ipso/output/";

	
	/* print out stage*/
	private static boolean showStageInfo = true;
	
	/* print out tasks info within each stage when the flag is set true */
	private static boolean showTaskInfo = false;
	
	/* print out stage info in ipso model */
	private static boolean showIPSO = false;
	
	/* use the first log file in the log event directory */
	private static boolean useOnce = true;

	/* Submitted as batch job */
	private static boolean isBatch = false;
	public static final String N_M_SET = "C:/Users/ruby/Desktop/ipso/N_m_set";
	
	//===============================
	//       Implementation
	//================================
	/*application execution time stamp */
	private static long appLaunchTimeMS = 0;
	private static long appFinishTimeMS = 0;
	
	private static long firstStageLaunchTimeMS = 0;

	/* For IPSO analysis , "stage name" -> "stage information" */
	//TODO: The index in this map can be usaged to form the real execution longest path
	private static HashMap<Long, IPSOStage> stages = new HashMap();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();

		/* If use as batch job processing */
		if(isBatch) {
			String nmConfig = N_M_SET;
			List<String> list = new ArrayList<>();

			try (Stream<String> stream = Files.lines(Paths.get(nmConfig))) {

				//1. filter line 3
				//2. convert all content to upper case
				//3. convert it into a List
				list = stream
						.filter(line -> !line.startsWith("line3"))
						.map(String::toUpperCase)
						.collect(Collectors.toList());

			} catch (IOException e) {
				e.printStackTrace();
			}

//			list.forEach(System.out::println);
			for(int i = 0; i < list.size(); i++){
				String[] pairs = list.get(i).split("\t");
				analyzeLogsForExprSets(Integer.valueOf(pairs[0]), Integer.valueOf(pairs[1]));
			}
		}
		else// single use processing the log for one specific experiment set (N, m)
			analyzeLogsForExprSets(N, m);
	}
	
	private static void analyzeLogsForExprSets(int pN, int pm) {
		Path source = null;
			
		if(pN == -1 || pm == -1) {
			source = Paths.get(LOG_DIR_RESEARCH);
		} else {
			/*Save the expr. config */
			IPSOExprConfig.getInstance().setNP(pN);
			IPSOExprConfig.getInstance().setMP(pm);
			
			
			source = Paths.get(LOG_DIR + pN + "-" + pm);
		}
		
		/* Read all log files in dir. and process each log file */
		List<Path> fileList = new ArrayList();
	    try {
			fileList = Files.walk(source).filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    if(useOnce)
	    	parseLogInPath(fileList.get(0));
	    else
	    	fileList.forEach(path -> parseLogInPath(path));		
	}

	/**
	 * Process the file line by line for 3 usages:
	 * 1. Show stage info
	 * 2. Show tasks info
	 * 3. Show ipso info
	 * @param path
	 * @return
	 */
	private static void parseLogInPath(Path path) {
		//clear cache
		stages.clear();
		
		//Print out the log file location
//		System.out.println(path);
		LOG.info(path);

		List<String> logList = new ArrayList<>();
		
		/* Usage 1: Show stage info in the log files*/
		if(showStageInfo) {
			
			/**
			 * Java 8 process file line by line
			 */
			try (Stream<String> stream = Files.lines(path)) {
				logList = stream.filter(line -> line.contains("SparkListenerStageCompleted")
						|| line.contains("SparkListenerApplicationEnd") || line.contains("SparkListenerApplicationStart"))
						.map(String::trim).collect(Collectors.toList());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

//			System.out.println(logList.size());
			/**
			 * Title, print each stage information in format:
			 *  StageID | numberOfTasks | Duration(s) | Deserialization Time(ms) | Computing Time(ms)
			 */
			System.out.println("StageID | StageName | numberOfTasks | SubmissionTime | ComplitionTime | Duration(ms) | Deserialization Time(ms) | Computing Time(ms) | GC Time(ms)");
			logList.forEach(line -> parseJSONLog(line));
			
			/* app execution duration in seconds. */
			long latency = appFinishTimeMS - appLaunchTimeMS;
			System.out.println("Applicatoin execution duration (ms) | " + latency);
			
			/* app execution overhead (time spent between app_launch time and first_stage_launch time */
			long overhead = firstStageLaunchTimeMS - appLaunchTimeMS;
			System.out.println("Application launch overhead (ms) | " + overhead);
		}

			
		/*Usage 2: Show Task info*/
		if(showTaskInfo) {
			
			/**
			 * Java 8 process file line by line to get stage info
			 */
			try (Stream<String> stream = Files.lines(path)) {
				logList = stream.filter(line -> line.contains("SparkListenerTaskEnd"))
						.map(String::trim).collect(Collectors.toList());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			/*Title*/
			System.out.println("TaskID | StageID | ExecutorID | TaskType | Submission Time | Completion Time"
					+ " | internal.metrics.executorRunTime"
					+ " | internal.metrics.jvmGCTime"
					+ " | internal.metrics.executorDeserializeCpuTime"
					+ " | internal.metrics.shuffle.write.writeTime"
					+ " | internal.metrics.executorCpuTime"
					+ " | internal.metrics.resultSerializationTime"
					+ " | internal.metrics.executorDeserializeTime");
			
			logList.forEach(line -> parseJSONLog(line));
		}
		
		
		/*Usage 3: Show IPSO info */
		if(showIPSO) {
//			List<String> logList = new ArrayList<>();
			
			/**
			 * Java 8 process file line by line
			 */
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
//			System.out.println(" stageID | type | wp | ws | wo");
//			stages.forEach((k, v) -> System.out.println(k+ " | " + v.getStageName() + " | " + v.stageType()
//															+ " | " + v.IPSO().wp()
//															+ " | " + v.IPSO().ws()
//															+ " | " + v.IPSO().wo()));
			
			//TODO: MileStone 1) finish the NTYPE stage analysis and output to files based on its stage name
			stages.forEach((k, v) -> output(k, v));
		}
		
	}

	/**
	 * MILESTONE (1): Analysis NTYPE stages
	 * @param stageID
	 * @param stage
	 */
	private static void output(Long stageID, IPSOStage stage) {
        //TITLE: N | m | stageName | wp | ws | wo
		String outString = "";
        
		try {
        	
        	if(stage.stageType() == IPSOStageTypes.NTYPE) {
        		
        		Path fpath=Paths.get(OUTPUT_DIR + stage.getStageName().hashCode()); 
        	      
        		//Create file
                if(!Files.exists(fpath)) {
                	 Files.createFile(fpath);
                	 outString += "N | m | duration | stageName | wp | ws | wo\n";
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
	 * Quick and ugly parse the msg line by line with gson...
	 * 
	 * @return
	 */
	private static void parseJSONLog(String line) {

		
		/* Customize gson for parsing Accumulable class (Long value in this calss casue trouble)*/
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Long.class, new LongDeserializer())
				.create();
		
		if (line.contains("SparkListenerApplicationEnd")) {
			EventApplicationEnd msgAppEnd = gson.fromJson(line, EventApplicationEnd.class);
//			System.out.println("app end time: " + msgAppEnd.getTimestamp());
			appFinishTimeMS = Long.valueOf(msgAppEnd.getTimestamp());
			
		} else if (line.contains("SparkListenerApplicationStart")){
			EventApplicationStart msgAppStart = gson.fromJson(line, EventApplicationStart.class);
//			System.out.println("app start time: " + msgAppStart.getTimestamp());
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
			if(showIPSO) {
//				stages.put(stageInfo.getStageID(), new IPSOStage(stageInfo.getStageID(), stage));
				long stageID = stage.getID();
				if(stages.size() == 0 || !stages.containsKey(stageID)) {
					stages.put(stageID, new IPSOStage(stageID));
				}
				
				try {
					stages.get(stageID).addStageInfo(stage);
				} catch (InvalidAttributesException e) {
					// TODO Auto-generated catch block
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
			if(showTaskInfo)
				taskMetrics.taskInfo();
			
			/*Cashed the task info per-stage for ipso analysis*/
			if(showIPSO) {
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
			
		} 
			else {
			//catch other type msg here
		}
	}
	
	/**
	 * Get deserialization time from stage info
	 * @param stageInfo
	 * @return
	 */
	private static long getDeserTimeFromStageInfo(StageInfo stageInfo) {
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
	private static long getComptTimeFromStageInfo(StageInfo stageInfo) {
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
	private static long getjvmGCTime(StageInfo stageInfo) {
		long comptL = 0;
		List<Accumulable> accumulables = stageInfo.getAccumulables();
		for(Accumulable accum : accumulables) {
			if(accum.getName().equals("internal.metrics.jvmGCTime"))
				comptL = accum.getValue();
		}
		
		return comptL;
	}

}
