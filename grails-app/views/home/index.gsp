<!doctype html>
<html>
<head>
    <asset:stylesheet src="home.css" media="screen, projection"/>
    <asset:stylesheet src="jquery-ui.min.css"/>
    <asset:javascript src="jquery-1.12.3.min.js"/>
    <asset:javascript src="jquery-ui.min.js"/>
    <asset:javascript src="trip-planner-helper.js"/>
    <asset:javascript src="spin.js"/>
    <title>Trip Planner</title>
</head>

<body onload="init();" onresize="resizeMap()">
<header></header>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script>
    $(function () {
        $('#location-search-start').autocomplete({
            source: function (request, response) {
                getAutocompleteData(request, response);
            },
            minLength: 2
        });
    });

    $(function () {
        $('#location-search-destination').autocomplete({
            source: function (request, response) {
                getAutocompleteData(request, response);
            },
            minLength: 2
        });
    });

    function changeAreaValue(newValue) {
        document.getElementById("area-range").innerHTML = newValue;
    }

    function drawRoute() {
        getRoute($('#location-search-start').val(), $('#location-search-destination').val(), $('#search-area-range').val());
    }

    function getRoute(start, destination, searchArea) {
        var ONE_HUNDRED_FIVETY_SECONDS = 150000;
        $.ajaxSetup({timeout: ONE_HUNDRED_FIVETY_SECONDS});
        var warningId = window.setTimeout(function () {
            clearMap();
            $('#submit-route-button').prop('disabled', false);
            stopSpinner();
            alert("The server didn't responded after 150s. Please try again later or try a smaller route.");
        }, ONE_HUNDRED_FIVETY_SECONDS);

        var userLang = navigator.language || navigator.userLanguage;
        $('#submit-route-button').prop('disabled', true);
        startSpinner();
        $.get('${g.createLink(controller: "home", action: "getRoute")}?start=' + start + '&destination=' + destination
                + "&lang=" + userLang + "&searchArea=" + searchArea
                , {}, function (data) {
        }).done(function (response) {
            window.clearTimeout(warningId);
                }).done(function (response) {
            clearMap();
            $('#submit-route-button').prop('disabled', false);
            stopSpinner();
            var responseJson = JSON.parse(response);
            if (!responseJson.success) {
                alert(responseJson.error);
            } else if (responseJson.route == undefined || responseJson.route == "") {
                alert("There is no route between the given locations!");
            } else {
                drawLine(responseJson.route, responseJson.startCoordinates);
                var poiClusters = responseJson.pois;
                var pois = [];
                var clusterCenters = [];
                for (var i = 0; i < poiClusters.length; i++) {
                    pois.push.apply(pois, poiClusters[i].points);
                    clusterCenters.push(poiClusters[i].clusterCenter);
                    var METRES_IN_KILOMETRES = 1000;
                    drawCircle(poiClusters[i].clusterCenter.lat,
                            poiClusters[i].clusterCenter.lon,
                            poiClusters[i].clusterRange * METRES_IN_KILOMETRES);
                }
                addMarkers(pois);
            }
        });
    }
</script>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<div class="searchTown">
    <form class="form-wrapper cf" action="javascript:void(0);">
        Please insert and select the start and destination locations below.
        <input id="location-search-start" type="text" placeholder="Starting point" required>
        <br/><br/>
        <input id="location-search-destination" type="text" placeholder="Destination" required>
        <br/><br/>
        <br/><br/><br/>
        <label for="search-area-range">Diameter of search area:</label><span id="area-range">16</span> km
        <input type="range" id="search-area-range" min="8" max="40" value="16" step="8"
               onchange="changeAreaValue(this.value)"/>

        <p></p><br/>
        <button type="submit" id="submit-route-button" class="standalone-button" onclick="drawRoute()">Submit</button>
    </form>

</div>

<div id="basicMap"></div>
<input type="hidden" id="start-longitude">
<input type="hidden" id="start-latitude">
<input type="hidden" id="destination-longitude">
<input type="hidden" id="destination-latitude">

<div id="spinner"></div>
</body>
</html>
