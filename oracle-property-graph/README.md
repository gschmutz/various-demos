cd dockerfiles/18.4.0

## Express Edition 18.4.0

Build the Oracle XE docker container 

```
./buildDockerImage.sh -v 18.4.0 -x -i
```

Run an instance using the following command

```
docker run --name xe \
		-p 1521:1521 -p 5500:5500 \
		-e ORACLE_PWD=manager \
		-e ORACLE_CHARACTERSET=AL32UTF8 \
		-v ./work/docker/db_setup_scripts:/opt/oracle/scripts/setup \
		oracle/database:18.4.0-xe
```

```
docker run --name xe \
		-p 1521:1521 -p 5500:5500 \
		-e ORACLE_PWD=manager \
		-e ORACLE_CHARACTERSET=AL32UTF8 \
		oracle/database:18.4.0-xe
```

## Standard Edition 18.3.0

```
./buildDockerImage.sh -v 18.3.0 -s -i
```

```
docker run --name orcl \
-p 1521:1521 -p 5500:5500 \
-e ORACLE_SID=ORCLCDB \
-e ORACLE_PDB=ORCLPDB1 \
-e ORACLE_PWD=manager \
-e ORACLE_CHARACTERSET=AL32UTF8 \
oracle/database:18.3.0-se2
```

## Enterprise Edition 18.3.0

```
./buildDockerImage.sh -v 18.3.0 -e -i
```

```
docker run --name orcl \
-p 1521:1521 -p 5500:5500 \
-e ORACLE_SID=ORCLCDB \
-e ORACLE_PDB=ORCLPDB1 \
-e ORACLE_PWD=manager \
-e ORACLE_CHARACTERSET=AL32UTF8 \
-v ${PWD}/db_setup_scripts:/opt/oracle/scripts/setup \
oracle/database:18.3.0-ee
```

```
alter session set container=ORCLPDB1;
```

```
CREATE USER scott IDENTIFIED BY tiger DEFAULT TABLESPACE users TEMPORARY TABLESPACE temp;
GRANT CONNECT, RESOURCE TO scott;
GRANT UNLIMITED TABLESPACE TO scott;
```

```
sqlplus scott/tiger@ORCLPDB1
```

```
BEGIN
    OPG_APIS.CREATE_PG('gt', 4, 8, 'users');
END;
/
```


```
cd /opt/oracle/product/18c/dbhome_1/demo/schema/human_resources
sqlplus / as sysdba
```

```
alter session set container=ORCLPDB1;
```

```
@hr_main.sql
```


```
cd /opt/oracle/product/18c/dbhome_1/demo/schema/human_resources
sqlplus / as sysdba
```

```
@oe_main.sql
```


```
declare
	cursor tabs is select table_name from dba_tables where owner ='HR';
	sqlstr varchar(100);
begin

	for tab in tabs
	loop
	    sqlstr:='grant select on hr.'||tab.table_name||' to scott';
	    execute immediate sqlstr;
	end loop;
end;
/
```

Create a view from HR sample schema employees table that has attributes

```
create or replace view  employees 
as 
select e.employee_id,e.first_name||' '||e.last_name as
 	full_name,d.department_name,e.salary,e2.first_name||' '||e2.last_name as 	manager_name,e.hire_date 
	,j.job_title,e.manager_id
from hr.employees e
left outer join hr.employees e2 on e2.employee_id =e.manager_id
join hr.jobs j on j.job_id=e.job_id
join hr.departments d on  d.department_id=e.department_id
where e.department_id is not null order by e.manager_id;
```

```
create or replace view employeeRelation as
select to_number(to_char(e.manager_id)||to_char(e.employee_id)) as relationID,
e.manager_id as source,
e.employee_id as destination,
'manage' as relationType,
to_date(e.hire_date) as hire_date,
e.manager_name
from employees e
union all
select relationID,
emp1,
emp2,
relationType,
to_date(hire_date) as hire_date ,
manager_name
from(
select to_number(to_char(a.employee_id)||to_char(b.employee_id)) as relationID,a.employee_id as emp1, b.employee_id as emp2,
'colleague' as relationType,case when a.hire_date>b.hire_date then a.hire_date else b.hire_date end as hire_date 
,a.manager_name
from employees a
join hr.employees b on b.manager_id=a.manager_id and a.employee_id<>b.employee_id --and a.employee_id<b.employee_id
order by a.employee_id);
```

## Working with Groovy

```
docker exec -ti orcl bash
```

Start the Groovy Shell
```
cd /opt/oracle/product/18c/dbhome_1/md/property_graph/dal/groovy
./gremlin-opg-rdbms.sh
```

Connect to the database

```
cfg = GraphConfigBuilder.forPropertyGraphRdbms().
   setJdbcUrl("jdbc:oracle:thin:@localhost:1521/ORCLPDB1").
   setUsername("scott").
	setPassword("tiger").
	setName("test").
	setMaxNumConnections(8).
	setLoadEdgeLabel(false).
	addVertexProperty("name", PropertyType.STRING, "default_name").
	addEdgeProperty("cost", PropertyType.DOUBLE, "1000000").
	build();
```

```
opg = OraclePropertyGraph.getInstance(cfg);
```

Start from scratch
```
opg.clearRepository();
opgdl=OraclePropertyGraphDataLoader.getInstance();
```

```
vfile="../../data/connections.opv"
efile="../../data/connections.ope" 
```

```
opgdl.loadData(opg, vfile, efile, 2,
10000, true, null);
```

## Python

```
pip install JPype1
```
