var map;
var markerLayer;
var lineLayer;
var lineStyle = {
    strokeColor: '#0000ff',
    strokeOpacity: 0.5,
    strokeWidth: 5
};

function init() {
    resizeMap();
    map = new OpenLayers.Map("basicMap", {
        projection: new OpenLayers.Projection("EPSG:4326"),
        displayProjection: new OpenLayers.Projection("EPSG:4326")
    });
    var ol = new OpenLayers.Layer.OSM();
    map.addLayer(ol);
    markerLayer = new OpenLayers.Layer.Markers("Markers");
    map.addLayer(markerLayer);

    lineLayer = new OpenLayers.Layer.Vector("Line Layer");

    map.addLayer(lineLayer);
    map.addControl(new OpenLayers.Control.DrawFeature(lineLayer, OpenLayers.Handler.Path));

    map.setCenter(new OpenLayers.LonLat(13.41, 52.52)
        .transform(
            new OpenLayers.Projection("EPSG:4326"),
            new OpenLayers.Projection("EPSG:900913")
        ), 15
    );
}

function drawLine(arrayOfPoints) {
    lineLayer.destroyFeatures();
    var points = [];
    arrayOfPoints.forEach(function (entry) {
        points.push(new OpenLayers.Geometry.Point(entry[0], entry[1])
            .transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject()))
    });

    var line = new OpenLayers.Geometry.LineString(points);

    var lineFeature = new OpenLayers.Feature.Vector(line, null, lineStyle);
    lineLayer.addFeatures([lineFeature]);

    map.setCenter(new OpenLayers.LonLat(
        $('#start-longitude').val()
        , $('#start-latitude').val())
        .transform(
            new OpenLayers.Projection("EPSG:4326"),
            new OpenLayers.Projection("EPSG:900913")
        ), 15);
}

function addMarker(lat, lon) {
    var lonLat = new OpenLayers.LonLat(lon, lat)
        .transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
    markerLayer.addMarker(new OpenLayers.Marker(lonLat));
}

function addMarkers(coordinates) {
    markerLayer.clearMarkers();
    for (var i = 0; i < coordinates.length; i++) {
        addMarker(coordinates[i][0], coordinates[i][1]);
    }
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
function invalidate(inputField) {
    $(inputField).attr('style', 'box-shadow:inset 0 0 1px 1px #d83c3c !important;');
    $('#submit-route-button').attr('style', 'background: #d83c3c !important;');
}

var spinner;
function startSpinner() {
    var opts = {
        lines: 13 // The number of lines to draw
        , length: 28 // The length of each line
        , width: 14 // The line thickness
        , radius: 42 // The radius of the inner circle
        , scale: 0.5 // Scales overall size of the spinner
        , corners: 1 // Corner roundness (0..1)
        , color: '#000' // #rgb or #rrggbb or array of colors
        , opacity: 0.25 // Opacity of the lines
        , rotate: 0 // The rotation offset
        , direction: 1 // 1: clockwise, -1: counterclockwise
        , speed: 1 // Rounds per second
        , trail: 60 // Afterglow percentage
        , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex: 2e9 // The z-index (defaults to 2000000000)
        , className: 'spinner' // The CSS class to assign to the spinner
        , top: '50%' // Top position relative to parent
        , left: '50%' // Left position relative to parent
        , shadow: false // Whether to render a shadow
        , hwaccel: false // Whether to use hardware acceleration
        , position: 'absolute' // Element positioning
    };
    var target = document.getElementById('spinner');
    spinner = new Spinner(opts).spin(target);
}
function stopSpinner() {
    spinner.stop();
}

