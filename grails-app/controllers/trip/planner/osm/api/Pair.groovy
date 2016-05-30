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


    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a +
                ", b=" + b +
                '}\n';
    }
}
