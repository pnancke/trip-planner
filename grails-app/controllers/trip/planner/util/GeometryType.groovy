package trip.planner.util

import org.hibernate.spatial.JTSGeometryType
import org.hibernate.spatial.dialect.mysql.MySQLGeometryTypeDescriptor

class GeometryType extends JTSGeometryType {

    GeometryType() {
        super(new MySQLGeometryTypeDescriptor())
    }
}
