package com.googlecode.gwtnes.client.halfnes;
//HalfNES, Copyright Andrew Hoffman, October 2010

import java.util.Arrays;

public class PPU {

    public PPURAM ppuram;
    private int scanline = 0;
    private int oamaddr = 0;
    private int loopyV = 0x0;//ppu memory pointer
    private int loopyT = 0x0;//temp pointer
    private int loopyX = 0;//fine x scroll
    private int[] OAM = new int[256];
    private int[] spriteshiftregH = new int[8];
    private int[] spriteshiftregL = new int[8];
    private int[] spriteXlatch = new int[8];
    private int[] spritepals = new int[8];
    private boolean[] spritebgflags = new boolean[8];
    private boolean sprite0hit = false;
    public int sprite0x = 0;
    private boolean even = true;
    private boolean bgpattern = true;
    private boolean sprpattern = false;
    private int readbuffer = 0;
    public int[] ppuregs = new int[0x8];
    //private DebugUI debuggui;
    //no longer using the hash tables for tile caches because they actually SLOW it DOWN a bit now.
//    private HashMap<Integer, int[]> tilebuff = new HashMap<Integer, int[]>(
//            1024, 1);
    //private HashMap<Integer, int[]> debugbuff;
    // NES Color Palette (NTSC)

    //uses the function to premultiply with alpha channel and do emphasis calcs
    private final static int[][] nescolor = GetNESColors();
    private int vraminc = 1;
    private int[] bitmap = new int[240 * 256];
    private final static boolean PPUDEBUG = false;
    //private BufferedImage newBuff;

    public PPU(final PPURAM ppuram) {
        this.ppuram = ppuram;
        Arrays.fill(OAM, 0xff);
        Arrays.fill(ppuregs, 0x00);
//        if (PPUDEBUG) {
//            //newBuff = new int[512][480];
//            debuggui = new DebugUI();
//            debuggui.run();
//            //debugbuff = new HashMap<Integer, int[]>(1024, 1);
//        }
    }

    public int read(final int regnum) {
        switch (regnum) {
            case 2:
                even = true;
                final int tmp = ppuregs[2];
                ppuregs[2] &= 0x7f;//turn off vblank flag
                return tmp;
            case 4:
                // reading this is NOT reliable but some games do it anyways
                return OAM[oamaddr];
            case 7:
                // PPUDATA
                // correct behavior. read is delayed by one
                // -unless- is a read from sprite pallettes
                if ((loopyV & 0x3fff) < 0x3f00) {
                    final int temp = readbuffer;
                    readbuffer = ppuram.read(loopyV & 0x3fff);
                    loopyV += vraminc;
                    return temp;
                } else {
                    readbuffer = ppuram.read((loopyV & 0x3fff) - 0x1000);
                    final int temp = ppuram.read(loopyV);
                    loopyV += vraminc;
                    return temp;
                }

            // and don't increment on read
            default:
                return 0x20; // open bus
        }
    }

    public void write(final int regnum, final int data) {
        //System.err.println("PPU write - wrote " + data + " to reg " + regnum);
        //debugdraw();
        switch (regnum) {
            case 0:
                ppuregs[0] = (short) data;
                vraminc = (Utils.getbit(data, 2) ? 32 : 1);
                //set 2 bits of vram address (nametable select)
                loopyT &= ~0xc00;
                loopyT += (data & 3) << 10;
                break;
            case 1:
                ppuregs[1] = (short) data;
            case 3:
                // PPUOAMADDR (2003)
                // most games just write zero and use the dma
                oamaddr = data & 0xff;
                break;
            case 4:
                // PPUOAMDATA(2004)
                OAM[oamaddr++] = data;
                oamaddr &= 0xff;
                // games don't write this directly anyway
                break;

            // PPUSCROLL(2005)
            case 5:
                if (even) {
                    // horizontal scroll
                    loopyT &= ~0x1f;
                    loopyX = data & 7;
                    loopyT += data >> 3;

                    even = false;
                } else {
                    // vertical scroll
                    loopyT &= ~0x7000;
                    loopyT += ((data & 7) << 12);
                    loopyT &= ~0x3e0;
                    loopyT += (data >> 3) << 5;
                    even = true;

                }
                break;

            case 6:
                // PPUADDR (2006)
                if (even) {
                    // high byte
                    loopyT &= 0xc0ff;
                    loopyT += ((data & 0x3f) << 8);
                    loopyT &= 0x3fff;
                    even = false;
                } else {
                    loopyT &= 0xff00;
                    loopyT += data;
                    loopyV = loopyT;
                    even = true;

                }
                break;
            case 7:
                // PPUDATA
                if (renderingisoff()) {
                    // if rendering is off its safe to write
                    ppuram.write((loopyV & 0x3fff), data);
                    loopyV += vraminc;
                } else {
                    //System.err.println("dropped write");
                    // write anyway though until i figure out which wrong thing to do
                    //also since the ppu doesn't get pixel level timing right now,
                    //i can't detect hblank.
                    ppuram.write((loopyV & 0x3fff), data);
                    loopyV += vraminc;
                }
            // increments on write but NOT on read
            default:
                break;
        }
    }

    public boolean renderingisoff() {
        // tells when it's ok to write to the ppu
        return (scanline >= 240) //|| (pixel > 256)
                || (!Utils.getbit(ppuregs[1], 3));
    }

    public boolean mmc3CounterClocking() {
        return (bgpattern != sprpattern) && !renderingisoff();
    }

    //TODO: drawLine is slow, needs performance improvement in browser
    public boolean drawLine(final int scanline) {
    	
        //this contains probably more magic numbers than the rest of the program combined.
        bgpattern = Utils.getbit(ppuregs[0], 4);
        sprpattern = Utils.getbit(ppuregs[0], 3);
        int bgcolor;
        final int bufferoffset = scanline * 256;

        bgcolor = ppuram.pal[0] + 256; //plus 256 is to give indication it IS the bgcolor
        //because bg color is special
        if (Utils.getbit(ppuregs[1], 3)) {
            //System.err.println(" BG ON!");
            // if bg is on, draw tiles.
            if (scanline == 0) {
                //update whole scroll
                loopyV = loopyT;
            } else {
                //update hscroll only
                loopyV &= ~0x41f;
                loopyV |= loopyT & 0x41f;
            }
            //draw background
            int ntoffset = (loopyV & 0xc00) | 0x2000;
            int attroffset = (loopyV & 0xc00) | 0x2000 + 0x3c0;
            boolean horizWrap = false;
            for (int tilenum = 0; tilenum < 33; ++tilenum) {
                //for each tile in row
                if ((tilenum * 8 + (((loopyV & 0x1f) << 3) + loopyX)) > 255 && !horizWrap) {
                    //if scrolling off the side of the nametable, bump address to next nametable
                    ntoffset ^= 0x400;
                    ntoffset -= 32;
                    attroffset ^= 0x400;
                    horizWrap = true;
                }
                //get palette number from attribute table byte
                final int palettenum = getattrtbl(attroffset, (ntoffset + loopyV + tilenum) & 0x1f, (((ntoffset + loopyV + tilenum) & 0x3e0) >> 5));
                final int tileaddr = ppuram.read(ntoffset + (loopyV & 0x3ff) + tilenum) * 16 + (bgpattern ? 0x1000 : 0);
                final int[] tile = getTile(tileaddr, bgcolor, palettenum * 4, (loopyV & 0x7000) >> 12);
                //now put inna buffer
                final int xpos = tilenum * 8 - loopyX;//not quite right yet
                for (int pxl = 0; pxl < 8; ++pxl) {
                    if ((pxl + xpos) < 256 && (pxl + xpos) >= 0) { //it's not off the screen
                        bitmap[pxl + xpos + bufferoffset] = tile[pxl];
                    }
                }
            }
            //increment loopy_v to next row of tiles
            int newfinescroll = loopyV & 0x7000;
            newfinescroll += 0x1000;
            loopyV &= ~0x7000;
            if (newfinescroll > 0x7000) {
                //reset the fine scroll bits and increment tile address to next row
                loopyV += 32;
            } else {
                //increment the fine scroll
                loopyV += newfinescroll;
            }
            if (((loopyV >> 5) & 0x1f) == 30) {
                //if incrementing loopy_v to the next row pushes us into the next
                //nametable, zero the "row" bits and go to next nametable
                loopyV &= ~0x3e0;
                loopyV ^= 0x800;
                ntoffset += 0x440;
                attroffset += 0x7c0;
            }
        } else {
            //System.err.println(" BG off");
            //if rendering is off draw either the background color OR
            //if the PPU address points to the palette, draw that color instead.
            bgcolor = ((loopyV > 0x3f00 && loopyV < 0x3fff) ? ppuram.read(loopyV) : ppuram.pal[0]);
            Arrays.fill(bitmap, bufferoffset, bufferoffset + 256, bgcolor + 256);
        }
        //draw sprites on top of whatever we had
        drawSprites(scanline, bgcolor);
        //hide leftmost 8 pixels if that flag is on
        if (!Utils.getbit(ppuregs[1], 1)) {
            for (int i = 0; i < 8; ++i) {
                bitmap[i + bufferoffset] = bgcolor;
            }
        }
        //deal with the grayscale flag
        if (Utils.getbit(ppuregs[1], 0)) {
            for (int i = bufferoffset; i < (bufferoffset + 256); ++i) {
                bitmap[i] &= 0x30;
            }
        }
        final int emph = (ppuregs[1] >> 5);
        //and now replace the nes color numbers with rgb colors (respecting color emph bits)
        for (int i = bufferoffset; i < (bufferoffset + 256); ++i) {
            bitmap[i] = nescolor[emph][bitmap[i] & 0xff];
        }
        if (sprite0hit) {
            sprite0hit = false;
            return true;
        } else {
            return false;
        }
    }

    private int[] lospixels = {0, 0, 0, 0, 0, 0, 0, 0};
    private void drawSprites(final int scanline, final int bgcolor) {
        if (!Utils.getbit(ppuregs[1], 4)) {
            return; //return immediately if sprites are disabled
        }
        final int bufferoffset = 256 * scanline;
        bgpattern = Utils.getbit(ppuregs[0], 4);
        sprpattern = Utils.getbit(ppuregs[0], 3);
        int ypos, offset, tilefetched;
        int found = 0;
        final boolean spritesize = Utils.getbit(ppuregs[0], 5);
        boolean sprite0here = false;
        //primary evaluation
        for (int spritestart = 0; spritestart < 255; spritestart += 4) {
            //for each sprite, first we cull the non-visible ones
            ypos = OAM[spritestart] + 1;
            offset = scanline - ypos;
            if (ypos > scanline || offset > (spritesize ? 15 : 7)) {
                //sprite is out of range vertically
                continue;
            }
            //if we're here it's a valid renderable sprite
            if (spritestart == 0) {
                sprite0here = true;
            }
            if (found >= 8) {
                //if more than 8 sprites, set overflow bit and STOP looking
                //todo: add "no sprite limit" option back
                ppuregs[2] |= 0x20;
                break;
            } else {
                //set up ye sprite for rendering
                final int oamextra = OAM[spritestart + 2];
                //bg flag
                spritebgflags[found] = Utils.getbit(oamextra, 5);
                //x value
                spriteXlatch[found] = OAM[spritestart + 3];
                spritepals[found] = ((oamextra & 3) + 4) * 4;
                if (Utils.getbit(oamextra, 7)) {
                    //if sprite is flipped vertically, reverse the offset
                    offset = (spritesize ? 15 : 7) - offset;
                }
                //now correction for the fact that 8x16 tiles are 2 separate tiles
                if (offset > 7) {
                    offset += 8;
                }
                //get tile address (8x16 sprites can use both pattern tbl pages but only the even tiles)
                final int tilenum = OAM[spritestart + 1];
                if (spritesize) {
                    tilefetched = ((tilenum & 1) * 0x1000)
                            + (tilenum & 0xfe) * 16;
                } else {
                    tilefetched = tilenum * 16
                            + ((sprpattern) ? 0x1000 : 0);
                }
                tilefetched += offset;
                //now load up the shift registers for said sprite
                final boolean hflip = Utils.getbit(oamextra, 6);
                if (!hflip) {
                    spriteshiftregL[found] = Utils.reverseByte(ppuram.read(tilefetched));
                    spriteshiftregH[found] = Utils.reverseByte(ppuram.read(tilefetched + 8));
                } else {
                    spriteshiftregL[found] = ppuram.read(tilefetched);
                    spriteshiftregH[found] = ppuram.read(tilefetched + 8);
                }
                ++found;
            }
        }
        for (int i = found; i < 8; ++i) {
            //fill unused sprite registers with zeros
            spriteshiftregL[found] = 0;
            spriteshiftregH[found] = 0;
        }

        //now, drawing the actual sprites on the buffer
        for(int i = 0; i < 8; ++i){
        	lospixels[i] = 0;
        }

        int off;
        int y, i;
        for (int x = 0; x < 256; ++x) {

            //per pixel in de line
            for (y = found - 1; y >= 0; --y) {
                off = x - spriteXlatch[y];
                if (off >= 0 && off <= 8) {

                    lospixels[y] = 2 * (spriteshiftregH[y] & 1) + (spriteshiftregL[y] & 1);
                    spriteshiftregH[y] >>= 1;
                    spriteshiftregL[y] >>= 1;
                }
            }
            //so now lospixels has all of the sprite shift reg results in order (hopefully). now what?
            //we take the LOWEST one out of the array that's not zero.
            i = 0;
            for (; i <= 8; ++i) {
                if (i == 8) {
                    //no sprite pixels on the line, so no work to do, so break out to next pxl.
                    break;
                }
                if (lospixels[i] != 0) {
                    break;
                }
            }
            if (i == 8) {
                //no sprite pixels on the line, so no work to do, so continue to next pxl.
                continue;
            }

            //and i is now the sprite byte to be rendered.
            final int bgoff = bufferoffset + x;
            final int bgpixel = bitmap[bgoff];
            if (sprite0here && (i == 0) && (bgpixel != bgcolor)) {
                //sprite 0 hit!
                sprite0hit = true;
                sprite0x = x;
            }
            //now, FINALLY, drawing.
            if (!spritebgflags[i] || (bgpixel == bgcolor)) {
                bitmap[bgoff] = ppuram.pal[spritepals[i] + lospixels[i]];
            }
        }
    }

    private int getattrtbl(final int ntstart, final int tilex, final int tiley) {
        final int base = ntstart + (tilex >> 2) + 8 * (tiley >> 2);
        if ((tilex & 2) == 0) {
            if ((tiley & 2) == 0) {
                return ppuram.read(base) & 3;
            } else {
                return (ppuram.read(base) >> 4) & 3;
            }
        } else {
            if ((tiley & 2) == 0) {
                return (ppuram.read(base) >> 2) & 3;
            } else {
                return (ppuram.read(base) >> 6) & 3;
            }
        }
    }

//    public void debugdraw() {
//        //old code, left for dumping out VRAM to debug window.
//        //SLOW.
//        final boolean tilemode = true;
//        if (tilemode) {
//            for (int i = 0; i < 32; ++i) {
//                for (int j = 0; j < 30; ++j) {
//                    newBuff.setRGB(i * 8, j * 8, 8, 8, oldgettile(ppuram.read(0x2000 + i + 32 * j) * 16 + (bgpattern ? 0x1000 : 0)), 0, 8);
//                }
//            }
//            for (int i = 0; i < 32; ++i) {
//                for (int j = 0; j < 30; ++j) {
//                    newBuff.setRGB(i * 8 + 255, j * 8, 8, 8, oldgettile(ppuram.read(0x2400 + i + 32 * j) * 16 + (bgpattern ? 0x1000 : 0)), 0, 8);
//                }
//            }
//            for (int i = 0; i < 32; ++i) {
//                for (int j = 0; j < 30; ++j) {
//                    newBuff.setRGB(i * 8, j * 8 + 239, 8, 8, oldgettile(ppuram.read(0x2800 + i + 32 * j) * 16 + (bgpattern ? 0x1000 : 0)), 0, 8);
//                }
//            }
//            for (int i = 0; i < 32; ++i) {
//                for (int j = 0; j < 30; ++j) {
//                    newBuff.setRGB(i * 8 + 255, j * 8 + 239, 8, 8, oldgettile(ppuram.read(0x2C00 + i + 32 * j) * 16 + (bgpattern ? 0x1000 : 0)), 0, 8);
//                }
//            }
//        } else {
//            //draw the tileset instead
//            for (int i = 0; i < 16; ++i) {
//                for (int j = 0; j < 32; ++j) {
//                    newBuff.setRGB(i * 8, j * 8, 8, 8, oldgettile((i + 16 * j) * 16), 0, 8);
//                }
//            }
//        }
//        //draw the palettes on the bottom.
//        for (int i = 0; i < 32; ++i) {
//            for (int j = 0; j < 16; ++j) {
//                for (int k = 0; k < 16; ++k) {
//                    newBuff.setRGB(j + i * 16, k + 256, nescolor[0][ppuram.pal[i]]);
//                }
//            }
//        }
//        debuggui.setFrame(newBuff);
//        //debugbuff.clear();
//    }

    public int[] oldgettile(final int patterntblptr) {
        // this'll be really really slow
        //for debug only
        int[] dat = new int[64];
        for (int i = 0; i < 8; ++i) {
            //per line of tile ( 1 byte)
            for (int j = 0; j < 8; ++j) {
                //per pixel(1 bit)
                dat[8 * i + j] = ((Utils.getbit(ppuram.read(i + patterntblptr), 7 - j)) ? 0x555555 : 0) + ((Utils.getbit(ppuram.read(i + patterntblptr + 8), 7 - j)) ? 0xaaaaaa : 0);
            }
        }
        return dat;
    }

    public int[] renderFrame() {
//        if (PPUDEBUG) {
//            debugdraw();
//        }
    	
        return bitmap;

    }


    //TODO: getTile is slow, needs performance improvement (~70% of execution time!)
    private int[] tiledata = new int[8];
    private int[] tilepal = new int[4];

    public int[] getTile(final int tileptr, final int bgcolor, final int paletteindex, final int off) {
        //returns an 8 pixel line of tile data fron given PPU ram location
        //with given offset and given palette. (color expressed as NES color number)
        tilepal[0] = bgcolor;
        tilepal[1] = ppuram.pal[paletteindex + 1];
        tilepal[2] = ppuram.pal[paletteindex + 2];
        tilepal[3] = ppuram.pal[paletteindex + 3];
        // per line of tile ( 1 byte)
        int linelowbits = ppuram.read(off + tileptr);
        int linehighbits = ppuram.read(off + tileptr + 8);
        for (int j = 7; j >= 0; --j) {
            // per pixel(1 bit)
            tiledata[j] = tilepal[((linehighbits & 1) << 1) + (linelowbits & 1)];
            linehighbits >>= 1;
            linelowbits >>= 1;
        }
        return tiledata;
    }

    private static int[][] GetNESColors() {
        //just or's all the colors with opaque alpha, to save time in other loops
        int[] colorarray = {0x757575, 0x271B8F, 0x0000AB,
            0x47009F, 0x8F0077, 0xAB0013, 0xA70000, 0x7F0B00, 0x432F00,
            0x004700, 0x005100, 0x003F17, 0x1B3F5F, 0x000000, 0x000000,
            0x000000, 0xBCBCBC, 0x0073EF, 0x233BEF, 0x8300F3, 0xBF00BF,
            0xE7005B, 0xDB2B00, 0xCB4F0F, 0x8B7300, 0x009700, 0x00AB00,
            0x00933B, 0x00838B, 0x000000, 0x000000, 0x000000, 0xFFFFFF,
            0x3FBFFF, 0x5F97FF, 0xA78BFD, 0xF77BFF, 0xFF77B7, 0xFF7763,
            0xFF9B3B, 0xF3BF3F, 0x83D313, 0x4FDF4B, 0x58F898, 0x00EBDB,
            0x000000, 0x000000, 0x000000, 0xFFFFFF, 0xABE7FF, 0xC7D7FF,
            0xD7CBFF, 0xFFC7FF, 0xFFC7DB, 0xFFBFB3, 0xFFDBAB, 0xFFE7A3,
            0xE3FFA3, 0xABF3BF, 0xB3FFCF, 0x9FFFF3, 0x000000, 0x000000,
            0x000000};
        for (int i = 0; i < colorarray.length; ++i) {
            colorarray[i] |= 0xff000000;
        }
        int[][] colors = new int[8][colorarray.length];
        for (int j = 0; j < colorarray.length; ++j) {
            colors[0][j] = colorarray[j];
            //emphasize red
            colors[1][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16)) << 16))
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8) * 0.7) << 8)
                    + (int) ((colorarray[j] & 0x000000ff) * 0.7);
            //emphasize green
            colors[2][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16) * 0.7)) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8)) << 8)
                    + (int) ((colorarray[j] & 0x000000ff) * 0.7);
            //emphasize yellow
            colors[3][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16))) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8)) << 8)
                    + (int) ((colorarray[j] & 0x000000ff) * 0.7);
            //emphasize blue
            colors[4][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16) * 0.7)) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8) * 0.7) << 8)
                    + (int) ((colorarray[j] & 0x000000ff));
            //emphasize purple
            colors[5][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16))) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8) * 0.7) << 8)
                    + (int) ((colorarray[j] & 0x000000ff));
            //emphasize cyan?
            colors[6][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16) * 0.7)) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8)) << 8)
                    + (int) ((colorarray[j] & 0x000000ff));
            //de-emph all 3 colors
            colors[7][j] = (0xff << 24)
                    + ((int) ((((colorarray[j] & 0x00ff0000) >> 16) * 0.7)) << 16)
                    + ((int) (((colorarray[j] & 0x0000ff00) >> 8) * 0.7) << 8)
                    + (int) ((colorarray[j] & 0x000000ff) * 0.7);

        }
        return colors;
    }
}
