package trip.planner.osm.model

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamImplicit

@XStreamAlias("node")
class Node {

    @XStreamAsAttribute
    Long id
    @XStreamAsAttribute
    Double lat
    @XStreamAsAttribute
    Double lon

    @XStreamImplicit(itemFieldName = "tag")
    List<Tag> tags = new ArrayList<>()


    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", tags=" + tags +
                '}';
    }
}
