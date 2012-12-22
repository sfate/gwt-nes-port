package com.gochromium.nes.client.emulator;

import com.google.gwt.user.client.Window;

//import java.io.IOException;
//import java.io.InputStream;


// Declare Imports




/** 
 *
 * Class for the Memory Manager required by the NESCafe NES Emulator.
 *
 * @author   David de Niese
 * @version  0.56f
 * @final    TRUE
 *
 */

public final class MemoryManager 
{


     /**
      *
      * <P>The current NES Machine.</P>
      *
      */

      protected NES nes;


     /**
      *
      * <P>The current Picture Processing Unit.</P>
      *
      */

      protected PPU ppu;



     /**
      *
      * <P>The current Graphical User Interface.</P>
      *
      */

//      private GUI gui;


     /**
      *
      * <P>The main memory.</P>
      *
      */

      protected int memory[] = new int[0x0800];


     /**
      *
      * <P>The Program ROM.</P>
      *
      */

      protected int programROM[] = null;





     /**
      *
      * Decompiler Executable Code Tags
      *
      */
      
      protected int programROMExecuteTags[] = null;
      
      
      public synchronized void setExecuteCodeMonitor(boolean val)
      {
         executableCodeMonitorEnabled = val;
         
      }

      public synchronized boolean getExecuteCodeMonitor()
      {
         return executableCodeMonitorEnabled;
         
      }
      
      private boolean executableCodeMonitorEnabled = false;
      
      public void setAsExecutableCode(int PC)
      {
         
         if (!getExecuteCodeMonitor())
            return;
            
         
         // Get Offset

            int c = bank[ ((PC & 0xF000) >> 12) ];


         // Set Memory as Executable

            programROMExecuteTags[ c + (PC & 0x0FFF) ] = PC;

      }
      
      public void clearExecutableCode()
      {
         
         for (int i=0; i<programROMExecuteTags.length; i++)
            programROMExecuteTags[i] = -1;
         
      }
      

      public void percentageExecutableCode()
      {

         double counter = 0;
         double total   = programROM.length;
                  
         for (int i=0; i<programROM.length; i++)
         {
            if (programROMExecuteTags[i]!=-1)
            {
               counter++;
            }
         }

         double percent = 100*counter/total;
         
         System.out.println("Percentage Analysed: " + (int)percent) ;
         
      }

      


     /**
      *
      * <P>Save RAM.</P>
      *
      */

      public int saveRAM[] = new int[0x10000];
      
      
      
     /**
      *
      * <P>Extended RAM from the Mapper Hardware</P>
      *
      */
      
      protected int mapper_extram[]   = new int[0x10000];
      
      
      
     /**
      *
      * <P>Size of Extended RAM from Mapper</P>
      *
      */
      
      public int mapper_extramsize = 0;



     /**
      *
      * <P>Respect Save RAM Writes.</P>
      *
      */

      public boolean enableSaveRAM = true;



     /**
      *
      * <P>The Program ROM Bank addresses.</P>
      *
      */

      private int bank[] = new int[16];  // Each bank is 0x1000





     /**
      *
      * <P>Variable for Zapper Trigger</P>
      *
      */

      public int zapperTrigger = 0;



     /**
      *
      * <P>Variable for Last Record Zapper X Position</P>
      *
      */

      public int zapperX = 0;



     /**
      *
      * <P>Variable for Last Record Zapper Y Position</P>
      *
      */

      public int zapperY = 0;







     /**
      *
      * <P>Create a new Memory Manager.</P>
      *
      */

      public MemoryManager (NES nes) 
      {


         // Grab References to the GUI and NES

//            this.gui = gui;
            this.nes = nes;
            this.ppu = nes.ppu;

      }



     /**
      *
      * <P>Return the current scanline number for Mappers.</P>
      *
      */
//
//      public final int getScanline()
//      {
//
//         return gui.tvController.scanLine;
//      }



     /**
      *
      * <P>Initialise the Memory Manager.</P>
      *
      */

      public final void init(NESCart game)//, String fileName) 
      {


         // Fetch the Program ROM

            programROM = game.getProgROM();




         // Clear Down Execute Tags
         
            programROMExecuteTags = new int[programROM.length];
            
            for (int i=0; i<programROMExecuteTags.length; i++)
               programROMExecuteTags[i] = -1;
            
         

         // Initialise the PPU Memory

            nes.ppu.PPUInit(game.getCharROM(), game.getMirroring(), game.getFourScreenNT());


         // Clear the Main Memory

            for (int i=0; i<memory.length; i++)
               memory[i] = 0;


         // Load the Trainer ROM if it Exists

            if (game.hasTrainer && game.trainerROM != null) 
            {

               for (int i=0; i<512; i++)
                  saveRAM[0x1000 + i] = game.trainerROM[i];

            } 
            else 
            {

               for (int i=0; i<512; i++)
                  saveRAM[0x1000 + i] = 0;

            }



         // Reset Frame IRQ Status
         
            nes.frameIRQEnabled = 0xFF;
            nes.frameIRQDisenabled = 0;





         // Determine if this is the NESCafe Demo ROM

            StringBuffer sb = new StringBuffer();

            for (int i=0; i<8; i++)
               sb.append((char)programROM[i]);


      }



     /**
      *
      * <P>Sets the offset in Program ROM of a Memory Bank.</P>
      *
      * @param bankNum The bank number to configure (0-15).
      * @param offsetInPRGROM The offset in Program ROM that the bank starts at.
      *
      */

      public final void setBankStartAddress (int bankNum, int offsetInPRGROM) 
      {

         offsetInPRGROM %= programROM.length;
         bank[bankNum % bank.length] = offsetInPRGROM;

      }


     /**
      *
      * <P>Read from Memory.</P>
      *
      * @return The value at the specified address.
      *
      */

      public final int read(int addr) 
      {
    	  if (addr < 0x2000) { 

    			// RAM Mirrored 4 Times
    			return memory[addr & 0x7FF];
    			
    		} else if (addr < 0x4000) { 
    		         
    			// Input/Output
    			return nes.ppu.read(addr & 0xE007);

    		} else if (addr < 0x4016) {

    			return 0;
    		        
    		} else if (addr < 0x4018) { 
    		         
    			// High I/O Regs
    			if (addr == 0x4016) {
    				// Joypad #1
    		        return nes.joyPad1.readJoyPadBit()  | 0;//readZapperData(1);
    			} 
//    			else if (addr == 0x4017) {
//    		        return nes.joyPad2.readJoyPadBit()  | 0;//readZapperData(2); 
//    			}
    		              
    		    return 0;

    		} else if (addr < 0x6000) { 
    		         
    		    return nes.mapper.accesslowread(addr);

    		} else if (addr < 0x8000) { 
    		         
    			// 0x6000 - 0x7FFF SaveRAM
    			return saveRAM[addr - 0x6000];

    		} else {
    		            // Read the Memory Address
//    		            try {
    		                // Get Offset
    		                int c = bank[ ((addr & 0xF000) >> 12) ];
    						// Return Memory
    						return programROM[ c + (addr & 0x0FFF) ];


//    		            } catch (Exception e) { return 0; }
    		}
    	  
    	  
    	  
//    	  int val = addr / 4096;
//    	  switch(val) {
//
//    	  case 0:
//    	  case 1:
//    	  	return memory[addr & 0x7FF];
//    	  case 2:
//    	  case 3:
//    		  return nes.ppu.read(addr & 0xE007);
//
//    	  case 4:
//    	  case 5:
//    	  case 6:
//    		  
//    		if (addr < 0x4018) { 
// 		         
//      			// High I/O Regs
//      			if (addr == 0x4016) {
//      				// Joypad #1
//      		        return nes.joyPad1.readJoyPadBit()  | 0;//readZapperData(1);
//      			}
//      		              
//      		    return 0;
//    		}
//
//    	  	return nes.mapper.accesslowread(addr);
//
//    	  case 7:
//    	  	return saveRAM[addr - 0x6000];
//    	  }
//
//    	  // Get Offset
//    	  int c = bank[ ((addr & 0xF000) >> 12) ];
//
//    	  // Return Memory
//    	  return programROM[ c + (addr & 0x0FFF) ];
    	  	
    	  
    	  
    	  
    	  

//
//
//         if (addr < 0x2000) 
//         { 
//
//
//            // RAM Mirrored 4 Times
//
//               return memory[addr & 0x7FF];
//
//         }
//         else if (addr < 0x4000) 
//         { 
//         
//            // Input/Output
//
//               return nes.ppu.read(addr & 0xE007);
//
//         }
//         else if (addr < 0x4016) 
//         {
//            
//            
//            // SPR-RAM DMA
//            
//               if (addr == 0x4014)
//               {
//                  
//                     return 0;
//                     
//               }
//               else if (addr == 0x4015 && ((nes.frameIRQEnabled & 0xC0)==0))
//               {
//
////TODO: re-enable sound
////                  return (nes.sound != null) ? nes.sound.read(addr) | 0x40 : 0x40;
//
//                  return 40;
//
//
//               }   
//         
//            
//
//            // Read from Sound Chip
////TODO: re-enable sound
////               return (nes.sound != null) ? nes.sound.read(addr) : 0;
//
//
//               return 0;
//        
//    
//            
//         }
//         else if (addr < 0x4018) 
//         { 
//         
//            // High I/O Regs
//
//               if (addr == 0x4016)
//               {
//                  
//                  // Joypad #1
//                  
//                     return nes.joyPad1.readJoyPadBit()  | 0;//readZapperData(1);
//       
//               }
//               else if (addr == 0x4017)
//               {
//                  
//                  // Joypad #2
//   
//                     return nes.joyPad2.readJoyPadBit()  | 0;//readZapperData(2);
//                     
//               }
//              
//               return 0;
//
//         }
//         else if (addr < 0x6000) 
//         { 
//         
//            // Expansion ROM
//
//               return nes.mapper.accesslowread(addr);
//
//
//         }
//         else if (addr < 0x8000) 
//         { 
//         
//            // 0x6000 - 0x7FFF SaveRAM
//
//               return saveRAM[addr - 0x6000];
//
//
//         } 
//         else 
//         {
//
//
//            // Check if Game Genie is Enabled
////
////               if (gameGenieActive)
////               {
////
////
////                  // Check if a Match was Found in the Game Genie Decoder
////
////                     int matchIndex = gameGenie.addressMatch(addr);
////
////
////                  // Grab Information from the Matched Code
////
////                     if (matchIndex >= 0)
////                     {
////
////
////                        // Read the Compare Value
////
////                           int compareVal = gameGenie.getValue(matchIndex);
////
////                        // If no Compare Value then Trigger Code
////
////                           if (compareVal == -1) return gameGenie.getValue(matchIndex);
////
////
////                        // Read the Memory Address
////
////                           int b = bank [ ((addr & 0xF000) >> 12)];
////                           int actualVal = programROM[ b + (addr & 0x0FFF) ];
////
////
////                        // Trigger the Game Genie if Compare Value doesn't Match Real Value
////
////                           if (actualVal == compareVal)
////                           {
////
////                              return actualVal;
////
////                           }
////                           else
////                           {
////
////                              return gameGenie.getValue(matchIndex);
////
////                           }
////
////
////                     }
////
////               }
//          
//
//
//            // Read the Memory Address
//
//               try 
//               {
//
//
//                  // Get Offset
//
//                     int c = bank[ ((addr & 0xF000) >> 12) ];
//
//
//                  // Return Memory
//
//                     return programROM[ c + (addr & 0x0FFF) ];
//
//
//               } 
//               catch (Exception e) { return 0; }
//
//         }


      }




      
      

  

     /**
      *
      * <P>Write to Memory.</P>
      *
      */

      public final void write(int addr, int value) 
      {


         if (addr < 0x2000) 
         {  



  


            // 0x0000 - 0x1FFF RAM

               memory[addr & 0x7FF] = value;
               return;

         }

         else if(addr < 0x4000) 
         { 
         
            // Low IO Registers
               
               nes.ppu.write(addr & 0xE007,value);
               return;

         }

         else if(addr < 0x4018) 
         { 
         
            // High IO Registers

              nes.mapper.accesslow(addr,value);
              
              switch(addr) 
              {

              case 0x4000:
              case 0x4001:
              case 0x4002:
              case 0x4003:
              case 0x4004:
              case 0x4005:
              case 0x4006:
              case 0x4007:
              case 0x4008:
              case 0x4009:
              case 0x400A:
              case 0x400B:
              case 0x400C:
              case 0x400D:
              case 0x400E:
              case 0x400F:
              case 0x4010:
              case 0x4011:
              case 0x4012:
              case 0x4013:


//TODO: reenable sound
//                   if (nes.sound != null) nes.sound.write(addr,value);
                   


                   return;

              case 0x4014: // Sprite DMA Register

                   int source[] = memory;
                   int k = value << 8;


                   switch (k & 0xF000) 
                   {

                      case 0x8000: // DMA Transfer from Program ROM
   
                           source = programROM;
                           k = bank[ ( k >> 12) + (k & 0x0FFF) ];
                           break;
   
   
                      case 0x6000: // DMA Transfer from SaveRAM
                      case 0x7000:



                           return;
   
   
                      case 0x5000: // DMA Transfer from Expansion RAM



                           return;
   
   
                      case 0x2000: // DMA Transfer from Registers
                      case 0x3000:
                      case 0x4000:
   
                           return;
   
   
                      case 0x0000: // DMA Transfer from RAM
                      case 0x1000:
   
                           source = memory;
                           k &= 0x7FF;
                           break;

                   }

                  
                  // Perform the DMA Transfer
                     for (int i = 0; i < 256; i++) 
                     {
                     
                        nes.ppu.spriteMemory[i] = source[k] & 0xFF;
                        k++;
                        
                     }

                  // Burn Some Cycles
                     nes.cpu.eatCycles(514);
                     return;



              case 0x4015:


//TODO: re-enable sound
//                   if (nes.sound != null) nes.sound.write(addr,value);



                   return;


              case 0x4016: // Joypad #1

                   if ((value & 0x1) == 0) nes.joyPad1.resetJoyPad();
                   return;
            
              case 0x4017: // Joypad #2

                   if ((value & 0x1) == 0) nes.joyPad2.resetJoyPad();

                   if (nes.frameIRQDisenabled==0) {
                   
                     nes.frameIRQEnabled = value;
                   }
                   

//TODO: re-enable sound
//                   if (nes.sound != null) nes.sound.write(addr,value);

                   
                   return;
              }

              return;

         } else if(addr < 0x6000) { 
         
            // Expansion ROM and Low Mapper Write Region
               nes.mapper.accesslow(addr,value);

         } else if(addr < 0x8000) { 
        	 
            // Save RAM
               nes.mapper.access(addr,value);
               if (enableSaveRAM) saveRAM[addr - 0x6000] = value;
               return;
               
         } else {
              nes.mapper.access(addr,value);
         }
      }



     /**
      *
      * <P>Read a 16 bit Word from Memory.</P>
      *
      */
      public final int readWord(int address) {
         return read(address) | (read(address + 1) << 8);
      }

     /**
      *
      * <P>Write a 16 bit Word to Memory.</P>
      *
      */
      public final void writeWord(int address, int value) {

         write(address, value & 0xFF);
         write(address + 1, value >> 8);
      }

     /**
      *
      * <P>Returns the Program ROM.</P>
      *
      */
      public final int[] getProgramROM() {

         return programROM;
      }
}

