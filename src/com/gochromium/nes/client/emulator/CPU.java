package com.gochromium.nes.client.emulator;

import java.io.InputStream;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;

/** 
 *
 * Class for the CPU Controller required by the NESCafe NES Emulator.
 *
 * @author   David de Niese
 * @version  0.56f
 * @final    TRUE
 *
 */
public final class CPU {

    /**
     *
     * <P>The current NES Machine.</P>
     *
     */
    private final NES nes;

    private final TVController tvController;


    /**
     *
     * <P>Whether this CPU Controller can request Screen Drawing.</P>
     *
     */
    private boolean allowDrawScreen = true;
    /**
     *
     * <P>True if the CPU is active.</P>
     *
     */
    protected boolean cpuActive = false;
    /**
     *
     * <P>True if the CPU is paused.</P>
     *
     */
    protected boolean cpuPaused = false;
    /**
     *
     * <P>True if the CPU is running instructions.</P>
     *
     *
     */
    protected boolean cpuRunning = false;
    /**
     *
     * <P>The current Cartridge ROM Image.</P>
     *
     */
    protected NESCart currentCart;
    /**
     *
     * 6502 Register : The Accumulator (8 bit)
     *
     */
    private int A;
    /**
     *
     * 6502 Register : X (8 bits)
     *
     */
    private int X;
    /**
     *
     * 6502 Register : Y (8 bits)
     *
     */
    private int Y;
    /**
     *
     * 6502 Register : The Processor Status Register (8 bits)
     *
     */
    private int P;
    /**
     *
     * 6502 Register : The Stack Index Register (8 bits)
     *
     */
    private int S;
    /**
     *
     * 6502 Register : The Program Counter Register (16 bits)
     *
     */
    private int PC;
    /**
     *
     * <P>The number of CPU Cycles until the next Horizontal Blank.</P>
     *
     */
    private float cyclesPending;
    /**
     *
     * <P>The number of CPU Cycles between Horizontal Blanks.</P>
     *
     */
    public float CYCLES_PER_LINE = 116.0f;

    /**
     *
     * <P>True if a stop request has been issued.</P>
     *
     */
    private boolean stopRequest = false;
    /**
     *
     * <P>Halt Instruction has been fetched.</P>
     *
     */
    private boolean halted = false;
    /**
     *
     * Debug Mode
     *
     */
    public boolean debug = false;

    /**
     *
     * <P>Create a new NES CPU Controller.</P>
     *
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     *
     */
    public CPU(NES nes, TVController tvController) {

        this.nes = nes;
        this.tvController = tvController;

    }


    
//    public final void run() {
//
//
//
//
//        // Check if PunchOut Special Mode has been Enabled
//
//
//
//
//
//        // Request an Internal Reset
//
//        intReset();
//
//
//        // Start Counter for number of Screens Displayed
//
//        
//
//
//        // Turn off the Stop Request since we are just Starting
//
//        stopRequest = false;
//        stopRequestFulfilled = false;
//
//
//
//
//
//        // Start Emulating
//        timer.scheduleRepeating(1);
//
//
//        // Signal that Stop Requested has been Received
//
////        stopRequestFulfilled = true;
//
//
//
//    }
    
    Timer timer = new Timer() {

		@Override
		public void run() {
			
            if (everyY > everyX) {


                // Suggest Minimum Time before Frame is Finished

                timeFrameStop = System.currentTimeMillis() + waitPeriod;


                // Reset the EveryY

                everyY = 1;

            }


            // Emulate until a Vertical Blank occurs

            emulateFrame();


            // Check for a Reset Request

//            if (resetRequest) {
//                reset();
//            }


            // Loop until at Least the Minimum Frame Time has Expired
//TODO: RE-ENALBE TIME FRAME STOP METHOD!!!!!!!!!!!!!!!!!!
//            if (everyY == everyX) {
//                while (timeFrameStop > System.currentTimeMillis());
//            }


            // After 32 Frames the PPU is stable so turn off Loading Screen

            if (counter != 0) {

                if (++counter > 32) {

                    counter = 0;

                }

            }

            // Increment Frame

            everyY++;
			
			
////			while(TVController.REFRESH_REQUIRED) {
//				run2();
////			}
//			
////			try {
////			for(int i=0;i<;i++){
////				run2();
////			}
////			Window.alert("500 cpu cycles reached");
////			}catch(Exception ex) {
////				Window.alert(ex.toString());
////			}
////				TVController.REFRESH_REQUIRED = true;
////			timer.schedule(1);
		}
        
    };
    
    
        // Check every 5 Frames

        final int everyX = 5;


        // State Ideal Frame Rate

        final int idealFrameRate = 60;


        // Calculate Wait Period

        final int waitPeriod = (1000 / idealFrameRate) * everyX;


        // Declare Time for Frame Stop

        long timeFrameStop = System.currentTimeMillis() + waitPeriod;


        // State Frame Counter

        int everyY = 1;
        int counter = 1;
    

    
    

    /**
     *
     * <P>Method to Eat up CPU Cycles.</P>
     *
     */
    public final void eatCycles(int cycles) {

        cyclesPending -= cycles;

    }

    /**
     *
     * <P>Load a Cartridge ROM for the CPU.</P>
     *
     * @param fileName The filename of the ROM image.
     *
     */
    public final void cpuLoadRom(InputStream is) {


        // Load Cart into NESCart Object

        currentCart = new NESCart();
        boolean fail = currentCart.loadRom(is);



        // Check if an Error Occurred

        if (fail) {


            // If no Error can be Identified then use this Description

            String errString = "Error Reading ROM";


            // Try and Identify the Error more Precisely

            switch (currentCart.getErrorCode()) {

                case NESCart.ERROR_IO:

                    errString = "Cannot Read ROM";
                    break;


                case NESCart.ERROR_FILE_FORMAT:

                    errString = "Invalid ROM";
                    break;


                case NESCart.ERROR_UNSUPPORTED_MAPPER:

//#ifndef PUNCHOUT
//
//                     String mapName = "Mapper '" + currentCart.getMapperName() + "'";
//                     errString = mapName + " is not currently supported.";
//                     break;
//
//#else

                    errString = "Unsupported Mapper";
                    break;


   


            }


            // Get Rid of the Cart Object

            currentCart = null;
            throw new RuntimeException(errString);

        }


        // Close any Current ROM

        cpuStop();





        // Load the Memory with the Cartridge Image

        nes.memory.init(currentCart);//, fileName);



        // Load Specific Memory Manager for Supported Mappers

        nes.mapper = currentCart.mapper;

        if (nes.mapper == null) {

            currentCart = null;
            String errString = "The Hardware could not be located for the Cartridge.";
            throw new RuntimeException(errString);

        }


        // Initialise the Memory Mapper and Force a Mapper Reset

        nes.ppu.latchMapper = false;
        nes.mapper.init(nes.memory);
        nes.mapper.setCRC(currentCart.crc32);





        // Inform user that the Memory and its Mapper have been Loaded and Initialised

        if (currentCart.getMapperNumber() > 0) {
        } else {


        }




        // Initialise the Sound
//TODO: re-enable sound
//        if (nes.sound != null) {
//            nes.sound.reset();
//        }



    }

    /**
     *
     * <P>Run the CPU.</P>
     *
     */
    public final void cpuRun() {


        // Activates the CPU

        cpuActive = true;


        // Sets Running Flag

        cpuRunning = true;


        // Starts the Nintendo 6502 Machine

//        Thread bob = new Thread(this);
//        bob.start();

        intReset();


        // Turn off the Stop Request since we are just Starting

        stopRequest = false;


        // Start Emulating
        timer.scheduleRepeating(1);

    }

    /**
     *
     * <P>Stop the current CPU.</P>
     *
     */
    public final void cpuStop() {


        // If the CPU is not Running Code then Return

        if (!cpuRunning) {
            return;
        }


        // Issue a Stop Request to the Processor

        stopProcessing();


        // Force a Closing

        cpuRunning = false;


    }

    /**
     *
     * Halt the CPU
     *
     */
    public final boolean isCPUHalted() {

        return halted;

    }

    /**
     *
     * <P>Request that the Processor performs a NMI
     *
     */
    public final void cpuNMI() {

        NMI();

    }

    /**
     *
     * <P>Ask the TV Controller to draw the Screen.</P>
     *
     * @param force True to force a draw.
     *
     */
    public final synchronized void drawScreen(boolean force) {


        // Check if the CPU is allowed to make this Request

        if (!allowDrawScreen) {
            return;
        }


        // Ask TV Controller to Draw whatever is in its Buffer

        tvController.drawScreen(force);


    }



////////////////////////////////////////////////////////////////////////////////
//
// Main Control Functions
//
////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * Clears the Display.
     *
     */
    private final void deleteScreen() {


    }

    /**
     *
     * <P>Emulate a Frame.</P>
     *
     */
    private final void emulateFrame() {

    	PPU localPPU = nes.ppu;
    	Mapper localMapper = nes.mapper;
    	
        // Start PPU Frame
    	localPPU.startFrame();


        // Lines 0-239
        for (int i = 0; i < 240; i++) {
            //TODO: figure out if I need to re-enable emulateCPUCycles
            emulateCPUCycles(CYCLES_PER_LINE);
            if (localMapper.syncH(i) != 0) {
                IRQ();
            }
            localPPU.drawScanLine();
        }

        // Frame IRQ
        if ((nes.frameIRQEnabled & 0xC0) == 0) {
            IRQ();
        }

        // Lines 240-261
        for (int i = 240; i < 262; i++) {

            // End of Virtual Blank
            if (i == 261) {
            	localPPU.endVBlank();
            }

            // Start of Virtual Blank
            if (i == 241) {

            	localPPU.startVBlank();
            	localMapper.syncV();

                emulateCPUCycles(1);
                if (localPPU.nmiEnabled()) {
                    NMI();
                }

                emulateCPUCycles(CYCLES_PER_LINE - 1);
                if (localMapper.syncH(i) != 0) {
                    IRQ();
                }
            }
            emulateCPUCycles(CYCLES_PER_LINE);
            if (localMapper.syncH(i) != 0) {
                IRQ();
            }
        }

        // Drawn the Screen
        drawScreen(false);

        //TODO:re-enable sound
        //nes.sound.refresh();
    }

    
    private final void emulateFrame2() {

        // Start PPU Frame
        nes.ppu.startFrame();


        // Lines 0-239
        for (int i = 0; i < 240; i++) {
            //TODO: figure out if I need to re-enable emulateCPUCycles
            emulateCPUCycles(CYCLES_PER_LINE);
            if (nes.mapper.syncH(i) != 0) {
                IRQ();
            }
            nes.ppu.drawScanLine();
        }

        // Frame IRQ
        if ((nes.frameIRQEnabled & 0xC0) == 0) {
            IRQ();
        }

        // Lines 240-261
        for (int i = 240; i <= 261; i++) {

            // End of Virtual Blank
            if (i == 261) {
                nes.ppu.endVBlank();
            }

            // Start of Virtual Blank
            if (i == 241) {

                nes.ppu.startVBlank();
                nes.mapper.syncV();

                emulateCPUCycles(1);
                if (nes.ppu.nmiEnabled()) {
                    NMI();
                }

                emulateCPUCycles(CYCLES_PER_LINE - 1);
                if (nes.mapper.syncH(i) != 0) {
                    IRQ();
                }
            }
            emulateCPUCycles(CYCLES_PER_LINE);
            if (nes.mapper.syncH(i) != 0) {
                IRQ();
            }
        }

        // Drawn the Screen
        drawScreen(false);

        //TODO:re-enable sound
        //nes.sound.refresh();
    }
    
    
    /**
     *
     * <P>Perform a Non Maskable Interrupt.</P>
     *
     */
    public final void NMI() {





        pushWord(PC);
        push(P & 0xEF); // CLEAR BRK
        PC = readWord(0xFFFA);
        cyclesPending += 7;

    }

    /**
     *
     * <P>Perform a IRQ/BRK Interrupt.</P>
     *
     */
    public final void IRQ() {

        if ((P & 0x4) == 0x00) {



            pushWord(PC);
            push(P & 0xEF); // CLEAR BRK
            PC = readWord(0xFFFE);
            P |= 0x04;
            cyclesPending += 7;

        }

    }

    /**
     *
     * <P>Emulate until the next Horizontal Blank is encountered.</P>
     *
     */
    public final void emulateCPUCycles(float cycles) {


        // Declare Deficit Cycles

        cyclesPending += cycles;


        // Loop until a Horizontal Blank is encountered

        while (cyclesPending > 0) {


            // Fetch and Execute the Next Instruction
//TODO: COMMENTED OUT FOR PROFILING, NEED TO RE-ENABLE	
            if (!halted) {
                instructionFetchExecute();
            } else {
                cyclesPending--;
            }


            // Check for a Stop Request

//            if (stopRequest) {
//                return;
//            }

        }

    }

    /**
     *
     * Halt the CPU
     *
     */
    public final void haltCPU() {

        halted = true;

    }

    
    /**
    *
    * <P>Fetch and Execute the next Instruction.</P>
    *
    */
   private final void instructionFetchExecute() {


       // Sanity Check
//TODO: COMMENTED OUT FOR PROFILING, NEED TO RE-ENABLE	
//       if (halted) {
//           return;
//       }


       // Display Debug Header Information







       // Fetch the Next Instruction Code
   	MemoryManager memory = nes.memory;
       int instCode = memory.read(PC++);


       // Declare Variables for Handling Addresses and Values

       int address;
       int writeVal;



       // Check if an Instruction Code can be Identified


       switch (instCode) {


           case 0x00:  // BRK


               address = PC + 1;
               pushWord(address);
               push(P | 0x10);

               PC = readWord(0xFFFE);
               P |= 0x04;
               P |= 0x10;
               break;


           case 0xA9:  // LDA #aa



               A = byImmediate();
               setStatusFlags(A);
               break;


           case 0xA5:  // LDA Zero Page



               A = memory.read(byZeroPage());
               setStatusFlags(A);
               break;


           case 0xB5:  // LDA $aa,X



               A = memory.read(byZeroPageX());
               setStatusFlags(A);
               break;


           case 0xAD:  // LDA $aaaa



               A = memory.read(byAbsolute());
               setStatusFlags(A);
               break;


           case 0xBD: // LDA $aaaa,X



               A = memory.read(byAbsoluteX());
               setStatusFlags(A);
               break;


           case 0xB9: // LDA $aaaa,Y


               A = memory.read(byAbsoluteY());
               setStatusFlags(A);
               break;


           case 0xA1: // LDA ($aa,X)


               A = memory.read(byIndirectX());
               setStatusFlags(A);
               break;


           case 0xB1: // LDA ($aa),Y


               A = memory.read(byIndirectY());
               setStatusFlags(A);
               break;


           case 0xA2:  // LDX #aa

               X = byImmediate();
               setStatusFlags(X);
               break;


           case 0xA6:  // LDX $aa


               X = memory.read(byZeroPage());
               setStatusFlags(X);
               break;


           case 0xB6:  // LDX $aa,Y


               X = memory.read(byZeroPageY());
               setStatusFlags(X);
               break;


           case 0xAE:  // LDX $aaaa


               X = memory.read(byAbsolute());
               setStatusFlags(X);
               break;


           case 0xBE:  // LDX $aaaa,Y


               X = memory.read(byAbsoluteY());
               setStatusFlags(X);
               break;


           case 0xA0:  // LDY #aa


               Y = byImmediate();
               setStatusFlags(Y);
               break;


           case 0xA4:  // LDY $aa



               Y = memory.read(byZeroPage());
               setStatusFlags(Y);
               break;


           case 0xB4:  // LDY $aa,X


               Y = memory.read(byZeroPageX());
               setStatusFlags(Y);
               break;


           case 0xAC:  // LDY $aaaa


               Y = memory.read(byAbsolute());
               setStatusFlags(Y);
               break;


           case 0xBC:  // LDY $aaaa,x


               Y = memory.read(byAbsoluteX());
               setStatusFlags(Y);
               break;

           case 0x85:  // STA $aa


               address = byZeroPage();
               write(address, A);
               break;


           case 0x95:  // STA $aa,X


               address = byZeroPageX();
               write(address, A);
               break;


           case 0x8D:  // STA $aaaa


               address = byAbsolute();
               write(address, A);
               break;


           case 0x9D:  // STA $aaaa,X


               address = byAbsoluteX();
               write(address, A);
               break;


           case 0x99:  // STA $aaaa,Y


               address = byAbsoluteY();
               write(address, A);
               break;


           case 0x81:  // STA ($aa,X)


               address = byIndirectX();
               write(address, A);
               break;


           case 0x91:  // STA ($aa),Y


               address = byIndirectY();
               write(address, A);
               break;


           case 0x86:  // STX $aa


               address = byZeroPage();
               write(address, X);
               break;


           case 0x96:  // STX $aa,Y


               address = byZeroPageY();
               write(address, X);
               break;


           case 0x8E:  // STX $aaaa


               address = byAbsolute();
               write(address, X);
               break;


           case 0x84:  // STY $aa


               address = byZeroPage();
               write(address, Y);
               break;


           case 0x94:  // STY $aa,X


               address = byZeroPageX();
               write(address, Y);
               break;


           case 0x8C:  // STY $aaaa


               address = byAbsolute();
               write(address, Y);
               break;


           case 0xAA:  // TAX


               X = A;
               setStatusFlags(X);
               break;


           case 0xA8:  // TAY


               Y = A;
               setStatusFlags(Y);
               break;


           case 0xBA:  // TSX


               X = S & 0xFF;
               setStatusFlags(X);
               break;


           case 0x8A:  // TXA


               A = X;
               setStatusFlags(A);
               break;


           case 0x9A:  // TXS


               S = X & 0XFF;
               break;


           case 0x98:  // TYA


               A = Y;
               setStatusFlags(A);
               break;


           case 0x09:  // ORA #aa


               A |= byImmediate();
               setStatusFlags(A);
               break;


           case 0x05:  // ORA $aa


               address = byZeroPage();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x15:  // ORA $aa,X


               address = byZeroPageX();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x0D:  // ORA $aaaa


               address = byAbsolute();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x1D:  // ORA $aaaa,X


               address = byAbsoluteX();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x19:  // ORA $aaaa,Y



               address = byAbsoluteY();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x01:  // ORA ($aa,X)


               address = byIndirectX();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x11:  // ORA ($aa),Y


               address = byIndirectY();
               A |= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x29:  // AND #aa


               A &= byImmediate();
               setStatusFlags(A);
               break;


           case 0x25:  // AND $aa



               address = byZeroPage();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x35:  // AND $aa,X


               address = byZeroPageX();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x2D:  // AND $aaaa


               address = byAbsolute();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x3D:  // AND $aaaa,X


               address = byAbsoluteX();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x39:  // AND $aaaa,Y


               address = byAbsoluteY();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x21:  // AND ($aa,X)


               address = byIndirectX();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x31:  // AND ($aa),Y


               address = byIndirectY();
               A &= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x49:  // EOR #aa


               A ^= byImmediate();
               setStatusFlags(A);
               break;


           case 0x45:  // EOR $aa


               address = byZeroPage();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x55:  // EOR $aa,X


               address = byZeroPageX();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x4D:  // EOR $aaaa


               address = byAbsolute();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x5D:  // EOR $aaaa,X


               address = byAbsoluteX();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x59:  // EOR $aaaa,Y


               address = byAbsoluteY();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x41:  // EOR ($aa,X)


               address = byIndirectX();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x51:  // EOR ($aa),Y


               address = byIndirectY();
               A ^= memory.read(address);
               setStatusFlags(A);
               break;


           case 0x24:  // BIT $aa


               operateBit(read(byZeroPage()));
               break;


           case 0x2C:  // BIT $aaaa


               operateBit(read(byAbsolute()));
               break;



           case 0x0A:  // ASL A



               A = ASL(A);
               break;


           case 0x06:  // ASL $aa



               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ASL(writeVal);
               write(address, writeVal);
               break;


           case 0x16:  // ASL $aa,X



               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ASL(writeVal);
               write(address, writeVal);
               break;


           case 0x0E:  // ASL $aaaa



               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ASL(writeVal);
               write(address, writeVal);
               break;

           case 0x1E:  // ASL $aaaa,X



               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ASL(writeVal);
               write(address, writeVal);
               break;


           case 0x4A:  // LSR A



               A = LSR(A);
               break;


           case 0x46:  // LSR $aa



               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = LSR(writeVal);
               write(address, writeVal);
               break;


           case 0x56:  // LSR $aa,X



               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = LSR(writeVal);
               write(address, writeVal);
               break;


           case 0x4E:  // LSR $aaaa



               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = LSR(writeVal);
               write(address, writeVal);
               break;


           case 0x5E:  // LSR $aaaa,X



               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = LSR(writeVal);
               write(address, writeVal);
               break;
           case 0x2A:  // ROL A


               A = ROL(A);
               break;


           case 0x26:  // ROL $aa (RWMW)


               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROL(writeVal);
               write(address, writeVal);
               break;


           case 0x36:  // ROL $aa,X (RWMW)


               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROL(writeVal);
               write(address, writeVal);
               break;


           case 0x2E:  // ROL $aaaa (RWMW)


               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROL(writeVal);
               write(address, writeVal);
               break;


           case 0x3E:  // ROL $aaaa,X (RWMW)


               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROL(writeVal);
               write(address, writeVal);
               break;


           case 0x6A:  // ROR A


               A = ROR(A);
               break;


           case 0x66:  // ROR $aa



               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROR(writeVal);
               write(address, writeVal);
               break;




           case 0x76:  // ROR $aa,X


               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROR(writeVal);
               write(address, writeVal);
               break;

           case 0x6E:  // ROR $aaaa



               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROR(writeVal);
               write(address, writeVal);
               break;


           case 0x7E:  // ROR $aaaa,X



               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = ROR(writeVal);
               write(address, writeVal);
               break;



           case 0x90:  // BCC



               branch(0x01, false);
               break;


           case 0xB0:  // BCS



               branch(0x01, true);
               break;


           case 0xD0:  // BNE



               branch(0x02, false);
               break;


           case 0xF0:  // BEQ



               branch(0x02, true);
               break;


           case 0x10:  // BPL


               branch(0x80, false);
               break;


           case 0x30:  // BMI



               branch(0x80, true);
               break;


           case 0x50:  // BVC



               branch(0x40, false);
               break;


           case 0x70:  // BVS



               branch(0x40, true);
               break;


           case 0x4C:  // JMP $aaaa



               PC = byAbsolute();
               break;


           case 0x6C:  // JMP ($aaaa)


               address = byAbsolute();



               if ((address & 0x00FF) == 0xFF) {
                   PC = (read(address & 0xFF00) << 8) | read(address);
               } else {
                   PC = readWord(address);
               }

               break;


           case 0x20:  // JSR $aaaa



               address = PC + 1;
               pushWord(address);
               PC = byAbsolute();
               break;


           case 0x60:  // RTS



               PC = popWord() + 1;
               break;


           case 0x40:  // RTI



               P = pop();
               PC = popWord();
               break;


           case 0x48:  // PHA



               push(A);
               break;


           case 0x08:  // PHP



               push(P | 0x10); // SET BRK
               break;


           case 0x68:  // PLA



               A = pop();
               setStatusFlags(A);
               break;


           case 0x28:  // PLP



               P = pop();
               break;


           case 0x18:  // CLC



               P &= 0xfe;
               break;


           case 0xD8:  // CLD



               P &= 0xf7;
               break;


           case 0x58:  // CLI



               P &= 0xfb;
               break;


           case 0xB8:  // CLV



               P &= 0xbf;
               break;


           case 0x38:  // SEC



               P |= 0x1;
               break;


           case 0xF8:  // SED



               P |= 0x8;
               break;


           case 0x78:  // SEI



               P |= 0x4;
               break;


           case 0xE6:  // INC $aa (RWMW)



               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = increment(writeVal);
               write(address, writeVal);
               break;


           case 0xF6:  // INC $aa,X (RWMW)



               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = increment(read(address));
               write(address, writeVal);
               break;


           case 0xEE:  // INC $aaaa (RWMW)



               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = increment(read(address));
               write(address, writeVal);
               break;


           case 0xFE:  // INC $aaaa,X (RWMW)



               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = increment(read(address));
               write(address, writeVal);
               break;


           case 0xE8:  // INX



               X++;
               X &= 0xff;
               setStatusFlags(X);
               break;


           case 0xC8:  // INY


               Y++;
               Y &= 0xff;
               setStatusFlags(Y);
               break;


           case 0xC6:  // DEC $aa (RWMW)

               address = byZeroPage();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = decrement(read(address));
               write(address, writeVal);
               break;


           case 0xD6:  // DEC $aa,X (RWMW)



               address = byZeroPageX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = decrement(read(address));
               write(address, writeVal);
               break;


           case 0xCE:  // DEC $aaaa (RWMW)


               address = byAbsolute();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = decrement(read(address));
               write(address, writeVal);
               break;


           case 0xDE:  // DEC $aaaa,X (RWMW)



               address = byAbsoluteX();
               writeVal = memory.read(address);
               write(address, writeVal);
               writeVal = decrement(read(address));
               write(address, writeVal);
               break;


           case 0xCA:  // DEX



               X--;
               X &= 0xff;
               setStatusFlags(X);
               break;


           case 0x88:  // DEY



               Y--;
               Y &= 0xff;
               setStatusFlags(Y);
               break;


           case 0x69:  // ADC #aa



               operateAdd(byImmediate());
               break;


           case 0x65:  // ADC $aa



               operateAdd(read(byZeroPage()));
               break;


           case 0x75:  // ADC $aa,X



               operateAdd(read(byZeroPageX()));
               break;


           case 0x6D:  // ADC $aaaa



               operateAdd(read(byAbsolute()));
               break;


           case 0x7D:  // ADC $aaaa,X



               operateAdd(read(byAbsoluteX()));
               break;


           case 0x79:  // ADC $aaaa,Y



               operateAdd(read(byAbsoluteY()));
               break;


           case 0x61:  // ADC ($aa,X)



               operateAdd(read(byIndirectX()));
               break;


           case 0x71:  // ADC ($aa),Y



               operateAdd(read(byIndirectY()));
               break;


           case 0xEB:  // SBC #aa
           case 0xE9:  // SBC #aa



               operateSub(byImmediate());
               break;


           case 0xE5:  // SBC $aa


               operateSub(read(byZeroPage()));
               break;


           case 0xF5:  // SBC $aa,X


               operateSub(read(byZeroPageX()));
               break;


           case 0xED:  // SBC $aaaa


               operateSub(read(byAbsolute()));
               break;


           case 0xFD:  // SBC $aaaa,X


               operateSub(read(byAbsoluteX()));
               break;


           case 0xF9:  // SBC $aaaa,Y



               operateSub(read(byAbsoluteY()));
               break;


           case 0xE1:  // SBC ($aa,X)



               operateSub(read(byIndirectX()));
               break;


           case 0xF1:  // SBC ($aa),Y


               operateSub(read(byIndirectY()));
               break;


           case 0xC9:  // CMP #aa



               operateCmp(A, byImmediate());
               break;


           case 0xC5:  // CMP $aa


               operateCmp(A, read(byZeroPage()));
               break;


           case 0xD5:  // CMP $aa,X



               operateCmp(A, read(byZeroPageX()));
               break;


           case 0xCD:  // CMP $aaaa



               operateCmp(A, read(byAbsolute()));
               break;

//UP TO HERE

           case 0xDD:  // CMP $aaaa,X



               operateCmp(A, read(byAbsoluteX()));
               break;


           case 0xD9:  // CMP $aaaa,Y


               operateCmp(A, read(byAbsoluteY()));
               break;


           case 0xC1:  // CMP ($aa,X)


               operateCmp(A, read(byIndirectX()));
               break;


           case 0xD1:  // CMP ($aa),Y



               operateCmp(A, read(byIndirectY()));
               break;


           case 0xE0:  // CPX #aa



               operateCmp(X, byImmediate());
               break;


           case 0xE4:  // CPX $aa



               operateCmp(X, read(byZeroPage()));
               break;


           case 0xEC:  // CPX $aaaa



               operateCmp(X, read(byAbsolute()));
               break;


           case 0xC0:  // CPY #aa


               operateCmp(Y, byImmediate());
               break;


           case 0xC4:  // CPY $aa



               operateCmp(Y, read(byZeroPage()));
               break;


           case 0xCC:  // CPY $aaaa



               operateCmp(Y, read(byAbsolute()));
               break;



//UNDOCUMENTED CODES
           case 0x1A:  // UNDOCUMENTED : NOP
           case 0x3A:
           case 0x5A:
           case 0x7A:
           case 0xDA:
           case 0xEA:
           case 0xFA:
           	break;

//           case 0x67:  // RRA $aa
//           case 0x77:  // RRA $aa,X
//           case 0x6F:  // RRA $aaaa
//           case 0x37:  // RLA $aa,X
//           case 0x2F:  // RLA $aaaa
//           case 0x3F:  // RLA $aaaa,X
//           case 0x3B:  // RLA $aaaa,Y
//           case 0x23:  // RLA ($aa,X)
//           case 0x33:  // RLA ($aa),Y
//           case 0x47:  // LSE $aa
//           case 0x57:  // LSE $aa,X
//           case 0x4F:  // LSE $aaaa
//           case 0x5F:  // LSE $aaaa,X
//           case 0x5B:  // LSE $aaaa,Y
//           case 0x43:  // LSE ($aa,X)
//           case 0x53:  // LSE ($aa),Y
//           case 0x17:  // ASO $aa,X
//           case 0x0F:  // ASO $aaaa
//           case 0x1F:  // ASO $aaaa,X
//           case 0x1B:  // ASO $aaaa,Y
//           case 0x03:  // ASO ($aa,X)
//           case 0x13:  // ASO ($aa),Y
//           case 0x07:  // ASO $aa
//           case 0xA7:  // LAX $aa
//           case 0xB7:  // LAX $aa,Y
//           case 0xAF:  // LAX $aaaa
//           case 0xBF:  // LAX $aaaa,Y
//           case 0xA3:  // LAX ($aa,X)
//           case 0xB3:  // LAX ($aa),Y
//           case 0x7F:  // RRA $aaaa,X
//           case 0x7B:  // RRA $aaaa,Y
//           case 0x63:  // RRA ($aa,X)
//           case 0x73:  // RRA ($aa),Y
//           case 0xEF:  // UNDOCUMENTED : INS aaaa
//           case 0xFF:  // UNDOCUMENTED : INS aaaa,X
//           case 0xFB:  // UNDOCUMENTED : INS aaaa,Y
//           case 0xE7:  // UNDOCUMENTED : INS aa
//           case 0xF7:  // UNDOCUMENTED : INS aa,X
//           case 0xE3:  // UNDOCUMENTED : INS (aa,X)
//           case 0xF3:  // UNDOCUMENTED : INS (aa),Y
//           case 0x8F:  // UNDOCUMENTED : AXS $aaaa
//           case 0x87:  // UNDOCUMENTED : AXS $aa
//           case 0x97:  // UNDOCUMENTED : AXS $aa,Y
//           case 0x83:  // UNDOCUMENTED : AXS ($aa,X)
//           case 0xCB:  // UNDOCUMENTED : SAX #aa
//           case 0xAB:  // UNDOCUMENTED : OAL #aa
//           case 0x4B:  // UNDOCUMENTED : ALR #aa
//           case 0x6B:  // UNDOCUMENTED : ARR #aa
//           case 0x8B:  // UNDOCUMENTED : XAA #aa
//           case 0xCF:  // UNDOCUMENTED : DCM aaaa
//           case 0xDF:  // UNDOCUMENTED : DCM aaaa,X
//           case 0xDB:  // UNDOCUMENTED : DCM $aaaa,Y
//           case 0xC7:  // UNDOCUMENTED : DCM $aa
//           case 0xD7:  // UNDOCUMENTED : DCM $aa,X
//           case 0xC3:  // UNDOCUMENTED : DCM (aa,X)
//           case 0xD3:  // UNDOCUMENTED : DCM (aa),Y
//           case 0xBB:  // UNDOCUMENTED : LAS aaaa,Y
//           case 0x0B:  // UNDOCUMENTED : ANC #aa
//           case 0x2B:
//           case 0x9B:  // UNDOCUMENTED : TAS $aaaa,Y
//           case 0x9C:  // UNDOCUMENTED : SAY $aaaa,X
//           case 0x9E:  // UNDOCUMENTED : XAS aaaa,Y
//           case 0x9F:  // UNDOCUMENTED : AXA aaaa,Y
//           case 0x93:  // UNDOCUMENTED : AXA (aa),Y
//           case 0x80:  // UNDOCUMENTED : SKB
//           case 0x82:
//           case 0x89:
//           case 0xC2:
//           case 0xE2:
//           case 0x04:
//           case 0x14:
//           case 0x34:
//           case 0x44:
//           case 0x54:
//           case 0x64:
//           case 0x74:
//           case 0xD4:
//           case 0xF4:
//           case 0x0C:  // UNDOCUMENTED : SKW
//           case 0x1C:
//           case 0x3C:
//           case 0x5C:
//           case 0x7C:
//           case 0xDC:
//           case 0xFC:
//           case 0x02:  // UNDOCUMENTED : HLT
//           case 0x12:
//           case 0x22:
//           case 0x32:
//           case 0x42:
//           case 0x52:
//           case 0x62:
//           case 0x72:
//           case 0x92:
//           case 0xB2:
//           case 0xD2:
//           case 0xF2:
//
//
//               halted = true;
//               PC--;
//               break;

           default:  // Unknown OpCode so Hang
           	halted=true;
               PC--;
               break;


       }


       // Decrement Cycles by number of Cycles in Instruction

       cyclesPending -= cycles[instCode];

   }
    
//    /**
//     *
//     * <P>Fetch and Execute the next Instruction.</P>
//     *
//     */
//    private final void instructionFetchExecute() {
//
//
//        // Sanity Check
////TODO: COMMENTED OUT FOR PROFILING, NEED TO RE-ENABLE	
////        if (halted) {
////            return;
////        }
//
//
//        // Display Debug Header Information
//
//
//
//
//
//
//
//        // Fetch the Next Instruction Code
//
//        int instCode = read(PC++);
//
//
//        // Declare Variables for Handling Addresses and Values
//
//        int address;
//        int writeVal;
//
//
//
//        // Check if an Instruction Code can be Identified
//
//
//        switch (instCode) {
//
//
//            case 0x00:  // BRK
//
//
//                address = PC + 1;
//                pushWord(address);
//                push(P | 0x10);
//
//                PC = readWord(0xFFFE);
//                P |= 0x04;
//                P |= 0x10;
//                break;
//
//
//            case 0xA9:  // LDA #aa
//
//
//
//                A = byImmediate();
//                setStatusFlags(A);
//                break;
//
//
//            case 0xA5:  // LDA Zero Page
//
//
//
//                A = read(byZeroPage());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xB5:  // LDA $aa,X
//
//
//
//                A = read(byZeroPageX());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xAD:  // LDA $aaaa
//
//
//
//                A = read(byAbsolute());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xBD: // LDA $aaaa,X
//
//
//
//                A = read(byAbsoluteX());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xB9: // LDA $aaaa,Y
//
//
//                A = read(byAbsoluteY());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xA1: // LDA ($aa,X)
//
//
//                A = read(byIndirectX());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xB1: // LDA ($aa),Y
//
//
//                A = read(byIndirectY());
//                setStatusFlags(A);
//                break;
//
//
//            case 0xA2:  // LDX #aa
//
//                X = byImmediate();
//                setStatusFlags(X);
//                break;
//
//
//            case 0xA6:  // LDX $aa
//
//
//                X = read(byZeroPage());
//                setStatusFlags(X);
//                break;
//
//
//            case 0xB6:  // LDX $aa,Y
//
//
//                X = read(byZeroPageY());
//                setStatusFlags(X);
//                break;
//
//
//            case 0xAE:  // LDX $aaaa
//
//
//                X = read(byAbsolute());
//                setStatusFlags(X);
//                break;
//
//
//            case 0xBE:  // LDX $aaaa,Y
//
//
//                X = read(byAbsoluteY());
//                setStatusFlags(X);
//                break;
//
//
//            case 0xA0:  // LDY #aa
//
//
//                Y = byImmediate();
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xA4:  // LDY $aa
//
//
//
//                Y = read(byZeroPage());
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xB4:  // LDY $aa,X
//
//
//                Y = read(byZeroPageX());
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xAC:  // LDY $aaaa
//
//
//                Y = read(byAbsolute());
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xBC:  // LDY $aaaa,x
//
//
//                Y = read(byAbsoluteX());
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xA7:  // LAX $aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byZeroPage());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xB7:  // LAX $aa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byZeroPageY());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX $aa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xAF:  // LAX $aaaa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byAbsolute());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0xBF:  // LAX $aaaa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byAbsoluteY());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xA3:  // LAX ($aa,X)
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byIndirectX());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX ($aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xB3:  // LAX ($aa),Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = read(byIndirectY());
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAX ($aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//            case 0x85:  // STA $aa
//
//
//                address = byZeroPage();
//                write(address, A);
//                break;
//
//
//            case 0x95:  // STA $aa,X
//
//
//                address = byZeroPageX();
//                write(address, A);
//                break;
//
//
//            case 0x8D:  // STA $aaaa
//
//
//                address = byAbsolute();
//                write(address, A);
//                break;
//
//
//            case 0x9D:  // STA $aaaa,X
//
//
//                address = byAbsoluteX();
//                write(address, A);
//                break;
//
//
//            case 0x99:  // STA $aaaa,Y
//
//
//                address = byAbsoluteY();
//                write(address, A);
//                break;
//
//
//            case 0x81:  // STA ($aa,X)
//
//
//                address = byIndirectX();
//                write(address, A);
//                break;
//
//
//            case 0x91:  // STA ($aa),Y
//
//
//                address = byIndirectY();
//                write(address, A);
//                break;
//
//
//            case 0x86:  // STX $aa
//
//
//                address = byZeroPage();
//                write(address, X);
//                break;
//
//
//            case 0x96:  // STX $aa,Y
//
//
//                address = byZeroPageY();
//                write(address, X);
//                break;
//
//
//            case 0x8E:  // STX $aaaa
//
//
//                address = byAbsolute();
//                write(address, X);
//                break;
//
//
//            case 0x84:  // STY $aa
//
//
//                address = byZeroPage();
//                write(address, Y);
//                break;
//
//
//            case 0x94:  // STY $aa,X
//
//
//                address = byZeroPageX();
//                write(address, Y);
//                break;
//
//
//            case 0x8C:  // STY $aaaa
//
//
//                address = byAbsolute();
//                write(address, Y);
//                break;
//
//
//            case 0xAA:  // TAX
//
//
//                X = A;
//                setStatusFlags(X);
//                break;
//
//
//            case 0xA8:  // TAY
//
//
//                Y = A;
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xBA:  // TSX
//
//
//                X = S & 0xFF;
//                setStatusFlags(X);
//                break;
//
//
//            case 0x8A:  // TXA
//
//
//                A = X;
//                setStatusFlags(A);
//                break;
//
//
//            case 0x9A:  // TXS
//
//
//                S = X & 0XFF;
//                break;
//
//
//            case 0x98:  // TYA
//
//
//                A = Y;
//                setStatusFlags(A);
//                break;
//
//
//            case 0x09:  // ORA #aa
//
//
//                A |= byImmediate();
//                setStatusFlags(A);
//                break;
//
//
//            case 0x05:  // ORA $aa
//
//
//                address = byZeroPage();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x15:  // ORA $aa,X
//
//
//                address = byZeroPageX();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x0D:  // ORA $aaaa
//
//
//                address = byAbsolute();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x1D:  // ORA $aaaa,X
//
//
//                address = byAbsoluteX();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x19:  // ORA $aaaa,Y
//
//
//
//                address = byAbsoluteY();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x01:  // ORA ($aa,X)
//
//
//                address = byIndirectX();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x11:  // ORA ($aa),Y
//
//
//                address = byIndirectY();
//                A |= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x29:  // AND #aa
//
//
//                A &= byImmediate();
//                setStatusFlags(A);
//                break;
//
//
//            case 0x25:  // AND $aa
//
//
//
//                address = byZeroPage();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x35:  // AND $aa,X
//
//
//                address = byZeroPageX();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x2D:  // AND $aaaa
//
//
//                address = byAbsolute();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x3D:  // AND $aaaa,X
//
//
//                address = byAbsoluteX();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x39:  // AND $aaaa,Y
//
//
//                address = byAbsoluteY();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x21:  // AND ($aa,X)
//
//
//                address = byIndirectX();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x31:  // AND ($aa),Y
//
//
//                address = byIndirectY();
//                A &= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x49:  // EOR #aa
//
//
//                A ^= byImmediate();
//                setStatusFlags(A);
//                break;
//
//
//            case 0x45:  // EOR $aa
//
//
//                address = byZeroPage();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x55:  // EOR $aa,X
//
//
//                address = byZeroPageX();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x4D:  // EOR $aaaa
//
//
//                address = byAbsolute();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x5D:  // EOR $aaaa,X
//
//
//                address = byAbsoluteX();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x59:  // EOR $aaaa,Y
//
//
//                address = byAbsoluteY();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x41:  // EOR ($aa,X)
//
//
//                address = byIndirectX();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x51:  // EOR ($aa),Y
//
//
//                address = byIndirectY();
//                A ^= read(address);
//                setStatusFlags(A);
//                break;
//
//
//            case 0x24:  // BIT $aa
//
//
//                operateBit(read(byZeroPage()));
//                break;
//
//
//            case 0x2C:  // BIT $aaaa
//
//
//                operateBit(read(byAbsolute()));
//                break;
//
//
//            case 0x07:  // ASO $aa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x0A:  // ASL A
//
//
//
//                A = ASL(A);
//                break;
//
//
//            case 0x06:  // ASL $aa
//
//
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ASL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x16:  // ASL $aa,X
//
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ASL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x0E:  // ASL $aaaa
//
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ASL(writeVal);
//                write(address, writeVal);
//                break;
//
//            case 0x1E:  // ASL $aaaa,X
//
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ASL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x17:  // ASO $aa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageX();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x0F:  // ASO $aaaa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//            case 0x1F:  // ASO $aaaa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO $aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x1B:  // ASO $aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x03:  // ASO ($aa,X)
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO ($aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x13:  // ASO ($aa),Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = ASL(read(address));
//                    write(address, writeVal);
//                    A |= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("ASO ($aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x4A:  // LSR A
//
//
//
//                A = LSR(A);
//                break;
//
//
//            case 0x46:  // LSR $aa
//
//
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = LSR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x56:  // LSR $aa,X
//
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = LSR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x4E:  // LSR $aaaa
//
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = LSR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x5E:  // LSR $aaaa,X
//
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = LSR(writeVal);
//                write(address, writeVal);
//                break;
//
//            case 0x47:  // LSE $aa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x57:  // LSE $aa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageX();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//            case 0x4F:  // LSE $aaaa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x5F:  // LSE $aaaa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE $aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x5B:  // LSE $aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x43:  // LSE ($aa,X)
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE ($aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x53:  // LSE ($aa),Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = LSR(read(address));
//                    write(address, writeVal);
//                    A ^= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LSE ($aa,Y)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x2A:  // ROL A
//
//
//                A = ROL(A);
//                break;
//
//
//            case 0x26:  // ROL $aa (RWMW)
//
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x36:  // ROL $aa,X (RWMW)
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x2E:  // ROL $aaaa (RWMW)
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x3E:  // ROL $aaaa,X (RWMW)
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROL(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x27:  // RLA $aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x37:  // RLA $aa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageX();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x2F:  // RLA $aaaa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x3F:  // RLA $aaaa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA $aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//            case 0x3B:  // RLA $aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//
//            case 0x23:  // RLA ($aa,X)
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA ($aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//
//            case 0x33:  // RLA ($aa),Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = ROL(read(address));
//                    write(address, writeVal);
//                    A &= writeVal;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("RLA ($aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x6A:  // ROR A
//
//
//                A = ROR(A);
//                break;
//
//
//            case 0x66:  // ROR $aa
//
//
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x67:  // RRA $aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x76:  // ROR $aa,X
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x77:  // RRA $aa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageX();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x6E:  // ROR $aaaa
//
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROR(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0x6F:  // RRA $aaaa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x7E:  // ROR $aaaa,X
//
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = ROR(writeVal);
//                write(address, writeVal);
//                break;
//
//            case 0x7F:  // RRA $aaaa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA $aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x7B:  // RRA $aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x63:  // RRA ($aa,X)
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA ($aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x73:  // RRA ($aa),Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = ROR(read(address));
//                    write(address, writeVal);
//                    operateAdd(address);
//                } else {
//                    usedUndocumentedCode("RRA ($aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x90:  // BCC
//
//
//
//                branch(0x01, false);
//                break;
//
//
//            case 0xB0:  // BCS
//
//
//
//                branch(0x01, true);
//                break;
//
//
//            case 0xD0:  // BNE
//
//
//
//                branch(0x02, false);
//                break;
//
//
//            case 0xF0:  // BEQ
//
//
//
//                branch(0x02, true);
//                break;
//
//
//            case 0x10:  // BPL
//
//
//                branch(0x80, false);
//                break;
//
//
//            case 0x30:  // BMI
//
//
//
//                branch(0x80, true);
//                break;
//
//
//            case 0x50:  // BVC
//
//
//
//                branch(0x40, false);
//                break;
//
//
//            case 0x70:  // BVS
//
//
//
//                branch(0x40, true);
//                break;
//
//
//            case 0x4C:  // JMP $aaaa
//
//
//
//                PC = byAbsolute();
//                break;
//
//
//            case 0x6C:  // JMP ($aaaa)
//
//
//                address = byAbsolute();
//
//
//
//                if ((address & 0x00FF) == 0xFF) {
//                    PC = (read(address & 0xFF00) << 8) | read(address);
//                } else {
//                    PC = readWord(address);
//                }
//
//                break;
//
//
//            case 0x20:  // JSR $aaaa
//
//
//
//                address = PC + 1;
//                pushWord(address);
//                PC = byAbsolute();
//                break;
//
//
//            case 0x60:  // RTS
//
//
//
//                PC = popWord() + 1;
//                break;
//
//
//            case 0x40:  // RTI
//
//
//
//                P = pop();
//                PC = popWord();
//                break;
//
//
//            case 0x48:  // PHA
//
//
//
//                push(A);
//                break;
//
//
//            case 0x08:  // PHP
//
//
//
//                push(P | 0x10); // SET BRK
//                break;
//
//
//            case 0x68:  // PLA
//
//
//
//                A = pop();
//                setStatusFlags(A);
//                break;
//
//
//            case 0x28:  // PLP
//
//
//
//                P = pop();
//                break;
//
//
//            case 0x18:  // CLC
//
//
//
//                P &= 0xfe;
//                break;
//
//
//            case 0xD8:  // CLD
//
//
//
//                P &= 0xf7;
//                break;
//
//
//            case 0x58:  // CLI
//
//
//
//                P &= 0xfb;
//                break;
//
//
//            case 0xB8:  // CLV
//
//
//
//                P &= 0xbf;
//                break;
//
//
//            case 0x38:  // SEC
//
//
//
//                P |= 0x1;
//                break;
//
//
//            case 0xF8:  // SED
//
//
//
//                P |= 0x8;
//                break;
//
//
//            case 0x78:  // SEI
//
//
//
//                P |= 0x4;
//                break;
//
//
//            case 0xE6:  // INC $aa (RWMW)
//
//
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = increment(writeVal);
//                write(address, writeVal);
//                break;
//
//
//            case 0xF6:  // INC $aa,X (RWMW)
//
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = increment(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xEE:  // INC $aaaa (RWMW)
//
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = increment(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xFE:  // INC $aaaa,X (RWMW)
//
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = increment(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xE8:  // INX
//
//
//
//                X++;
//                X &= 0xff;
//                setStatusFlags(X);
//                break;
//
//
//            case 0xC8:  // INY
//
//
//                Y++;
//                Y &= 0xff;
//                setStatusFlags(Y);
//                break;
//
//
//            case 0xC6:  // DEC $aa (RWMW)
//
//                address = byZeroPage();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = decrement(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xD6:  // DEC $aa,X (RWMW)
//
//
//
//                address = byZeroPageX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = decrement(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xCE:  // DEC $aaaa (RWMW)
//
//
//                address = byAbsolute();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = decrement(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xDE:  // DEC $aaaa,X (RWMW)
//
//
//
//                address = byAbsoluteX();
//                writeVal = read(address);
//                write(address, writeVal);
//                writeVal = decrement(read(address));
//                write(address, writeVal);
//                break;
//
//
//            case 0xCA:  // DEX
//
//
//
//                X--;
//                X &= 0xff;
//                setStatusFlags(X);
//                break;
//
//
//            case 0x88:  // DEY
//
//
//
//                Y--;
//                Y &= 0xff;
//                setStatusFlags(Y);
//                break;
//
//
//            case 0x69:  // ADC #aa
//
//
//
//                operateAdd(byImmediate());
//                break;
//
//
//            case 0x65:  // ADC $aa
//
//
//
//                operateAdd(read(byZeroPage()));
//                break;
//
//
//            case 0x75:  // ADC $aa,X
//
//
//
//                operateAdd(read(byZeroPageX()));
//                break;
//
//
//            case 0x6D:  // ADC $aaaa
//
//
//
//                operateAdd(read(byAbsolute()));
//                break;
//
//
//            case 0x7D:  // ADC $aaaa,X
//
//
//
//                operateAdd(read(byAbsoluteX()));
//                break;
//
//
//            case 0x79:  // ADC $aaaa,Y
//
//
//
//                operateAdd(read(byAbsoluteY()));
//                break;
//
//
//            case 0x61:  // ADC ($aa,X)
//
//
//
//                operateAdd(read(byIndirectX()));
//                break;
//
//
//            case 0x71:  // ADC ($aa),Y
//
//
//
//                operateAdd(read(byIndirectY()));
//                break;
//
//
//            case 0xEB:  // SBC #aa
//            case 0xE9:  // SBC #aa
//
//
//
//                operateSub(byImmediate());
//                break;
//
//
//            case 0xE5:  // SBC $aa
//
//
//                operateSub(read(byZeroPage()));
//                break;
//
//
//            case 0xF5:  // SBC $aa,X
//
//
//                operateSub(read(byZeroPageX()));
//                break;
//
//
//            case 0xED:  // SBC $aaaa
//
//
//                operateSub(read(byAbsolute()));
//                break;
//
//
//            case 0xFD:  // SBC $aaaa,X
//
//
//                operateSub(read(byAbsoluteX()));
//                break;
//
//
//            case 0xF9:  // SBC $aaaa,Y
//
//
//
//                operateSub(read(byAbsoluteY()));
//                break;
//
//
//            case 0xE1:  // SBC ($aa,X)
//
//
//
//                operateSub(read(byIndirectX()));
//                break;
//
//
//            case 0xF1:  // SBC ($aa),Y
//
//
//                operateSub(read(byIndirectY()));
//                break;
//
//
//            case 0xEF:  // UNDOCUMENTED : INS aaaa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byAbsolute()));
//                } else {
//                    usedUndocumentedCode("INS aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xFF:  // UNDOCUMENTED : INS aaaa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byAbsoluteX()));
//                } else {
//                    usedUndocumentedCode("INS aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xFB:  // UNDOCUMENTED : INS aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byAbsoluteY()));
//                } else {
//                    usedUndocumentedCode("INS aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xE7:  // UNDOCUMENTED : INS aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byZeroPage()));
//                } else {
//                    usedUndocumentedCode("INS aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xF7:  // UNDOCUMENTED : INS aa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byZeroPageX()));
//                } else {
//                    usedUndocumentedCode("INS aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0xE3:  // UNDOCUMENTED : INS (aa,X)
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byIndirectX()));
//                } else {
//                    usedUndocumentedCode("INS (aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xF3:  // UNDOCUMENTED : INS (aa),Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    operateCmp(A, increment(byIndirectY()));
//                } else {
//                    usedUndocumentedCode("INS (aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xC9:  // CMP #aa
//
//
//
//                operateCmp(A, byImmediate());
//                break;
//
//
//            case 0xC5:  // CMP $aa
//
//
//                operateCmp(A, read(byZeroPage()));
//                break;
//
//
//            case 0xD5:  // CMP $aa,X
//
//
//
//                operateCmp(A, read(byZeroPageX()));
//                break;
//
//
//            case 0xCD:  // CMP $aaaa
//
//
//
//                operateCmp(A, read(byAbsolute()));
//                break;
//
//// UP TO HERE
//
//            case 0xDD:  // CMP $aaaa,X
//
//
//
//                operateCmp(A, read(byAbsoluteX()));
//                break;
//
//
//            case 0xD9:  // CMP $aaaa,Y
//
//
//                operateCmp(A, read(byAbsoluteY()));
//                break;
//
//
//            case 0xC1:  // CMP ($aa,X)
//
//
//                operateCmp(A, read(byIndirectX()));
//                break;
//
//
//            case 0xD1:  // CMP ($aa),Y
//
//
//
//                operateCmp(A, read(byIndirectY()));
//                break;
//
//
//            case 0xE0:  // CPX #aa
//
//
//
//                operateCmp(X, byImmediate());
//                break;
//
//
//            case 0xE4:  // CPX $aa
//
//
//
//                operateCmp(X, read(byZeroPage()));
//                break;
//
//
//            case 0xEC:  // CPX $aaaa
//
//
//
//                operateCmp(X, read(byAbsolute()));
//                break;
//
//
//            case 0xC0:  // CPY #aa
//
//
//                operateCmp(Y, byImmediate());
//                break;
//
//
//            case 0xC4:  // CPY $aa
//
//
//
//                operateCmp(Y, read(byZeroPage()));
//                break;
//
//
//            case 0xCC:  // CPY $aaaa
//
//
//
//                operateCmp(Y, read(byAbsolute()));
//                break;
//
//
//            case 0x8F:  // UNDOCUMENTED : AXS $aaaa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = A & X;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXS $aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x87:  // UNDOCUMENTED : AXS $aa
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = A & X;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXS $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x97:  // UNDOCUMENTED : AXS $aa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageY();
//                    writeVal = A & X;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXS $aa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x83:  // UNDOCUMENTED : AXS ($aa,X)
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = A & X;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXS $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xCB:  // UNDOCUMENTED : SAX #aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    X = (A & X) - byImmediate();
//                    P |= X < 0 ? 0 : 1;
//                    X &= 0xFF;
//                    setStatusFlags(X);
//                } else {
//                    usedUndocumentedCode("SAX #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xAB:  // UNDOCUMENTED : OAL #aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A |= 0xEE;
//                    A &= byImmediate();
//                    X = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("OAL #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x4B:  // UNDOCUMENTED : ALR #aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    writeVal = A & byImmediate();
//                    A = LSR(A);
//                } else {
//                    usedUndocumentedCode("ALR #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x6B:  // UNDOCUMENTED : ARR #aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    writeVal = A & byImmediate();
//                    A = ROR(A);
//                } else {
//                    usedUndocumentedCode("ARR #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x8B:  // UNDOCUMENTED : XAA #aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    A = X & byImmediate();
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("XAA #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xCF:  // UNDOCUMENTED : DCM aaaa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsolute();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM aaaa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xDF:  // UNDOCUMENTED : DCM aaaa,X
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xDB:  // UNDOCUMENTED : DCM $aaaa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xC7:  // UNDOCUMENTED : DCM $aa
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPage();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM $aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xD7:  // UNDOCUMENTED : DCM $aa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byZeroPageX();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM $aa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xC3:  // UNDOCUMENTED : DCM (aa,X)
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectX();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM (aa,X)");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xD3:  // UNDOCUMENTED : DCM (aa),Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = (read(address) - 1) & 0xFF;
//                    operateCmp(A, writeVal);
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("DCM (aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0xBB:  // UNDOCUMENTED : LAS aaaa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    A = read(address) & S;
//                    X = A;
//                    S = A;
//                    setStatusFlags(A);
//                } else {
//                    usedUndocumentedCode("LAS aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x0B:  // UNDOCUMENTED : ANC #aa
//            case 0x2B:
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byImmediate();
//                    writeVal = read(address) & A;
//                    setStatusFlags(writeVal);
//                    P |= (P & 0x80) >> 7;
//                } else {
//                    usedUndocumentedCode("ANC #aa");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x9B:  // UNDOCUMENTED : TAS $aaaa,Y
//
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    S = X & A;
//                    address = byAbsoluteY();
//                    writeVal = ((address & 0xFF00) >> 8) + 1;
//                    writeVal &= S;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("TAS $aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x9C:  // UNDOCUMENTED : SAY $aaaa,X
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteX();
//                    writeVal = ((address & 0xFF00) >> 8) + 1;
//                    writeVal &= Y;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("SAY $aaaa,X");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x9E:  // UNDOCUMENTED : XAS aaaa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = ((address & 0xFF00) >> 8) + 1;
//                    writeVal &= X;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("XAS aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x9F:  // UNDOCUMENTED : AXA aaaa,Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byAbsoluteY();
//                    writeVal = ((address & 0xFF00) >> 8) + 1;
//                    writeVal &= X;
//                    writeVal &= A;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXA aaaa,Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x93:  // UNDOCUMENTED : AXA (aa),Y
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    address = byIndirectY();
//                    writeVal = ((address & 0xFF00) >> 8) + 1;
//                    writeVal &= X;
//                    writeVal &= A;
//                    write(address, writeVal);
//                } else {
//                    usedUndocumentedCode("AXA (aa),Y");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//
//            case 0x80:  // UNDOCUMENTED : SKB
//            case 0x82:
//            case 0x89:
//            case 0xC2:
//            case 0xE2:
//            case 0x04:
//            case 0x14:
//            case 0x34:
//            case 0x44:
//            case 0x54:
//            case 0x64:
//            case 0x74:
//            case 0xD4:
//            case 0xF4:
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    PC++;
//                } else {
//                    usedUndocumentedCode("SKB");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x0C:  // UNDOCUMENTED : SKW
//            case 0x1C:
//            case 0x3C:
//            case 0x5C:
//            case 0x7C:
//            case 0xDC:
//            case 0xFC:
//
//
//                if (false /*nes.usersettings.useUndocumentedOpCodes*/) {
//                    PC += 2;
//                } else {
//                    usedUndocumentedCode("SKW");
//                    halted = true;
//                    PC--;
//                }
//                break;
//
//            case 0x1A:  // UNDOCUMENTED : NOP
//            case 0x3A:
//            case 0x5A:
//            case 0x7A:
//            case 0xDA:
//            case 0xEA:
//            case 0xFA:
//
//
//                break;
//
//
//            case 0x02:  // UNDOCUMENTED : HLT
//            case 0x12:
//            case 0x22:
//            case 0x32:
//            case 0x42:
//            case 0x52:
//            case 0x62:
//            case 0x72:
//            case 0x92:
//            case 0xB2:
//            case 0xD2:
//            case 0xF2:
//
//
//                halted = true;
//                PC--;
//                break;
//
//            default:  // Unknown OpCode so Hang
//
//                PC--;
//                break;
//
//
//        }
//
//
//        // Decrement Cycles by number of Cycles in Instruction
//
//        cyclesPending -= cycles[instCode];
//
//    }

    /**
     *
     * <P>Reset the Processor</P>
     *
     */
    private final void reset() {



        // Save the SaveRAM

//            nes.memory.saveSaveRAM();


        // Reset the Memory Manager

        nes.memory.init(currentCart);//, nes.currentCartFileName);


        // Reset the Mapper

        nes.mapper.init(nes.memory);



        // Reset the Sound Engine
//TODO: re-enable sound
//        if (nes.sound != null) {
//            nes.sound.reset();
//        }



        // Reset the Internal CPU

        intReset();


        // Inform User that Reset was Successful



    }

    /**
     *
     * <P>Correct the CPU Cycles for a Couple of Odd Games.</P>
     *
     */
    public void correctCPUCycles() {

        // Check Mapper Type

        if (nes.mapper == null) {

            CYCLES_PER_LINE = 116.0f;
            return;

        }



        // Get CRC for Cartridge

        long crc = currentCart.crc32;



        // Print CRC

        System.out.println(crc);



        // Identify the Game by Mapper then CRC

        switch (nes.mapper.getMapperNumber()) {

            case 0x04: {
                if (crc == 0xA0B0B742l) {
                    // Mario 3 (U)

                    CYCLES_PER_LINE = 144.0f;
                    return;

                }
            }
            case 0x07: {
                if (crc == 0x279710DCl) {
                    // BattleToads (U)

                    CYCLES_PER_LINE = 112.0f;
                    return;
                }
            }

            default: {

                CYCLES_PER_LINE = 116.0f;
                return;

            }


        }


    }



    /**
     *
     * <P>Reset the internal CPU registers.</P>
     *
     */
    private final void intReset() {


        // Correct CPU Cycles for Odd Games

        correctCPUCycles();


        // Reset the CPU Registers

        A = 0x00;
        X = 0x00;
        Y = 0x00;
        P = 0x04;
        S = 0xFF;


        halted = false;


        // Read the Reset Vector for PC Address

        PC = readWord(0xFFFC);



    }

    /**
     *
     * <P>Request that the current CPU stops Processing.</P>
     *
     */
    public final void stopProcessing() {


        // Place the Stop Request

        stopRequest = true;


        // Wait for the Stop Request to be Received

        try {

//            sleep(200);

        } catch (Exception e) {
        }


    }

    /**
     *
     * <P>Wait while CPU is not Active.</P>
     *
     */
    private final void waitWhileNotActive() {


        // Wait while CPU is not Active

        while (!cpuActive) {


            // Draw the Screen

            deleteScreen();


            // Check for the Stop Request whilst non Active

            if (stopRequest) {
                return;
            }


            // Check for Load and Save State Requests


            // Sleep for 1/10th of a Second

            try {

//                sleep(100);

            } catch (Exception controlPausedException) {
            }

        }


    }

    /**
     *
     * <P>Wait while CPU is paused.</P>
     *
     */
    private final void waitWhilePaused() {

if(true)return;
        // Wait while CPU is Paused

        while (cpuPaused) {

            // Draw the Screen

            drawScreen(true);


            // Check for Stop Request

            if (stopRequest) {
                return;
            }


            // Check for Load and Save State Requests

//                  if (gui.saveStateRequest) nes.stateSave();
//                  if (gui.loadStateRequest) nes.stateLoad();


            // Sleep for 1/10th of a Second

            try {

//                sleep(100);

            } catch (Exception controlPausedException) {
            }

        }


    }

////////////////////////////////////////////////////////////////////////////////
//
// Memory IO Functions
//
////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * <P>Read Byte from memory.</P>
     *
     * @param  address Address in memory to read from.
     * @return value at the specified address.
     *
     */
    private final int read(int addr) {


        return nes.memory.read(addr);

    }

    /**
     *
     * <P>Read Word from memory.</P>
     *
     * @param  address Address in memory to read from.
     * @return value at the specified address.
     *
     */
    private final int readWord(int address) {

        return nes.memory.readWord(address);

    }

    /**
     *
     * <P>Write Byte to memory.</P>
     *
     * @param  address Address in memory to write to.
     * @param  value   value to write.
     *
     */
    private final void write(int address, int value) {

        nes.memory.write(address, value);

    }

    /**
     *
     * <P>Write Word to memory.</P>
     *
     * @param  address Address in memory to write to.
     * @param  value   Value to write.
     *
     */
    private final void writeWord(int address, int value) {

        nes.memory.writeWord(address, value);

    }

////////////////////////////////////////////////////////////////////////////////
//
// Addressing Mode Functions
//
////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * <P>Get value by Immediate Mode Addressing - #$00</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byImmediate() {

        int i = read(PC++);



        return i;

    }

    /**
     *
     * <P>Get value by Absolute Mode Addressing - $aaaa</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byAbsolute() {

        int address = readWord(PC);



        PC += 2;
        return address;

    }

    /**
     *
     * <P>Get value by Absolute Y Mode Addressing - $aaaa,Y</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byAbsoluteY() {

        int i = byAbsolute();

        int j = i + Y;
        checkPageBoundaryCrossing(i, j);
        return j;

    }

    /**
     *
     * <P>Get value by Absolute X Mode Addressing - $aaaa,X</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byAbsoluteX() {

        int i = byAbsolute();


        int j = i + X;
        checkPageBoundaryCrossing(i, j);
        return j;

    }

    /**
     *
     * <P>Get value by Zero Page Mode Addressing - $aa</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byZeroPage() {

        int address = read(PC++);


        return address;

    }

    /**
     *
     * <P>Get value by Zero Page X Mode Addressing - $aa,X</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byZeroPageX() {

        int address = read(PC++);



        return (address + X) & 0xff;

    }

    /**
     *
     * <P>Get value by Zero Page Y Mode Addressing - $aa,Y</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byZeroPageY() {

        int address = read(PC++);



        return address + Y & 0xff;

    }

    /**
     *
     * <P>Get value by Indirect X Mode Addressing - ($aa,X)</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byIndirectX() {

        int address = read(PC++);



        address += X;
        address &= 0xFF;
        return readWord(address);

    }

    /**
     *
     * <P>Get value by Indirect Y Mode Addressing - ($aa),Y</P>
     *
     * @return The value by the specified addressing mode in relation to the current PC.
     *
     */
    private final int byIndirectY() {

        int address = read(PC++);


        address = readWord(address);
        checkPageBoundaryCrossing(address, address + Y);
        return address + Y;

    }

////////////////////////////////////////////////////////////////////////////////
//
// Utility Functions
//
////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * <P>Decrement the number of cycles pending if over a page boundary.</P>
     *
     * @param address1 The first address.
     * @param address2 The second address.
     *
     */
    private final void checkPageBoundaryCrossing(int address1, int address2) {

        if (((address2 ^ address1) & 0x100) != 0) {
            cyclesPending--;
        }

    }

    /**
     *
     * <P>Set the Zero and Negative Status Flags.</P>
     *
     * @param value The value used to determine the Status Flags.
     *
     */
    private final void setStatusFlags(int value) {

        P &= 0x7D;
        P |= znTable[value];

    }

    /**
     *
     * <P>Perform Arithmetic Shift Left.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int ASL(int i) {

        P &= 0x7C;
        P |= i >> 7;
        i <<= 1;
        i &= 0xFF;
        P |= znTable[i];
        return i;

    }

    /**
     *
     * <P>Perform Logical Shift Right.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int LSR(int i) {

        P &= 0x7C;
        P |= i & 0x1;
        i >>= 1;
        P |= znTable[i];
        return i;

    }

    /**
     *
     * <P>Perform Rotate Left.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int ROL(int i) {

        i <<= 1;
        i |= P & 0x1;
        P &= 0x7C;
        P |= i >> 8;
        i &= 0xFF;
        P |= znTable[i];
        return i;

    }

    /**
     *
     * <P>Perform Rotate Right.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int ROR(int i) {

        int j = P & 0x1;
        P &= 0x7C;
        P |= i & 0x1;
        i >>= 1;
        i |= j << 7;
        P |= znTable[i];
        return i;
    }

    /**
     *
     * <P>Perform Incrementation.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int increment(int i) {

        i = ++i & 0xff;
        setStatusFlags(i);
        return i;

    }

    /**
     *
     * <P>Perform Decrementation.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final int decrement(int i) {

        i = --i & 0xff;
        setStatusFlags(i);
        return i;

    }

    /**
     *
     * <P>Perform Add with Carry (no decimal mode on NES).</P>
     *
     * @param i The value used by the function.
     *
     */
    private final void operateAdd(int i) {

        // Store Carry

        int k = P & 0x1;


        // Store Add Result

        int j = A + i + k;


        // Turn Off CZN

        P &= 0x3C;


        // Set Overflow (V)

        P |= (~(A ^ i) & (A ^ i) & 0x80) == 0 ? 0 : 0x40;


        // Set Carry (C)

        P |= j <= 255 ? 0 : 0x1;


        // Set A

        A = j & 0xFF;


        // Set ZN

        P |= znTable[A];

    }

    /**
     *
     * <P>Perform Subtract with Carry (no decimal mode on NES).</P>
     *
     * @param i The value used by the function.
     *
     */
    private final void operateSub(int i) {


        // Store Carry

        int k = ~P & 0x1;


        // Store Subtract Result

        int j = A - i - k;


        // Turn Off CZN

        P &= 0x3C;


        // Set Overflow (V)

        P |= (~(A ^ i) & (A ^ i) & 0x80) == 0 ? 0 : 0x40;


        // Set Carry

        P |= j < 0 ? 0 : 0x1;


        // Set A

        A = j & 0xFF;


        // Set ZN in P

        P |= znTable[A];

    }

    /**
     *
     * <P>Perform Compare Function.</P>
     *
     * @param i The first value.
     * @param j The second value.
     *
     */
    private final void operateCmp(int i, int j) {

        int k = i - j;
        P &= 0x7C;
        P |= k < 0 ? 0 : 0x1;
        P |= znTable[k & 0xff];

    }

    /**
     *
     * <P>Perform Bit Function.</P>
     *
     * @param i The value used by the function.
     *
     */
    private final void operateBit(int i) {

        P &= 0x3D;
        P |= i & 0xc0;
        P |= (A & i) != 0 ? 0 : 0x2;

    }

    /**
     *
     * <P>Function for Handling Branches</P>
     *
     * @param flagNum The byte value to compare.
     * @param flagVal The expected truth value for a branch.
     *
     */
    private final void branch(int flagNum, boolean flagVal) {

        int offset = (byte) read(PC++);


        if (((P & flagNum) != 0) == flagVal) {

            checkPageBoundaryCrossing(PC + offset, PC);
            PC = PC + offset;
            cyclesPending--;

        }

    }

    /**
     *
     * <P>Push a value onto the Stack.</P>
     *
     * @param stackVal The value to push.
     *
     */
    private final void push(int stackVal) {

        write(S + 256, stackVal);
        S--;
        S &= 0xff;

    }

    /**
     *
     * <P>Pop a value from the Stack.</P>
     *
     * @return The value on top of the Stack.
     *
     */
    private final int pop() {

        S++;
        S &= 0xff;
        return read(S + 256);

    }

    /**
     *
     * <P>Push a Word onto the Stack.</P>
     *
     * @param stackVal The 16 bit word to push.
     *
     */
    private final void pushWord(int stackVal) {

        push((stackVal >> 8) & 0xFF);
        push(stackVal & 0xFF);

    }

    /**
     *
     * <P>Pop a Word from the Stack.</P>
     *
     * @return The 16 bit word on top of the Stack.
     *
     */
    private final int popWord() {

        return pop() + pop() * 256;

    }

    public int getPC() {
        return PC;
    }

////////////////////////////////////////////////////////////////////////////////
//
// Save State Functions
//
////////////////////////////////////////////////////////////////////////////////

    /**
     *
     * Array of CPU Cycles for each Machine Code Instruction
     *
     */
    private final int cycles[] = {
        7, // 0x00  BRK
        6, // 0x01  ORA (aa,X)
        2, // 0x02  HLT
        8, // 0x03  ASO (ab,X)
        3, // 0x04  SKB
        3, // 0x05  ORA aa
        5, // 0x06  ASL aa
        5, // 0x07  ASO aa
        3, // 0x08  PHP
        2, // 0x09  ORA #aa
        2, // 0x0A  ASL A
        2, // 0x0B  ANC #aa
        4, // 0x0C  SKW
        4, // 0x0D  ORA aaaa
        6, // 0x0E  ASL aaaa
        6, // 0x0F  ASO aaaa
        2, // 0x10  BPL
        5, // 0x11  ORA (aa),Y
        2, // 0x12  HLT
        8, // 0x13  ASO (ab),Y
        4, // 0x14  SKB
        4, // 0x15  ORA aa,X
        6, // 0x16  ASL aa,X
        6, // 0x17  ASO aa,X
        2, // 0x18  CLC
        4, // 0x19  ORA aaaa,Y
        2, // 0x1A  NOP
        7, // 0x1B  ASO aaaa,Y
        5, // 0x1C  SKW
        5, // 0x1D  ORA aaaa,X
        7, // 0x1E  ASL aaaa,X
        7, // 0x1F  ASO aaaa,X
        6, // 0x20  JSR aaaa
        6, // 0x21  AND (aa,X)
        2, // 0x22  HLT
        8, // 0x23  RLA (aa,X)
        3, // 0x24  BIT aa
        3, // 0x25  AND aa
        5, // 0x26  ROL aa
        5, // 0x27  RLA aa
        4, // 0x28  PLP
        2, // 0x29  AND #aa
        2, // 0x2A  ROL A
        2, // 0x2B  ANC #aa
        4, // 0x2C  BIT aaaa
        4, // 0x2D  AND aaaa
        6, // 0x2E  ROL aaaa
        6, // 0x2F  RLA aaaa
        2, // 0x30  BMI aa
        5, // 0x31  AND (aa),Y
        2, // 0x32  HLT
        8, // 0x33  RLA (ab),Y
        4, // 0x34  SKB
        4, // 0x35  AND aa,X
        6, // 0x36  ROL aa,X
        6, // 0x37  RLA aa,X
        2, // 0x38  SEC
        4, // 0x39  AND aaaa,Y
        2, // 0x3A  NOP
        7, // 0x3B  RLA aaaa,Y
        5, // 0x3C  SKW
        5, // 0x3D  AND aaaa,X
        7, // 0x3E  ROL aaaa,X
        7, // 0x3F  RLA aaaa,X
        6, // 0x40  EOR aaaa
        6, // 0x41  EOR (aa,X)
        2, // 0x42  HLT
        8, // 0x43  LSE (aa,X)
        3, // 0x44  SKB
        3, // 0x45  EOR aa
        5, // 0x46  LSR aa
        5, // 0x47  LSE aa
        3, // 0x48  PHA
        2, // 0x49  EOR #aa
        2, // 0x4A  LST A
        2, // 0x4B  ALR #aa
        3, // 0x4C  JMP aaaa
        4, // 0x4D  RTI
        6, // 0x4E  LSE aaaa
        6, // 0x4F  LSE aaaa
        2, // 0x50  BVC aa
        5, // 0x51  EOR (aa),Y
        2, // 0x52  HLT
        8, // 0x53  LSE (aa),Y
        4, // 0x54  SKB
        4, // 0x55  EOR aa,X
        6, // 0x56  LSR aa,X
        6, // 0x57  LSE aa,X
        2, // 0x58  CLI
        4, // 0x59  EOR aaaa,Y
        2, // 0x5A  NOP
        7, // 0x5B  LSE aaaa,Y
        5, // 0x5C  SKW
        5, // 0x5D  EOR aaaa,X
        7, // 0x5E  LSR aaaa,X
        7, // 0x5F  LSE aaaa,X
        4, // 0x60  RTS
        6, // 0x61  ADC (aa,X)
        2, // 0x62  HLT
        8, // 0x63  RRA (aa,X)
        3, // 0x64  SKB
        3, // 0x65  ADC aa
        5, // 0x66  ROR aa
        5, // 0x67  RRA aa
        4, // 0x68  PLA
        2, // 0x69  ADC #aa
        2, // 0x6A  ROR A
        2, // 0x6B  ARR #aa
        5, // 0x6C  JMP (aa)
        4, // 0x6D  ADC aaaa
        6, // 0x6E  ROR aaaa
        6, // 0x6F  RRA aaaa
        4, // 0x70  BVS aa
        5, // 0x71  ADC (aa),Y
        2, // 0x72  HLT
        8, // 0x73  RRA (aa),Y
        4, // 0x74  SKB
        4, // 0x75  ADC aa,X
        6, // 0x76  ROR aa,X
        6, // 0x77  RRA aa,X
        2, // 0x78  SEI
        4, // 0x79  ADC aaaa,Y
        2, // 0x7A  NOP
        7, // 0x7B  RRA aaaa,Y
        5, // 0x7C  SKW
        5, // 0x7D  ADC aaaa,X
        7, // 0x7E  ROR aaaa,X
        7, // 0x7F  RRA aaaa,X
        2, // 0x80  SKB
        6, // 0x81  STA (aa,X)
        2, // 0x82  SKB
        6, // 0x83  AXS (aa,X)
        3, // 0x84  STY aa
        3, // 0x85  STA aa
        3, // 0x86  STX aa
        3, // 0x87  AXS aa
        2, // 0x88  DEY
        2, // 0x89  SKB
        2, // 0x8A  TXA
        2, // 0x8B  XAA #aa
        4, // 0x8C  STY aaaa
        4, // 0x8D  STA aaaa
        4, // 0x8E  STX aaaa
        4, // 0x8F  AXS aaaa
        2, // 0x90  BCC aa
        6, // 0x91  STA (aa),Y
        2, // 0x92  HLT
        6, // 0x93  AXA (aa),Y
        4, // 0x94  STY aa,X
        4, // 0x95  STA aa,X
        4, // 0x96  STX aa,Y
        4, // 0x97  AXS aa,Y
        2, // 0x98  TYA
        5, // 0x99  STA aaaa,Y
        2, // 0x9A  TXS
        5, // 0x9B  TAS aaaa,Y
        5, // 0x9C  SAY aaaa,X
        5, // 0x9D  STA aaaa,X
        5, // 0x9E  XAS aaaa,Y
        5, // 0x9F  AXA aaaa,Y
        2, // 0xA0  LDY #aa
        6, // 0xA1  LDA (aa,X)
        2, // 0xA2  LDX #aa
        6, // 0xA3  LAX (aa,X)
        3, // 0xA4  LDY aa
        3, // 0xA5  LDA aa
        3, // 0xA6  LDX aa
        3, // 0xA7  LAX aa
        2, // 0xA8  TAY
        2, // 0xA9  LDA #aa
        2, // 0xAA  TAX
        2, // 0xAB  OAL #aa
        4, // 0xAC  LDY aaaa
        4, // 0xAD  LDA aaaa
        4, // 0xAE  LDX aaaa
        4, // 0xAF  LAX aaaa
        2, // 0xB0  BCS aa
        5, // 0xB1  LDA (aa),Y
        2, // 0xB2  HLT
        5, // 0xB3  LAX (aa),Y
        4, // 0xB4  LDY aa,X
        4, // 0xB5  LDA aa,X
        4, // 0xB6  LDX aa,Y
        4, // 0xB7  LAX aa,Y
        2, // 0xB8  CLV
        4, // 0xB9  LDA aaaa,Y
        2, // 0xBA  TSX
        4, // 0xBB  LAS aaaa,Y
        4, // 0xBC  LDY aaaa,X
        4, // 0xBD  LDA aaaa,X
        4, // 0xBE  LDX aaaa,Y
        4, // 0xBF  LAX aaaa,Y
        2, // 0xC0  CPY #aa
        6, // 0xC1  CMP (aa,X)
        2, // 0xC2  SKB
        8, // 0xC3  DCM (aa,X)
        3, // 0xC4  CPY aa
        3, // 0xC5  CMP aa
        5, // 0xC6  DEC aa
        5, // 0xC7  DCM aa
        2, // 0xC8  INY
        2, // 0xC9  CMP #aa
        2, // 0xCA  DEX
        2, // 0xCB  SAX #aa
        4, // 0xCC  CPY aaaa
        4, // 0xCD  CMP aaaa
        6, // 0xCE  DEC aaaa
        6, // 0xCF  DCM aaaa
        2, // 0xD0  BNE aa
        5, // 0xD1  CMP (aa),Y
        2, // 0xD2  HLT
        8, // 0xD3  DCM (aa),Y
        4, // 0xD4  SKB
        4, // 0xD5  CMP aa,X
        6, // 0xD6  DEC aa,X
        6, // 0xD7  DCM aa,X
        2, // 0xD8  CLD
        4, // 0xD9  CMP aaaa,Y
        2, // 0xDA  NOP
        7, // 0xDB  DCM aaaa,Y
        5, // 0xDC  SKW
        5, // 0xDD  CMP aaaa,X
        7, // 0xDE  DEC aaaa,X
        7, // 0xDF  DCM aaaa,X
        2, // 0xE0  CPX #aa
        6, // 0xD1  SBC (aa,X)
        2, // 0xE2  SKB
        8, // 0xE3  INS (aa,X)
        3, // 0xE4  CPX aa
        3, // 0xE5  SBC aa
        5, // 0xE6  INC aa
        5, // 0xE7  INS aa
        2, // 0xE8  INX
        2, // 0xE9  SBC #aa
        2, // 0xEA  NOP
        2, // 0xEB  SBC #aa
        4, // 0xEC  CPX aaaa
        4, // 0xED  SBC aaaa
        6, // 0xEE  INC aaaa
        6, // 0xEF  INS aaaa
        2, // 0xF0  BEQ aa
        5, // 0xF1  SBC (aa),Y
        2, // 0xF2  HLT
        8, // 0xF3  INS (aa),Y
        4, // 0xF4  SKB
        4, // 0xF5  SBC aa,X
        6, // 0xF6  INC aa,X
        6, // 0xF7  INS aa,X
        2, // 0xF8  SED
        4, // 0xF9  SBC aaaa,Y
        2, // 0xFA  NOP
        7, // 0xFB  INS aaaa,Y
        5, // 0xFC  SKW
        5, // 0xFD  SBC aaaa,X
        7, // 0xFE  INC aaaa,X
        7 // 0xFF  INS aaaa,X
    };
    /**
     *
     * Array of Zero and Negative Flags for Speedy Lookup
     *
     */
    private final int znTable[] = {
        002, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128
    };
}
