package trip.planner.osm.model

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("tag")
class Tag {

    @XStreamAsAttribute
    String k
    @XStreamAsAttribute
    String v

    String getK() {
        return k
    }

    String getV() {
        return v
    }
}
