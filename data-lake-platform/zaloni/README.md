# Zaloni ZDP

## Setup

Deployment Name = trivadiszdp 
Deployment Location = westeurope

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

## Using Zaloni

* ZDP URL: <http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:9090> 
* Ambari URL: <http://zdp502trivadiszdp.westeurope.cloudapp.azure.com:8080> 
* <http://sandbox-hdp.hortonworks.com:18081/>


## Open Issues / Questions

### Wizard File Ingest

2. File Wizard can only create a new Entity? 
3. No Lineage shown in File Wizard Ingestion?

### Manual File Ingest

3. If using the Manual File Ingest, I have to create the Entity manually first? What is the correct approach to create an Entity, when using the Manul Ingestion
4. What is the purpose of Ingesting without an Entity? Does it make sense?
5. Can I manually ingest from let's say CSV and directly store it as Parquet? Transformation while ingesting? 

### Stream Ingestion

1. Stream Ingestion is through Flume? How to configure it?
2. Stream Ingestion is not "Entity-based"?
2. Sink in a Stream Ingestion can only be SequenceFile?

### Entity Definition

6. Why is an HCatalog_Table Name not changeable on the Entity Technical Information?
