<!doctype html>
<html>
<head>
    <asset:stylesheet src="home.css" media="screen, projection"/>
    <title>Trip Planner</title>
</head>

<body>
<header></header>

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
        <br/><p></p><br/>
        <button type="submit" class="standalone-button">Submit</button>
    </form>
</div>
</body>
</html>
