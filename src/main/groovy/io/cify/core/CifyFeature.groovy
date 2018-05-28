package io.cify.core

import gherkin.formatter.model.Feature

/**
 * Created by FOB Solutions
 */
class CifyFeature {
    Feature feature
    Date startDate
    List<CifyScenario> scenarios
    Date endDate
    Status status
    List<Map> devices

    CifyFeature(Map map) {
        Map featureMap = map["feature"] as Map
        Feature feature = new Feature(
                featureMap["comments"] as List,
                featureMap["tags"] as List,
                featureMap["keyword"] as String,
                featureMap["name"] as String,
                featureMap["description"] as String,
                featureMap["line"] as Integer,
                featureMap["id"] as String
        )
        this.feature = feature
        this.startDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["startDate"] as String)
        this.scenarios = parseScenarios(map["scenarios"] as List)
        this.endDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["endDate"] as String)
        this.status = map["status"] as Status
        this.devices = map["devices"] as List
    }

    CifyFeature(Feature feature) {
        this.feature = feature
        this.startDate = new Date()
        this.scenarios = new ArrayList()
        status = Status.WAITING
        this.devices = new ArrayList<>()
    }

    void addScenario(CifyScenario cifyScenario) {
        scenarios.add(cifyScenario)
    }

    private static List<CifyScenario> parseScenarios(List scenarios) {
        List<CifyScenario> cifyScenarios = new ArrayList<>()
        scenarios.each {
            cifyScenarios.add(new CifyScenario(it as Map))
        }
        return cifyScenarios
    }
}