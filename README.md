<p align="center">
	<img src="http://www.efsa.europa.eu/profiles/efsa/themes/responsive_efsa/logo.png" alt="European Food Safety Authority"/>
</p>

# EFSA Report Creator Library
This is a library which provides several useful functionalities that can be used to create and send reports. In particular, it provides
with an user interface with a table structure, which is based on an external configuration file (tablesSchema.xlsx).
In particular, the library contains also several procedures to manage the complex communication process with the EFSA data collection framework (DCF).
More precisely, it is possible to send/submit/reject/amend a report and to see its acknowledgment when needed.

## Dependencies
All project dependencies are listed in the [pom.xml](pom.xml) file.

## Import the project
In order to import the project correctly into the integrated development environment (e.g. Eclipse), it is necessary to download the project together with all its dependencies.
The project and all its dependencies are based on the concept of "project object model" and hence Apache Maven is used for the specific purpose.
In order to correctly import the project into the IDE it is firstly required to download or clone all the required dependencies as stated in the list below: 

	<dependencies>
		<module>email-generator</module>
		<module>dcf-webservice-framework</module>
		<module>exceptions-manager</module>
		<module>http-manager</module>
		<module>progress-bar</module>
		<module>sql-script-executor</module>
		<module>version-manager</module>
		<module>window-size-save-restore</module>
	</dependencies>
	
Next, extract all the zip packages inside your workspace. At this stage you can simply open the IDE and import all projects available in the workspace.

_Please note that Maven is required in order to download the libraries required by the TSE tool._

_Please note that the "SWT (swt_3.7.1.v3738a.jar)" and the "Jface (org.eclipse.jface_3.7.0.I20110522-1430.jar)" libraries must be downloaded and added to the project's build path._

## Documentation
### User interface package
The EFSA-RCL contains a package to easily create a configurable user interface. In particular, it is possible to generate tables to collect data (similar to excel spreadsheets) according to the configuration. In particular, the file config/tablesSchema.xlsx must contain the structure of every RCL table which should be created.

### Table configuration
Every sheet of the excel file represents a table (excluding the Relations, Tables and MessageConfig sheets, which are exceptions). Every table sheet shall contain the list of fields which will be rendered in the user interface (i.e. the rows of the excel file, every row will create a new column in the UI). In particular, it is necessary to specify several properties of each field in order to be created and rendered correctly. These properties are specified inside the excel file by the following columns:

* id: identifier of the column, this should be unique inside the same sheet since it will be used as primary key
* code: code of the column (not used)
* xmlTag: tag which will be used for the field for creating the exported .xml file containing the table data
* label: text which will be displayed as column name in the user interface
* tip: text which will be displayed as advice when the mouse cursor is left for few seconds on the column name
* type: type of the column, possible values:
	* picklist, catalogue field composed of code + label
	* string, character field
	* u_integer, integer field >= 0 (negative values forbidden)
	* foreignKey, for sql foreign keys which are needed in the table
* Controlled terminology: name of the catalogue related to the picklist (not used)
* mandatory: (yes/no) it indicates if the column is mandatory or not for the data collection
* editable: (yes/no) it indicates if the column can be edited or not by the user
* visible: (yes/no) it indicates if the column should be rendered in the user interface or not, this is useful for automated fields 
