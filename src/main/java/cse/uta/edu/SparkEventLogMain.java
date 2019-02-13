/**
 * 
 */
package cse.uta.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cse.uta.edu.IPSO.SparkEventLogInterpreter;
import cse.uta.edu.Utils.IPSOConfig;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author ruby
 * 
 *         Run this tool to read and parse Spark 2.x event log (obtain form
 *         Spark History Web UI)
 *
 */
public class SparkEventLogMain {
	private static final Logger LOG = Logger.getLogger(SparkEventLogMain.class);

    //===============================
    //       Entry.
    //================================
	public static void main(String[] args) {
		BasicConfigurator.configure();

        Options options = new Options();
        Option opt = new Option("c",true, "Configuration file (required) is missing");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        SparkEventLogInterpreter interpreter = new SparkEventLogInterpreter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("c");
            Configuration conf = new PropertiesConfiguration(configFile);

            Level logLevel = Level.toLevel(conf.getString(IPSOConfig.LOG_LEVEL, ""),
                    IPSOConfig.DEFAULT_LOG_LEVEL);
            Logger.getRootLogger().setLevel(logLevel);

            interpreter.init(conf);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "java IPSO-1.0-SNAPSHOT.jar", options);
        } catch (ConfigurationException e) {
            System.out.println("Configuration file is not found at the path.");
            e.printStackTrace();
        }

		/* If use as batch job processing */
		if(interpreter.isBatch()) {
            LOG.info("Batch job process enabled, analyzing Spark log files within all of the sub-directories under given path.");

			String batchConfig = interpreter.getBatchConfigPath();
			List<String> list = new ArrayList<>();

			try (Stream<String> stream = Files.lines(Paths.get(batchConfig))) {

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
				interpreter.analyzeLogsForExprSets(Integer.valueOf(pairs[0]), Integer.valueOf(pairs[1]));
			}

			//second iteration output IPSO info
			if(interpreter.isIPSO()) {
                for(int j = 0; j < list.size(); j++){
                    String[] pairs = list.get(j).split("\t");
                    interpreter.calcIPSO(Integer.valueOf(pairs[0]), Integer.valueOf(pairs[1]));
                }
                interpreter.outputIPSO();
            }
		}
		else {// single use processing the log for one specific experiment set (N, m)
            LOG.info("Batch job process disabled, analyzing Spark log files passed by the single directory.");
            interpreter.analyzeLogsForExprSets();
        }
	}

}
