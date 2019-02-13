package cse.uta.edu.IPSO;

public class IPSOExprID {
    private int N;
    private int m;

    IPSOExprID(int pN, int pm) {
        N = pN;
        m = pm;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        IPSOExprID key = (IPSOExprID) o;
        if(N != key.N) return false;
        if(m != key.m) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = result * prime + N;
        result = result * prime + m;
        return result;
    }
}
