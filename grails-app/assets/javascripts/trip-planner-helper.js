var map;
var markerLayer;
var lineLayer;
var clusterCenterLayer;
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

    clusterCenterLayer = new OpenLayers.Layer.Vector("Cluster Center Layer");
    map.addLayer(clusterCenterLayer);

    map.addControl(new OpenLayers.Control.DrawFeature(lineLayer, OpenLayers.Handler.Path));

    map.setCenter(new OpenLayers.LonLat(13.41, 52.52)
        .transform(
            new OpenLayers.Projection("EPSG:4326"),
            new OpenLayers.Projection("EPSG:900913")
        ), 15
    );
}

function drawLine(arrayOfPoints, startCoordinates) {
    lineLayer.destroyFeatures();
    var points = [];
    arrayOfPoints.forEach(function (entry) {
        points.push(new OpenLayers.Geometry.Point(entry.lon, entry.lat)
            .transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject()))
    });

    var line = new OpenLayers.Geometry.LineString(points);

    var lineFeature = new OpenLayers.Feature.Vector(line, null, lineStyle);
    lineLayer.addFeatures([lineFeature]);

    map.setCenter(new OpenLayers.LonLat(startCoordinates[1], startCoordinates[0]).transform(
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
    for (var i = 0; i < coordinates.length; i++) {
        addMarker(coordinates[i].lat, coordinates[i].lon);
    }
}
function clearMarkers() {
    markerLayer.clearMarkers();
    clusterCenterLayer.destroyFeatures();
}

function drawCircle(lat, lon, range) {
    var current_point = new OpenLayers.Geometry.Point(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
    //see Issue #68
    var circle_geometry = OpenLayers.Geometry.Polygon.createRegularPolygon(current_point, range * 1.66, 50, 0);
    clusterCenterLayer.addFeatures([new OpenLayers.Feature.Vector(circle_geometry)]);
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
                "label": label
            };
            items.push(currItem);
        }
    });
    var uniqueItems = [];
    $.each(items, function(i, el){
        if($.inArray(el.label, uniqueItems) === -1) uniqueItems.push(el.label);
    });
    return uniqueItems;
}

function getAutocompleteData(request, response) {
    var userLang = navigator.language || navigator.userLanguage;
    $.getJSON('https://photon.komoot.de/api/?limit=15', {q: request.term, lang: userLang}, function (data) {
    }).done(function (json) {
        response(generateAutocompleteData(json));
    });
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

