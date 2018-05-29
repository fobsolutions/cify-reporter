package io.cify.views.main

import io.cify.Constants
import io.cify.core.CifyFeature
import io.cify.core.CifyScenario
import io.cify.core.Status
import io.cify.views.BasePage
import io.cify.views.common.StacktracePage
import io.cify.views.common.StepPage
import org.apache.commons.io.FileUtils

import java.util.concurrent.TimeUnit

/**
 * Created by FOB Solutions
 * Generates main page for Report
 */
class MainPage extends BasePage {

    private
    static File mainOverviewTemplate = new File(Constants.REPORT_DIR + Constants.TEMPLATES_PATH + "main/overview.html")
    private static File mainTemplateFile = new File(Constants.REPORT_DIR + Constants.TEMPLATES_PATH + "main/index.html")
    private
    static File tabpanelTemplateFile = new File(Constants.REPORT_DIR + Constants.TEMPLATES_PATH + "main/tabpanel.html")

    /**
     * Generate main page
     * @param features
     * @param projectName
     * @param suiteName
     */
    static void generateMainPage(List<CifyFeature> features, String projectName, String suiteName) {
        String htmlString = FileUtils.readFileToString(mainTemplateFile)
        htmlString = htmlString.replace("{projectName}", projectName)

        htmlString = htmlString.replace("{TESTS_TAB_PANEL}", generateTab(suiteName, features, "tests", true, "Feature name"))
        htmlString = htmlString.replace("{DEVICES_TAB_PANEL}", generateTab(suiteName, features, "devices", false, "Device name"))

        new File(Constants.REPORT_DIR + "index.html").write(htmlString)
    }

    /**
     * Generate overview
     * @param features
     * @param suiteName
     * @param strategy
     * @return
     */
    private static String getScenarioOverviewString(List<CifyFeature> features, String suiteName, String strategy) {

        String startDate = features.first().startDate.toString()

        List<Date> dates = new ArrayList<>()
        features.each { CifyFeature feature ->

            feature.getScenarios().each { CifyScenario scenario ->
                dates.add(scenario.getStartDate())
                dates.add(scenario.getEndDate())
            }
        }

        dates = dates.sort {
            it.getTime()
        }

        long startDatelong = dates.first().getTime()
        long endDatelong = dates.last().getTime()

        int failedCount = 0
        int passedCount = 0

        if (strategy == "tests") {
            features.each { CifyFeature feature ->
                feature.getScenarios().each { CifyScenario scenario ->
                    if (scenario.getStatus() == Status.PASSED) {
                        passedCount++
                    } else {
                        failedCount++
                    }
                }
            }
        } else {
            Map<String, Map<CifyFeature, List<CifyScenario>>> sortedFeatures = sortFeaturesByDevices(features)

            sortedFeatures.each { String devices, Map<CifyFeature, List<CifyScenario>> featureListMap ->
                List<CifyScenario> fullScenarioList = new ArrayList<>()
                featureListMap.each { CifyFeature feature, List<CifyScenario> scenarios ->
                    fullScenarioList.addAll(scenarios)
                }
                if (fullScenarioList.find { it.getStatus() == Status.FAILED }) {
                    failedCount++
                } else {
                    passedCount++
                }
            }
        }

        String overviewString = FileUtils.readFileToString(mainOverviewTemplate)

        overviewString = overviewString.replace("{startDate}", startDate.toString())
        overviewString = overviewString.replace("{duration}", convertMilliseconds(endDatelong - startDatelong))
        overviewString = overviewString.replace("{suiteName}", suiteName)
        overviewString = overviewString.replace("{failedPercentage}", (failedCount / (passedCount + failedCount) * 100).toString() + "%")
        overviewString = overviewString.replace("{failedCount}", failedCount.toString())
        overviewString = overviewString.replace("{passedCount}", passedCount.toString())

        return overviewString
    }

    /**
     * Generate step view
     * @param features
     * @param strategy
     * @param status
     * @return
     */
    private static String getStepsViewString(List<CifyFeature> features, String strategy, Status status) {
        String fullStepsString = ""
        if (strategy == "tests") {
            Map<String, List<CifyScenario>> sortedFeatures = sortByFeatures(features)

            sortedFeatures.each { String featureName, List<CifyScenario> scenarios ->

                Map<String, Map<Status, List<CifyScenario>>> failedPassedMap = sortByFailedAndPassed(featureName, scenarios)

                if (!failedPassedMap.get(featureName).get(status).isEmpty()) {

                    String fullStacktraceString = ""

                    failedPassedMap.get(featureName).get(status).each { CifyScenario scenario ->
                        String devicesString = getDevicesName(scenario)
                        String stacktraceString = StacktracePage.generateStacktrace(
                                scenario.getScenario().getName(),
                                devicesString,
                                "",
                                scenario.getUrl(),
                                [],
                                []
                        )

                        fullStacktraceString = fullStacktraceString + stacktraceString
                    }

                    String stepString = StepPage.generateStepPage(
                            status == Status.FAILED ? "failed" : "passed",
                            "",
                            featureName,
                            featureName.replace(" ", "-") + "-tests-" + status,
                            fullStacktraceString
                    )
                    fullStepsString = fullStepsString + stepString
                }
            }

        } else {
            Map<String, Map<CifyFeature, List<CifyScenario>>> sortedFeatures = sortFeaturesByDevices(features)

            sortedFeatures.each { String devices, Map<CifyFeature, List<CifyScenario>> featureListMap ->
                Map<String, Map<Status, Map<CifyFeature, List<CifyScenario>>>> failedPassedMap = sortByFailedAndPassed(devices, featureListMap)

                if (!failedPassedMap.get(devices).get(status).isEmpty()) {
                    String fullStacktraceString = ""
                    failedPassedMap.get(devices).get(status).each { CifyFeature feature, List<CifyScenario> scenarios ->
                        scenarios.each { CifyScenario scenario ->
                            String stacktraceString = StacktracePage.generateStacktrace(
                                    scenario.getScenario().getName(),
                                    devices,
                                    "",
                                    scenario.getUrl(),
                                    [],
                                    []
                            )

                            fullStacktraceString = fullStacktraceString + stacktraceString
                        }
                    }
                    String stepString = StepPage.generateStepPage(
                            status == Status.FAILED ? "failed" : "passed",
                            "",
                            devices,
                            devices.replace(" ", "-") + "-devices-" + status,
                            fullStacktraceString
                    )
                    fullStepsString = fullStepsString + stepString
                }
            }
        }
        return fullStepsString
    }

    /**
     * Genertate tab
     * @param suiteName
     * @param features
     * @param strategy
     * @param isActive
     * @param tableHeader
     * @return
     */
    private
    static String generateTab(String suiteName, List<CifyFeature> features, String strategy, boolean isActive, String tableHeader) {
        String tabPaneLString = FileUtils.readFileToString(tabpanelTemplateFile)
        tabPaneLString = tabPaneLString.replace("{OVERVIEW_HTML_PAGE}", getScenarioOverviewString(features, suiteName, strategy))
        tabPaneLString = tabPaneLString.replace("{PASSED_STEP_HTML_PAGE}", getStepsViewString(features, strategy, Status.PASSED))
        tabPaneLString = tabPaneLString.replace("{FAILED_STEP_HTML_PAGE}", getStepsViewString(features, strategy, Status.FAILED))
        tabPaneLString = tabPaneLString.replace("{isActive}", isActive ? "active" : "")
        tabPaneLString = tabPaneLString.replace("{tabId}", strategy)
        tabPaneLString = tabPaneLString.replace("{tableHeader}", tableHeader)
        return tabPaneLString
    }

    /**
     * Get device names
     * @param scenario
     * @return
     */
    private static String getDevicesName(CifyScenario scenario) {

        String devicesString = ""
        scenario.getDevices().each { Map device ->
            if (devicesString != "") {
                devicesString = devicesString + "\n"
            }
            if (!device.get("deviceName") != "") {
                devicesString = devicesString + device.get("deviceName")
            } else {
                devicesString = devicesString + device.get("capability")
            }
        }
        return devicesString
    }

    /**
     * Sort by devices
     * @param features
     * @return
     */
    private static Map sortFeaturesByDevices(List<CifyFeature> features) {
        Map<String, Map<CifyFeature, List<CifyScenario>>> sortedFeatures = [:]
        features.each { CifyFeature feature ->

            feature.getScenarios().each { CifyScenario scenario ->

                String devicesString = getDevicesName(scenario)

                if (!sortedFeatures.get(devicesString)) {
                    sortedFeatures.put(devicesString, [:])
                }

                if (!sortedFeatures.get(devicesString).get(feature)) {
                    sortedFeatures.get(devicesString).put(feature, new ArrayList<CifyScenario>())
                }

                sortedFeatures.get(devicesString).get(feature).add(scenario)
            }
        }
        return sortedFeatures
    }

    /**
     * Sort by features
     * @param features
     * @return
     */
    private static Map sortByFeatures(List<CifyFeature> features) {
        Map<String, List<CifyScenario>> sortedFeatures = [:]
        features.each { CifyFeature feature ->
            if (!sortedFeatures.get(feature.getFeature().getName())) {
                sortedFeatures.put(feature.getFeature().getName(), new ArrayList<>())
            }
            sortedFeatures.get(feature.getFeature().getName()).addAll(feature.getScenarios())
        }
        return sortedFeatures
    }

    /**
     * Sort features by failed and passed
     * @param devices
     * @param featureListMap
     * @return
     */
    private static Map sortByFailedAndPassed(String devices, Map<CifyFeature, List<CifyScenario>> featureListMap) {
        Map<String, Map<Status, Map<CifyFeature, List<CifyScenario>>>> failedPassedMap = [:]
        if (!failedPassedMap.get(devices)) {
            failedPassedMap.put(devices, [:])
            failedPassedMap.get(devices).put(Status.FAILED, [:])
            failedPassedMap.get(devices).put(Status.PASSED, [:])
        }

        featureListMap.each { CifyFeature cifyFeature, List<CifyScenario> scenarios ->
            scenarios.each { CifyScenario scenario ->
                if (!failedPassedMap.get(devices).get(scenario.getStatus())) {
                    failedPassedMap.get(devices).put(scenario.getStatus(), [:])
                }
                if (!failedPassedMap.get(devices).get(scenario.getStatus()).get(cifyFeature)) {
                    failedPassedMap.get(devices).get(scenario.getStatus()).put(cifyFeature, new ArrayList<CifyScenario>())
                }

                failedPassedMap.get(devices).get(scenario.getStatus()).get(cifyFeature).add(scenario)
            }
        }
        return failedPassedMap
    }

    /**
     * Sort features by failed and passed
     * @param featureName
     * @param scenarios
     * @return
     */
    private static Map sortByFailedAndPassed(String featureName, List<CifyScenario> scenarios) {
        Map<String, Map<Status, List<CifyScenario>>> failedPassedMap = [:]
        if (!failedPassedMap.get(featureName)) {
            failedPassedMap.put(featureName, [:])
            failedPassedMap.get(featureName).put(Status.FAILED, [])
            failedPassedMap.get(featureName).put(Status.PASSED, [])
        }

        scenarios.each { CifyScenario scenario ->
            if (!failedPassedMap.get(featureName).get(scenario.getStatus())) {
                failedPassedMap.get(featureName).put(scenario.getStatus(), [])
            }

            failedPassedMap.get(featureName).get(scenario.getStatus()).add(scenario)
        }
        return failedPassedMap
    }

    private static String convertMilliseconds(long duration) {
        String.format("%d hours %d minutes %d seconds",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) -
                        TimeUnit.MILLISECONDS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        )
    }
}
