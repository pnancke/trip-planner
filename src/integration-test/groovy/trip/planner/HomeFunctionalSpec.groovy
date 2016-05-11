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
    public static final String LEIPZIG_FULLNAME = "Leipzig, Leipzig Deutschland"

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
}
