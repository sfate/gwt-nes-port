package com.googlecode.gwtnes.client;

import java.io.InputStream;


/** 
 *
 * Class for the NES Machine.
 *
 */
public final class NES {


      protected JoyPad joyPad1;
      protected JoyPad joyPad2;
      protected CPU cpu;
      protected TVController tvController;
      protected Mapper mapper;
      protected MemoryManager memory;
      protected PPU ppu;


      protected Palette palette = new Palette();
      public int frameIRQEnabled = 0xFF;
      public int frameIRQDisenabled = 0;



     /**
      *
      * <P>Initialise the Nintendo Machine.</P>
      *
      * @param theGUI The Graphical User Interface in use.
      *
      */
      public final void init(TVController tvController)
      {

         // Grab Reference to the Current Graphical User Interface
            this.tvController = tvController;

         // Create Controller 1
            joyPad1 = new JoyPad(JoyPad.JOYPAD_1);

         // Create Controller 2
            joyPad2 = new JoyPad(JoyPad.JOYPAD_2);

         // Create a CPU
            cpu = new CPU(this,tvController);

         // Create a PPU
            ppu = new PPU(this,tvController);

         // Create a Memory Manager
            memory = new MemoryManager(this); 
      }

     /**
      *
      * <P>Load a cartridge and start it running.</P>
      *
      * @param fileName The filename of the cartridge to load.
      *
      * @return True if the Cartridge was successfully loaded.
      *
      */
      public final boolean cartLoad(InputStream is) {


         // Clear the Screen

            try 
            {

               // Load the ROM from the File
  
                  cpu.cpuLoadRom(is);


            } catch (RuntimeException e) {
                  return false;
            }



         // Stop the Currently Loaded ROM

            cpu.cpuStop();
            



         // Run the new ROM

            cpu.cpuRun();
            



         // Return Signal
            return true;
      }

}

