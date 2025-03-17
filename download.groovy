#!/usr/bin/env groovy
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder

@Grapes([
    @Grab(group='org.codehaus.jackson', module='jackson-mapper-asl', version='1.9.13'),
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2'),
    @Grab(group='org.apache.ant', module='ant', version='1.8.1'),
    @Grab(group='org.xerial', module='sqlite-jdbc', version='3.16.1'),
    @GrabExclude('xml-apis:xml-apis'),
    @GrabConfig(systemClassLoader=true)
])


class Downloader {
    def authUrl = "http://jenkins-ci.org"

    def db
    def workingDir

    Downloader(workingDir, db){
        this.db = db
        this.workingDir = workingDir
    }

    // gets all compressed JSON files from jenkins-ci

    void getFiles(username, pwd) {

        println("get files from $authUrl")
        def site = new HTTPBuilder(authUrl)
        def userAuth = "$username:$pwd".toString()
        def authHeaders = "Basic ${userAuth.bytes.encodeBase64()}"

        try {
            def doc = site.get(path: '/census/', headers: [Authorization: authHeaders])

            if (doc.toString().contains("308 Permanent Redirect")) {
                println("Error: Incorrect password provided. Access denied.")
                return
            }

            doc.depthFirst().collect { it }.findAll {
                it.name() == "A"
            }.each {
                def fileName = it.attributes()["href"]
                if (fileName.endsWith(".json.gz")) {
                    handleFileDownload(site, fileName, authHeaders)
                }
            }
        } catch (Exception e) {
            println ("Error: Unable to fetch files - ${e.message}")
        }
    }

    private void handleFileDownload(HTTPBuilder site, String filename, String authHeaders){

        if(!DBHelper.doImport(db,filename)){
            println ("ignore $fileName (already imported)")
        }

        File targetArchive = new File(workingDir, filename)
        if(targetArchive.exists()){
            println ("ignore $fileName (already exists)")
        }

        def fileUrl = '/census/'+fileName
        println ("download $fileUrl")
        File tmp = new File(workingDir,fileName+".tmp")
        tmp << site.get(contentType: ContentType.BINARY, path: fileUrl, headers:[Authorization:authHeaders] )
        tmp.renameTo(targetArchive)

    }

    def run(args) {
        if(args.size() != 1){
            println "no password for $authUrl given..."
        }else{
            def username = "jenkins"
            getFiles(username, args[0])
        }
    }
}

def workingDir = new File("target")
workingDir.mkdirs()
def db = DBHelper.setupDB(workingDir)
new Downloader(workingDir, db).run(this.args)
