package io.cify.views.common

import io.cify.views.BasePage
import org.apache.commons.io.FileUtils

/**
 * Created by FOB Solutions
 * Generate step page for report
 */
class StepPage {

    static String generateStepPage(String status, String duration, String name, String stepId, String stacktrace) {
        String stepString = FileUtils.readFileToString(BasePage.stepTemplate)
        String stepStatus
        if (status == "passed") {
            stepStatus = "success"
        } else if (status == "failed") {
            stepStatus = "danger"
        } else {
            stepStatus = "warning"
        }

        String stepClass = status == "failed" ? "show" : "hide"

        stepString = stepString.replace("{cucumberStepIsFailed}", (status == "failed").toString())
        stepString = stepString.replace("{cucumberStepClass}", stepClass)
        stepString = stepString.replace("{cucumberStepStatus}", stepStatus)
        stepString = stepString.replace("{cucumberStepDuration}", duration)
        stepString = stepString.replace("{cucumberStepName}", name)
        stepString = stepString.replace("{cucumberStepId}", stepId)
        stepString = stepString.replace(" {STACKTRACE_HTML_PAGE}", stacktrace)
        return stepString
    }

}
