/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.gwtnes.client.halfnes;

import java.util.Arrays;

import com.googlecode.gwtnes.client.halfnes.mappers.Mapper;

/**
 *
 * @author Andrew
 */
public class PPURAM {
    public PPU ppu;
    // PPU nametables
    private final int[] pput0 = new int[0x400];
    private final int[] pput1 = new int[0x400];
    private final int[] pput2 = new int[0x400];
    private final int[] pput3 = new int[0x400];
    //99% of games only use 2 of these, but we have to create 4 and use ptrs to them
    //for those with extra RAM for 4 screen mirror
    private int[] nt0;
    private int[] nt1;
    private int[] nt2;
    private int[] nt3;
    //and these are pointers to the nametables, so  for singlescreen when we switch
    //and then switch back the data in the other singlescreen NT isn't gone.
    // PPU pallette
    int[] pal = new int[0x20];
    private Mapper mapper;

    public PPURAM(Mapper mapper, Mapper.MirrorType mirror) {
        this.mapper = mapper;
        // init memory

        Arrays.fill(pput0, 0xa0);
        Arrays.fill(pput1, 0xb0);
        Arrays.fill(pput2, 0xc0);
        Arrays.fill(pput3, 0xd0);
        Arrays.fill(pal, 32);
        setmirroring(mirror);
    }

    public final void setmirroring(final Mapper.MirrorType type) {
        switch (type) {
            case H_MIRROR:
                nt0 = pput0;
                nt1 = pput0;
                nt2 = pput1;
                nt3 = pput1;
                break;
            case V_MIRROR:
                nt0 = pput0;
                nt1 = pput1;
                nt2 = pput0;
                nt3 = pput1;

                break;
            case SS_MIRROR0:
                nt0 = pput0;
                nt1 = pput0;
                nt2 = pput0;
                nt3 = pput0;
                break;
            case SS_MIRROR1:
                nt0 = pput1;
                nt1 = pput1;
                nt2 = pput1;
                nt3 = pput1;
                break;
            case FOUR_SCREEN_MIRROR:
            default:
                nt0 = pput0;
                nt1 = pput1;
                nt2 = pput2;
                nt3 = pput3;
                break;
        }
    }

    public int read(int addr) {
    	
        addr &= 0x3fff;
        switch ((addr) >> 12) {
            case 0:
            case 1:
                return mapper.ppuRead(addr);
            case 2:
            case 3:
                if (addr >= 0x3f00 && addr <= 0x3fff) {
                    addr &= 0x1f;
                    if (addr >= 0x10 && ((addr & 3) == 0)) {
                        addr -= 0x10;
                    }
                    return pal[addr];
                }
                switch (((addr - 0x2000) & 0xfff) >> 10) {
                    case 0:
                        return nt0[(addr - 0x2000) & 0xfff];
                    case 1:
                        return nt1[(addr - 0x2400) & 0xfff];
                    case 2:
                        return nt2[(addr - 0x2800) & 0xfff];
                    case 3:
                    default:
                        return nt3[(addr - 0x2c00) & 0xfff];
                }
            default:
                System.err.println("THE PPU HAS BEEN KIDNAPPED BY READS! "
                        + Utils.hex(addr)
                        + "\nARE YOU A BAD ENOUGH DUDE TO RESCUE THE PPU?");

        }
        return addr >> 8;
    }

    public void write(int addr, final int data) {
        addr &= 0x3fff;
        switch ((addr) >> 12) {
            case 0:
            case 1:
                mapper.ppuWrite(addr, data);
                break;
            case 2:
            case 3:
                if (addr >= 0x3f00 && addr <= 0x3fff) {
                    addr &= 0x1f;
                    //System.err.println("wrote "+utils.hex(data)+" to palette index " + utils.hex(addr));
                    if (addr >= 0x10 && ((addr & 3) == 0)) { //0x10,0x14,0x18 etc are mirrors of 0x0, 0x4,0x8 etc
                        addr -= 0x10;
                    }
                    pal[addr] = (data & 0x3f);
                    break;
                }
                switch (((addr - 0x2000) & 0xfff) / 0x400) {
                    case 0:
                        nt0[(addr - 0x2000) & 0xfff] = data;
                        break;
                    case 1:
                        nt1[(addr - 0x2400) & 0xfff] = data;
                        break;
                    case 2:
                        nt2[(addr - 0x2800) & 0xfff] = data;
                        break;
                    case 3:
                        nt3[(addr - 0x2c00) & 0xfff] = data;
                        ;
                        break;
                    default:

                }
                break;
            default:
                System.err.println("THE PPU HAS BEEN KIDNAPPED BY WRITES! "
                        + Utils.hex(addr)
                        + "\nARE YOU A BAD ENOUGH DUDE TO RESCUE THE PPU?");
        }
    }
}
