package org.jpc.emulator.peripheral;

import org.jpc.emulator.*;
import org.jpc.emulator.motherboard.*;
import java.util.logging.*;
import java.io.*;

public class PCSpeaker extends AbstractHardwareComponent implements IOPortCapable
{
    private static final Logger LOGGING;
    private static final int SPEAKER_MAX_FREQ = 11025;
    private static final int SPEAKER_MIN_FREQ = 10;
    private static final int SPEAKER_OFF = 0;
    private static final int SPEAKER_ON = 2;
    private static final int SPEAKER_PIT_OFF = 1;
    private static final int SPEAKER_PIT_ON = 3;
    private static final int SPEAKER_SAMPLE_RATE = 22050;
    private static final int SPEAKER_VOLUME = 16000;
    private int currentNote;
    private int dummyRefreshClock;
    private boolean enabled;
    private boolean ioportRegistered;
    private int mode;
    private IntervalTimer pit;
    private int speakerOn;
    private int waitingForPit;

    static {
        LOGGING = Logger.getLogger(PCSpeaker.class.getName());
    }

    public PCSpeaker() {
        int velocity = 90;
        this.enabled = false;
        this.ioportRegistered = false;
        if (this.enabled) {
            this.configure();
        }
    }

    private void configure() {
        this.enabled = false;
    }

    private static int frequencyToNote(final double n) {
        return 69 + (int)(12.0 * (Math.log(n) - Math.log(440.0)) / Math.log(2.0));
    }

    private int getNote() {
        double n = 1193182 / this.pit.getInitialCount(2);
        if (n > 11025.0) {
            n = 11025.0;
        }
        if (n < 10.0) {
            n = 10.0;
        }
        return frequencyToNote(n);
    }

    private void playNote(final int n) {
    }

    private void programChange(final int n) {
    }

    private void stopNote(final int n) {
    }

    @Override
    public void acceptComponent(final HardwareComponent hardwareComponent) {
        if (hardwareComponent instanceof IntervalTimer && hardwareComponent.initialised()) {
            this.pit = (IntervalTimer)hardwareComponent;
        }
        if (hardwareComponent instanceof IOPortHandler && hardwareComponent.initialised()) {
            ((IOPortHandler)hardwareComponent).registerIOPortCapable(this);
            this.ioportRegistered = true;
        }
    }

    public void enable(final boolean b) {
        if (!b) {
            this.enabled = false;
            return;
        }
        this.enabled = true;
        this.configure();
    }

    @Override
    public boolean initialised() {
        return this.ioportRegistered && this.pit != null;
    }

    @Override
    public int ioPortReadByte(final int n) {
        final int out = this.pit.getOut(2);
        this.dummyRefreshClock ^= 0x1;
        final int n2 = this.speakerOn << 1;
        boolean b;
        if (this.pit.getGate(2)) {
            b = true;
        }
        else {
            b = false;
        }
        return (b ? 1 : 0) | n2 | out << 5 | this.dummyRefreshClock << 4;
    }

    @Override
    public int ioPortReadLong(final int n) {
        return (0xFFFF & this.ioPortReadWord(n)) | (0xFFFF0000 & this.ioPortReadWord(n + 2) << 16);
    }

    @Override
    public int ioPortReadWord(final int n) {
        return (0xFF & this.ioPortReadByte(n)) | (0xFF00 & this.ioPortReadByte(n + 1) << 8);
    }

    @Override
    public void ioPortWriteByte(final int n, final int n2) {
        while (true) {
            Label_0108: {
                Label_0092: {
                    synchronized (this) {
                        if (this.enabled) {
                            this.speakerOn = (0x1 & n2 >> 1);
                            final IntervalTimer pit = this.pit;
                            final int n3 = n2 & 0x1;
                            boolean b = false;
                            if (n3 != 0) {
                                b = true;
                            }
                            pit.setGate(2, b);
                            if ((n2 & 0x1) != 0x1) {
                                break Label_0108;
                            }
                            if (this.speakerOn != 1) {
                                break Label_0092;
                            }
                            this.mode = 3;
                            this.waitingForPit = 0;
                        }
                        return;
                    }
                }
                this.mode = 1;
                this.stopNote(this.currentNote);
                return;
            }
            this.mode = 0;
            this.stopNote(this.currentNote);
            if (this.speakerOn != 0) {
                PCSpeaker.LOGGING.log(Level.INFO, "manual speaker management not implemented");
            }
        }
    }

    @Override
    public void ioPortWriteLong(final int n, final int n2) {
        this.ioPortWriteWord(n, n2);
        this.ioPortWriteWord(n + 2, n2 >> 16);
    }

    @Override
    public void ioPortWriteWord(final int n, final int n2) {
        this.ioPortWriteByte(n, n2);
        this.ioPortWriteByte(n + 1, n2 >> 8);
    }

    @Override
    public int[] ioPortsRequested() {
        return new int[] { 97 };
    }

    @Override
    public void loadState(final DataInput dataInput) throws IOException {
        this.ioportRegistered = false;
        this.dummyRefreshClock = dataInput.readInt();
        this.speakerOn = dataInput.readInt();
    }

    public void play() {
        synchronized (this) {
            ++this.waitingForPit;
            if (this.enabled && this.waitingForPit == 2 && this.pit.getMode(2) == 3) {
                int lastNote = this.currentNote;
                this.currentNote = this.getNote();
                this.stopNote(lastNote);
                this.playNote(this.currentNote);
            }
        }
    }

    @Override
    public void reset() {
        this.pit = null;
        this.ioportRegistered = false;
    }

    @Override
    public void saveState(final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.dummyRefreshClock);
        dataOutput.writeInt(this.speakerOn);
    }

    @Override
    public void updateComponent(final HardwareComponent hardwareComponent) {
        if (hardwareComponent instanceof IOPortHandler && hardwareComponent.updated()) {
            ((IOPortHandler)hardwareComponent).registerIOPortCapable(this);
            this.ioportRegistered = true;
        }
    }

    @Override
    public boolean updated() {
        return this.ioportRegistered && this.pit.updated();
    }
}
