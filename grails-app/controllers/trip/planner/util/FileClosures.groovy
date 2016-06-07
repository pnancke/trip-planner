package trip.planner.util

import trip.planner.osm.api.POIApi


class FileClosures {

    public static final Closure<File> BEGIN_STREAM = { File file, String term ->
        file << term.substring(0, term.indexOf("\n</osm>"))
    }


    public static final Closure<File> NORMAL_STREAM = { File file, String term ->
        file << "\t${term.substring(term.indexOf("<node"), term.indexOf("\n</osm>"))}"
    }


    public static final Closure<File> END_STREAM = { File file, String term ->
        file << "\t${term.substring(term.indexOf("<node"), term.size())}"
    }

    public static final Closure<File> ONE_BBOX = { File file, String term ->
        file << term
    }

    public static List<POIApi> convertToFileWritable(List<POIApi> apis) {
        apis.each {
            it.setWriter(NORMAL_STREAM)
        }
        apis.get(0).setWriter(BEGIN_STREAM)
        apis.get(apis.size() - 1).setWriter(END_STREAM)
        apis
    }
}
