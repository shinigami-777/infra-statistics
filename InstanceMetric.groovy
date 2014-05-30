/**
 * A metric instance for one instance
 */
class InstanceMetric {
    String instanceId
    String jenkinsVersion
    String servletContainer
    def plugins
    def jobTypes
    def nodesOnOs
    int totalExecutors
}
