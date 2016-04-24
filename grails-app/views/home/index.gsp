<!doctype html>
<html>
<head>
    <asset:stylesheet src="home.css" media="screen, projection"/>
    <asset:stylesheet src="jquery-ui.min.css"/>
    <asset:javascript src="jquery-1.12.3.min.js"/>
    <asset:javascript src="jquery-ui.min.js"/>
    <title>Trip Planner</title>
</head>

<body onload="init();" onresize="resizeMap()">
<header></header>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script>
    function init() {
        resizeMap();
        var map = new OpenLayers.Map("basicMap");
        var mapnik = new OpenLayers.Layer.OSM();
        map.addLayer(mapnik);
        map.setCenter(new OpenLayers.LonLat(13.41, 52.52) // Center of the map
                .transform(
                        new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
                        new OpenLayers.Projection("EPSG:900913") // to Spherical Mercator Projection
                ), 15 // Zoom level
        );
    }
    function resizeMap() {
        var margin = 20;
        var searchWidth = 475 + margin;
        var searchHeight = 270;
        var searchPercentage = searchWidth / window.innerWidth;
        var mapPercentage = 1 - searchPercentage;
        var basicMapElem = document.getElementById("basicMap");
        var footerSpace = window.innerHeight * 0.15;
        if (mapPercentage < 0.45) {
            basicMapElem.style.height = window.innerHeight - searchHeight - footerSpace + 'px';
        } else {
            basicMapElem.style.width = window.innerWidth * mapPercentage + 'px';
            basicMapElem.style.height = window.innerHeight - margin - footerSpace + 'px';
        }
    }

    function generateAutocompleteData(json) {
        var items = [];
        $.each(json.features, function (key, val) {
            var currItem = {};
            if (val.properties.osm_value != 'administrative' && val.properties.osm_value != 'political') {
                var name = val.properties.name;
                var address = [val.properties.street,
                    val.properties.housenumber,
                    val.properties.city,
                    val.properties.country]
                        .filter(function (str) {
                    return str;
                }).join(' ');
                var label = [name, address].filter(function (str) {
                    return str;
                }).join(', ');
                currItem = {
                    "label": label,
                    "longitude": val.geometry.coordinates[0],
                    "latitude": val.geometry.coordinates[1]
                };
                items.push(currItem);
            }
        });
        return items;
    }

    function getAutocompleteData(request, response) {
        var userLang = navigator.language || navigator.userLanguage;
        $.getJSON('https://photon.komoot.de/api/?limit=15', {q: request.term, lang: userLang}, function (data) {
        }).done(function (json) {
            response(generateAutocompleteData(json));
        });
    }

    function setStartCoordinates(ui) {
        $('#start-longitude').val(ui.item.longitude);
        $('#start-latitude').val(ui.item.latitude);
        $(this).attr('style', 'box-shadow:inset 0 0 1px 1px #4B9741 !important;');
        $('#location-search-start').val(ui.item.label);
        refreshSubmitButtonState();
        return false;
    }

    function setDestinationCoordinates(ui) {
        $('#destination-longitude').val(ui.item.longitude);
        $('#destination-latitude').val(ui.item.latitude);
        $(this).attr('style', 'box-shadow:inset 0 0 1px 1px #4B9741 !important;');
        $('#location-search-destination').val(ui.item.label);
        refreshSubmitButtonState();
        return false;
    }

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

    function areStartAndDestCoordSet() {
        return $('#start-longitude').val()
                && $('#start-latitude').val()
                && $('#destination-longitude').val()
                && $('#destination-latitude').val()
    }

    function refreshSubmitButtonState() {
        if (areStartAndDestCoordSet()) {
            $('#submit-route-button').attr('style', 'background: #4B9741 !important;');
        }
    }

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

    function invalidate(inputField) {
        $(inputField).attr('style', 'box-shadow:inset 0 0 1px 1px #d83c3c !important;');
        $('#submit-route-button').attr('style', 'background: #d83c3c !important;');
    }
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
        <button type="submit" id="submit-route-button" class="standalone-button">Submit</button>
    </form>

</div>

<div id="basicMap"></div>
<input type="hidden" id="start-longitude">
<input type="hidden" id="start-latitude">
<input type="hidden" id="destination-longitude">
<input type="hidden" id="destination-latitude">
</body>
</html>
