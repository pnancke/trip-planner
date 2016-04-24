<!doctype html>
<html>
<head>
    <asset:stylesheet src="home.css" media="screen, projection"/>
    <asset:stylesheet src="jquery-ui.min.css"/>
    <asset:javascript src="jquery-1.12.3.min.js"/>
    <asset:javascript src="jquery-ui.min.js"/>
    <asset:javascript src="trip-planner-helper.js"/>
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
            minLength: 2,
            select: function (event, ui) {
                return setStartCoordinates.call(this, ui);
            }
        });
    });

    $(function () {
        $('#location-search-destination').autocomplete({
            source: function (request, response) {
                getAutocompleteData(request, response);
            },
            minLength: 2,
            select: function (event, ui) {
                return setDestinationCoordinates.call(this, ui);
            }
        });
    });
</script>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<div class="searchTown">
    <form class="form-wrapper cf" action="javascript:void(0);">
        Please insert and select the start and destination locations below.
        <input id="location-search-start" class="location-verify-color" type="text" placeholder="Starting point"
               oninput="invalidate(this);" required>
        <br/><br/>
        <input id="location-search-destination" class="location-verify-color" type="text" placeholder="Destination"
               oninput="invalidate(this);" required>
        <br/><br/>
        <label for="max-time-selector">Maximum travel time without retention (hours):<br/></label><br/><input
            type="number"
            id="max-time-selector"
            name="max-time-selector"
            class="max-time-selector"
            value="1"
            min="0"
            step="any">
        <br/>

        <p></p><br/>
        <button type="submit" id="submit-route-button" class="standalone-button" onclick="drawRoute()">Submit</button>
    </form>

</div>

<div id="basicMap"></div>
<input type="hidden" id="start-longitude">
<input type="hidden" id="start-latitude">
<input type="hidden" id="destination-longitude">
<input type="hidden" id="destination-latitude">
</body>
</html>
