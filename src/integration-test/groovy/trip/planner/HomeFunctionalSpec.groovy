package trip.planner

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
class HomeFunctionalSpec extends GebSpec {

    public static final String LEIPZIG = "Leipzig"
    public static final String BERLIN = "Berlin"
    public static final String NEW_YORK = "New York"
    public static final String LEIPZIG_FULLNAME = "Leipzig Deutschland"

    def setup() {
    }

    def cleanup() {
    }

    void "test autocompletion of the start-search-field"() {
        when: "A valid city is given"
        HomePage page = browser.to HomePage
        page.enterCity(page.startSearchField, LEIPZIG)

        then: "The autocorrect works"
        assert page.getAutoCompleteText(page.startDropdown).contains(LEIPZIG_FULLNAME)
    }

    void "test autocompletion of the destination-search-field"() {
        when: "A valid city is given"
        HomePage page = browser.to HomePage
        page.enterCity(page.destinationSearchField, LEIPZIG)

        then: "The autocorrect works"
        assert page.getAutoCompleteText(page.destinationDropdown).contains(LEIPZIG_FULLNAME)
    }

    void "test generate route spinner exists"() {
        when: "Valid cities are given"
        HomePage page = browser.to HomePage
        page.enterCity(page.startSearchField, LEIPZIG)
        page.enterCity(page.destinationSearchField, LEIPZIG)
        page.submitButton.click()

        then: "spinner spinns"
        assert page.spinnerExists()
    }

    void "test generate route spinner stops spinning"() {
        when: "Valid cities are given"
        HomePage page = browser.to HomePage
        page.enterCity(page.startSearchField, LEIPZIG)
        page.enterCity(page.destinationSearchField, BERLIN)
        page.submitButton.click()

        then: "spinner stops spinning"
        assert waitFor(155){
            !page.spinnerExists()
        }
    }

    void "test generate route no route exists"() {
        when: "Unconnectable cities are given"
        HomePage page = browser.to HomePage
        page.enterCity(page.startSearchField, LEIPZIG)
        page.enterCity(page.destinationSearchField, NEW_YORK)

        then: "Alert occurs"
        withAlert(wait: 151) {
            page.submitButton.click()
        }
    }
}
