package trip.planner.osm.model

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamImplicit

@XStreamAlias("osm")
class POIRequest {

    @XStreamImplicit(itemFieldName = "node")
    List<Node> nodes = new ArrayList<>()

    String remark
}
