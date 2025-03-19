import groovy.sql.Sql

class DBHelper {
    private static final String DB_NAME = "stats.db"

    static Sql setupDB(workingDir) {
        def dbFile = new File(workingDir, DB_NAME)
        boolean dbExists = dbFile.exists()

        Sql db = Sql.newInstance("jdbc:sqlite:" + dbFile.absolutePath, "org.sqlite.JDBC")
        if (!dbExists) {
            createTables(db)
        }
        else{
            println("Database does not exist")
        }

        return db
    }

    // Create tables if database exists
    private static void createTables(Sql db) {

        db.execute("create table jenkins(instanceid, month, version, jvmvendor, jvmname, jvmversion)")
        db.execute("create table plugin(instanceid, month, name, version)")
        db.execute("create table job(instanceid, month, type, jobnumber)")
        db.execute("create table node(instanceid, month, osname, nodenumber)")
        db.execute("create table executor(instanceid, month, numberofexecutors)")
        db.execute("create table importedfile(name)")
        db.execute("CREATE INDEX plugin_name on plugin (name)")
        db.execute("CREATE INDEX jenkins_version on jenkins (version)")
        db.execute("CREATE INDEX plugin_month on plugin (month)")
        db.execute("CREATE INDEX plugin_namemonth on plugin (name,month)")
    }

    // Check if the file with the given name already imported?
    static boolean doImport(db, fileName){
        if(db){
            def filePrefix = fileName.substring(0, fileName.indexOf("."))+"%"
            def rows = db.rows("select name from importedfile where name like $filePrefix;")
            return rows.size() == 0
        }
        true
    }
}
