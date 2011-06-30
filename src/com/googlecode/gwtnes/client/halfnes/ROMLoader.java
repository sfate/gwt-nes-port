package com.googlecode.gwtnes.client.halfnes;
//HalfNES, Copyright Andrew Hoffman, October 2010

import com.googlecode.gwtnes.client.halfnes.mappers.BadMapperException;
import com.googlecode.gwtnes.client.halfnes.mappers.Mapper;

public class ROMLoader {
    //this is the oldest code in the project... I'm honestly ashamed
    //at how it's structured but for now it works.
    //TODO: fix this up

    public String name;
    public int prgsize;
    public int chrsize;
    public Mapper.MirrorType scrolltype;
    public int mappertype;
    public int prgoff;
    public int chroff;
    public boolean savesram = false;
    private int[] therom;

    public ROMLoader(byte[] file, String name) {
        this.therom = readfromfile(file);
        this.name = name;
    }
    
    
    public static int[] readfromfile(byte[] file) {


        int[] ints = new int[file.length];
        for (int i = 0; i < file.length; i++) {
            ints[i] = (short) (file[i] & 0xFF);
        }
        return ints;
    }

    public int[] ReadHeader(int len) {
        // iNES header is 16 bytes, nsf header is 128
        return load(len, -16);
    }

    public void parseInesheader() throws BadMapperException {
        int[] inesheader = ReadHeader(16);
        // decode iNES 1.0 headers
        // 1st 4 bytes : $4E $45 $53 $1A
        if (inesheader[0] != 0x4E || inesheader[1] != 0x45
                || inesheader[2] != 0x53 || inesheader[3] != 0x1A) {
            // not a valid file
            if (inesheader[0] == 'U') {
                throw new BadMapperException("This is a UNIF file with the wrong extension");
            }
            throw new BadMapperException("iNES Header Invalid");

        }
        prgsize = 16384 * inesheader[4];
        chrsize = 8192 * inesheader[5];
        scrolltype = Utils.getbit(inesheader[6], 3) ? Mapper.MirrorType.FOUR_SCREEN_MIRROR : (Utils.getbit(inesheader[6], 0) ? Mapper.MirrorType.V_MIRROR : Mapper.MirrorType.H_MIRROR);
        savesram = Utils.getbit(inesheader[6], 1);
        //TODO: 4-screen and single screen
        mappertype = (inesheader[6] >> 4);


        if (inesheader[11] + inesheader[12] + inesheader[13] + inesheader[14]
                + inesheader[15] == 0) {// fix for DiskDude
            mappertype += ((inesheader[7] >> 4) << 4);
        }

        // calc offsets; header not incl. here
        prgoff = 0;
        chroff = 0 + prgsize;
    }

    public int[] load(int size, int offset){
        int[] bindata = new int[size];
        
//        for (var i = 0; i < length; i++) {
//          dest[i + destPos] = src[i + srcPos];
//        }
        
        for(int i=0;i<size;i++) {
        	bindata[i] = therom[i+offset+16];
        }
        //System.arraycopy(therom, offset + 16, bindata, 0, size);
        return bindata;
    }
}
