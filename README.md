
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
