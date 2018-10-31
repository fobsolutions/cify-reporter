package io.cify

import groovy.json.JsonSlurper
import io.cify.core.CifyFeature
import io.cify.views.details.DetailsPage
import io.cify.views.main.MainPage
import org.apache.commons.io.FileUtils

/**
 * Created by FOB Solutions
 */
class CifyReportBuilder {

    static void generateReports(String projectName, String suiteName) {
        setup()
        List files = collectResults()
        if (!files.isEmpty()) {
            List<CifyFeature> features = collectFeatures(files)
            DetailsPage.generateDetailsPages(features, projectName, suiteName)
            MainPage mainPage = new MainPage()
            mainPage.generateMainPage(features, projectName, suiteName)
        }
    }

    private static List<CifyFeature> collectFeatures(List files) {
        List<CifyFeature> cifyFeatures = new ArrayList<>()
        files.each {
            def jsonSlurper = new JsonSlurper()
            def reader = new BufferedReader(new InputStreamReader(new FileInputStream(it.getPath() as String), "UTF-8"))
            def reportData = jsonSlurper.parse(reader)
            reportData = reportData.get("report")
            CifyFeature feature = new CifyFeature(reportData["feature"] as Map)
            cifyFeatures.add(feature)
        }
        return cifyFeatures
    }

    private static List collectResults() {
        List<File> reports = new ArrayList<>()
        File reportDir = new File(Constants.REPORT_PATH)
        reportDir.listFiles().each {
            if (it.isFile()) {
                reports.add(it)
            }
        }

        return reports
    }

    static void setup() {
        File reportDirectory = new File(Constants.REPORT_DIR)

        File directory = new File(reportDirectory.getPath() + "/details/")
        if (!directory.isDirectory()) {
            directory.mkdirs()
        }

        File imagesFolder = new File(reportDirectory.getPath() + "/images/")
        File scriptsFolder = new File(reportDirectory.getPath() + "/scripts/")
        File stylesFolder = new File(reportDirectory.getPath() + "/styles/")
        File templatesFolder = new File(reportDirectory.getPath() + "/templates/")

        if (imagesFolder.isDirectory()) {
            imagesFolder.deleteDir()
        }

        if (scriptsFolder.isDirectory()) {
            scriptsFolder.deleteDir()
        }

        if (stylesFolder.isDirectory()) {
            imagesFolder.deleteDir()
        }

        if (templatesFolder.isDirectory()) {
            templatesFolder.deleteDir()
        }

        new File(reportDirectory.getPath() + "/images").mkdirs()
        new File(reportDirectory.getPath() + "/scripts").mkdirs()
        new File(reportDirectory.getPath() + "/styles").mkdirs()
        new File(reportDirectory.getPath() + "/templates").mkdirs()

        // Images
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "images/logo.png"), new File(reportDirectory.getPath() + "/images/logo.png"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "images/btn-back.png"), new File(reportDirectory.getPath() + "/images/btn-back.png"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "images/bg-header-line.png"), new File(reportDirectory.getPath() + "/images/bg-header-line.png"))

        // Styles
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "styles/main.css"), new File(reportDirectory.getPath() + "/styles/main.css"))

        // Scripts
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "scripts/main.js"), new File(reportDirectory.getPath() + "/scripts/main.js"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "scripts/plugins.js"), new File(reportDirectory.getPath() + "/scripts/plugins.js"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "scripts/scripts.js"), new File(reportDirectory.getPath() + "/scripts/scripts.js"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "scripts/vendor.js"), new File(reportDirectory.getPath() + "/scripts/vendor.js"))

        // Templates
        new File(reportDirectory.getPath() + "/templates/common").mkdirs()
        new File(reportDirectory.getPath() + "/templates/details").mkdirs()
        new File(reportDirectory.getPath() + "/templates/main").mkdirs()

        // templates/common
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/common/stacktrace.html"), new File(reportDirectory.getPath() + "/templates/common/stacktrace.html"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/common/step.html"), new File(reportDirectory.getPath() + "/templates/common/step.html"))

        // templates/details
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/details/details.html"), new File(reportDirectory.getPath() + "/templates/details/details.html"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/details/overview.html"), new File(reportDirectory.getPath() + "/templates/details/overview.html"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/details/video.html"), new File(reportDirectory.getPath() + "/templates/details/video.html"))

        // templates/main
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/main/index.html"), new File(reportDirectory.getPath() + "/templates/main/index.html"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/main/overview.html"), new File(reportDirectory.getPath() + "/templates/main/overview.html"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilder().getClass().getClassLoader().getResourceAsStream(Constants.TEMPLATES_PATH + "report/main/tabpanel.html"), new File(reportDirectory.getPath() + "/templates/main/tabpanel.html"))

    }
}
