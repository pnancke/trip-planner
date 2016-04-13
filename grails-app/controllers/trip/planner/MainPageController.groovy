package trip.planner

import trip.planner.apis.PlaceApi
import trip.planner.apis.RestApiResolver
import trip.planner.apis.RouteApi

class MainPageController {


    def index() {}

    def searchField(String term) {
        //TODO: delete sample code and use the given term instead
        RestApiResolver.getResponse(RouteApi.fromStartToEnd("9.256506,49.24001", "8.73083,49.7606"))
        RestApiResolver.getResponse(PlaceApi.place("Leipzig"))
    }
}
