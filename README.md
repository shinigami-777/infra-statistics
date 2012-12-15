Jenkins Usage Statistics
========================

These scripts generate various data from existing census JSON files collected by the jenkins-ci.org infrastructure.
More specifically:

- http://stats.jenkins-ci.org/
- data used by the ['jenkins-plugin-info' confluence macro](https://github.com/jenkinsci/backend-jenkins-plugin-info-plugin)


HOWTO
-----

1. download the raw data (*.json.gz) from jenkins-ci.org

   $> groovy download.groovy [pwd]

2. generate the graphs
   ... you might have to increase the memory: JAVA_OPTS="-Xmx4000M"

   $> groovy generateStats.groovy

The final SVGs will be in target/svg


3. collect the data from the raw json format and store it into a local SQLight database
   
    $> groovy collectNumbers.groovy

4. generate the fine grained data set (json) for each plugin
   
    $> groovy createJson.groovy

The final json files will be in [worksapce]/target/stats - these files have to be uploaded to a webserver to make them available for the confluence plugin.


All the scripts can be reexecuted in case a failure happens, e.g. the download will only download the files he needs and collecting the numbers will only happen on the raw data which is not imported yet.
