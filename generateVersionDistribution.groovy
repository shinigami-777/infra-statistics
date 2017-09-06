#!/usr/bin/env groovy
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

File file = new File("target/stats", "jenkins-version-per-plugin-version.json")

def json = new JsonSlurper().parseText(file.text)."jenkins-version-per-plugin-version";

json.each { plugin, versions ->
    File out = new File("target/pluginversions", plugin + ".html")
    out.parentFile.mkdirs()
    out.text = new File("generateVersionDistribution-template.html").text.replace("__NAME__", plugin).replace("__DATA__", new JsonBuilder(versions).toString())
}

"./generate-pluginversions-index.sh target/pluginsversions".execute()