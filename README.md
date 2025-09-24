# IPSO-beta

## A Light-weighted Spark Log Analyzer for Performance

[IPSO](https://mentis.uta.edu/explore/profile/hao-che) (In-Proportion and Scale-Out-induced scaling model) is a simple speedup model that extends the traditional speedup laws (Amdahl's Law/Gustafson's Law) in the modern big data analytics era. It's built on top of the traditional parallel/sequential execution model and illustrate the whole solution space of data-intensive applications in three dimensions: in-proportional horizontal or vertical factors and scale-out-induced factors.

This tool (beta) implements the IPSO model on Apache [Spark](https://spark.apache.org/) platform and provides basic features including:
1. Parse the Spark 2.x version log files to get the data-intensive application execution latencies in a granularity of stages/tasks;
2. Configurable features to process the execution model base on IPSO to obtain three important scaling-factors for the application;
3. Configurable features to write-out performance metrics for further analysis.


## Installation

IPSO requires the following software packages for development:
* JDK 8+
* [Maven](https://maven.apache.org/).

> Now support Java 21 Runtime


## Getting started

Please create a configuration file to turn on/off certain features of IPSO before usage (example as following):

### IPSO.conf

    # IPSO Configuration file example

    # Input path
    ## 1) Uncomment if Batch feature is turned off: Path to Spark log files
    # log.root.dir=<Path_to_log>

    ## 2) Uncomment if Batch feature is on, Path to the root folder
    # log.root.dir=<Path_to_root>

    # Boolean flags to indicate whether or not printing out logger information
    # for stages
    show.stage.info=false
    # for tasks
    show.tasks.info=false

    # If set, the IPSO will take only the first file within the directory for analysis (default by true)
    quick.analysis=true

    # IPSO featured performance analysis
    ipso.analysis=true
    # If using IPSO feature, the output file path must be set to hold temperate and permanent output 
    output.dir=<Path_output>

    # Batch job processing (processing Spark log files within all sub-directories of given directory)
    batch.process=true
    # Set the path to your batch configuraiton file (for exepriment sets' details)
    batch.conf.path=<Path_to_batch_conf>

After creating the configuration file run IPSO with the following command (replace IPSO_JAR and CONF_FILE with their corresponding paths):

    $ java -cp IPSO_JAR cse.uta.edu.SparkEventLogMain -c CONF_FILE

Contact
-------
- Zhongwei Li <zhongwei.li@mavs.uta.edu>
- Feng Duan <feng.duan@mavs.uta.edu>