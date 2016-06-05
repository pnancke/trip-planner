package trip.planner.osm.api

class Pair<A, B> {

    private A a
    private B b

    Pair(A a, B b) {
        this.a = a
        this.b = b
    }

    A getA() {
        return a
    }

    void setA(A a) {
        this.a = a
    }

    B getB() {
        return b
    }

    void setB(B b) {
        this.b = b
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Pair pair = (Pair) o

        if (a != pair.a) return false
        if (b != pair.b) return false

        return true
    }

    int hashCode() {
        int result
        result = (a != null ? a.hashCode() : 0)
        result = 31 * result + (b != null ? b.hashCode() : 0)
        return result
    }

    public String toString() {
        return "{" + a + ", " + b + '}';
    }
}
