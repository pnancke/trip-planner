package trip.planner

import geb.Page
import geb.navigator.Navigator
import org.openqa.selenium.By

class HomePage extends Page {

    static url = "http://localhost:8080/home/index";

    static content = {
        startSearchField { $(By.id("location-search-start")) }
        destinationSearchField { $(By.id("location-search-destination")) }
        startDropdown(required: false) { $(By.id("ui-id-1")) }
        destinationDropdown(required: false) { $(By.id("ui-id-2")) }
    }

    static at = {
        startSearchField.present
    }

    void enterCity(Navigator searchField, String name) {
        waitFor {
            searchField.displayed
        }
        searchField.value name
    }

    List<String> getAutoCompleteText(Navigator autoComplNav) {
        waitFor(10) { autoComplNav.displayed }
        autoComplNav.children()*.text()
    }
}
