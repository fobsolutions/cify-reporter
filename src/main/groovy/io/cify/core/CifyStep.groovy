package io.cify.core

import gherkin.formatter.model.Result
import gherkin.formatter.model.Step

/**
 * Created by FOB Solutions
 */
class CifyStep {
    List embeddings
    List writings
    Step step = null
    Long duration = 0
    Map device
    Result result
    Date startDate
    Date endDate

    CifyStep(Step step) {
        this.step = step
        this.startDate = new Date()
        this.embeddings = new ArrayList()
        this.writings = new ArrayList()
    }

    CifyStep(Map map) {
        this.embeddings = map["embeddings"] as List
        this.writings = map["writings"] as List
        Map stepMap = map["step"] as Map
        this.step = new Step(
                stepMap["comments"] as List,
                stepMap["keyword"] as String,
                stepMap["name"] as String,
                stepMap["line"] as Integer,
                stepMap["rows"] as List,
                null
        )
        Map resultMap = map["result"] as Map

        this.result = new Result(
                resultMap["status"] as String,
                resultMap["duration"] as Long,
                resultMap["errorMessage"] as String
        )
        this.duration = map["duration"] as Long
        this.device = map["device"] as Map
        this.startDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["startDate"] as String)
        this.endDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", map["endDate"] as String)
    }

    void addEmbeddings(String mimeType, byte[] data) {
        embeddings.add([mimeType: mimeType, data: data])
    }

    void addWritings(String text) {
        writings.add(text)
    }
}