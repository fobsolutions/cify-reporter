package io.cify.core

import gherkin.formatter.model.Background
import gherkin.formatter.model.Scenario

/**
 * Created by FOB Solutions
 */
class CifyScenario {
    Map conditions
    Scenario scenario
    Background background
    Date startDate
    Date endDate
    List<CifyStep> steps
    Status status
    List<Map> devices
    String url

    CifyScenario(Scenario scenario) {
        this.scenario = scenario
        this.startDate = new Date()
        this.steps = new ArrayList()
        this.conditions = [before: [], after: []]
        this.status = Status.WAITING
        this.devices = new ArrayList<>()
        this.url = ""
    }

    CifyScenario(Map map) {
        this.conditions = map["conditions"] as Map
        Map scenarioMap = map["scenario"] as Map
        this.scenario = new Scenario(
                scenarioMap["comments"] as List,
                scenarioMap["tags"] as List,
                scenarioMap["keyword"] as String,
                scenarioMap["name"] as String,
                scenarioMap["description"] as String,
                scenarioMap["line"] as Integer,
                scenarioMap["id"] as String
        )
        this.steps = parseCifySteps(map["steps"] as List)
        this.startDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["startDate"] as String)
        this.endDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["endDate"] as String)
        this.status = map["status"] as Status
        this.devices = map["devices"] as List
    }

    void addStep(CifyStep cifyStep) {
        steps.add(cifyStep)
    }

    CifyStep getNextStep() {
        steps.find {
            it.getResult() == null
        }
    }

    void setUrl(String url) {
        this.url = url
    }

    private static List<CifyStep> parseCifySteps(List steps) {
        List<CifyStep> cifySteps = new ArrayList<>()
        steps.each {
            cifySteps.add(new CifyStep(it as Map))
        }
        return cifySteps
    }
}

