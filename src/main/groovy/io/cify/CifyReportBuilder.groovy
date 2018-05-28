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
        List<CifyFeature> features = collectFeatures(files)
        DetailsPage.generateDetailsPages(features, projectName, suiteName)
        MainPage mainPage = new MainPage()
        mainPage.generateMainPage(features, projectName, suiteName)
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
        File reportDir = new File(Constants.REPORT_PATH)
        return reportDir.listFiles()
    }

    private static void setup() {
        File reportDirectory = new File(Constants.REPORT_DIR)

        File directory = new File(reportDirectory.getPath() + "/details/")
        if (!directory.isDirectory()) {
            directory.mkdirs()
        }

        File templateImagesFolder = new File(Constants.TEMPLATES_PATH + "images/")
        File templateScriptsFolder = new File(Constants.TEMPLATES_PATH + "scripts/")
        File templateStylesFolder = new File(Constants.TEMPLATES_PATH + "styles/")

        File imagesFolder = new File(reportDirectory.getPath() + "/images/")
        File scriptsFolder = new File(reportDirectory.getPath() + "/scripts/")
        File stylesFolder = new File(reportDirectory.getPath() + "/styles/")

        if (imagesFolder.isDirectory()) {
            imagesFolder.deleteDir()
        }

        if (scriptsFolder.isDirectory()) {
            scriptsFolder.deleteDir()
        }

        if (stylesFolder.isDirectory()) {
            imagesFolder.deleteDir()
        }

        FileUtils.copyDirectory(templateImagesFolder, new File(reportDirectory.getPath() + "/images/"))
        FileUtils.copyDirectory(templateScriptsFolder, new File(reportDirectory.getPath() + "/scripts/"))
        FileUtils.copyDirectory(templateStylesFolder, new File(reportDirectory.getPath() + "/styles/"))
    }
}
