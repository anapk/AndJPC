package org.jpc.emulator.peripheral;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jpc.emulator.AbstractHardwareComponent;
import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.motherboard.IOPortCapable;
import org.jpc.emulator.motherboard.IOPortHandler;
import org.jpc.emulator.motherboard.IntervalTimer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    @Nullable
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

    private int getNote()
    {
        double freq = IntervalTimer.PIT_FREQ/pit.getInitialCount(2); //actual frequency in Hz
        if (freq > SPEAKER_MAX_FREQ)
            freq = SPEAKER_MAX_FREQ;
        if (freq < SPEAKER_MIN_FREQ)
            freq = SPEAKER_MIN_FREQ;
        return frequencyToNote(freq);
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
    public int ioPortReadByte(int address)
    {
        int out = pit.getOut(2);
        dummyRefreshClock ^= 1;
        return (speakerOn << 1) | (pit.getGate(2) ? 1 : 0) | (out << 5) |
                (dummyRefreshClock << 4);
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
    public synchronized void ioPortWriteByte(int address, int data)
    {
        if (!enabled)
            return;
        speakerOn = (data >> 1) & 1;
        pit.setGate(2, (data & 1) != 0);
        int mode;
        if ((data & 1 ) == 1)
        {
            if (speakerOn == 1)
            {
                //connect speaker to PIT
                mode = SPEAKER_PIT_ON;
                waitingForPit = 0;
                //play();
            }
            else
            {
                //leave speaker disconnected from following PIT
                mode = SPEAKER_PIT_OFF;
                stopNote(currentNote);
            }
        }
        else
        {
            // zero bit is 0, speaker follows bit 1
            mode = SPEAKER_OFF;
            stopNote(currentNote);
            if (speakerOn != 0)
                LOGGING.log(Level.INFO, "manual speaker management not implemented");
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

    @NonNull
    @Override
    public int[] ioPortsRequested() {
        return new int[] { 97 };
    }

    @Override
    public void loadState(@NonNull final DataInput dataInput) throws IOException {
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
    public void saveState(@NonNull final DataOutput dataOutput) throws IOException {
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
