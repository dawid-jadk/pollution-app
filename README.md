#pollution-app

Pollution data visualisation application built for University project.
Android application fetches pollution information from the server, which was stored by the pollution sensing device built using Arduino board. The purpose of this application was to represents how certain areas of London are polluted. The data is represented in terms of numbers and simple graphs allowing to compare how pollution has changed. The use of geographical location stored by the device allows the mobile application users to retrieve the most relevant pollution information closest to them. Additional home screen widget delivers fast access to important pollution information such as alerts when pollution levels are high. The application has been designed to work with only few of London boroughs, although it can be redesigned easily to work with more places. 

## Installation

A hosting is needed to deploy required PHP files which are used by the application to grab the pollution data.
Please upload PHP files to your server (files from php_files folder).
Set up the MySQL database on your server and edit the database connection fields in all the PHP files according to your database connection details. Upload the sql/create_table.sql to create the required table in your database.
Lastly edit the hostname of where the PHP files are located (helpers/Constants.java)

## Usage

Compile with Android Studio
Run on Android devices ( minSdkVersion 9 )

## Screenshots

![](/screenshots/1.jpg?raw=false)
![](/screenshots/2.jpg?raw=false)
![](/screenshots/3.jpg?raw=false)
![](/screenshots/4.jpg?raw=false)
![](/screenshots/5.jpg?raw=false)
![](/screenshots/6.jpg?raw=false)

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## History

TODO: Write history

## Acknowledgments

1. square for retrofit 2
2. ddanny for AChartEngine
3. PhilJay for MPAndroidChart
4. GmapGIS for an easy tool to create polygonic areas

## License
TODO: Write license
