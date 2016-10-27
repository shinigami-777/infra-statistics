/**
 * A metric instance for one instance
 */
class InstanceMetric {
    String instanceId
    String jenkinsVersion
    String servletContainer
    InstanceJVM jvm
    def plugins
    def jobTypes
    def nodesOnOs
    int totalExecutors
}

