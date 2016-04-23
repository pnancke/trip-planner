<!doctype html>
<html>
<head>
    <asset:stylesheet src="home.css" media="screen, projection"/>
    <title>Trip Planner</title>
</head>

<body onload="init();" onresize="resizeMap()">
<header></header>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script>
    function init() {
        resizeMap();
        map = new OpenLayers.Map("basicMap");
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
            console.log("map under searchTown-field");
        } else {
            basicMapElem.style.width = window.innerWidth * mapPercentage + 'px';
            basicMapElem.style.height = window.innerHeight - margin - footerSpace + 'px';
        }
    }
</script>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<div class="searchTown">
    <form class="form-wrapper cf" action="javascript:void(0);">
        <input id="town-search-start" type="text" placeholder="Starting point" required>
        <button type="submit" onclick="">Search</button>
        <br/><br/>
        <input id="town-search-end" type="text" placeholder="Destination" required>
        <button type="submit" onclick="">Search</button>
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
        <button type="submit" class="standalone-button">Submit</button>
    </form>

</div>

<div id="basicMap"></div>
</body>
</html>
