package com.googlecode.gwtnes.client;



/** 
 *
 * Class for the Mapper 4 Controller used by NESCafe.
 *
 * @author David de Niese
 * @author Brad Rydzewski
 * @version  0.56f
 * @final    TRUE
 *
 */

public class Mapper004 extends Mapper 
{




     /**
      *
      * <P>Determine the number of the Memory Mapper.</P>
      *
      */

      public final int getMapperNumber() 
      {

         return 4;

      }



     /**
      *
      * <P>Method to initialise the Memory Mapper.</P>
      *
      * @param mm Memory Manager to initialise the Mapper with.
      *
      */

      public void init(MemoryManager mm)
      {

         // Assign Local Pointer for Memory Manager Object

            this.mm = mm;


         // Cause a Reset

            reset();


      }



     /**
      *
      * The current Interrupt Counter Value
      *
      */

      private int irq_counter=0;



     /**
      *
      * Whether the Interrupt Counter is enabled
      *
      */

      private boolean irq_enabled = false;


     /**
      *
      * Interrupt Latch Value
      *
      */

      private int irq_latch=0;



     /**
      *
      * <P>The Registers for the MMC</P>
      *
      */

      private int[] regs = new int[8];



     /**
      *
      * <P>Program ROM Pointer for Bank 0</P>
      *
      */


      private int prg0 = 0;


     /**
      *
      * <P>Program ROM Pointer for Bank 1</P>
      *
      */

      private int prg1 = 1;


     /**
      *
      * <P>VROM Pointer for Bank 0 (and 1)</P>
      *
      */

      private int chr01 = 0;


     /**
      *
      * <P>VROM Pointer for Bank 2 (and 3)</P>
      *
      */

      private int chr23 = 0;


     /**
      *
      * <P>VROM Pointer for Bank 4</P>
      *
      */

      private int chr4 = 0;


     /**
      *
      * <P>VROM Pointer for Bank 5</P>
      *
      */

      private int chr5 = 0;


     /**
      *
      * <P>VROM Pointer for Bank 6</P>
      *
      */

      private int chr6 = 0;


     /**
      *
      * <P>VROM Pointer for Bank 7</P>
      *
      */

      private int chr7 = 0;



     /**
      *
      * <P>Reset the Mapper.</P>
      *
      */

      public final void reset() 
      {


         // Clear the Registers

            for(int i = 0; i < 8; i++) regs[i] = 0x00;


         // Switch Banks 8-B to First 16k of Program ROM

            prg0 = 0;
            prg1 = 1;
            MMC3_set_CPU_banks();



         // Set VROM Banks

            if (getNum1KVROMBanks()>0)
            {

               chr01 = 0;
               chr23 = 2;
               chr4  = 4;
               chr5  = 5;
               chr6  = 6;
               chr7  = 7;
               MMC3_set_PPU_banks();

            }
            else
            {

               chr01 = chr23 = chr4 = chr5 = chr6 = chr7 = 0;

            }


         // Reset IRQ Status

            irq_enabled = false;
            irq_counter = 0;
            irq_latch = 0;


         // Switch PPU Memory Banks

            setPPUBanks(0,1,2,3,4,5,6,7);


      }



     /**
      *
      * <P>Access the Mapper.</P>
      *
      */

      public final void access(int addr, int data) 
      {


         // Check Address within Range

            if (addr < 0x8000) return;



         // Determine the Function

            switch(addr & 0xE001)
            {


               // Command Register

                  case 0x8000:
                     {

                        regs[0] = data;
                        MMC3_set_PPU_banks();
                        MMC3_set_CPU_banks();

                     }
                     break;


               // Activate Register

                  case 0x8001: 
                     {

                        regs[1] = data;
                        int bank_num = regs[1];


                        switch(regs[0] & 0x07)
                        {

                           case 0x00:
                              {

                                 bank_num &= 0xfe;
                                 chr01 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x01:
                              {

                                 bank_num &= 0xfe;
                                 chr23 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x02:
                              {

                                 chr4 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x03:
                              {

                                 chr5 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x04:
                              {

                                 chr6 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x05:
                              {

                                 chr7 = bank_num;
                                 MMC3_set_PPU_banks();

                              }
                              break;


                           case 0x06:
                              {

                                 prg0 = bank_num;
                                 MMC3_set_CPU_banks();

                              }
                              break;


                           case 0x07:
                              {

                                 prg1 = bank_num;
                                 MMC3_set_CPU_banks();

                              }
                              break;

                        }

                     }
                     break;


               // Handle Mirroring

                  case 0xA000:
                     {

                        regs[2] = data;

                        if (!mm.ppu.mirrorFourScreen)
                        {

                           if ((data & 0x01)!=0)
                           {

                              setMirroringHorizontal();

                           }
                           else
                           {

                              setMirroringVertical();

                           }

                        }

                     }
                     break;


               // Handle Save RAM at 0x6000 - 0x7FFF

                  case 0xA001:
                     {

                        regs[3] = data;
                        mm.enableSaveRAM = ((data & 0x80) == 0x80);

                     }
                     break;


               // Store IRQ Counter

                  case 0xC000:
                     {

                        regs[4] = data;
                        irq_counter = regs[4];
 
                     }
                     break;

 
               // Store IRQ Counter

                  case 0xC001:
                     {

                        regs[5] = data;
                        irq_latch = regs[5];

                     }
                     break;


               // Disable IRQ

                  case 0xE000:
                     {

                        regs[6] = data;
                        irq_enabled = false;

                     }
                     break;



               // Enable IRQ

                  case 0xE001:
                     {

                        regs[7] = data;
                        irq_enabled = true;

                     }
                     break;

            }


      }





     /**
      *
      * <P>Syncronise the Memory Mapper Horizontally.</P>
      *
      */

      public final int syncH(int scanline) 
      {


         // Check if IRQ Enabled

            if (irq_enabled)
            {

               // Check for Visible Scanline

                  if ((scanline >= 0) && (scanline < 240))
                  {

                     // Check if Background or Sprites Enabled

                        if ((mm.nes.ppu.REG_2001 & 0x18) != 00)
                        {

                           // Decrement IRQ Counter

                              irq_counter--;


                           // Check if Counter Down to Zero

                              if (irq_counter < 0)
                              {

                                 // Set Counter to Latch and Fire Interupt

                                    irq_counter = irq_latch;
                                    return 3;

                              }

                        }
                  }
            }

            return 0;

      }



     /**
      *
      * <P>Set the MMC3 CPU Banks</P>
      *
      */

      private void MMC3_set_CPU_banks()
      {


         // Map Program ROM 

            if (prg_swap())
            {

               setCPUBanks(getNum8KRomBanks()-2,prg1,prg0,getNum8KRomBanks()-1);
            }
            else
            {

               setCPUBanks(prg0,prg1,getNum8KRomBanks()-2,getNum8KRomBanks()-1);

            }

      }



     /**
      *
      * <P>Set the MMC3 PPU Banks</P>
      *
      */

      private void MMC3_set_PPU_banks()
      {

         // Check if VROM Banks Exist and Map Them

            if(getNum1KVROMBanks() !=0 )
            {


               // Check if Swap Low and High Character ROM

                  if (chr_swap())
                  {

                     setPPUBanks(chr4,chr5,chr6,chr7,chr01,chr01+1,chr23,chr23+1);

                  }
                  else
                  {

                     setPPUBanks(chr01,chr01+1,chr23,chr23+1,chr4,chr5,chr6,chr7);

                  }

            }
            else
            {


               // No VROM Banks so Map VRAM

                  if(chr_swap())
                  {

                     setVRAMBank(0, chr4);
                     setVRAMBank(1, chr5);
                     setVRAMBank(2, chr6);
                     setVRAMBank(3, chr7);
                     setVRAMBank(4, chr01+0);
                     setVRAMBank(5, chr01+1);
                     setVRAMBank(6, chr23+0);
                     setVRAMBank(7, chr23+1);

                  }
                  else
                  {

                     setVRAMBank(0, chr01+0);
                     setVRAMBank(1, chr01+1);
                     setVRAMBank(2, chr23+0);
                     setVRAMBank(3, chr23+1);
                     setVRAMBank(4, chr4);
                     setVRAMBank(5, chr5);
                     setVRAMBank(6, chr6);
                     setVRAMBank(7, chr7);

                  }

            }

      }


     /**
      *
      * <P>Determine if Character ROM is Swapped</P>
      *
      */

      private boolean chr_swap()
      {

         return (regs[0] & 0x80)!=0;
 
      }


     /**
      *
      * <P>Determine if Program ROM is Swapped</P>
      *
      */

      private boolean prg_swap()
      {

         return (regs[0] & 0x40)!=0;

      }




     /**
      * 
      * <P>Loads the State of the Memory Mapper from an InputStream.</P>
      *
      */



}


