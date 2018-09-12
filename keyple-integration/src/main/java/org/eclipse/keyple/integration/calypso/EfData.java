package org.eclipse.keyple.integration.calypso;

public class EfData {

    private int recNumb;

    private byte sfi;

    private int recSize;

    public EfData(int recNumb, byte sfi, int recSize) {
        this.recNumb = recNumb;
        this.sfi = sfi;
        this.recSize = recSize;
    }

    public int getRecNumb() {
        return recNumb;
    }

    public byte getSfi() {
        return sfi;
    }

    public int getRecSize() {
        return recSize;
    }
}
