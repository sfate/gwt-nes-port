package com.googlecode.gwtnes.client.halfnes;

import java.util.ArrayList;

//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.SourceDataLine;

import com.googlecode.gwtnes.client.halfnes.audio.ExpansionSoundChip;
import com.googlecode.gwtnes.client.halfnes.audio.NoiseTimer;
import com.googlecode.gwtnes.client.halfnes.audio.SquareTimer;
import com.googlecode.gwtnes.client.halfnes.audio.Timer;
import com.googlecode.gwtnes.client.halfnes.audio.TriangleTimer;

public class APU {

    public int samplerate = 1;
    //private SourceDataLine sdl;
    private final Timer[] timers = {new SquareTimer(8), new SquareTimer(8), new TriangleTimer(), new NoiseTimer()};
    private boolean soundEnable;
    private double cyclespersample;
    public NES nes;
    CPU cpu;
    CPURAM cpuram;
    private int apucycle = 0;
    private int remainder = 0;
    private static final int[] noiseperiod = {4, 8, 16, 32, 64, 96, 128,
        160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
    // different for PAL
    private byte[] audiobuf;
    private int bufptr = 0;
    private int accum = 0;
    private ArrayList<ExpansionSoundChip> expnSound = new ArrayList<ExpansionSoundChip>();
    private boolean soundFiltering;
    private int[] tnd_lookup;
    private int[] square_lookup;
    int outputvol;

    public APU(final NES nes, final CPU cpu, final CPURAM cpuram) {
        //fill square, triangle volume lookup tables
        square_lookup = new int[31];
        for (int i = 0; i < square_lookup.length; ++i) {
            square_lookup[i] = (int) ((95.52 / (8128.0 / i + 100)) * 65536);
        }
        tnd_lookup = new int[203];
        for (int i = 0; i < tnd_lookup.length; ++i) {
            tnd_lookup[i] = (int) ((163.67 / (24329.0 / i + 100)) * 65536);
        }

        //then init the audio stream
        this.nes = nes;
        this.cpu = cpu;
        this.cpuram = cpuram;
        soundEnable = false;//nes.getPrefs().getBoolean("soundEnable", true);
        soundFiltering =false;// nes.getPrefs().getBoolean("soundFiltering", false);
        outputvol = 13107;//nes.getPrefs().getInt("outputvol", 13107);
        if (soundEnable) {
//            samplerate = nes.getPrefs().getInt("sampleRate", 44100);
//            cyclespersample = 1789773.0 / samplerate;
//            final int samplesperframe = (int) Math.ceil(samplerate * 2 / 60.);
//            audiobuf = new byte[(int) (samplesperframe * 2)];
//            //for getting list of Mixers on sytem...
//            //don't really need if we can just do GetSourceDataLine();
//            //but that didn't work in Java 1.4
////            Mixer.Info[] mi = AudioSystem.getMixerInfo();
////            for (Mixer.Info m : mi) {
////                System.err.println(m.toString());
////            }
////            Mixer mixer = AudioSystem.getMixer(mi[0]);
////            Line.Info[] lineinfo = mixer.getSourceLineInfo();
////            for (Line.Info l : lineinfo) {
////                System.err.println(l.toString());
////            }
//            try {
//                AudioFormat af = new AudioFormat(
//                        samplerate,
//                        16,//bit
//                        1,//channel
//                        true,//signed
//                        false //little endian
//                        );
//                sdl = AudioSystem.getSourceDataLine(af);
//                sdl.open(af, samplesperframe * 4); //create 4 frame audio buffer
//                sdl.start();
//            } catch (LineUnavailableException a) {
//                System.err.println(a);
//                nes.messageBox("Unable to inintialize sound.");
//                soundEnable = false;
//            } catch (IllegalArgumentException a) {
//                System.err.println(a);
//                nes.messageBox("Unable to inintialize sound.");
//                soundEnable = false;
//            }
        }
    }

    public void stopAudio() {
//        if (soundEnable) {
//            sdl.flush();
//            sdl.stop();
//        }
    }

    public void startAudio() {
//        if (soundEnable) {
//            sdl.start();
//        }
    }

    public final void destroy() {
//        if (soundEnable) {
//            sdl.stop();
//            sdl.close();
//        }
    }

    public boolean bufferHasLessThan(final int samples) {
        //returns true if the audio buffer has less than the specified amt of samples remaining in it
        return false;//(sdl == null) ? false : ((sdl.getBufferSize() - sdl.available()) <= samples);
    }

    public final int read(final int addr) {
        updateto((int) cpu.cycles);
        switch (addr) {
            case 0x15:
                //returns channel status
                //for future ref: NEED to put those ternary operators in parentheses!
                //otherwise order of operations does the wrong thing.
                final int returnval = ((lengthctr[0] > 0) ? 1 : 0)
                        + ((lengthctr[1] > 0) ? 2 : 0)
                        + ((lengthctr[2] > 0) ? 4 : 0)
                        + ((lengthctr[3] > 0) ? 8 : 0)
                        + ((dmcsamplesleft > 0) ? 16 : 0)
                        + (statusframeint ? 64 : 0)
                        + (statusdmcint ? 128 : 0);
                if (statusframeint) {
                    --cpu.interrupt;
                }
                statusframeint = false;
                //System.err.println("*" + utils.hex(returnval));
                return returnval;
            case 0x16:
                final int tmp = controller1latched & 1;
                controller1latched = ((controller1latched >> 1) | 0x100);
                return tmp | 0x40;//or with 0x40 because open bus sets that bit
            //without that Paperboy and Captain Planet won't read controller
            case 0x17:
                final int tmp2 = controller2latched & 1;
                controller2latched = ((controller2latched >> 1) | 0x100);
                return tmp2 | 0x40;
            default:
                return 0x40; //open bus
        }
    }
    private boolean ctrllatch = false;
    private int controller1latched = 0x00;
    private int controller2latched = 0x00;
    final private static int[] dutylookup = {1, 2, 4, 6};

    public void addExpnSound(ExpansionSoundChip chip) {
        expnSound.add(chip);
    }

    public void write(final int reg, final int data) {
        //This is how values written to any of the APU's memory
        //mapped registers change the state of the system.
        updateto((int) cpu.cycles - 1);
        //System.err.println("Wrote " + utils.hex(data) + " to " + utils.hex(reg) + " @ cycle " + cpu.cycles);
        switch (reg) {
            case 0x0:
                //length counter 1 halt
                lenctrHalt[0] = Utils.getbit(data, 5);
                // pulse 1 duty cycle
                timers[0].setduty(dutylookup[data >> 6]);
                // and envelope
                envConstVolume[0] = Utils.getbit(data, 4);
                envelopeValue[0] = data & 15;
                //setvolumes();
                break;
            case 0x1:
                //pulse 1 sweep setup
                //sweep enabled
                sweepenable[0] = Utils.getbit(data, 7);
                //sweep divider period
                sweepperiod[0] = (data >> 4) & 7;
                //sweep negate flag
                sweepnegate[0] = Utils.getbit(data, 3);
                //sweep shift count
                sweepshift[0] = (data & 7);
                sweepreload[0] = true;
                break;
            case 0x2:
                // pulse 1 timer low bit
                timers[0].setperiod((timers[0].getperiod() & 0xfe00) + (data << 1));
                break;
            case 0x3:
                // length counter load, timer 1 high bits
                if (lenCtrEnable[0]) {
                    lengthctr[0] = lenctrload[data >> 3];
                }
                timers[0].setperiod((timers[0].getperiod() & 0x1ff) + ((data & 7) << 9));
                // sequencer restarted
                timers[0].reset();
                //envelope also restarted
                envelopeStartFlag[0] = true;
                break;
            case 0x4:
                //length counter 2 halt
                lenctrHalt[1] = Utils.getbit(data, 5);
                // pulse 2 duty cycle
                timers[1].setduty(dutylookup[data >> 6]);
                // and envelope
                envConstVolume[1] = Utils.getbit(data, 4);
                envelopeValue[1] = data & 15;
                //setvolumes();
                break;
            case 0x5:
                //pulse 2 sweep setup
                //sweep enabled
                sweepenable[1] = Utils.getbit(data, 7);
                //sweep divider period
                sweepperiod[1] = (data >> 4) & 7;
                //sweep negate flag
                sweepnegate[1] = Utils.getbit(data, 3);
                //sweep shift count
                sweepshift[1] = (data & 7);
                sweepreload[1] = true;
                break;
            case 0x6:
                // pulse 2 timer low bit
                timers[1].setperiod((timers[1].getperiod() & 0xfe00) + (data << 1));
                break;
            case 0x7:
                if (lenCtrEnable[1]) {
                    lengthctr[1] = lenctrload[data >> 3];
                }
                timers[1].setperiod((timers[1].getperiod() & 0x1ff) + ((data & 7) << 9));
                // sequencer restarted
                timers[1].reset();
                //envelope also restarted
                envelopeStartFlag[1] = true;
                break;
            case 0x8:
                //triangle linear counter load
                linctrreload = data & 0x7f;
                //and length counter halt
                lenctrHalt[2] = Utils.getbit(data, 7);
                break;
            case 0x9:
                break;
            case 0xA:
                // triangle low bits of timer
                timers[2].setperiod((((timers[2].getperiod() * 1) & 0xff00) + data) / 1);
                break;
            case 0xB:
                // triangle length counter load
                // and high bits of timer
                if (lenCtrEnable[2]) {
                    lengthctr[2] = lenctrload[data >> 3];
                }
                timers[2].setperiod((((timers[2].getperiod() * 1) & 0xff) + ((data & 7) << 8)) / 1);
                linctrflag = true;
                break;
            case 0xC:
                //noise halt and envelope
                lenctrHalt[3] = Utils.getbit(data, 5);
                envConstVolume[3] = Utils.getbit(data, 4);
                envelopeValue[3] = data & 0xf;
                //setvolumes();
                break;
            case 0xD:
                break;
            case 0xE:
                timers[3].setduty(Utils.getbit(data, 7) ? 6 : 1);
                timers[3].setperiod(noiseperiod[data & 15]);
                break;
            case 0xF:
                //noise length counter load, envelope restart
                if (lenCtrEnable[3]) {
                    lengthctr[3] = lenctrload[data >> 3];
                }
                envelopeStartFlag[3] = true;
                break;
            case 0x10:
                dmcirq = Utils.getbit(data, 7);
                dmcloop = Utils.getbit(data, 6);
                dmcrate = dmcperiods[data & 0xf];
                break;
            case 0x11:
                dmcvalue = data;
                if (dmcvalue > 0x7f) {
                    dmcvalue = 0x7f;
                }
                if (dmcvalue < 0) {
                    dmcvalue = 0;
                }
                break;
            case 0x12:
                dmcstartaddr = (data << 6) + 0xc000;
                break;
            case 0x13:
                dmcsamplelength = (data << 4) + 1;
                break;
            case 0x14:
                //sprite dma
                cpuram.write(0x2003, 0);
                //^shouldn't actually do that... but otherwise gauntlet breaks
                //No Idea what nestopia is doing.
                for (int i = 0; i < 256; ++i) {
                    cpuram.write(0x2004, cpuram.read((data << 8) + i));
                }
                cpu.cycles += 513;
                break;
            case 0x15:
                //status register
                // counter enable(silence channel when bit is off)
                for (int i = 0; i < 4; ++i) {
                    lenCtrEnable[i] = Utils.getbit(data, i);
                    //THIS was the channels not cutting off bug! If you toggle a channel's
                    //status on and off very quickly then the length counter should
                    //IMMEDIATELY be forced to zero.
                    if (!lenCtrEnable[i]) {
                        lengthctr[i] = 0;
                    }
                }
                if (Utils.getbit(data, 4)) {
                    if (dmcsamplesleft == 0) {
                        restartdmc();
                    }
                } else {
                    dmcsamplesleft = 0;
                    dmcsilence = true;
                }
                if (statusdmcint) {
                    --cpu.interrupt;
                }
                statusdmcint = false;
                break;
            case 0x16:
                // strobe controller 1 + 2
                if (Utils.getbit(data, 0)) {
                    ctrllatch = true;
                } else {
                    if (ctrllatch) {
                        controller1latched = nes.getcontroller1().getbyte();
                        controller2latched = nes.getcontroller2().getbyte();
                    }
                    ctrllatch = false;
                }
                break;
            case 0x17:
                ctrmode = Utils.getbit(data, 7) ? 5 : 4;
                apuintflag = Utils.getbit(data, 6);
                framectr = 0;
                if (Utils.getbit(data, 7)) {
                    clockframecounter();
                }
                break;
        }
    }

    public final void updateto(final int cpucycle) {
        //still have to run this even if sound is disabled, some games rely on DMC IRQ etc.
        if (soundFiltering) {
            //linear sampling code

            while (apucycle < cpucycle) {
                ++remainder;
                clockdmc();
                if (apucycle % 7445 == 7440) {
                    clockframecounter();
                }
                timers[0].clock();
                timers[1].clock();
                if (lengthctr[2] > 0 && linearctr > 0) {
                    timers[2].clock();
                }
                timers[3].clock();
                int mixvol = getOutputLevel();
                for (ExpansionSoundChip chip : expnSound) {
                    chip.clock(1);
                }
                accum += mixvol;
                if ((apucycle % cyclespersample) < 1) {
                    //not quite right - there's a non-integer # cycles per sample.
                    accum /= remainder;
                    remainder = 0;
                    outputSample(accum);
                    accum = 0;
                }
                ++apucycle;
            }
        } else {
            //point sampling code
            while (apucycle < cpucycle) {
                ++remainder;
                clockdmc();
                if (apucycle % 7445 == 7440) {
                    clockframecounter();
                }
                if ((apucycle % cyclespersample) < 1) {
                    //not quite right - there's a non-integer # cycles per sample.
                    timers[0].clock(remainder);
                    timers[1].clock(remainder);
                    if (lengthctr[2] > 0 && linearctr > 0) {
                        timers[2].clock(remainder);
                    }
                    timers[3].clock(remainder);
                    int mixvol = getOutputLevel();
                    for (ExpansionSoundChip chip : expnSound) {
                        chip.clock(remainder);
                    }
                    remainder = 0;
                    outputSample(mixvol);
                }
                ++apucycle;
            }
        }
    }

    private int getOutputLevel() {
        int vol = square_lookup[volume[0] * timers[0].getval()
                + volume[1] * timers[1].getval()];
        vol += tnd_lookup[
                    3 * timers[2].getval()
                + 2 * volume[3] * timers[3].getval()
                + dmcvalue];
        for (ExpansionSoundChip chip : expnSound) {
            vol *= 0.8;
            vol += chip.getval();
        }
        return (int) ((vol * (outputvol / 16384.))) ^ 0x8000;
    }

    private void outputSample(final int sample) {
        audiobuf[bufptr] = (byte) (sample & 0xff);
        audiobuf[bufptr + 1] = (byte) ((sample >> 8) & 0xff);
        bufptr += 2;
    }

    public void finishframe() {
//        updateto(29781);
//        apucycle = 0;
//        if (soundEnable) {
////            if (sdl.available() == sdl.getBufferSize()) {
////                System.err.println("Audio is underrun");
////            }
//            if (sdl.available() < bufptr) {
////                System.err.println("Audio is blocking");
//                if (nes.isFrameLimiterOn()) {
//                    //write to audio buffer and don't worry if it blocks
//                    sdl.write(audiobuf, 0, bufptr);
//                }
//                //else don't bother to write if the buffer is full
//            } else {
//                sdl.write(audiobuf, 0, bufptr);
//            }
//        }
//        bufptr = 0;
    }
    private boolean apuintflag = true;
    private boolean statusdmcint = false;
    private boolean statusframeint = false;
    private int framectr = 0;
    private int ctrmode = 4;

    private void clockframecounter() {
        //System.err.println("frame ctr clock " + framectr);
        //should be ~4x a frame, 240 Hz
        if (framectr < 4) {
            setenvelope();
            setlinctr();
        }
        if ((ctrmode == 4 && (framectr == 1 || framectr == 3))
                || (ctrmode == 5 && (framectr == 0 || framectr == 2))) {
            setlength();
            setsweep();
        }
        if (!apuintflag && (framectr == 3) && (ctrmode == 4)) {
            if (!statusframeint && nes.framecount > 8) {
                ++cpu.interrupt;
                //fixes Jurassic Park and Twin Cobra (otherwise they get unexpected interrupts)
                //don't ask why 8 frames is the magic number
                statusframeint = true;
            }

        }
        ++framectr;
        framectr %= ctrmode;
        setvolumes();
    }
    private boolean[] lenCtrEnable = {true, true, true, true};
    private int[] volume = {0, 0, 0, 0};

    private void setvolumes() {
        volume[0] = ((lengthctr[0] <= 0 || sweepsilence[0]) ? 0 : (((envConstVolume[0]) ? envelopeValue[0] : envelopeCounter[0])));
        volume[1] = ((lengthctr[1] <= 0 || sweepsilence[1]) ? 0 : (((envConstVolume[1]) ? envelopeValue[1] : envelopeCounter[1])));
        volume[3] = ((lengthctr[3] <= 0) ? 0 : ((envConstVolume[3]) ? envelopeValue[3] : envelopeCounter[3]));
        //System.err.println("setvolumes " + volume[1]);
    }
    //instance variables for dmc unit
    private final static int[] dmcperiods = {428, 380, 340, 320, 286, 254,
        226, 214, 190, 160, 142, 128, 106, 84, 72, 54};
    private int dmcrate = 0x36;
    private int dmcpos = 0;
    private int dmcshiftregister = 0;
    private int dmcbuffer = 0;
    private int dmcvalue = 0;
    private int dmcsamplelength = 0;
    private int dmcsamplesleft = 0;
    private int dmcstartaddr = 0xc000;
    private int dmcaddr = 0xc000;
    private int dmcbitsleft = 8;
    private boolean dmcsilence = true;
    private boolean dmcirq = false;
    private boolean dmcloop = false;

    private void clockdmc() {
        ++dmcpos;
        if (dmcpos > dmcrate) {
            dmcpos = 0;
            if (!dmcsilence) {
                dmcvalue += (Utils.getbit(dmcshiftregister, 0) ? 2 : -2);
                //DMC output register doesn't wrap around
                if (dmcvalue > 0x7f) {
                    dmcvalue = 0x7f;
                }
                if (dmcvalue < 0) {
                    dmcvalue = 0;
                }
                dmcshiftregister >>= 1;
                --dmcbitsleft;
                if (dmcbitsleft <= 0) {
                    dmcbitsleft = 8;
                    dmcshiftregister = dmcbuffer;
                    dmcfillbuffer();
                }
            }
        }
    }

    private void dmcfillbuffer() {
        if (dmcsamplesleft > 0) {
            dmcbuffer = cpuram.read(dmcaddr++);
            cpu.cycles += 2;
            //DPCM Does steal cpu cycles - this should actually vary between 1-4
            //can't do this properly without a cycle accurate cpu/ppu
            if (dmcaddr > 0xffff) {
                dmcaddr = 0x8000;
            }
            --dmcsamplesleft;
        } else {
            if (dmcloop) {
                restartdmc();
            } else if (dmcirq) {
                ++cpu.interrupt;
                statusdmcint = true;
            } else {
                dmcsilence = true;
            }
        }

    }

    private void restartdmc() {
        dmcaddr = dmcstartaddr;
        dmcsamplesleft = dmcsamplelength;
        dmcsilence = false;
    }
    private int[] lengthctr = {0, 0, 0, 0};
    private final static int[] lenctrload = {10, 254, 20, 2, 40, 4, 80, 6,
        160, 8, 60, 10, 14, 12, 26, 14, 12, 16, 24, 18, 48, 20, 96, 22,
        192, 24, 72, 26, 16, 28, 32, 30};
    private boolean[] lenctrHalt = {true, true, true, true};

    private void setlength() {
        for (int i = 0; i < 4; ++i) {
            if (!lenctrHalt[i] && lengthctr[i] > 0) {
                --lengthctr[i];
                if (lengthctr[i] == 0) {
                    setvolumes();
                }
            }
        }
    }
    private int linearctr = 0;
    private int linctrreload = 0;
    private boolean linctrflag = false;

    private void setlinctr() {
        if (linctrflag) {
            linearctr = linctrreload;
        } else if (linearctr > 0) {
            --linearctr;
        }
        if (!lenctrHalt[2]) {
            linctrflag = false;
        }
    }
    //instance variables for envelope units
    private int[] envelopeValue = {15, 15, 15, 15};
    private int[] envelopeCounter = {0, 0, 0, 0};
    private int[] envelopePos = {0, 0, 0, 0};
    private boolean[] envConstVolume = {true, true, true, true};
    private boolean[] envelopeStartFlag = {false, false, false, false};

    private void setenvelope() {
        for (int i = 0; i < 4; ++i) {
            if (envelopeStartFlag[i]) {
                envelopeStartFlag[i] = false;
                envelopePos[i] = envelopeValue[i];
                envelopeCounter[i] = 15;
            } else {
                --envelopePos[i];
            }
            if (envelopePos[i] <= 0) {
                envelopePos[i] = envelopeValue[i];
                if (envelopeCounter[i] > 0) {
                    --envelopeCounter[i];
                } else if (lenctrHalt[i] && envelopeCounter[i] <= 0) {
                    envelopeCounter[i] = 15;
                }
            }
        }
    }
    //instance variables for sweep unit
    private boolean[] sweepenable = {false, false};
    private boolean[] sweepnegate = {false, false};
    private boolean[] sweepsilence = {false, false};
    private boolean[] sweepreload = {false, false};
    private int[] sweepperiod = {15, 15};
    private int[] sweepshift = {0, 0};
    private int[] sweeppos = {0, 0};

    private void setsweep() {
        for (int i = 0; i < 2; ++i) {
            sweepsilence[i] = false;
            if (sweepreload[i]) {
                sweepreload[i] = false;
                sweeppos[i] = sweepperiod[i];
            } else {
                ++sweeppos[i];
                final int rawperiod = timers[i].getperiod();
                int shiftedperiod = (rawperiod >> sweepshift[i]);
                if (sweepnegate[i]) {
                    //invert bits of period
                    //add 1 on second channel only
                    shiftedperiod = -shiftedperiod + i;
                }
                shiftedperiod += timers[i].getperiod();
                if ((rawperiod < 8)) {
                    // silence channel
                    sweepsilence[i] = true;
                } else if (sweepenable[i] && (sweepshift[i] != 0) && lengthctr[i] > 0
                        && sweeppos[i] > sweepperiod[i]) {
                    sweeppos[i] = 0;
                    timers[i].setperiod(shiftedperiod);
                }
            }
        }
    }
}
