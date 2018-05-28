package io.cify

/**
 * This class is responsible for holding constant values
 *
 * Created by FOB Solutions
 */

class Constants {

    /**
     * Cify report components
     */
    static final String TEMPLATES_PATH = "src/main/resources/templates/"

    /**
     * Cify report static HTML templates
     */
    static final String REPORTS_TEMPLATES_PATH = getClass().getClassLoader().getResource(TEMPLATES_PATH + "report/").getPath()

    /**
     * Cify report path
     */
    static final String REPORT_DIR = "build/cify/reports/cify/html/"

    /**
     * Sample reports
     */
    static final String REPORT_PATH = getClass().getClassLoader().getResource("src/main/resources/templates/example").getPath()

}
