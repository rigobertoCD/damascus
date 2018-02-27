#Liferay Development With Damascus

Github: https://github.com/yasuflatland-lf/damascus
Wiki: https://github.com/yasuflatland-lf/damascus/wiki

##1. Install Damascus
###1.1. Mac
```bash
curl https://raw.githubusercontent.com/yasuflatland-lf/damascus/master/installers/global | sudo sh
```

###1.2. Windows
1. Download jpm (https://raw.githubusercontent.com/jpm4j/jpm4j.installers/master/dist/jpm-setup.exe) and install.
2. Install damascus.jar with jpm as follows. ```jpm install https://github.com/yasuflatland-lf/damascus/raw/master/latest/damascus.jar```

###1.3. Linux
1. Download and install Liferay Workspace (https://sourceforge.net/projects/lportal/files/Liferay%20Workspace/1.5.0.1/) to install jpm.
2. Install damascus.jar with jpm as follows. ```jpm install https://github.com/yasuflatland-lf/damascus/raw/master/latest/damascus.jar```

###1.4. How to update
1. Run jpm remove damascus to uninstall damascus.
2. Remove all files under ```${user}/.damascus``` folder. If you've modified files, please change them accordingly after regenerating configurations and templates.
3. Follow How to install section to install again


##2. Generate project skeleton with Damascus
1. Create a Liferay workspace with Blade cli or Liferay IDE / Liferay Developer Studio. For more details, please see https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/blade-cli
2. After creating Liferay workspace, navigate to under ```modules``` folder and run ```damascus -init Todo -p com.liferay.sb.test -v 70```. You can also add the option ```-yaml``` to create a YAML file instead of a JSON file.
3. Navigate to ```todo``` folder. You'll see ```base.json``` (or ```base.yaml```)  file is created. For detailed configuration, please see https://github.com/yasuflatland-lf/damascus/wiki Just for demonstration now, we'll create a scaffolding as it is.
4. Type ```damascus -create``` and damascus will create a scaffolding service and portlet according to the base.json or base.yaml file.
5. Start up your Liferay server and in the ```Todo``` folder, type ```blade deploy```. Blade will run properly and service and portlet will be deployed.
6. Add the proyect to your favorite IDE.

##3. Modify the Generated code by Damascus
Damascus generates 3 modules:
1. **-api** module:  API module of the Service Builder
2. **-service** module:  service inmplementations of Service Builder.
3. **-web** module: Portlet module.
###3.1. *-api
Damascus only creates static keys. Thearfore, you don't have to modify anything.
###3.2. *-service
Damascus generates the next aspects inside *-service:
- Permission
- Indexer
- LocalSeriveImpl
- Model hints
- Trash Handler
- Validator
- Workflow Handler

####3.2.1. Permission
Damascus the permission definition file and the classes that handle the permission.  You **just** have to modify the permisison definition during your development.
The permission definition file is located at: ```*-service/src/main/resources/META-INF/resource-actions/default.xml```

####3.2.2. Indexer
Indexer is the class in charge of manage the index and search aspects of your entities.  One Indexer is created for each entity.  You have to modify the function ```doGetDocument```according to your requirements. For more information, please see https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/creating-a-guestbook-indexer
The indexers are located at: ```*-service/src/main/java/${packagePath}/service/util/```

####3.2.3. LocalServiceImpl
Damascus creates for you the CRUD operations for your entities (including Trash, Asset Framework, Resources and Workflow)
The LocalServiceImpls are located at: ```*-service/src/main/java/${packagePath}/service/impl/```
The more important functions are:
1. ```get${entityName}FromRequest```:  creates the entity object from a PortletRequest.  Used by Add and Update operations in the ```*-web``` project
2. ```getInitialized${entityName}```:  initialize  an entity object. The default values can be added here.
3. ```addEntry``` and ```protected _addEntry```: used to store an entity in the database. These functions also creates the Asset, Workflow Instance and Resource of the entity.
3. ```updateEntry``` and ```protected _updateEntry```: used to update an entity in the database. These functions also manage the Asset, Workflow Instance and Resource of the entity.

####3.2.4. Model Hints
The ```portlet-model-hints.xml``` is created with the correct configuration for each field. You can modify it according to your requirements.
It is located at: ```*-service/src/main/resources/META-INF/portlet-model-hints.xml```
The Model Hints are used by Liferay to create the database and the correct HTML inputs in the views.

####3.2.5. Trash Handlers
A Trash Hanler is created for each entity. You don't have to modify them. They are located at ```*-service/src/main/java/${packagePath}/trash/```

####3.2.6. Validator
A Model Validator is created for each entity. Damascus only generates the **"required"** validations. You have to **modify** and **add** validatos according to your requirements.
They are located at: ```*-service/src/main/java/${packagePath}/service/util/```

####3.2.7. Workflow Handler
A WorkflowHandler and a WorkflowManager are created for each entity. You don't have to modify any of them. 
They are located at ```*-service/src/main/java/${packagePath}/service/workflow/```

###3.3. *-web
Damascus creates a Module with a robustus CRUD Portlet for each entity.  The CRUDs manage:
- Add, View and Update.
- Asset Framework support.
- Move to trash and permanent deletion.
- Workflow
- Permissions
- Search and order the list.

You have to modify them according to your requirements.

####3.3.1. JSP sctructure
Damascus creates the next structure of JSP files for each entity:
- **asset** ---> Used by Asset Publisher and Asset Framework
--  -- **abstract.jsp**
--  -- **full_content.jsp**
- **configuration.jsp** ----> Portlet Configuration
- **edit_actions.jsp** ----> Actions column of the Search Container
- **edit.jsp** ---> Used by Add and Update operations
- **init.jsp**
- **search_results.jspf** ---> Obtains the results used by de Search Container
- **view_record.jsp**
- **view.jsp** ---> List of entity objects. It manage Trash, Search, Order-by, Edit.

####3.3.2. Packages
Damascus generates the next packages for the ***-web** module.

#####3.3.2.1. {basePackage}.web.portlet
Damascus generates two portlets for each entity. One is a Control Panel portlet (AdminApp) and the other is the droppable portlet (WebPortlet):
- **{entity}AdminPortlet.java**: Control Panel Portlet main class.
- **{entry}WebPortlet.java**: Droppable Portlet main class.
- **{entry}PanelApp.java**: Adds AdminPortlet to the Control Panel.
- **{entry}PortletLayoutFinder.java**

#####3.3.2.2. {basePackage}.web.portlet.action
Crud MVC Commands to manage render and action phases:
- **{entity}CrudMVCActionCommand.java**
- **{entity}CrudMVCRenderCommand.java**

Default render command.  It shows the entity list:
- **{entity}ViewMVCRenderCommand.java**

Web Portlet Configuration (to add a new configuration please see 3.3.3):
- **{entity}Configuration.java**
- **{entity}ConfigurationAction.java**

Finder Entry used by Soctial Activity and Asset Framework:
- **{entity}FindEntryAction.java**
- **{entity}FindEntryHelper.java**

#####3.3.2.3. {basePackage}.web.upload
In Liferay 7 you can create Items Selector Popups where you can search and find an entity. Damascus use the Item Selector to upload a file, store it in the Documents & Media and save the file's URI in the entity.
- **{entry}ItemSelectorHelper.java**

#####3.3.2.4. {basePackage}.web.util
Used to obtain the search results from the Index and from the DB
- **{entry}ViewHelper.java**

#####3.3.2.5. {basePackage}.web.constants
- **{entry}WebKeys.java**

#####3.3.2.6. {basePackage}.web.social
- **{entity}ActivityInterpreter.java**

#####3.3.2.7. {basePackage}.web.asset
- **{entity}AssetRenderer.java**
- **{entity}AssetRendererFactory.java**

####3.3.3. Add a WebPortlet Configuration field
To add a configuration field to the WebPortlet you have to modify the next files:
- **{entity}Configuration.java**:  you have to add a new property in this interface with its ```@Meta.AD``` anotation.  example:
``` java
    @Meta.AD(deflt = "defaultValue", required = false)
    public String newConfigurationField();
```
- **{entity}ConfigurationAction.java**: Retrive the field's value from the request and store it in the Portlet Preferences:
``` java
    ////....
    String newConfigurationField = ParamUtil.getString(actionRequest,
                         "newConfigurationField");
    ////....
    setPreference(actionRequest,
                          "newConfigurationField", newConfigurationField);
    ////....
```
- **{entity}/init.jsp**: retreive the new field's value
```
    String newConfigurationField = StringPool.BLANK;
    ////.... 
    newConfigurationField =
        HtmlUtil.escape(
            portletPreferences.getValue(
                "newConfigurationField", ${entity}Configuration.newConfigurationField()));
```
- **{entity}/configuration.jsp**: Add an input for the new field
``` jsp
    <aui:input type="text" name="newConfigurationField" value="<%= newConfigurationField %>" />
```
Now you can use the new configuration field in the portlet's jsp files.