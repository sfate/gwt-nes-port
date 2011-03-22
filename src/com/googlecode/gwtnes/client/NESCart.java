package com.googlecode.gwtnes.client;

import java.io.IOException;
import java.io.InputStream;

/** 
 *
 * Class for the loading Cartridge ROM Images.
 *
 */
public final class NESCart {


     /**
      *
      * <P>No Error occurred when opening the Cartridge.</P>
      *
      */
      protected static final int ERROR_NONE = 0x00;


     /**
      *  
      * <P>The Cartridge file is not in the correct format.</P>
      *
      */
      protected static final int ERROR_FILE_FORMAT = 0x10;


     /**
      *
      * <P>The Cartridge uses an unsupported Memory Mapper.</P>
      *
      */
      protected static final int ERROR_UNSUPPORTED_MAPPER = 0x12;


     /**
      *
      * <P>The Cartridge doesn't contain any Program ROM.</P>
      *
      */
      protected static final int ERROR_MISSING_PROGRAM_ROM = 0x13;


     /**
      *
      * <P>An IO Error Occurred whilst opening the Cartridge ROM File.</P>
      *
      */
      protected static final int ERROR_IO = 0x14;












     /**
      *
      * <P>The Mapper to Load</P>
      *
      */
      protected Mapper mapper = null;
      
     /**
      *
      * <P>Number of the Mapper used by the current Cart.</P>
      *
      */
      private int MapperNumber = 0;

     /**
      *
      * <P>Description of the Mapper used by the current Cart.</P>
      *
      */
      private String MapperName = "No Mapper";

     /**
      *
      * <P>Whether the current Mapper is supported by NESCafe.</P>
      *
      */
      private boolean supported = false;

     /**
      *
      * <P>The last recorded Error Code.</P>
      *
      */
      private int errorCode = 0;

     /**
      * 
      * <P>The number of Program ROM Banks on the Cart.</P>
      *
      */
      private int numProgramROMBanks = 0;

     /**
      *
      * <P>The number of Character ROM Banks on the Cart.</P>
      *
      */
      private int numCharacterROMBanks = 0;

     /**
      *
      * <P>The Program ROM.</P>
      *
      */
      private int progROM[];

     /**
      *
      * <P>The Character ROM.</P>
      *
      */
      private int charROM[];

     /**
      *
      * <P>Trainer ROM.</P>
      *
      */
      protected int trainerROM[];
      
     /**
      *
      * <P>Whether Cart has Vertical Mirroring Enabled.</P>
      *
      */
      private boolean vMirroring = false;

     /**
      *
      * <P>Whether Cart has Four Screen NameTables.</P>
      *
      */
      private boolean fourScreenNT = false;

     /**
      *
      * <P>Whether Cart has 512 Byte Trainer.</P>
      *
      */
      protected boolean hasTrainer = false;

     /**
      *
      * <P>Whether Cart has Save RAM.</P> 
      *
      */
      protected boolean hasSaveRAM = false;

     /**
      *
      * <P>The CRC32 Value of the Cartridge
      *
      */
      protected long crc32 = 0;

     /**
      *
      * <P>Create a new NESCart Object.</P>
      *
      * @param currentGUI The current Graphical User Interface.
      *
      */
      public NESCart () {

      }

     /**
      * 
      * <P>Load a ROM image into this NESCart Object.</P>
      *
      * @param fileName The NES ROM file to be loaded.
      *
      * @return True if an error occurred whilst opening the file.
      *
      */
      public final boolean loadRom(InputStream is) {


         // Reset Error Information

            errorCode = NESCart.ERROR_NONE;


  

            try 
            { 
               // Check the Signature is Correct (NES)

                  if ( is.read() != 0x4E || 
                       is.read() != 0x45 || 
                       is.read() != 0x53 || 
                       is.read() != 0x1A ) {

                     is.close();
                     errorCode = NESCart.ERROR_FILE_FORMAT;
                     return true;

                  }


               // Read the Number of 16k Banks of Program ROM

                  numProgramROMBanks = is.read();


               // Read the Number of 8k Banks of Character ROM
   
                  numCharacterROMBanks = is.read();


               // Read Flags from File

                  int attributes = is.read();

                  vMirroring   = (attributes & 0x1) != 0;
                  hasSaveRAM   = (attributes & 0x2) != 0;
                  hasTrainer   = (attributes & 0x4) != 0;
                  fourScreenNT = (attributes & 0x8) != 0;



               // Grab Low and High Bytes of Mapper Number

                  int lowMapper  = (attributes >> 4) & 0x0F;
                  int highMapper = is.read() & 0xF0;

                  MapperNumber = lowMapper | highMapper;


               // If anything in reserved bits then dont trust Mapper High Nybble

                  for (int i=0; i<8; i++)
                     if (is.read() != 0) MapperNumber &= 0xF;


           
            } 
            catch (Exception e) 
            {


                  errorCode = NESCart.ERROR_IO;
                  return true;
               
            }




         // Catch IO Exceptions

            try 
            {


               // Load the Trainer if Present

                  if (hasTrainer) 
                  {

                     trainerROM = new int[512];
                     for (int i=0; i<trainerROM.length; i++)
                        trainerROM[i] = (((byte)is.read()) + 256) & 0xFF;


                  }


               // Load the Program ROM

                  progROM = new int[16384 * numProgramROMBanks];

                  for (int i = 0; i < progROM.length; i++)
                     progROM[i] = (((byte)is.read()) + 256) & 0xFF;



               // Load the Character ROM

                  charROM = new int[8192 * numCharacterROMBanks];

                  for (int i = 0; i < charROM.length; i++)
                     charROM[i] = (((byte)is.read()) + 256) & 0xFF;



               // Close the Input Stream

                  is.close();
                  

            } 
            catch (IOException e) 
            {

               errorCode = NESCart.ERROR_IO;
               return true;

            }
            
            
            
         // Record CRC32 for Cartridge
         //TODO: re-add CRC32 query
//            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
//            byte[] tempArray = new byte[progROM.length + charROM.length];
//            
//            for (int i=0;i<progROM.length; i++)
//               tempArray[i] = (byte)(progROM[i] & 0xFF);
//               
//            for (int i=0; i<charROM.length; i++)
//               tempArray[i + progROM.length] = (byte)(charROM[i] & 0xFF);
//               
         
         
         // Calculate CRC and Inform Mapper
         
//            crc.update(tempArray);
//            crc32 = crc.getValue();       
//            tempArray = null;



         ////////////////////////////////////////////////////////////////////////
         //
         // Make Corrections to Information
         //
         ////////////////////////////////////////////////////////////////////////
         
//            if (crc32 == 674943524l)
//            {
//               
//               // Ai Sensei no Oshiete - Watashi no Hoshi
//               
//                  MapperNumber = 32;
//               
//            }
//            else if (crc32 == 283262982l)
//            {
//            
//               // // Flintstones - The Rescue of Dino & Hoppy, The (J)
//               
//                  MapperNumber = 48;
//                  
//            }
//            else if (crc32 == 2055219138l)
//            {
//            
//               // Supare Mario Bros, Tetris, Nintendo World Cup
//               
//                  MapperNumber = 47;
//                  
//            }
            


         // Find the Mapper Name
            switch(MapperNumber) 
            {

            case 0x00:
   
                MapperName = "No Mapper";
                supported  = true;
                mapper     = new Mapper();
                break;


            case 0x01: 

                MapperName = "Nintendo MMC1";
                supported  = true;
                mapper     = new Mapper001();
                break;

            case 0x02:

                MapperName = "Simple PROM Switch (UNROM)";
                supported  = true;
                mapper     = new MapperCollection(2);
                break;

            case 0x03:

                MapperName = "Simple VROM Switch (CNROM)";
                supported  = true;
                mapper     = new Mapper003();
                break;

            case 0x04: 

                MapperName = "Nintendo MMC3";
                supported  = true;
                mapper     = new Mapper004();
                break;

            case 0x05: 

                MapperName = "Nintendo MMC5";
                supported  = true;
                mapper     = new Mapper005();
                break;

            case 0x06:

                MapperName = "Konami FFE F4xxx";
                supported  = true;
                mapper     = new Mapper006();
                break;
                
            case 0x07:

                MapperName = "Rare AOROM";
                supported  = true;
                mapper     = new MapperCollection(7);
                break;

            case 0x08:

                MapperName = "Konami FFE F3xxx";
                supported  = true;
                mapper     = new MapperCollection(8);
                break;


            case 0x09: 

                MapperName = "Nintendo MMC2";
                supported  = true;
                mapper     = new Mapper009();
                break;

/*
            case 0x0A: 

                MapperName = "Nintendo MMC4";
                supported  = true;
                mapper     = new Mapper010();
                break;

            case 0x0B:

                MapperName = "Color Dreams";
                supported  = true;
                mapper     = new MapperCollection(11);
                break;

            case 0x0D:

                MapperName = "CPROM";
                supported  = false;
                mapper     = null;
                break;

            case 0x0F:

                MapperName = "100-in-1";
                supported  = true;
                mapper     = new Mapper015();
                break;

            case 0x10:

                MapperName = "Bandai Chip";
                supported  = false;
                mapper     = null;
                break;

            case 0x11:

                MapperName = "Konami FFE F8xxx";
                supported  = true;
                mapper     = new Mapper017();
                break;

            case 0x12:

                MapperName = "Jaleco SS8806";
                supported  = true;
                mapper     = new Mapper018();
                break;

            case 0x13:

                MapperName = "Namcot 106";
                supported  = false;
                mapper     = null;
                break;

            case 0x14:

                MapperName = "Nintendo Disk System";
                supported  = false;
                mapper     = null;
                break;

            case 0x15:

                MapperName = "Konami VRC4 2A";
                supported  = true;
                mapper     = new Mapper021();
                break;

            case 0x16:

                MapperName = "Konami VRC4 type 1B";
                supported  = true;
                mapper     = new Mapper022();
                break;

            case 0x17:

                MapperName = "Konami VRC2 type B";
                supported  = true;
                mapper     = new Mapper023();
                break;

            case 0x18:

                MapperName = "Konami VRC6";
                supported  = false;
                mapper     = null;
                break;

            case 0x19:

                MapperName = "Konami VRC4";
                supported  = false;
                mapper     = null;
                break;


            case 0x1A:

                MapperName = "Konami VRC6V";
                supported  = false;
                mapper     = null;
                break;

            case 0x20:

                MapperName = "Irem G-101";
                supported  = true;
                mapper     = new Mapper032();
                break;


            case 0x21:

                MapperName = "Taito TC0190 TC0350";
                supported  = true;
                mapper     = new Mapper033();
                break;

            case 0x22:

                MapperName = "Nina-1";
                supported  = true;
                mapper     = new MapperCollection(34);
                break;

            case 0x28:

                MapperName = "SMB2J";
                supported  = true;
                mapper     = new Mapper040();
                break;


            case 0x29:

                MapperName = "Caltron 6-in-1";
                supported  = true;
                mapper     = new Mapper041();
                break;


            case 0x2A:

                MapperName = "Mario Baby";
                supported  = false;
                mapper     = null;
                break;

            case 0x2B:

                MapperName = "SMB2J";
                supported  = false;
                mapper     = null;
                break;

            case 0x2C:

                MapperName = "Super Hik 7-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0x2D:

                MapperName = "1000000-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0x2E:

                MapperName = "Rumble Station";
                supported  = true;
                mapper     = new Mapper046();
                break;

            case 0x2F:

                MapperName = "NES-QJ";
                supported  = true;
                mapper     = new Mapper047();
                break;

            case 0x30:

                MapperName = "Taito TC190V";
                supported  = true;
                mapper     = new Mapper048();
                break;

            case 0x31:

                MapperName = "Super Hik 4-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0x32:

                MapperName = "SMB2J";
                supported  = true;
                mapper     = new Mapper050();
                break;

            case 0x33:

                MapperName = "11-in-1 Ball Games";
                supported  = false;
                mapper     = null;
                break;

            case 0x34:

                MapperName = "Mario 7-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0x39:

                MapperName = "54-in-1";
                supported  = true;
                mapper     = new Mapper057();
                break;

            case 0x3A:

                MapperName = "68-in-1";
                supported  = true;
                mapper     = new MapperCollection(58);
                break;

            case 0x3C:

                MapperName = "65-in-1";
                supported  = true;
                mapper     = new MapperCollection(60);
                break;

            case 0x3E:

                MapperName = "Mapper 62";
                supported  = true;
                mapper     = new MapperCollection(62);
                break;

            case 0x40:

                MapperName = "Tengen Rambo-1";
                supported  = true;
                mapper     = new Mapper064();
                break;

            case 0x41:

                MapperName = "Irem H-3001";
                supported  = true;
                mapper     = new Mapper065();
                break;

            case 0x42:

                MapperName = "Bandai 74161/32";
                supported  = true;
                mapper     = new MapperCollection(66);
                break;

            case 0x43:

                MapperName = "Sunsoft Mapper 3";
                supported  = true;
                mapper     = new Mapper067();
                break;

            case 0x44:

                MapperName = "Sunsoft Mapper 4";
                supported  = true;
                mapper     = new Mapper068();
                break;

            case 0x45:

                MapperName = "Sunsoft Mapper 5";
                supported  = false;
                mapper     = null;
                break;
                
            case 0x46:

                MapperName = "74161/32";
                supported  = true;
                mapper     = new Mapper070();
                break;

            case 0x47:

                MapperName = "Camerica Mapper";
                supported  = true;
                mapper     = new MapperCollection(71);
                break;
                
            case 0x48:

                MapperName = "Jaleco Early Mapper 0";
                supported  = true;
                mapper     = new MapperCollection(72);
                break;

            case 0x49:

                MapperName = "Konami VRC3";
                supported  = true;
                mapper     = new Mapper073();
                break;
                
            case 0x4B:

                MapperName = "Jaleco/Konami VRC1";
                supported  = true;
                mapper     = new Mapper075();
                break;

            case 0x4C:

                MapperName = "Namco 109";
                supported  = true;
                mapper     = new Mapper076();
                break;

            case 0x4D:

                MapperName = "Irem Early Mapper 0";
                supported  = true;
                mapper     = new MapperCollection(77);
                break;

            case 0x4E:

                MapperName = "Jaleco 74161/32";
                supported  = true;
                mapper     = new MapperCollection(78);
                break;

            case 0x4F:

                MapperName = "Nina-3 (AVE)";
                supported  = true;
                mapper     = new MapperCollection(79);
                break;

            case 0x50:

                MapperName = "Taito X-005";
                supported  = true;
                mapper     = new Mapper080();
                break;

            case 0x52:

                MapperName = "Taito C075";
                supported  = true;
                mapper     = new Mapper082();
                break;

            case 0x53:

                MapperName = "Cony";
                supported  = false;
                mapper     = null;
                break;

            case 0x55:

                MapperName = "Konami VRC7";
                supported  = false;
                mapper     = null;
                break;

            case 0x56:

                MapperName = "Jaleco Early Mapper 2";
                supported  = true;
                mapper     = new MapperCollection(86);
                break;

            case 0x57:

                MapperName = "Konami 74161/32";
                supported  = true;
                mapper     = new MapperCollection(87);
                break;

            case 0x58:

                MapperName = "Namco 118";
                supported  = true;
                mapper     = new Mapper088();
                break;

            case 0x59:

                MapperName = "Sunsoft Early Mapper";
                supported  = true;
                mapper     = new MapperCollection(89);
                break;

            case 0x5A:

                MapperName = "PC-JY-??";
                supported  = false;
                mapper     = null;
                break;

            case 0x5B:
			
                MapperName = "PC-HK-SF3";
                supported  = true;
                mapper     = new Mapper091();
                break;
				
            case 0x5C:

                MapperName = "Jaleco Early Mapper 1";
                supported  = true;
                mapper     = new MapperCollection(92);
                break;

            case 0x5D:

                MapperName = "Sunsoft 74161/32";
                supported  = true;
                mapper     = new MapperCollection(93);
                break;

            case 0x5E:

                MapperName = "Capcom 74161/32";
                supported  = true;
                mapper     = new MapperCollection(94);
                break;

            case 0x5F:

                MapperName = "Namco 106M";
                supported  = false;
                mapper     = null;
                break;

            case 0x60:

                MapperName = "Bandai 74161/32";
                supported  = false;
                mapper     = null;
                break;

            case 0x61:

                MapperName = "Irem 74161/32";
                supported  = true;
                mapper     = new MapperCollection(97);
                break;

            case 0x63:
			
                MapperName = "VS Unisystem";
                supported  = true;
                mapper     = new MapperCollection(99);
                break;
				
            case 0x64:

                MapperName = "Nesticle MMC3";
                supported  = false;
                mapper     = null;
                break;

            case 0x65:

                MapperName = "Jaleco 74161/32";
                supported  = true;
                mapper     = new MapperCollection(101);
                break;

            case 0x69:

                MapperName = "Nintendo World Championship";
                supported  = true;
                mapper     = new Mapper105();
                break;

            case 0x70:

                MapperName = "PC-Asder";
                supported  = false;
                mapper     = null;
                break;

            case 0x71:

                MapperName = "PC-Sachen/Hacker";
                supported  = true;
                mapper     = new Mapper113();
                break;

            case 0x75:

                MapperName = "PC-Future";
                supported  = true;
                mapper     = new Mapper117();
                break;

            case 0x76:

                MapperName = "IQS MMC3";
                supported  = false;
                mapper     = null;
                break;

            case 0x77:

                MapperName = "TQ-ROM";
                supported  = true;
                mapper     = new Mapper119();
                break;

            case 0x7A:

                MapperName = "Sunsoft 74161/32";
                supported  = true;
                mapper     = new Mapper122();
                break;

            case 0x8C:

                MapperName = "Mapper 140";
                supported  = true;
                mapper     = new MapperCollection(140);
                break;

            case 0x97:

                MapperName = "VS Unisystem (Konami)";
                supported  = true;
                mapper     = new MapperCollection(151);
                break;

            case 0xA0:

                MapperName = "PC-Aladdin";
                supported  = false;
                mapper     = null;
                break;

            case 0xB4:

                MapperName = "Nichibutsu";
                supported  = true;
                mapper     = new MapperCollection(180);
                break;

            case 0xB5:

                MapperName = "Hacker Internation Type 2";
                supported  = true;
                mapper     = new MapperCollection(181);
                break;

            case 0xB6:

                MapperName = "PC-SuperDonkeyKong";
                supported  = true;
                mapper     = new Mapper182();
                break;

            case 0xB7:

                MapperName = "Gimmick (Bootleg)";
                supported  = true;
                mapper     = new Mapper183();
                break;


            case 0xB8:

                MapperName = "Sunsoft 74161/32";
                supported  = true;
                mapper     = new MapperCollection(184);
                break;

            case 0xB9:

                MapperName = "CHR-ROM Disable Protect";
                supported  = true;
                mapper     = new MapperCollection(185);
                break;

            case 0xBB:

                MapperName = "Street Fighter Zero 2 97";
                supported  = false;
                mapper     = null;
                break;
                
            case 0xBC:

                MapperName = "Bandai Karaoke Studio";
                supported  = false;
                mapper     = null;
                break;

            case 0xBD:

                MapperName = "Street Fighter 2 Yoko";
                supported  = true;
                mapper     = new Mapper189();
                break;
                

            case 0xDE:

                MapperName = "Mapper 0xDE";
                supported  = true;
                mapper     = new MapperCollection(222);
                break;

            case 0xE1:

                MapperName = "72-in-1";
                supported  = true;
                mapper     = new Mapper225();
                break;

            case 0xE2:

                MapperName = "76-in-1";
                supported  = true;
                mapper     = new Mapper226();
                break;

            case 0xE3:

                MapperName = "1200-in-1";
                supported  = true;
                mapper     = new Mapper227();
                break;

            case 0xE4:

                MapperName = "Action 52";
                supported  = true;
                mapper     = new Mapper228();
                break;

            case 0xE5:

                MapperName = "31-in-1";
                supported  = true;
                mapper     = new MapperCollection(229);
                break;

            case 0xE6:

                MapperName = "22-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0xE7:

                MapperName = "20-in-1";
                supported  = true;
                mapper     = new MapperCollection(231);
                break;

            case 0xE8:

                MapperName = "Quattro Games";
                supported  = true;
                mapper     = new MapperCollection(232);
                break;

            case 0xE9:

                MapperName = "42-in-1";
                supported  = true;
                mapper     = new MapperCollection(233);
                break;

            case 0xEA:

                MapperName = "Maxi 15";
                supported  = false;
                mapper     = null;
                break;

            case 0xEB:

                MapperName = "150-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0xEC:

                MapperName = "800-in-1";
                supported  = true;
                mapper     = new Mapper236();
                break;

            case 0xED:

                MapperName = "70-in-1";
                supported  = false;
                mapper     = null;
                break;

            case 0xF0:

                MapperName = "Gen Ke Le Zhuan";
                supported  = true;
                mapper     = new MapperCollection(240);
                break;

            case 0xF2:

                MapperName = "Wai Xing Zhan Shi";
                supported  = true;
                mapper     = new MapperCollection(242);
                break;

            case 0xF3:

                MapperName = "PC-Sachen/Hacker";
                supported  = true;
                mapper     = new Mapper243();
                break;

            case 0xF4:
            
               MapperName = "Mapper 244";
               supported  = true;
               mapper     = new MapperCollection(244);
               break;
            
            
            case 0xF5:

                MapperName = "Yong Zhe Dou E Long";
                supported  = true;
                mapper     = new Mapper245();
                break;

            case 0xF6:

                MapperName = "Phone Serm Berm";
                supported  = true;
                mapper     = new MapperCollection(246);
                break;

            case 0xF8:

                MapperName = "Bao Qing Tian";
                supported  = true;
                mapper     = new Mapper248();
                break;

            case 0xFB:

                MapperName = "Mapper 251";
                supported  = true;
                mapper     = new Mapper251();
                break;

            case 0xFF:

                MapperName = "110-in-1";
                supported  = true;
                mapper     = new Mapper255();
                break;


*/
            default:
                throw new RuntimeException("Invalid Mapping");

            }



         // Start Kicking up Exceptions

            if (!supported || mapper == null) 
            {

               errorCode = NESCart.ERROR_UNSUPPORTED_MAPPER;
               return true;

            }


            if (numProgramROMBanks == 0) 
            {

               errorCode = NESCart.ERROR_MISSING_PROGRAM_ROM;
               return true;

            }



         // Return Successful
         
            return false;
            
            
      }

     /**
      *
      * <P>Gets the Carts Character ROM.</P>
      *
      * @return The Carts Character ROM.
      *
      */
      public final int[] getCharROM() {
    	  return charROM;
      }


     /**
      *
      * <P>Gets the last recorded error code.</P> 
      *
      * @return The last recorded error code.
      *
      */
      public final int getErrorCode() {
         return errorCode; 
      }


     /** 
      *
      * <P>Determines if Cart uses Four-Screen Name Tables.</P>
      *
      * @return True if Cart uses Four-Screen Name Tables.
      *
      */
      public final boolean getFourScreenNT(){ 
    	  return fourScreenNT; 
      }


     /**
      *
      * <P>Determines the name of the Carts Memory Mapper.</P>
      *
      * @return The name of the Carts Memory Mapper.
      *
      */
      public final String getMapperName() {
    	  return MapperName;
      }


     /**
      *
      * <P>Determines the number of the Carts Memory Mapper.</P>
      *
      * @return The number of the Carts Memory Mapper.
      *
      */
      public final int getMapperNumber() {

         return MapperNumber;
      }


     /**
      *
      * <P>Determines how the Cart is mirrored.</P>
      *
      * @return True if Cart uses Vertical Mirroring.
      *
      */
      public final boolean getMirroring() { 
         return vMirroring; 
      }


     /**
      *
      * <P>Gets the Carts Program ROM.</P>
      *
      * @return The Carts Program ROM.
      *
      */
      public final int[] getProgROM() {
         return progROM;
      }
}
