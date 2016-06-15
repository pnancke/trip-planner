package trip.planner

import com.google.common.base.Preconditions
import trip.planner.osm.api.Point

class PointOfInterest {

    Integer poiId
    String osm_id
    Double lat
    Double lon
    String access
    String addr_housename
    String addr_housenumber
    String addr_interpolation
    String admin_level
    String aerialway
    String aeroway
    String amenity
    String area
    String barrier
    String bicycle
    String brand
    String bridge
    String boundary
    String building
    String capital
    String construction
    String covered
    String culvert
    String cutting
    String denomination
    String disused
    String ele
    String embankment
    String foot
    String generator_source
    String harbour
    String highway
    String historic
    String horse
    String intermittent
    String junction
    String landuse
    String layer
    String leisure
    String ship_lock
    String man_made
    String military
    String motorcar
    String name
    String osm_natural
    String office
    String oneway
    String operator
    String place
    String poi
    String population
    String power
    String power_source
    String public_transport
    String railway
    String ref
    String religion
    String route
    String service
    String shop
    String sport
    String surface
    String toll
    String tourism
    String tower_type
    String tunnel
    String water
    String waterway
    String wetland
    String width

    static constraints = {
        access nullable: true
        addr_housename nullable: true
        addr_housenumber nullable: true
        addr_interpolation nullable: true
        admin_level nullable: true
        aerialway nullable: true
        aeroway nullable: true
        amenity nullable: true
        area nullable: true
        barrier nullable: true
        bicycle nullable: true
        brand nullable: true
        bridge nullable: true
        boundary nullable: true
        building nullable: true
        capital nullable: true
        construction nullable: true
        covered nullable: true
        culvert nullable: true
        cutting nullable: true
        denomination nullable: true
        disused nullable: true
        ele nullable: true
        embankment nullable: true
        foot nullable: true
        generator_source nullable: true
        harbour nullable: true
        highway nullable: true
        historic nullable: true
        horse nullable: true
        intermittent nullable: true
        junction nullable: true
        landuse nullable: true
        layer nullable: true
        leisure nullable: true
        ship_lock nullable: true
        man_made nullable: true
        military nullable: true
        motorcar nullable: true
        name nullable: true
        osm_natural nullable: true
        office nullable: true
        oneway nullable: true
        operator nullable: true
        place nullable: true
        poi nullable: true
        population nullable: true
        power nullable: true
        power_source nullable: true
        public_transport nullable: true
        railway nullable: true
        ref nullable: true
        religion nullable: true
        route nullable: true
        service nullable: true
        shop nullable: true
        sport nullable: true
        surface nullable: true
        toll nullable: true
        tourism nullable: true
        tower_type nullable: true
        tunnel nullable: true
        water nullable: true
        waterway nullable: true
        wetland nullable: true
        width nullable: true
    }

    static mapping = {
        table 'point_of_interest'
        id column: "poi_id",
                name: "poiId",
                type: 'integer',
                generator: 'identity'
    }

    static List<PointOfInterest> getPOIsInBBox(Point start, Point end) {
        if (!coordinatesAreInRightOrder(start, end)) {
            Point tmp = start
            start = end
            end = tmp
        }
        findAllByLatBetweenAndLonBetween(start.lat, end.lat, start.lon, end.lon)
    }

    static List<PointOfInterest> getPOIsInRouteArea(List<Point> route, Double area) {
        List<PointOfInterest> pois = new ArrayList<>()
        route.each {
            List<PointOfInterest> between = getPOIsInBBox(
                    new Point(it.lon - area, it.lat - area), new Point(it.lon + area, it.lat + area))
            pois.addAll(between)
        }
        pois.unique()
    }

    static boolean coordinatesAreInRightOrder(Point start, Point destination) {
        Preconditions.checkNotNull(start.lon)
        Preconditions.checkNotNull(start.lat)
        Preconditions.checkNotNull(destination.lon)
        Preconditions.checkNotNull(destination.lat)

        if (start.lat == destination.lat && start.lon == destination.lon) {
            throw new IllegalArgumentException("The start-position equals the destination-position!")
        }

        return (start.lon <= destination.lon && start.lat <= destination.lat)
    }

    @Override
    public String toString() {
        return "PointOfInterest{" +
                "id=" + id +
                ", poiId=" + poiId +
                ", osm_id='" + osm_id + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", access='" + access + '\'' +
                ", addr_housename='" + addr_housename + '\'' +
                ", addr_housenumber='" + addr_housenumber + '\'' +
                ", addr_interpolation='" + addr_interpolation + '\'' +
                ", admin_level='" + admin_level + '\'' +
                ", aerialway='" + aerialway + '\'' +
                ", aeroway='" + aeroway + '\'' +
                ", amenity='" + amenity + '\'' +
                ", area='" + area + '\'' +
                ", barrier='" + barrier + '\'' +
                ", bicycle='" + bicycle + '\'' +
                ", brand='" + brand + '\'' +
                ", bridge='" + bridge + '\'' +
                ", boundary='" + boundary + '\'' +
                ", building='" + building + '\'' +
                ", capital='" + capital + '\'' +
                ", construction='" + construction + '\'' +
                ", covered='" + covered + '\'' +
                ", culvert='" + culvert + '\'' +
                ", cutting='" + cutting + '\'' +
                ", denomination='" + denomination + '\'' +
                ", disused='" + disused + '\'' +
                ", ele='" + ele + '\'' +
                ", embankment='" + embankment + '\'' +
                ", foot='" + foot + '\'' +
                ", generator_source='" + generator_source + '\'' +
                ", harbour='" + harbour + '\'' +
                ", highway='" + highway + '\'' +
                ", historic='" + historic + '\'' +
                ", horse='" + horse + '\'' +
                ", intermittent='" + intermittent + '\'' +
                ", junction='" + junction + '\'' +
                ", landuse='" + landuse + '\'' +
                ", layer='" + layer + '\'' +
                ", leisure='" + leisure + '\'' +
                ", ship_lock='" + ship_lock + '\'' +
                ", man_made='" + man_made + '\'' +
                ", military='" + military + '\'' +
                ", motorcar='" + motorcar + '\'' +
                ", name='" + name + '\'' +
                ", osm_natural='" + osm_natural + '\'' +
                ", office='" + office + '\'' +
                ", oneway='" + oneway + '\'' +
                ", operator='" + operator + '\'' +
                ", place='" + place + '\'' +
                ", poi='" + poi + '\'' +
                ", population='" + population + '\'' +
                ", power='" + power + '\'' +
                ", power_source='" + power_source + '\'' +
                ", public_transport='" + public_transport + '\'' +
                ", railway='" + railway + '\'' +
                ", ref='" + ref + '\'' +
                ", religion='" + religion + '\'' +
                ", route='" + route + '\'' +
                ", service='" + service + '\'' +
                ", shop='" + shop + '\'' +
                ", sport='" + sport + '\'' +
                ", surface='" + surface + '\'' +
                ", toll='" + toll + '\'' +
                ", tourism='" + tourism + '\'' +
                ", tower_type='" + tower_type + '\'' +
                ", tunnel='" + tunnel + '\'' +
                ", water='" + water + '\'' +
                ", waterway='" + waterway + '\'' +
                ", wetland='" + wetland + '\'' +
                ", width='" + width + '\'' +
                ", version=" + version +
                '}';
    }
}
