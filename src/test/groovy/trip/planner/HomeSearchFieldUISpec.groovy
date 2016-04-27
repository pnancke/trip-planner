package trip.planner

import spock.lang.Ignore
import spock.lang.Specification

class HomeSearchFieldUISpec extends Specification {

    HomeUI home

    def setup() {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        home = new HomeUI()
    }

    @Ignore
    def "fails: button-click without any input"() {
        expect:
        home.open()
        home.clickSubmitButton()
    }

    @Ignore
    def "fails: button-click with a sample input in start"() {
        expect:
        home.open()
        home.fillStartField("Leipzig")
        home.clickSubmitButton()
    }

    @Ignore
    def "fails: button-click with a sample input in destination"() {
        expect:
        home.open()
        home.fillDestinationField("Berlin")
        home.clickSubmitButton()
    }

    @Ignore
    def "fails: button-click without chosen places and inputs"() {
        expect:
        home.open()
        home.fillStartField("Leipzig")
        home.fillDestinationField("Berlin")
        home.clickSubmitButton()
    }
}
