# Zaloni ZDP

Deployment Name = trivadiszdp 
Deployment Location = westeurope

* ZDP URL: <http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:9090> 
* Ambari URL: <http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:8080> 


To login to the Azure VM provisioned before:

```
ssh zaloni@zdp502trivadiszdp.westeurope.cloudapp.azure.com
```

Start ZDP services

```
cd startup-scripts
./start-zdp.sh
```

After you are in the azure vm shell you would have to ssh again for ZDP using the following command. 

```
ssh root@sandbox-hdp
```

## Open Issues / Questions
1. Unable to Ingest a JSON file through the wizard
2. File Wizard can only create a new Entity?
3. No Lineage shown in File Wizard Ingestion?
2. Stream Ingestion is not "Entity-based"?
2. Sink in a Stream Ingestion can only be SequenceFile?
3. Database Ingest fails with the following error ([Logfile](http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:9090/bedrock-app/services/rest/workflow/viewLog/DB_wiz_ingest_wf_1546624281926/28/DB_wiz_ingest_wf_1546624281926-28-INFO.log))

	```
INFO: Running Sqoop version: 1.4.6.2.6.4.0-91
Jan 04, 2019 6:44:14 PM org.apache.sqoop.tool.BaseSqoopTool applyCredentialsOptions
WARNING: Setting your password on the command-line is insecure. Consider using -P instead.
Jan 04, 2019 6:44:14 PM org.apache.sqoop.tool.BaseSqoopTool validateOutputFormatOptions
INFO: Using Hive-specific delimiters for output. You can override
Jan 04, 2019 6:44:14 PM org.apache.sqoop.tool.BaseSqoopTool validateOutputFormatOptions
INFO: delimiters with --fields-terminated-by, etc.
Jan 04, 2019 6:44:15 PM org.apache.sqoop.manager.MySQLManager initOptionDefaults
INFO: Preparing to use a MySQL streaming resultset.
Jan 04, 2019 6:44:16 PM org.apache.sqoop.tool.CodeGenTool generateORM
INFO: Beginning code generation
Jan 04, 2019 6:44:16 PM org.apache.sqoop.manager.SqlManager execute
INFO: Executing SQL statement: SELECT t.* FROM `order_t` AS t LIMIT 1
Jan 04, 2019 6:44:16 PM org.apache.sqoop.util.LoggingUtils logAll
SEVERE: Error reading from database: java.sql.SQLException: Streaming result set com.mysql.jdbc.RowDataDynamic@d86a6f is still active. No statements may be issued when any streaming result sets are open and in use on a given connection. Ensure that you have called .close() on any active streaming result sets before attempting more queries.
java.sql.SQLException: Streaming result set com.mysql.jdbc.RowDataDynamic@d86a6f is still active. No statements may be issued when any streaming result sets are open and in use on a given connection. Ensure that you have called .close() on any active streaming result sets before attempting more queries.
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:934)
	at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:931)
	at com.mysql.jdbc.MysqlIO.checkForOutstandingStreamingData(MysqlIO.java:2735)
	at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:1899)
	at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2151)
	at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2619)
	at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2569)
	at com.mysql.jdbc.StatementImpl.executeQuery(StatementImpl.java:1524)
	at com.mysql.jdbc.ConnectionImpl.getMaxBytesPerChar(ConnectionImpl.java:3003)
	at com.mysql.jdbc.Field.getMaxBytesPerCharacter(Field.java:602)
	at com.mysql.jdbc.ResultSetMetaData.getPrecision(ResultSetMetaData.java:445)
	at org.apache.sqoop.manager.SqlManager.getColumnInfoForRawQuery(SqlManager.java:305)
	at org.apache.sqoop.manager.SqlManager.getColumnTypesForRawQuery(SqlManager.java:260)
	at org.apache.sqoop.manager.SqlManager.getColumnTypes(SqlManager.java:246)
	at org.apache.sqoop.manager.ConnManager.getColumnTypes(ConnManager.java:328)
	at org.apache.sqoop.orm.ClassWriter.getColumnTypes(ClassWriter.java:1853)
	at org.apache.sqoop.orm.ClassWriter.generate(ClassWriter.java:1653)
	at org.apache.sqoop.tool.CodeGenTool.generateORM(CodeGenTool.java:107)
	at org.apache.sqoop.tool.ImportTool.importTable(ImportTool.java:488)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableUsingSqoop(ZDPImportTool.java:465)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableWithoutImpersonation(ZDPImportTool.java:292)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableWithImpersonationSupport(ZDPImportTool.java:261)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.run(ZDPImportTool.java:143)
	at org.apache.sqoop.Sqoop.run(Sqoop.java:147)
	at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:76)
	at org.apache.sqoop.Sqoop.runSqoop(Sqoop.java:183)
	at org.apache.sqoop.Sqoop.runTool(Sqoop.java:225)
	at org.apache.sqoop.Sqoop.runTool(Sqoop.java:234)
	at org.apache.sqoop.Sqoop.main(Sqoop.java:243)
Jan 04, 2019 6:44:16 PM com.zaloni.bedrock.sqoop.plugin.ZDPImportTool run
SEVERE: Import Failed: 
Error occurred while writing data to HDFS or while initialising Hive Import:
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableWithImpersonationSupport(ZDPImportTool.java:267)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.run(ZDPImportTool.java:143)
	at org.apache.sqoop.Sqoop.run(Sqoop.java:147)
	at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:76)
	at org.apache.sqoop.Sqoop.runSqoop(Sqoop.java:183)
	at org.apache.sqoop.Sqoop.runTool(Sqoop.java:225)
	at org.apache.sqoop.Sqoop.runTool(Sqoop.java:234)
	at org.apache.sqoop.Sqoop.main(Sqoop.java:243)
Caused by: java.io.IOException: No columns to generate for ClassWriter
	at org.apache.sqoop.orm.ClassWriter.generate(ClassWriter.java:1659)
	at org.apache.sqoop.tool.CodeGenTool.generateORM(CodeGenTool.java:107)
	at org.apache.sqoop.tool.ImportTool.importTable(ImportTool.java:488)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableUsingSqoop(ZDPImportTool.java:465)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableWithoutImpersonation(ZDPImportTool.java:292)
	at com.zaloni.bedrock.sqoop.plugin.ZDPImportTool.importTableWithImpersonationSupport(ZDPImportTool.java:261)
	... 7 more

```
	
4. Error in File Wizard Ingest from WASB (<http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:9090/bedrock-app/services/rest/workflow/viewLog/__parent_ingest_wiz_wf_3/22/__parent_ingest_wiz_wf_3-22-DEBUG.log>):

