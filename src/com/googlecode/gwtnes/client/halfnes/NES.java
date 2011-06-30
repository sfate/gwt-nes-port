package com.googlecode.gwtnes.client.halfnes;

//import java.util.prefs.Preferences;

import java.util.Date;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.googlecode.gwtnes.client.halfnes.mappers.BadMapperException;
import com.googlecode.gwtnes.client.halfnes.mappers.Mapper;

/**
 *
 * @author Andrew Hoffman
 */
public class NES {

    //private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private Mapper mapper;
    private APU apu;
    private CPU cpu;
    private CPURAM cpuram;
    private PPU ppu;
    private ControllerInterface controller1;
    private ControllerInterface controller2;
    final public static String VERSION = "0.036";
    private boolean runEmulation, dontSleep = false;
    public long frameStartTime = 0;
    public long framecount = 0;
    private long frameDoneTime = 0;
    private long sleepingtest = 0;
    private boolean frameLimiterOn = true;
    private String curRomPath;
    private String curRomName;
    private byte[] curRomBytes;
    private final GUIInterface gui;
//    private FrameLimiterInterface limiter = new FrameLimiterImpl(this);

    public NES(final GUIInterface gui) {
    	this.gui = gui;
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                gui.run();
//            }
//        });
    }

    public void run(final byte[] rom, final String name) {
        //Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
        //set thread priority higher than the interface thread
        curRomPath = name;
        curRomBytes = rom;
        loadROM(rom, name);
        run();
    }
    int failCount = 0;
    public void run() {

    	Timer timer = new Timer() {
			@Override
			public void run() {
				try {
					System.out.println("frame @ "+ new Date());
				runframe();
				}catch(Exception ex) {
					failCount++;
					if(failCount<5) {
					Window.alert(ex.getMessage());
					}
				}
			}
    	};
    	timer.scheduleRepeating(1);

//        while (true) {
//            if (runEmulation) {
//                frameStartTime = System.nanoTime();
//                runframe();
//                if (frameLimiterOn && !dontSleep) {
//                    //limiter.sleep();
//                }
//                frameDoneTime = System.nanoTime() - frameStartTime;
//            } else {
//                //limiter.sleepFixed();
//            }
//        }
    }

    public synchronized void runframe() {
        final int scanlinectrfire = 256;
        //the main method sequencing everything that has to happen in the nes each frame
        //loops unrolled a bit to avoid some conditionals every cycle
        //vblank
        //start by setting nmi
        if ((Utils.getbit(ppu.ppuregs[0], 7) && framecount > 1)) {
            //^hack, without this Lolo 2 and/or Vegavox NSFs don't work
            cpu.runcycle(241, 9000);
            cpu.nmi();
            // do the nmi but let cpu run ONE extra instruction first
            // (fixes Adventures of Lolo 2, Milon's Secret Castle, Solomon's Key)
        }
        for (int scanline = 241; scanline < 261; ++scanline) {
            //most of vblank period
            cpu.cycle(scanline, scanlinectrfire);
            mapper.notifyscanline(scanline);
            cpu.cycle(scanline, 341);
        }
        //scanline 261 
        //turn off vblank flag
        ppu.ppuregs[2] &= 0x80;
        cpu.cycle(261, 30);
        // turn off sprite 0, sprite overflow flags
        ppu.ppuregs[2] &= 0x9F;
        cpu.cycle(261, scanlinectrfire);
        mapper.notifyscanline(261);
        cpu.cycle(261, (((framecount & 1) == 1) && Utils.getbit(ppu.ppuregs[1], 3)) ? 340 : 341);
        //odd frames are shorter by one PPU pixel if rendering is on.

        dontSleep = apu.bufferHasLessThan(1000);
        //if the audio buffer is completely drained, don't sleep for this frame
        //this is to prevent the emulator from getting stuck sleeping too much
        //on a slow system or when the audio buffer runs dry.

        apu.finishframe();
        cpu.modcycles();
        //active drawing time
        for (int scanline = 0; scanline < 240; ++scanline) {
            if (!ppu.drawLine(scanline)) {
                cpu.cycle(scanline, scanlinectrfire);
                mapper.notifyscanline(scanline);
            } else {
                //it is de sprite zero line
                final int sprite0x = ppu.sprite0x;
                if (sprite0x < scanlinectrfire) {
                    cpu.cycle(scanline, sprite0x);
                    ppu.ppuregs[2] |= 0x40; //sprite 0 hit
                    cpu.cycle(scanline, scanlinectrfire);
                    mapper.notifyscanline(scanline);
                } else {
                    cpu.cycle(scanline, scanlinectrfire);
                    mapper.notifyscanline(scanline);
                    cpu.cycle(scanline, sprite0x);
                    ppu.ppuregs[2] |= 0x40; //sprite 0 hit
                }
            }
            //and finish out the scanline
            cpu.cycle(scanline, 341);
        }
        //scanline 240: dummy fetches
        cpu.cycle(240, scanlinectrfire);
        mapper.notifyscanline(240);
        cpu.cycle(240, 341);
        //set the vblank flag
        ppu.ppuregs[2] |= 0x80;
        //render the frame
        gui.setFrame(ppu.renderFrame());
        if ((framecount & 2047) == 0) {
            //save sram every 30 seconds or so
            saveSRAM(true);
        }
        ++framecount;
    }

    public void setControllers(ControllerInterface controller1, ControllerInterface controller2) {
        this.controller1 = controller1;
        this.controller2 = controller2;
    }

    public void toggleFrameLimiter() {
        if (frameLimiterOn) {
            frameLimiterOn = false;
        } else {
            frameLimiterOn = true;
        }
    }

    public synchronized void loadROM(final byte[] rom, final String filename) {
        if (apu != null) {
            //if rom already running save its sram before closing
            apu.destroy();
            saveSRAM(false);
        }
        runEmulation = false;
        frameLimiterOn = true;
        final ROMLoader loader = new ROMLoader(rom, filename);
        try {
            if (filename.toLowerCase().endsWith(".nes")) {
                loader.parseInesheader();
                mapper = Mapper.getCorrectMapper(loader.mappertype);
                System.out.println("Mapper Type: " + loader.mappertype);
                mapper.setLoader(loader);
                mapper.loadrom();
                //now some annoying getting of all the references where they belong
                cpuram = mapper.getCPURAM();
                cpu = mapper.cpu;
                ppu = mapper.ppu;
                apu = new APU(this, cpu, cpuram);
                cpuram.setAPU(apu);
                cpuram.setPPU(ppu);
                curRomPath = filename;
                curRomName = filename;//FileUtils.getFilenamefromPath(filename);

            }
        } catch (BadMapperException e) {
            gui.messageBox("Error Loading File: ROM is"
                    + " corrupted or uses an unsupported mapper.\n");
            runEmulation = false;
            return;

        }
        framecount = 0;
        //if savestate exists, load it
//        if (mapper.hasSRAM()) {
//            loadSRAM();
//        }
        //and start emulation

        cpu.init();
        runEmulation = true;
    }

    private void saveSRAM(final boolean async) {
//        if (mapper != null && mapper.hasSRAM() && mapper.supportsSaves()) {
//
//            if (async) {
//                FileUtils.asyncwritetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
//            } else {
//                FileUtils.writetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
//            }
//        }
    }

    private void loadSRAM() {
//        final String name = FileUtils.stripExtension(curRomPath) + ".sav";
//        if (FileUtils.exists(name) && mapper.supportsSaves()) {
//            mapper.setPRGRAM(FileUtils.readfromfile(name));
//        }

    }

    public void quit() {
        //save SRAM and quit
        if (cpu != null && curRomPath != null) {
            runEmulation = false;
            saveSRAM(false);
        }
        //System.exit(0);
    }

    public synchronized void reset() {
        if (cpu != null) {
            cpu.reset();
            runEmulation = true;
            apu.stopAudio();
            apu.startAudio();
        }
        //reset frame counter as well because PPU is reset
        //on Famicom, PPU is not reset when Reset is pressed
        //but some NES games expect it to be and you get garbage.
        framecount = 0;
    }

    public synchronized void reloadROM() {
        loadROM(curRomBytes, curRomPath);
    }

    public synchronized void pause() {
        if (apu != null) {
            apu.stopAudio();
        }
        runEmulation = false;
    }

    public long getFrameTime() {
        return frameDoneTime;
    }

    public String getrominfo() {
        if (mapper != null) {
            return mapper.getrominfo();
        }
        return null;
    }

//    public Preferences getPrefs() {
//        return prefs;
//    }

    public synchronized void frameAdvance() {
        runEmulation = false;
        if (cpu != null) {
            runframe();
        }
    }

    public synchronized void resume() {
        if (apu != null) {
            apu.startAudio();
        }
        if (cpu != null) {
            runEmulation = true;
        }
    }

    public String getCurrentRomName() {
        return curRomName;
    }

    public boolean isFrameLimiterOn() {
        return frameLimiterOn;
    }

    public void messageBox(final String string) {
        gui.messageBox(string);
    }

    public ControllerInterface getcontroller1() {
        return controller1;
    }

    public ControllerInterface getcontroller2() {
        return controller2;
    }
}
