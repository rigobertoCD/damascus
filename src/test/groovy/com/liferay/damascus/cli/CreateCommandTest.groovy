package com.liferay.damascus.cli

import com.beust.jcommander.internal.Maps
import com.liferay.damascus.cli.common.*
import com.liferay.damascus.cli.json.DamascusBase
import com.liferay.damascus.cli.test.tools.TestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.commons.lang3.StringUtils
import spock.lang.Specification
import spock.lang.Unroll

class CreateCommandTest extends Specification {
    static def DS = DamascusProps.DS;
    static def workspaceRootDir = TestUtils.getTempPath() + "damascustest";
    static def workspaceName = "workspace"
    static def workTempDir = "";
    static def createCommand;

    def setup() {
        //Cleanup enviroment
        FileUtils.deleteDirectory(new File(workspaceRootDir));
        TemplateUtil.getInstance().clear();

        //Create Workspace
        CommonUtil.createWorkspace(workspaceRootDir, workspaceName);

        //Execute all tests under modules
        workTempDir = workspaceRootDir + DS + workspaceName + DS + "modules";

        TestUtils.setFinalStatic(CreateCommand.class.getDeclaredField("CREATE_TARGET_PATH"), workTempDir + DS);
        createCommand = new CreateCommand();
    }

    @Unroll("Smoke test for service.xml generate ProjectName<#projectName> version <#liferayVersion> Package <#packageName>")
    def "Smoke test for service.xml generate"() {

        when:
        Map params = Maps.newHashMap();

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        //Output base.json with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                DamascusProps.BASE_JSON,
                damascus,
                workTempDir + DS + DamascusProps.BASE_JSON)

        //Load base.json into object
        DamascusBase retrievedObj = JsonUtil.getObject(workTempDir + DS + DamascusProps.BASE_JSON, DamascusBase.class)

        //Setup output files
        def outputFile = workTempDir + DS + DamascusProps.SERVICE_XML

        //Generate Service XML
        createCommand.generateScaffolding(
                retrievedObj,
                DamascusProps.SERVICE_XML,
                outputFile,
                retrievedObj.applications[0])

        def f = new File(outputFile)

        //Testing nodes
        def rootNode = TestUtils.getPath(outputFile)
        def ret1 = rootNode.entity[0].column[0].@name

        then:
        true == f.exists()
        false == f.isDirectory()
        false == ret1.equals("")

        where:
        projectName | liferayVersion | packageName
        "ToDo"      | "70"           | "com.liferay.test"
    }

    @Unroll("Smoke test for generating Project Project<#projectName> Package<#packageName>")
    def "Smoke test for generating Project"() {

        when:
        def projectNameCommon = workTempDir + DS + projectName + DS + projectName;
        def service_path = new File(projectNameCommon + "-service");
        def api_path = new File(projectNameCommon + "-api");
        def buildGradle = new File(workTempDir + DS + projectName + DS + "build.gradle")
        def serviceXml = new File(projectNameCommon + "-service" + DS + "service.xml");

        def webName = projectName;
        if (!projectName.endsWith("-web")) {
            webName = projectName.concat("-web");
        }
        def webNameDir = workTempDir + DS + projectName + DS + webName
        def srcDir_web = new File(webNameDir + DS + "src")
        def gradlewFile = new File(webNameDir + DS + "gradlew")
        def gradlewBatFile = new File(webNameDir + DS + "gradlew.bat")

        //Generate project skeleton
        createCommand.generateProjectSkeleton(
                projectName,
                packageName,
                workTempDir,
                web_switch)

        //Setup output files
        def outputFile = workTempDir + DS + projectName
        def f = new File(outputFile)
        def fweb = new File(webNameDir);

        then:
        //*-service / *-api
        true == f.exists()
        true == f.isDirectory()
        true == service_path.exists()
        true == api_path.exists()
        true == buildGradle.exists()
        true == serviceXml.exists()

        //*-web
        web_exist == fweb.exists()
        web_isdir == fweb.isDirectory()
        web_src_exist == srcDir_web.exists()
        web_gradlew_exist == gradlewFile.exists()
        web_gradlewbat_exist == gradlewBatFile.exists()

        where:
        projectName | packageName                 | web_exist | web_isdir | web_src_exist | web_gradlew_exist | web_gradlewbat_exist | web_switch
        "ToDo"      | "com.liferay.test"          | false     | false     | false         | false             | false                | false
        "ToDo"      | "com.liferay.test"          | true      | true      | true          | false             | false                | true
        "To-Do"     | "com.bar.foo.packeage.long" | true      | true      | true          | false             | false                | true
        "T_Ask"     | "com.foo.bar"               | true      | true      | true          | false             | false                | true
    }

    @Unroll("Create Test from Main ProjectName<#projectName> version <#liferayVersion> Package <#packageName>")
    def "Create Test from Main"() {
        when:
        Map params = Maps.newHashMap();
        def workTempDirAPI = workTempDir + DS + expectedProjectDirName + DS + expectedProjectDirName + "-api";

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.json with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                DamascusProps.BASE_JSON,
                damascus,
                workTempDir + DS + DamascusProps.BASE_JSON)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        //Test files / directories are property generated
        def projectNameCommon = workTempDir + DS + expectedProjectDirName + DS + expectedProjectDirName;
        def service_path = new File(projectNameCommon + "-service");
        def api_path = new File(projectNameCommon + "-api");
        def buildGradle = new File(workspaceRootDir + DS + workspaceName + DS + "build.gradle")
        def serviceXml = new File(projectNameCommon + "-service" + DS + "service.xml");
        def implFile = FileUtils.listFiles(service_path, new RegexFileFilter(".*LocalServiceImpl.java"), TrueFileFilter.INSTANCE)
        def validatorFile = FileUtils.listFiles(new File(workTempDir + DS + expectedProjectDirName), new RegexFileFilter(".*Validator.java"), TrueFileFilter.INSTANCE)
        def portletKeysFile = FileUtils.listFiles(new File(workTempDirAPI), new RegexFileFilter(".*PortletKeys.java"), TrueFileFilter.INSTANCE)

        def f = new File(workTempDir + DS + expectedProjectDirName)

        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def checkLoop = getCheckLoop(expectedProjectDirName);

        then:
        //*-service / *-api
        true == f.exists()
        true == f.isDirectory()
        true == service_path.exists()
        true == api_path.exists()
        true == buildGradle.exists()
        true == serviceXml.exists()
        0 != implFile.size()
        0 != validatorFile.size()
        0 != portletKeysFile.size()
        checkLoop.each { trow ->
            def targetFile1 = FileUtils.listFiles(new File(trow.path), new RegexFileFilter(trow.target), TrueFileFilter.INSTANCE)
            assert trow.amount == targetFile1.size()
        }

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "sample-sb"

    }

    def getCheckLoop(expectedProjectDirName) {
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        return [
                [path: pathMap["apiPath"], target: ".*ActivityKeys.java", amount: 1],
                [path: pathMap["apiPath"], target: ".*bnd.bnd", amount: 1],
                [path: pathMap["apiPath"], target: ".*build.gradle", amount: 1],
                [path: pathMap["apiPath"], target: ".*PortletKeys.java", amount: 1],
                [path: pathMap["apiPath"], target: ".*ValidateException.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*bnd.bnd", amount: 1],
                [path: pathMap["servicePath"], target: ".*build.gradle", amount: 1],
                [path: pathMap["servicePath"], target: ".*default.xml", amount: 1],
                [path: pathMap["servicePath"], target: ".*Indexer.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*LocalServiceImpl.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*PermissionChecker.java", amount: 2],
                [path: pathMap["servicePath"], target: ".*portlet.properties", amount: 1],
                [path: pathMap["servicePath"], target: ".*portlet-model-hints.xml", amount: 1],
                [path: pathMap["servicePath"], target: ".*ResourcePermissionChecker.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*TrashHandler.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*Validator.java", amount: 1],
                [path: pathMap["servicePath"], target: ".*WorkflowHandler.java", amount: 1],
                [path: pathMap["webPath"], target: ".*abstract.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*ActivityInterpreter.java", amount: 1],
                [path: pathMap["webPath"], target: ".*AssetRenderer.java", amount: 1],
                [path: pathMap["webPath"], target: ".*AssetRendererFactory.java", amount: 1],
                [path: pathMap["webPath"], target: ".*bnd.bnd", amount: 1],
                [path: pathMap["webPath"], target: ".*build.gradle", amount: 1],
                [path: pathMap["webPath"], target: ".*ConfigurationAction.java", amount: 1],
                [path: pathMap["webPath"], target: ".*Configuration.java", amount: 1],
                [path: pathMap["webPath"], target: ".*configuration.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*CrudMVCActionCommand.java", amount: 1],
                [path: pathMap["webPath"], target: ".*CrudMVCRenderCommand.java", amount: 1],
                [path: pathMap["webPath"], target: ".*default.xml", amount: 1],
                [path: pathMap["webPath"], target: ".*edit.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*edit_actions.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*full_content.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*init.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*ItemSelectorHelper.java", amount: 1],
                [path: pathMap["webPath"], target: ".*portlet.properties", amount: 1],
                [path: pathMap["webPath"], target: ".*search_results.jspf", amount: 1],
                [path: pathMap["webPath"], target: ".*view.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*ViewHelper.java", amount: 1],
                [path: pathMap["webPath"], target: ".*ViewMVCRenderCommand.java", amount: 1],
                [path: pathMap["webPath"], target: ".*view_record.jsp", amount: 1],
                [path: pathMap["webPath"], target: ".*WebKeys.java", amount: 1],
                [path: pathMap["webPath"], target: ".*WebPortlet.java", amount: 1],
                [path: pathMap["webPath"], target: ".*portlet.properties", amount: 1],
                [path: pathMap["webPath"], target: ".*FindEntryAction.java", amount: 1],
                [path: pathMap["webPath"], target: ".*FindEntryHelper.java", amount: 1],
                [path: pathMap["webPath"], target: ".*PortletLayoutFinder.java", amount: 1],
                [path: pathMap["webPath"], target: ".*AdminPortlet.java", amount: 1],
                [path: pathMap["webPath"], target: ".*PanelApp.java", amount: 1],
                [path: pathMap["rootPath"], target: ".*build.gradle", amount: 4],
                [path: pathMap["rootPath"], target: ".*settings.gradle", amount: 1]
        ];

    }

    @Unroll("Run Damascus with a different template")
    def "Run Damascus with a different template"() {
        setup:
        Map params = Maps.newHashMap();

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        //Output base.json with parameters and create the default templates
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                DamascusProps.VERSION_70,
                DamascusProps.BASE_JSON,
                damascus,
                workTempDir + DS + DamascusProps.BASE_JSON)

        File org = new File(DamascusProps.TEMPLATE_FILE_PATH + DS + DamascusProps.VERSION_70);
        File dist = new File(DamascusProps.TEMPLATE_FILE_PATH + DS + liferayVersion);
        FileUtils.copyDirectory(org, dist)

        // Delete base.json and the default template so that following method can create a new one
        // and see if Damascus actually can point a new template.
        FileUtils.deleteQuietly(new File(workTempDir + DS + DamascusProps.BASE_JSON))
        FileUtils.deleteQuietly(org)

        when:
        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.json with parameters and create the default templates
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                DamascusProps.BASE_JSON,
                damascus,
                workTempDir + DS + DamascusProps.BASE_JSON)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        then:
        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def checkLoop = getCheckLoop(expectedProjectDirName)

        checkLoop.each { trow ->
            def targetFile1 = FileUtils.listFiles(new File(trow.path), new RegexFileFilter(trow.target), TrueFileFilter.INSTANCE)
            assert trow.amount == targetFile1.size()
        }

        cleanup:
        FileUtils.copyDirectory(dist, org)
        FileUtils.deleteQuietly(dist)

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "mytemp"       | "com.liferay.test" | "sample-sb"

    }

    @Unroll("Template creation tests with asset flags variations <#baseFilename> <#prohibitedTerms> <#prohibitedInServiceImpl>")
    def "Template creation tests with asset flags variations"() {
        setup:
        Map params = Maps.newHashMap();

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        when:
        FileUtils.deleteQuietly(new File(DamascusProps.CACHE_DIR_PATH))

        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.json with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                baseFilename,
                damascus,
                workTempDir + DS + DamascusProps.BASE_JSON)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        then:
        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def apiTargetFiles = FileUtils.listFiles(new File(pathMap["apiPath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def serviceTargetFiles = FileUtils.listFiles(new File(pathMap["servicePath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def webJavaTargetFiles = FileUtils.listFiles(new File(pathMap["webPath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def webJspTargetFiles = FileUtils.listFiles(new File(pathMap["webPath"]), new RegexFileFilter(".*\\.jsp.*"), TrueFileFilter.INSTANCE)

        def prohibitedTermsTargetFiles = new ArrayList(webJspTargetFiles)

        if (prohibitedInServiceImpl) {
            prohibitedTermsTargetFiles.addAll(
                    serviceTargetFiles.findAll { it.name.endsWith('LocalServiceImpl.java') })
        }

        apiTargetFiles.size() > 1
        serviceTargetFiles.size() > 1
        webJavaTargetFiles.size() > 1
        webJspTargetFiles.size() > 1

        noFileContainsAnyTerm(prohibitedTermsTargetFiles, prohibitedTerms)

        where:
        projectName | liferayVersion | packageName        | baseFilename                 | prohibitedTerms            | prohibitedInServiceImpl | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "base_activity_false.json"   | ['activity', 'activities'] | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_categories_false.json" | ['category', 'categories'] | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_discussion_false.json" | ['Comments', 'discussion'] | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_ratings_false.json"    | ['ratings']                | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_tags_false.json"       | ['tags']                   | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_related_false.json"    | ['asset-links']            | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_workflow_false.json"   | ['WorkflowDefinition', 'workflowManager', 'WorkflowHandler', 'WorkflowInstance' ]            | true                    | "sample-sb"

    }


    @Unroll("Do not generate web test <#projectName> version <#liferayVersion> Package <#packageName> expectedProjectDirName <#expectedProjectDirName>")
    def "Do not generate web test"() {
        when:
        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        def target_file_path = workTempDir + DS + DamascusProps.BASE_JSON
        def dmsb = TestUtils.createBaseJsonMock(projectName, liferayVersion, packageName, target_file_path)
        dmsb.applications.get(0).web = false
        JsonUtil.writer(target_file_path,dmsb)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        def fpath = TestUtils.getPathMap(expectedProjectDirName)
        def f = new File(fpath["webPath"])

        then:
        //*-web doesn't exit
        false == f.exists()
        false == f.isDirectory()

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "sample-sb"

    }
    
    @Unroll("YAML Create Test from Main ProjectName<#projectName> version <#liferayVersion> Package <#packageName>")
    def "YAML Create Test from Main"() {
        when:
        System.out.println "TEST: YAML Create Test from Main"
        Map params = Maps.newHashMap();
        def workTempDirAPI = workTempDir + DS + expectedProjectDirName + DS + expectedProjectDirName + "-api";

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.yaml with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                DamascusProps.BASE_YAML,
                damascus,
                workTempDir + DS + DamascusProps.BASE_YAML)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        //Test files / directories are property generated
        def projectNameCommon = workTempDir + DS + expectedProjectDirName + DS + expectedProjectDirName;
        def service_path = new File(projectNameCommon + "-service");
        def api_path = new File(projectNameCommon + "-api");
        def buildGradle = new File(workspaceRootDir + DS + workspaceName + DS + "build.gradle")
        def serviceXml = new File(projectNameCommon + "-service" + DS + "service.xml");
        def implFile = FileUtils.listFiles(service_path, new RegexFileFilter(".*LocalServiceImpl.java"), TrueFileFilter.INSTANCE)
        def validatorFile = FileUtils.listFiles(new File(workTempDir + DS + expectedProjectDirName), new RegexFileFilter(".*Validator.java"), TrueFileFilter.INSTANCE)
        def portletKeysFile = FileUtils.listFiles(new File(workTempDirAPI), new RegexFileFilter(".*PortletKeys.java"), TrueFileFilter.INSTANCE)

        def f = new File(workTempDir + DS + expectedProjectDirName)

        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def checkLoop = getCheckLoop(expectedProjectDirName);

        then:
        //*-service / *-api
        true == f.exists()
        true == f.isDirectory()
        true == service_path.exists()
        true == api_path.exists()
        true == buildGradle.exists()
        true == serviceXml.exists()
        0 != implFile.size()
        0 != validatorFile.size()
        0 != portletKeysFile.size()
        checkLoop.each { trow ->
            def targetFile1 = FileUtils.listFiles(new File(trow.path), new RegexFileFilter(trow.target), TrueFileFilter.INSTANCE)
            assert trow.amount == targetFile1.size()
        }

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "sample-sb"

    }
  
    @Unroll("YAML Run Damascus with a different template")
    def "YAML Run Damascus with a different template"() {
        setup:
        System.out.println("TEST: YAML Run Damascus with a different template")
        Map params = Maps.newHashMap();

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        //Output base.yaml with parameters and create the default templates
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                DamascusProps.VERSION_70,
                DamascusProps.BASE_YAML,
                damascus,
                workTempDir + DS + DamascusProps.BASE_YAML)

        File org = new File(DamascusProps.TEMPLATE_FILE_PATH + DS + DamascusProps.VERSION_70);
        File dist = new File(DamascusProps.TEMPLATE_FILE_PATH + DS + liferayVersion);
        FileUtils.copyDirectory(org, dist)

        // Delete base.yaml and the default template so that following method can create a new one
        // and see if Damascus actually can point a new template.
        FileUtils.deleteQuietly(new File(workTempDir + DS + DamascusProps.BASE_YAML))
        FileUtils.deleteQuietly(org)

        when:
        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.yaml with parameters and create the default templates
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                DamascusProps.BASE_YAML,
                damascus,
                workTempDir + DS + DamascusProps.BASE_YAML)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        then:
        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def checkLoop = getCheckLoop(expectedProjectDirName)

        checkLoop.each { trow ->
            def targetFile1 = FileUtils.listFiles(new File(trow.path), new RegexFileFilter(trow.target), TrueFileFilter.INSTANCE)
            assert trow.amount == targetFile1.size()
        }

        cleanup:
        FileUtils.copyDirectory(dist, org)
        FileUtils.deleteQuietly(dist)

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "mytemp"       | "com.liferay.test" | "sample-sb"

    }
  
    @Unroll("YAML Template creation tests with asset flags variations <#baseFilename> <#prohibitedTerms> <#prohibitedInServiceImpl>")
    def "YAML Template creation tests with asset flags variations"() {
        setup:
        
        System.out.println "TEST: YAML Template creation tests with asset flags variations"
        Map params = Maps.newHashMap();

        //Set parameters
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap();
        damascus.put('damascus', params);

        when:
        FileUtils.deleteQuietly(new File(DamascusProps.CACHE_DIR_PATH))

        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        //Output base.yaml with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                baseFilename,
                damascus,
                workTempDir + DS + DamascusProps.BASE_YAML)

        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        then:
        //Target path map of a project
        def pathMap = TestUtils.getPathMap(expectedProjectDirName)

        def apiTargetFiles = FileUtils.listFiles(new File(pathMap["apiPath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def serviceTargetFiles = FileUtils.listFiles(new File(pathMap["servicePath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def webJavaTargetFiles = FileUtils.listFiles(new File(pathMap["webPath"]), new RegexFileFilter(".*\\.java"), TrueFileFilter.INSTANCE)
        def webJspTargetFiles = FileUtils.listFiles(new File(pathMap["webPath"]), new RegexFileFilter(".*\\.jsp.*"), TrueFileFilter.INSTANCE)

        def prohibitedTermsTargetFiles = new ArrayList(webJspTargetFiles)

        if (prohibitedInServiceImpl) {
            prohibitedTermsTargetFiles.addAll(
                    serviceTargetFiles.findAll { it.name.endsWith('LocalServiceImpl.java') })
        }

        apiTargetFiles.size() > 1
        serviceTargetFiles.size() > 1
        webJavaTargetFiles.size() > 1
        webJspTargetFiles.size() > 1

        noFileContainsAnyTerm(prohibitedTermsTargetFiles, prohibitedTerms)

        where:
        projectName | liferayVersion | packageName        | baseFilename                 | prohibitedTerms            | prohibitedInServiceImpl | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "base_activity_false.yaml"   | ['activity', 'activities'] | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_categories_false.yaml" | ['category', 'categories'] | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_discussion_false.yaml" | ['Comments', 'discussion'] | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_ratings_false.yaml"    | ['ratings']                | true                    | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_tags_false.yaml"       | ['tags']                   | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_related_false.yaml"    | ['asset-links']            | false                   | "sample-sb"
        "SampleSB"  | "70"           | "com.liferay.test" | "base_workflow_false.yaml"   | ['WorkflowDefinition', 'workflowManager', 'WorkflowHandler', 'WorkflowInstance' ]            | true                    | "sample-sb"
    }
    void noFileContainsAnyTerm(files, terms) {
        files.each { file ->
            terms.each { term ->
                assert !file.text.contains(term)
            }
        };
    }


    @Unroll("YAML Do not generate web test <#projectName> version <#liferayVersion> Package <#packageName> expectedProjectDirName <#expectedProjectDirName>")
    def "YAML Do not generate web test"() {
        when:
        System.out.println("YAML Do not generate web test")
        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()

        def target_file_path = workTempDir + DS + DamascusProps.BASE_YAML
             
        Map params = Maps.newHashMap();   
        params.put("projectName", projectName)
        params.put("liferayVersion", liferayVersion)
        params.put("packageName", packageName)
        String entityName = projectName.replace("-", "")
        params.put("entityName", entityName)
        params.put("entityNameLower", StringUtils.lowerCase(entityName))
        Map damascus = Maps.newHashMap()
        damascus.put('damascus', params)
        
        // Once clear _cfg to initialize with an actual test target template directory
        TemplateUtil.getInstance().clear()
        
        //Output base.yaml with parameters.
        TemplateUtil.getInstance().process(
                TemplateUtilTest.class,
                liferayVersion,
                "base_no_web.yaml",
                damascus,
                target_file_path)
        
        //Run damascus -create
        String[] args = ["-create"]
        Damascus.main(args)

        def fpath = TestUtils.getPathMap(expectedProjectDirName)
        def f = new File(fpath["webPath"])

        then:
        //*-web doesn't exit
        false == f.exists()
        false == f.isDirectory()

        where:
        projectName | liferayVersion | packageName        | expectedProjectDirName
        "SampleSB"  | "70"           | "com.liferay.test" | "sample-sb"
    }
    
}
