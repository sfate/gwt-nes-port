/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.gwtnes.client.halfnes.audio;

import com.googlecode.gwtnes.client.halfnes.Utils;

/**
 *
 * @author Andrew
 */
public class FME7SoundChip implements ExpansionSoundChip {
    //not complete... missing volume envelopes and noise channel at the moment.
    //sound test for Gimmick - Hold Select, push Start on title screen

    private final Timer[] timers = {new SquareTimer(32), new SquareTimer(32), new SquareTimer(32)};
    private boolean[] enable = {false, false, false};
    private int[] volume = {0, 0, 0};
    private int apucycle = 0;
    private boolean clocknow = false;
    int currentval = 0;
    private int[] volumetbl = getvoltbl();

    public void write(final int register, final int data) {
        //System.err.println(register + " " + data);
        switch (register) {
            case 0:
                timers[0].setperiod((timers[0].getperiod() & 0xf00) + data);
                break;
            case 1:
                timers[0].setperiod((timers[0].getperiod() & 0xff) + ((data & 0xf) << 8));
                break;
            case 2:
                timers[1].setperiod((timers[1].getperiod() & 0xf00) + data);
                break;
            case 3:
                timers[1].setperiod((timers[1].getperiod() & 0xff) + ((data & 0xf) << 8));
                break;
            case 4:
                timers[2].setperiod((timers[2].getperiod() & 0xf00) + data);
                break;
            case 5:
                timers[2].setperiod((timers[2].getperiod() & 0xff) + ((data & 0xf) << 8));
                break;
            case 7:
                for (int i = 0; i < 3; ++i) {
                    enable[i] = !(Utils.getbit(data, i));
                }
            case 8:
                volume[0] = data & 0xf;
                break;
            case 9:
                volume[1] = data & 0xf;
                break;
            case 0xa:
                volume[2] = data & 0xf;
                break;
        }
    }

    public final void clock(final int cycle) {
        timers[0].clock(cycle);
        timers[1].clock(cycle);
        timers[2].clock(cycle);
    }

    public final int getval() {
        final int mixvol = 100 * (((enable[0] ? volumetbl[volume[0]] : 0) * timers[0].getval()
                + (enable[1] ? volumetbl[volume[1]] : 0) * timers[1].getval())
                + (enable[2] ? volumetbl[volume[2]] : 0) * timers[2].getval());
        return mixvol;
    }

    public static int[] getvoltbl() {
        int[] vols = new int[16];
        for (int i = 0; i < 16; ++i) {
            vols[i] = (int) (Math.pow(1.3, i));
        }
        //utils.printarray(vols);
        return vols;
    }
}
