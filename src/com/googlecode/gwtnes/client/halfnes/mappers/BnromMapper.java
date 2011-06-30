package com.googlecode.gwtnes.client.halfnes.mappers;
//HalfNES, Copyright Andrew Hoffman, October 2010
import com.googlecode.gwtnes.client.halfnes.*;

/**
 *
 * @author Andrew
 */
public class BnromMapper extends Mapper {

    @Override
    public void loadrom() throws BadMapperException {
        super.loadrom();
        for (int i = 0; i < 32; ++i) {
            prg_map[i] = (1024 * i) & (prgsize - 1);
        }
        for (int i = 0; i < 8; ++i) {
            chr_map[i] = (1024 * i) & (chrsize - 1);
        }
    }

    @Override
    public void cartWrite(int addr, int data) {
        if (addr < 0x8000 || addr > 0xffff) {
            super.cartWrite(addr,data);
            return;
        }
        //remap all 32k of PRG to 32 x bank #
        int bankstart = 32 * (data & 7);
        for (int i = 0; i < 32; ++i) {
            prg_map[i] = (1024 * (i + bankstart)) & (prgsize - 1);
        }
        ppuram.setmirroring(Utils.getbit(data, 4) ? MirrorType.V_MIRROR : MirrorType.H_MIRROR);

    }
}
