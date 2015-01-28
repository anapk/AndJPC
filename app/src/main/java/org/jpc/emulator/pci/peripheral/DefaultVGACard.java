package org.jpc.emulator.pci.peripheral;

import android.support.annotation.NonNull;

import uk.co.jads.android.jpc.Dimension;
import uk.co.jads.android.jpc.PCMonitor;

public final class DefaultVGACard extends VGACard
{
    private int height;
    private PCMonitor monitor;
    private int[] rawImageData;
    private int width;
    private int xmax;
    private int xmin;
    private int ymax;
    private int ymin;

    @Override
    protected void dirtyDisplayRegion(final int n, final int n2, final int n3, final int n4) {
        this.xmin = Math.min(n, this.xmin);
        this.xmax = Math.max(n + n3, this.xmax);
        this.ymin = Math.min(n2, this.ymin);
        this.ymax = Math.max(n2 + n4, this.ymax);
    }

    public int[] getDisplayBuffer() {
        return this.rawImageData;
    }

    @NonNull
    @Override
    public Dimension getDisplaySize() {
        return new Dimension(this.width, this.height);
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getXMax() {
        return this.xmax;
    }

    public int getXMin() {
        return this.xmin;
    }

    public int getYMax() {
        return this.ymax;
    }

    public int getYMin() {
        return this.ymin;
    }

    public final void prepareUpdate() {
        this.xmin = this.width;
        this.xmax = 0;
        this.ymin = this.height;
        this.ymax = 0;
    }

    @Override
    public void resizeDisplay(final int n, final int n2) {
        this.monitor.resizeDisplay(n, n2);
    }

    @Override
    protected int rgbToPixel(final int n, final int n2, final int n3) {
        return 0xFF000000 | (n & 0xFF) << 16 | (n2 & 0xFF) << 8 | (n3 & 0xFF);
    }

    @Override
    public void saveScreenshot() {
        throw new RuntimeException("Not implemented");
    }

    public void setDisplayBuffer(final int[] rawImageData, final int width, final int height) {
        this.rawImageData = rawImageData;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setMonitor(final PCMonitor monitor) {
        this.monitor = monitor;
    }
}
