package io.cify.plugins

import gherkin.formatter.Formatter
import gherkin.formatter.NiceAppendable
import gherkin.formatter.Reporter
import gherkin.formatter.model.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import io.cify.core.CifyFeature
import io.cify.core.CifyScenario
import io.cify.core.CifyStep
import io.cify.core.Status
import io.cify.framework.core.Device
import io.cify.framework.core.DeviceCategory
import io.cify.framework.core.DeviceManager

/**
 * Created by FOB Solutions
 *
 * This class is responsible for providing cucumber run information to cify framework
 * and reporting test results
 */
class CifyReporterPlugin implements Formatter, Reporter {

    private static final long NANO_TO_MILLI_DIVIDER = 1000000L

    def jsonSlurper = new JsonSlurper()
    def report = new JsonBuilder()
    def root = report.report {
        devices(
                [
                        BROWSER: {},
                        IOS    : {},
                        ANDROID: {}
                ]
        )
    }

    CifyFeature cifyFeature
    CifyScenario cifyScenario
    CifyStep cifyStep

    private final NiceAppendable out

    CifyReporterPlugin(Appendable out) {
        this.out = new NiceAppendable(out)
    }

    /**
     * Is called at the beginning of the scenario life cycle, meaning before the first "before" hook.
     * @param scenario the {@link Scenario} of the current lifecycle
     */
    /**
     * Is called in case any syntax error was detected during the parsing of the feature files.
     *
     * @param state the current state of the parser machine
     * @param event detected event
     * @param legalEvents expected event
     * @param uri the URI of the feature file
     * @param line the line number of the event
     */
    @Override
    void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {

    }

    /**
     * Called at the beginning of each feature.
     *
     * @param uri the feature's URI
     */
    @Override
    void uri(String uri) {

    }

    /**
     * Called after the uri, but before the actual feature execution.
     *
     * @param feature the to be executed {@linkplain Feature}
     */
    @Override
    void feature(Feature feature) {

        DeviceCategory.values().each {
            Map caps = DeviceManager.getInstance().getCapabilities().toDesiredCapabilities(it).toJson()
            report.getContent()["report"]["devices"][it as String] = caps
        }

        cifyFeature = new CifyFeature(feature)
    }

    /**
     * Called before the actual execution of the scenario outline step container.
     *
     * @param scenarioOutline the to be executed {@link ScenarioOutline}
     */
    @Override
    void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    /**
     * Called before the actual execution of the scenario examples. This is called after
     * the scenarioOutline,
     * but before any actual scenario example.
     *
     * @param examples the to be executed
     */
    @Override
    void examples(Examples examples) {

    }

    @Override
    void startOfScenarioLifeCycle(Scenario scenario) {
        cifyScenario = new CifyScenario(scenario)
    }

    /**
     * Called before the actual execution of the background step container.
     *
     * @param background the to be executed {@link Background}
     */
    @Override
    void background(Background background) {
        cifyScenario.setBackground(background)
    }

    /**
     * Called before the actual execution of the scenario step container.
     *
     * @param scenario the to be executed {@link Scenario}
     */
    @Override
    void scenario(Scenario scenario) {

    }

    /**
     * Is called for each step of a step container. <b>Attention:</b> All steps are iterated through
     * this method before any step is actually executed.
     *
     * @param step the {@link Step} to be executed
     */
    @Override
    void step(Step step) {
        if (cifyScenario) {
            cifyStep = new CifyStep(step)
            cifyScenario.addStep(cifyStep)
        }
    }

    /**
     * Is called at the end of the scenario life cycle, meaning after the last "after" hook.
     * * @param scenario the {@link Scenario} of the current lifecycle
     */
    @Override
    void endOfScenarioLifeCycle(Scenario scenario) {
        cifyScenario.setEndDate(new Date())
        cifyScenario.setDevices(collectDevicesFromScenario(cifyScenario))
        cifyFeature.addScenario(cifyScenario)
    }

    /**
     * Indicates that the last file has been processed. This should print out any closing output,
     * such as completing the JSON string, but it should *not* close any underlying streams/writers.
     */
    @Override
    void done() {
        out.append(new JsonBuilder(root).toPrettyString())
    }

    /**
     * Closes all underlying streams.
     */
    @Override
    void close() {
        out.close()
    }

    /**
     * Indicates the End-Of-File for a Gherkin document (.feature file)
     */
    @Override
    void eof() {
        cifyFeature.setEndDate(new Date())
        cifyFeature.setDevices(collectDevicesFromFeature(cifyFeature))
        root["report"].put("feature", jsonSlurper.parseText(new JsonBuilder(cifyFeature).toPrettyString()))
    }

    @Override
    void before(Match match, Result result) {
        (cifyScenario.getConditions()["before"] as List).add([match: jsonSlurper.parseText(new JsonBuilder(match).toPrettyString()), result: jsonSlurper.parseText(new JsonBuilder(result).toPrettyString())])
    }

    /**
     * Result step for cucumber, called after every step
     * @param result
     */
    @Override
    void result(Result result) {
        Device device = DeviceManager.getInstance().getAllActiveDevices().find {
            it.active
        }
        Map capabilities = device.getCapabilities().toJson()
        long durationInMilliseconds = result.duration ? result.duration / NANO_TO_MILLI_DIVIDER : 0
        cifyScenario.getNextStep().setDuration(durationInMilliseconds)
        cifyScenario.getNextStep().setDevice(capabilities)
        cifyScenario.getNextStep().setEndDate(new Date())
        cifyScenario.getNextStep().setResult(result)

        if (result.getStatus() == "failed" || result.getStatus() == "skipped") {
            cifyFeature.setStatus(Status.FAILED)
            cifyScenario.setStatus(Status.FAILED)
        } else {
            cifyFeature.setStatus(Status.PASSED)
            cifyScenario.setStatus(Status.PASSED)
        }
    }

    @Override
    void after(Match match, Result result) {
        (cifyScenario.getConditions()["after"] as List).add([match: jsonSlurper.parseText(new JsonBuilder(match).toPrettyString()), result: jsonSlurper.parseText(new JsonBuilder(result).toPrettyString())])
    }

    @Override
    void match(Match match) {

    }

    @Override
    void embedding(String mimeType, byte[] data) {
        cifyStep.addEmbeddings(mimeType, data)
    }

    @Override
    void write(String text) {
        cifyStep.addWritings(text)
    }

    private static List<Map> collectDevicesFromScenario(CifyScenario scenario) {
        List<Map> devices = new ArrayList<>()
        scenario.getSteps().each { CifyStep step ->
            Map device = devices.find {
                (it == step.getDevice())
            }

            if (!device) {
                devices.add(step.getDevice())
            }
        }
        return devices.unique()
    }

    private static List<Map> collectDevicesFromFeature(CifyFeature feature) {
        List<Map> devices = new ArrayList<>()
        feature.getScenarios().each { CifyScenario scenario ->
            devices.addAll(collectDevicesFromScenario(scenario))
        }
        return devices.unique()
    }
}