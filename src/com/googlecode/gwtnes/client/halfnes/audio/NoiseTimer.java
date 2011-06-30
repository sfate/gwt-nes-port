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
public class NoiseTimer extends Timer {

    private int whichbit = 1;
    private int counter = 1;

    public void NoiseTimer() {
        period = 0;
    }

    public void setduty(int duty) {
        whichbit = duty;
    }

    public final void clock() {
        if (--position < 0) {
            position = period;
            counter = (counter >> 1)
                    | ((Utils.getbit(counter, whichbit)
                    ^ Utils.getbit(counter, 0))
                    ? 16384 : 0);
        }
    }

    public final int getval() {
        return (counter & 1);
    }

    @Override
    public void reset() {
        position = 0;
    }

    @Override
    public void clock(final int cycles) {
        for(int i = 0; i < cycles; ++i){
            clock();
        }
    }

    @Override
    public void setperiod(final int newperiod) {
        period = newperiod;
    }
}
