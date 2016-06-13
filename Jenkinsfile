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
    docker.image('mongo:2').withRun('-p 27017:27017') { container ->
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
    echo 'Not publishing census just yet'

    stage 'Publish stats'
    echo 'Not publishing stats just yet'
}


/* previous freestyle config

    still to be done:

    git checkout gh-pages
    git config user.name `hostname`
    git config user.email &quot;no-reply@jenkins-ci.org&quot;
    cp -R target/svg/* svg/
    git add svg
    git commit -am &quot;generating stats&quot; || true
    git push git@github.com:jenkinsci/infra-statistics.git gh-pages
    git checkout master
*/
