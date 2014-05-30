/*
    Analyze which version of Jenkins is deployed with which servlet container.
 */
@Grapes([
    @Grab(group='org.jenkins-ci', module='version-number', version='1.1'),
    @Grab(group='org.codehaus.jackson', module='jackson-mapper-asl', version='1.9.3')
])
import hudson.util.VersionNumber;

def servlets = [:];

args.each { arg ->
    new JenkinsMetricParser().forEachInstance(new File(arg)) { InstanceMetric i ->
        if (new VersionNumber(i.jenkinsVersion) >= new VersionNumber("1.554")) {
            def servlet = normalize(i.servletContainer);
            def v = servlets[servlet] ?: 0;
            v++;
            servlets[servlet] = v;
        }
    }
}

class Row {
    String name;
    int count;
}

def all = 0;
def rows = servlets.collect { k,v -> new Row(name:k, count:v) }
rows.sort { a,b -> a.count-b.count };
rows.each { r ->
    println "${r.count}\t${r.name}"
    all += r.count;
}
println all;

def normalize(String n) {
    def prefixes = [
        "Apache Tomcat/5",
        "Apache Tomcat/6",
        "Apache Tomcat/7",
        "Apache Tomcat/8",
        "jetty-6",
        "jetty-7",
        "jetty-8",
        "jetty-9",
        "jetty/6",
        "jetty/7",
        "jetty/8",
        "jetty/9",
    ]
    if (n==null)    return null;
    for (String prefix : prefixes) {
        if (n.startsWith(prefix))
            return prefix;
    }
    if (n.length()>64)  n=n.substring(0,64)
    return n;
}