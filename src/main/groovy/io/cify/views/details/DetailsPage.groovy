package io.cify.views.details

import groovy.json.JsonBuilder
import io.cify.Constants
import io.cify.core.CifyFeature
import io.cify.core.CifyScenario
import io.cify.core.CifyStep
import io.cify.views.BasePage
import io.cify.views.common.StacktracePage
import io.cify.views.common.StepPage
import org.apache.commons.io.FileUtils

/**
 * Created by FOB Solutions
 * Generates details page for report
 */
class DetailsPage extends BasePage {

    /**
     * Generate all detail pages
     * @param featureList
     * @param projectName
     * @param suiteName
     */
    static void generateDetailsPages(List<CifyFeature> featureList, String projectName, String suiteName) {
        featureList.each { CifyFeature feature ->
            feature.getScenarios().each { CifyScenario scenario ->
                scenario.setUrl(generateDetailsPage(feature.getFeature().getName(), scenario, projectName, suiteName))
            }
        }
    }

    /**
     * Generate single details page
     * @param featureName
     * @param scenario
     * @param projectName
     * @param suiteName
     * @return page name - used as URL
     */
    static String generateDetailsPage(String featureName, CifyScenario scenario, String projectName, String suiteName) {

        // Overall template
        File htmlTemplateFile = new File(Constants.REPORTS_TEMPLATES_PATH + "details/details.html")
        String htmlString = FileUtils.readFileToString(htmlTemplateFile)

        htmlString = htmlString.replace("{projectName}", projectName)

        htmlString = htmlString.replace("{OVERVIEW_HTML_PAGE}", getScenarioOverviewString(featureName, scenario, projectName, suiteName))
        htmlString = htmlString.replace("{STEP_HTML_PAGE}", getStepsViewString(scenario, "steps"))
        htmlString = htmlString.replace("{DEVICES_STEP_HTML_PAGE}", getStepsViewString(scenario, "devices"))

        File report = new File(Constants.REPORT_DIR + "/details/" + scenario.getScenario().getId() + new Date().hashCode() + ".html")
        report.write(htmlString)
        return report.getName()

    }

    /**
     * Generates scenario overview page
     * @param featureName
     * @param scenario
     * @param projectName
     * @param suiteName
     * @return
     */
    static String getScenarioOverviewString(String featureName, CifyScenario scenario, String projectName, String suiteName) {

        //Overview template
        File overviewTemplate = new File(Constants.REPORTS_TEMPLATES_PATH + "details/overview.html")
        String overviewString = FileUtils.readFileToString(overviewTemplate)

        String testName = scenario.getScenario().getName()
        String startDate = scenario.getStartDate().toString()
        String duration = scenario.getEndDate() - scenario.getStartDate()

        overviewString = overviewString.replace("{projectName}", projectName)
        overviewString = overviewString.replace("{suiteName}", suiteName)
        overviewString = overviewString.replace("{scenarioVideoSource}", getVideoFilePath(scenario.getScenario().getName()))
        overviewString = overviewString.replace("{scenarioDuration}", duration)
        overviewString = overviewString.replace("{testName}", testName)
        overviewString = overviewString.replace("{featureName}", featureName)
        overviewString = overviewString.replace("{startDate}", startDate)

        return overviewString
    }

    /**
     * Generates steps views for strategies
     * @param scenario
     * @param strategy
     * @return
     */
    static String getStepsViewString(CifyScenario scenario, String strategy) {
        String fullStepsString = ""
        if (strategy == "steps") {

            fullStepsString = fullStepsString + getConditions(scenario, "before")

            scenario.getSteps().each { CifyStep step ->

                String stacktraceString = StacktracePage.generateStacktrace(
                        "",
                        "",
                        step.getResult().getStatus() == "failed" ? step.getResult().getErrorMessage() : "",
                        "",
                        step.getEmbeddings(),
                        step.getWritings()
                )

                String stepString = StepPage.generateStepPage(
                        step.getResult().getStatus() == "failed",
                        step.getDuration() as String,
                        step.getStep().getKeyword() + step.getStep().getName(),
                        step.getStep().getName().replace(" ", "-") + "-step",
                        stacktraceString
                )

                fullStepsString = fullStepsString + stepString
            }
            fullStepsString = fullStepsString + getConditions(scenario, "after")
        } else {
            scenario.getDevices().each { Map device ->

                String stepName
                if (!device.get("deviceName") != "") {
                    stepName = device.get("deviceName")
                } else {
                    stepName = device.get("capability")
                }

                String stacktraceString = StacktracePage.generateStacktrace(
                        "",
                        "",
                        new JsonBuilder(device).toPrettyString(),
                        "",
                        [],
                        []
                )

                String stepString = StepPage.generateStepPage(
                        scenario.getStatus() == Status.FAILED,
                        "",
                        stepName,
                        device.hashCode() + "deviceId",
                        stacktraceString
                )

                fullStepsString = fullStepsString + stepString
            }
        }
        return fullStepsString

    }

    /**
     * Gets conditions
     * @param scenario
     * @param condition
     * @return
     */
    private static String getConditions(CifyScenario scenario, String condition) {
        List<Map> conditions = scenario.getConditions().get(condition) as List
        String stacktraceString = ""
        conditions.each { Map conditionMap ->
            stacktraceString = stacktraceString + StacktracePage.generateStacktrace(
                    conditionMap.get("match").get("location") as String,
                    "",
                    new JsonBuilder(conditionMap).toPrettyString(),
                    "",
                    [],
                    []
            )

        }

        boolean isFailed = scenario.getConditions().get(condition).find {
            it.get("result").get("status") == "failed"
        }

        String name = condition == "before" ? "Preconditions" : "After conditions"

        String stepString = StepPage.generateStepPage(
                isFailed,
                "",
                name,
                scenario.getScenario().getName() + "-" + name.replace(" ", "-"),
                stacktraceString
        )
           return stepString
        }

    /**
     * Gets video file
     * */
    private static String getVideoFilePath(String scenarioName) {
        String videoDirectoryString = System.getProperty("videoDir") +
                System.getProperty("task", "plug-and-play") +
                "/" +
                scenarioName.replace(" ", "-") +
                "/"

        File videoDir = new File(videoDirectoryString)
        try {
            return videoDir.listFiles().first().getPath()
        } catch (ignored) {
            return ""
        }
    }
}
