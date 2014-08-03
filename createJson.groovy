@GrabConfig(systemClassLoader=true)
@Grab('org.xerial:sqlite-jdbc:3.7.2')
import org.sqlite.*
import java.sql.*
import java.util.zip.GZIPInputStream;

import groovy.xml.MarkupBuilder

class Generator {

    def db
    def statsDir

    def Generator(workingDir, db){
        this.db = db
        this.statsDir = new File(workingDir, "stats")
    }

    def generateInstallationsJson() {

        println "generating installations.json..."
        def installations = [:]
        db.eachRow("SELECT version, COUNT(*) AS number FROM jenkins WHERE month=(select MAX(month) FROM plugin) GROUP BY version;") {
            installations.put it.version, it.number
        }

        def json = new groovy.json.JsonBuilder()
        json.installations(installations)
        new File(statsDir, "installations.json") << groovy.json.JsonOutput.prettyPrint(json.toString())
    }


    def generatePluginsJson() {

        println "fetching plugin names..."
        def names = []
        // fetch all plugin names, excluding the private ones...
        db.eachRow("SELECT name FROM plugin WHERE name NOT LIKE 'privateplugin%' GROUP BY name ;") { names << it.name }
        println "found ${names.size()} plugins"

        def total = [:];
        db.eachRow("SELECT month, COUNT(*) AS number FROM jenkins GROUP BY month ORDER BY month ASC;") {
            total[it.month] = it.number;
        }

        names.each{ name ->
            def month2number = [:]
            def month2percentage = [:]
            def file = new File(statsDir, "${name}.stats.json")
            // fetch the number of installations per plugin per month
            db.eachRow("SELECT month, COUNT(*) AS number FROM plugin WHERE name = $name GROUP BY month ORDER BY month ASC;") {
                month2number.put it.month, it.number
                month2percentage[it.month] = (it.number as float)*100/(total[it.month] as float)
            }
            def json = new groovy.json.JsonBuilder()
            json name:name, installations:month2number, installationsPercentage:month2percentage
            file << groovy.json.JsonOutput.prettyPrint(json.toString())
            println "wrote: $file.absolutePath"
        }

        names.each { name ->
            def version2number = [:]
            def version2percentage = [:]
            def file = new File(statsDir, "${name}.versions.json")
            // fetch the number of installations per plugin version this month
            db.eachRow("SELECT COUNT(*) AS number, version, month FROM plugin WHERE name = $name AND month = (SELECT MAX(month) FROM plugin) GROUP BY version") {
                version2number.put it.version, it.number
                version2percentage[it.version] = (it.number as float)*100/(total[it.month] as float)
            }
            def json = new groovy.json.JsonBuilder()
            json name:name, installations:version2number, installationsPercentage:version2percentage
            file << groovy.json.JsonOutput.prettyPrint(json.toString())
            println "wrote: $file.absolutePath"
        }
    }

    def generateLatestNumbersJson() {
        println "generating latestNumbers.json..."
        def plugins = [:]
        def latestMonth;
        db.eachRow("SELECT name, COUNT(*) AS number, month FROM plugin WHERE month=(select MAX(month) FROM plugin) AND name NOT LIKE 'privateplugin%' GROUP BY name, month;"){
            plugins.put it.name, it.number
            latestMonth = it.month // ok, this is probably not the nicest way, but the month is realy the same for all the numbers anyway
        }
        def json = new groovy.json.JsonBuilder()
        json month:latestMonth, plugins:plugins
        new File(statsDir, "latestNumbers.json") << groovy.json.JsonOutput.prettyPrint(json.toString())
    }


    // like installations.json, but cumulative descending: number indicates number of installations of given version or higher
    def generateCapabilitiesJson() {
        println "generating capabilities.json..."
        def installations = [:]
        def higherCap = 0
        db.eachRow("SELECT version, COUNT(*) AS number FROM jenkins WHERE month=(select MAX(month) FROM jenkins) AND version LIKE '1.%' GROUP BY version ORDER BY version DESC;") {
            installations.put it.version, it.number + higherCap
            higherCap += it.number
        }

        def json = new groovy.json.JsonBuilder()
        json.installations(installations)
        new File(statsDir, "capabilities.json") << groovy.json.JsonOutput.prettyPrint(json.toString())
    }

    def run() {

        // clean the stats directory
        statsDir.deleteDir()
        statsDir.mkdirs()

        generateCapabilitiesJson()
        generateInstallationsJson()
        generateLatestNumbersJson()
        generatePluginsJson()

    }
}


def workingDir = new File("target")
def db = DBHelper.setupDB(workingDir)
new Generator(workingDir, db).run()





