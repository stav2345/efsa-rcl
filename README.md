
# EFSA Report Creator Library
This is a library which provides several useful functionalities that can be used to create and send reports. In particular, it provides
with an user interface with a table structure, which is based on an external configuration file (tablesSchema.xlsx).
In particular, the library contains also several procedures to manage the complex communication process with the EFSA data collection framework (DCF).
More precisely, it is possible to send/submit/reject/amend a report and to see its acknowledgment when needed.

# How to install the library in Eclipse
Go to your project in Eclipse and click File -> Import. Select from the list the element 'Existing Projects into Workspace' and the insert the path
where you have stored in your local machine the EFSA Report Creator Library project. Afterwards click on Finish.
Thereafter, right-click your project element in the package explorer and then select _**build path -> configure build path**_ from the menu.
Then open the _**Project**_ tab and click the _**Add**_ button and select the EFSA Report Creator Library project which was just imported.

Now your project is able to use the EFSA Report Creator Library methods to create a customized tool for a specific data collection.

# Documentation
## User interface package

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






### Creating a new table

	class NewWindow extends TableDialog {
	}

# Dependencies
The project needs the following projects to work properly:
* https://github.com/openefsa/zip-manager
* https://github.com/openefsa/version-manager
* https://github.com/openefsa/Dcf-webservice-framework
* https://github.com/openefsa/Progress-bar
* https://github.com/openefsa/http-manager
