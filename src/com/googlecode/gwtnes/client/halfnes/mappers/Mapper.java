package com.googlecode.gwtnes.client.halfnes.mappers;
//HalfNES, Copyright Andrew Hoffman, October 2010
import java.util.Arrays;

import com.googlecode.gwtnes.client.halfnes.CPU;
import com.googlecode.gwtnes.client.halfnes.CPURAM;
import com.googlecode.gwtnes.client.halfnes.PPU;
import com.googlecode.gwtnes.client.halfnes.PPURAM;
import com.googlecode.gwtnes.client.halfnes.ROMLoader;

public abstract class Mapper {

    private ROMLoader loader;
    protected int mappertype = 0, prgsize = 0, prgoff = 0, chroff = 0, chrsize = 0;
    public CPU cpu;
    public CPURAM cpuram;
    public PPURAM ppuram;
    public PPU ppu;
    protected int[] prg;
    protected int[] chr;
    protected MirrorType scrolltype;
    protected int[] chr_map;
    protected int[] prg_map;
    protected boolean haschrram = false;
    protected boolean hasprgram = true;
    protected boolean savesram = false;
    protected int[] prgram = new int[8192];

    public boolean supportsSaves() {
        return savesram;
    }

    public static enum MirrorType {

        H_MIRROR, V_MIRROR, SS_MIRROR0, SS_MIRROR1, FOUR_SCREEN_MIRROR
    };

    public void loadrom() throws BadMapperException {
        loader.parseInesheader();
        prgsize = loader.prgsize;
        mappertype = loader.mappertype;
        prgoff = loader.prgoff;
        chroff = loader.chroff;
        chrsize = loader.chrsize;
        scrolltype = loader.scrolltype;
        savesram = loader.savesram;
        prg = loader.load(prgsize, prgoff);
        chr = loader.load(chrsize, chroff);

        if (chrsize == 0) {//chr ram
            haschrram = true;
            chrsize = 8192;
            chr = new int[8192];
        }
        prg_map = new int[32];
        for (int i = 0; i < 32; ++i) {
            prg_map[i] = (1024 * i) & (prgsize - 1);
        }
        chr_map = new int[8];
        for (int i = 0; i < 8; ++i) {
            chr_map[i] = (1024 * i) & (chrsize - 1);
        }
        cpuram = new CPURAM(this);
        ppuram = new PPURAM(this, this.scrolltype);
        cpu = new CPU(cpuram);
        ppu = new PPU(ppuram);
        ppuram.ppu = ppu;

    }
    //write into the cartridge's address space

    public void cartWrite(final int addr, final int data) {
        //default no-mapper operation just writes if in PRG RAM range
        if (addr >= 0x6000 && addr <= 0x8000) {
            prgram[addr - 0x6000] = data;
        }
    }

    public int cartRead(final int addr) {
        // by default has wram at 0x6000 and cartridge at 0x8000-0xfff
        // but some mappers have different so override for those
        if (addr >= 0x6000) {
            if (addr < 0x8000) {
                return hasprgram ? prgram[addr - 0x6000] : (addr >> 8);
            } else {
                return prg[prg_map[((addr & 0xffff) - 0x8000) >> 10] + (addr & 1023)];
            }
        }
        return addr >> 8; //open bus
    }

    public int ppuRead(final int addr) {
        return chr[chr_map[addr >> 10] + (addr & 1023)];
    }

    public void ppuWrite(final int addr, final int data) {
        if (haschrram) {
            // Shame on you, Milon's Secret Castle. What possible
            // reason could you have to write to your own chr rom?
            // ohh wait, that's right, banking.
            chr[chr_map[addr >> 10] + (addr & 1023)] = data;
            // anyway, only allowing writes when there's actual ram here.
        }
    }

    public void notifyscanline(final int scanline) {
        //this is empty so that mappers w/o a scanline counter need not implement
    }

    public static Mapper getCorrectMapper(final int type) throws BadMapperException {
        switch (type) {
            case 0:
                return new NromMapper();
            case 1:
                return new MMC1Mapper();
            case 2:
            case 71:
                return new UnromMapper();
            case 3:
                return new CnromMapper();
            case 4:
                return new MMC3Mapper();
            case 7:
                return new AnromMapper();
            case 9:
                return new MMC2Mapper();
            case 11:
                return new ColorDreamsMapper();
            case 21:
            case 23:
            case 25:
                //VRC4 has three different mapper numbers for six differnet address line layouts
                //but they're all handled in the same file
                return new VRC4Mapper(type);
            case 22:
                return new VRC2Mapper();
            case 24:
            case 26:
                return new VRC6Mapper(type);
            case 34:
                return new BnromMapper();
            case 66:
                return new GnromMapper();
            case 69:
                return new FME7Mapper();
            case 78:
                return new Mapper78();
            case 87:
                return new Mapper87();
            case 184:
                return new Sunsoft01Mapper();
            default:
                System.err.println("unsupported mapper # " + type);
                throw new BadMapperException("Unsupported mapper: " + type);
        }
    }

    public String getrominfo() {
        return ("ROM INFO: \n"
                + "Filename:     " + loader.name + "\n"
                + "Mapper:       " + mappertype + "\n"
                + "PRG Size:     " + prgsize / 1024 + " K\n"
                + "CHR Size:     " + (haschrram ? 0 : chrsize / 1024) + " K\n"
                + "Mirroring:    " + scrolltype.toString() + "\n"
                + "Battery Save: " + ((savesram) ? "Yes" : "No"));
    }

    public boolean hasSRAM() {
        return savesram;
    }

    public void setLoader(final ROMLoader l) {
        loader = l;
    }

    public CPURAM getCPURAM() {
        return cpuram;
    }

    public void setPRGRAM(final int[] newprgram) {
    	prgram = new int[newprgram.length];
    	System.arraycopy(newprgram, 0, prgram, 0, prgram.length);
    		
//        prgram = newprgram.clone();

    }

    public int[] getPRGRam() {
    	int[] newprgram = new int[prgram.length];
    	System.arraycopy(prgram, 0, newprgram, 0, newprgram.length);
    	return newprgram;
//        return prgram.clone();
    }
}
