CREATE TABLE 'pollution_values' (
'gas1_value' double NOT NULL DEFAULT '0',
'gas2_value' double NOT NULL DEFAULT '0',
'gas3_value' double NOT NULL DEFAULT '0',
'time_value' datetime NOT NULL DEFAULT '2000-12-31 23:59:59',
'location_lat_value' double NOT NULL DEFAULT '0',
'location_lng_value' double NOT NULL DEFAULT '-0',
'ID' int NOT NULL AUTO_INCREMENT,
PRIMARY KEY (ID) 
) ENGINE=MyISAM DEFAULT CHARSET=latin1 
