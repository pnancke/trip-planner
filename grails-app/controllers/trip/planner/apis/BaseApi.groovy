package trip.planner.apis

import com.google.common.base.Joiner
import org.apache.commons.collections.map.HashedMap

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 13.04.16 - 23:55
 */
class BaseApi {

    public static final Joiner.MapJoiner REQUEST_PARAM_JOINER = Joiner.on('&').withKeyValueSeparator("=")
    private String baseUri
    private Map<String, String> params

    BaseApi(URI baseUri) {
        this.baseUri = baseUri
        params = new HashedMap();
    }

    def addParams(String name, String value) {
        this.params.put(name, value)
    }

    URI buildRequestURI() {
        new URI(baseUri + "?" + REQUEST_PARAM_JOINER.join(params))
    }
}
