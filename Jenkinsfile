#!/usr/bin/env groovy

/*
 * Definition of the build/processing of Jenkins project usage statistics
 * <https://wiki.jenkins-ci.org/display/JENKINS/Usage+Statistics>
 */

final String JAVA_TOOL   = 'jdk8'
final String GROOVY_TOOL = 'groovy'
final String USAGE_HOST  = 'usage.jenkins.io'
final String CENSUS_HOST = 'census.jenkins.io'


/* `census` is a node label for a single machine, ideally, which will be
 * consistently used for processing usage statistics and generating census data
 */
node('census && docker') {
    /* grab our code from source control */
    checkout scm

    String javaHome = tool(name: 'jdk8')
    String groovyHome = tool(name: 'groovy')

    List<String> customEnv = [
        "PATH+JDK=${javaHome}/bin",
        "PATH+GROOVY=${groovyHome}/bin",
        "JAVA_HOME=${javaHome}",
    ]

    final String usagestats_dir = './usage-stats'
    final String census_dir = './census'

    stage 'Sync raw data and census files'
    sh "rsync -avz ${USAGE_HOST}:/srv/usage/usage-stats ."
    sh "rsync -avz ${CENSUS_HOST}:/srv/census/census ."


    stage 'Process raw logs'
    // Nuke and recreate the Mongo data directory.
    sh "rm -rf mongo-data"
    sh "mkdir -p mongo-data"

    // Use the Mongo data directory in the workspace.
    docker.image('mongo:2').withRun('-p 27017:27017 -v ' + pwd() + "/mongo-data:/data/db") { container ->
        withEnv(customEnv) {
            sh "groovy parseUsage.groovy --logs ${usagestats_dir} --output ${census_dir} --incremental"
        }
    }

    stage 'Generate census data'
    withEnv(customEnv) {
        sh 'mkdir -p target'
        sh "groovy collectNumbers.groovy ${census_dir}/*.json.gz"
        sh 'groovy createJson.groovy'
    }

    stage 'Generate stats'
    withEnv(customEnv) {
        sh "groovy generateStats.groovy ${census_dir}/*.json.gz"
    }

    stage 'Publish census'
    sh "rsync -avz ${census_dir} ${CENSUS_HOST}:/srv/census"

    stage 'Publish stats'
    try {
        sh './publish-svgs.sh'
    }
    finally {
        sh 'git checkout master'
    }
}
