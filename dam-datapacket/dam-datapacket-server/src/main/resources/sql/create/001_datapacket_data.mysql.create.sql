 [/*data.001_DATAPACKET_DATA.check*/]

INSERT INTO tm_model_tree (ID,SECRET_CODE,TENANT_ID,CREATE_DEPT,CREATE_TIME,CREATE_USER,MODIFY_DEPT,MODIFY_TIME,MODIFY_USER,NODE_NAME,PID) VALUES
	 ('02841EC4A322430588087532970B1580',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'分系统','-1'),
	 ('02841EC4A322430588087532970B1581',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'模块','-1'),
	 ('02841EC4A322430588087532970B1582',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'单机','-1'),
	 ('02841EC4A322430588087532970B1583',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'运载','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1584',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'武器','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1585',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'卫星','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1586',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'载人航天','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1587',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'深空探测','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1588',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'电推进','02841EC4A322430588087532970B1580'),
	 ('02841EC4A322430588087532970B1589',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'运载','02841EC4A322430588087532970B1581');
INSERT INTO tm_model_tree (ID,SECRET_CODE,TENANT_ID,CREATE_DEPT,CREATE_TIME,CREATE_USER,MODIFY_DEPT,MODIFY_TIME,MODIFY_USER,NODE_NAME,PID) VALUES
	 ('02841EC4A322430588087532970B1590',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'武器','02841EC4A322430588087532970B1581'),
	 ('02841EC4A322430588087532970B1591',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'卫星','02841EC4A322430588087532970B1581'),
	 ('02841EC4A322430588087532970B1592',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'载人航天','02841EC4A322430588087532970B1581'),
	 ('02841EC4A322430588087532970B1593',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'深空探测','02841EC4A322430588087532970B1581'),
	 ('02841EC4A322430588087532970B1594',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'电推进','02841EC4A322430588087532970B1581'),
	 ('02841EC4A322430588087532970B1595',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'发动机','02841EC4A322430588087532970B1582'),
	 ('02841EC4A322430588087532970B1596',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'阀门','02841EC4A322430588087532970B1582'),
	 ('02841EC4A322430588087532970B1597',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'容器','02841EC4A322430588087532970B1582'),
	 ('02841EC4A322430588087532970B1598',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'机电','02841EC4A322430588087532970B1582');
insert into PF_SCHEMA_HISTORY(ID,DESCRIPTION,VERSION) values('332C0FE466394863B2701261ESCSDEGO','001_DATAPACKET_DATA','001');