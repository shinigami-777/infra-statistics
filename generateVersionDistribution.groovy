#!/usr/bin/env groovy
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

Path indir = Paths.get("pluginversions-static")
Path outdir = Paths.get("target/pluginversions")

Files.createDirectories(outdir)

Files.list(indir).each { file ->
    Files.copy(file, outdir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING)
}

File file = new File("target/stats", "jenkins-version-per-plugin-version.json")

def json = new JsonSlurper().parse(file)."jenkins-version-per-plugin-version";

def htmlTemplate = new File("generateVersionDistribution-template.html").text
json.each { plugin, versions ->
    File out = new File(outdir.toFile(), plugin + ".html")

    out.text = htmlTemplate.replaceAll("__NAME__", plugin).replace("__DATA__", new JsonBuilder(versions).toString())
}

"./generate-pluginversions-index.sh target/pluginversions".execute()
