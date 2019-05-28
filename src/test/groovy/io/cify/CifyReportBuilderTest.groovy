package io.cify

import org.apache.commons.io.FileUtils

/**
 * Created by FOB Solutions
 */
class CifyReportBuilderTest extends GroovyTestCase {
    void setUp() {
        super.setUp()
        new File("build/cify/reports/cify").mkdirs()

        // Copy template reports
        FileUtils.copyInputStreamToFile(new CifyReportBuilderTest().getClass().getClassLoader().getResourceAsStream("reports/login.feature__105757564_0.json"), new File("build/cify/reports/cify/login.feature__105757564_0.json"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilderTest().getClass().getClassLoader().getResourceAsStream("reports/login.feature__1213612198_0.json"), new File("build/cify/reports/cify/login.feature__1213612198_0.json"))
        FileUtils.copyInputStreamToFile(new CifyReportBuilderTest().getClass().getClassLoader().getResourceAsStream("reports/login.feature__-2037089408_0.json"), new File("build/cify/reports/cify/login.feature__-2037089408_0.json"))

    }

    void testGenerateReports() {
        CifyReportBuilder.generateReports("Test Project", "Test suite")
    }
}
