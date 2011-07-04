package com.gochromium.nes.client.emulator;

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;

/** 
 *
 * Class for the JoyPad Controller used by NESCafe.
 *
 * @author   David de Niese
 * @version  0.56f
 * @final    TRUE
 *
 */
public final class JoyPad {

    /**
     *
     * Status of the Joypad
     *
     */
    private int joypad = 0;
    /**
     *
     * Declare variable for current button being read
     *
     */
    private int joypadBit = 0;
    /**
     *
     * Button A on the JoyPad 1.
     *
     */
    protected int BUTTON1_A = 88;//'X';
    /**
     *
     * Button B on the JoyPad 1.
     *
     */
    protected int BUTTON1_B = 90;//'Z';
    /**
     *
     * The START button on the JoyPad 1.
     *
     */
    protected int BUTTON1_START = 13;//'\n';
    /**
     *
     * The SELECT button on the JoyPad 1.
     *
     */
    protected int BUTTON1_SELECT = 17;//0x20;
    /**
     *
     * The UP button on the JoyPad 1.
     *
     */
    protected int BUTTON1_UP = 38;
    /**
     *
     * The DOWN button on the JoyPad 1.
     *
     */
    protected int BUTTON1_DOWN = 40;
    /**
     *
     * The LEFT button on the JoyPad1 .
     *
     */
    protected int BUTTON1_LEFT = 37;
    /**
     *
     * The RIGHT button on the JoyPad 1.
     *
     */
    protected int BUTTON1_RIGHT = 39;
    /**
     *
     * Button A on the JoyPad 2.
     *
     */
    protected int BUTTON2_A = 'T';
    /**
     *
     * Button B on the JoyPad 2.
     *
     */
    protected int BUTTON2_B = 'R';
    /**
     *
     * The START button on the JoyPad 2.
     *
     */
    protected int BUTTON2_START = 'E';
    /**
     *
     * The SELECT button on the JoyPad 2.
     *
     */
    protected int BUTTON2_SELECT = 'Q';
    /**
     *
     * The UP button on the JoyPad 2.
     *
     */
    protected int BUTTON2_UP = 'W';
    /**
     *
     * The DOWN button on the JoyPad 2.
     *
     */
    protected int BUTTON2_DOWN = 'S';
    /**
     *
     * The LEFT button on the JoyPad2.
     *
     */
    protected int BUTTON2_LEFT = 'A';
    /**
     *
     * The RIGHT button on the JoyPad 2.
     *
     */
    protected int BUTTON2_RIGHT = 'D';
    /**
     *
     * JoyPad One.
     *
     */
    protected static final int JOYPAD_1 = 0x01;
    /**
     *
     * JoyPad Two.
     *
     */
    protected static final int JOYPAD_2 = 0x02;
    /**
     *
     * <P>The current Joypad Number.</P>
     *
     */
    @SuppressWarnings("unused")
	private int joyPadNumber = JOYPAD_1;

    /**
     *
     * <P>Creates a new JoyPad object.</P>
     *
     */
    public JoyPad(int controllerNumber) {
        joyPadNumber = controllerNumber;
    }

    /**
     *
     * <P>Method to hold down a button on the JoyPad</P>
     *
     * @param button
     *        The button to be pressed.
     *
     */
    public final void buttonDown(int button) {

            if(button==BUTTON1_A) // Button A is Down (X)
            	joypad |= 0x1;
            else if(button==BUTTON1_B) // Button B is Down (Z)
            	joypad |= 0x2;
            else if(button==BUTTON1_SELECT) // Select is Down
            	joypad |= 0x4;
            else if(button==BUTTON1_START) // Start is Down
            	joypad |= 0x8;
            else if(button==BUTTON1_UP) // Up
            	joypad |= 0x10;
            else if(button==BUTTON1_DOWN) // Down
            	joypad |= 0x20;
            else if(button==BUTTON1_LEFT) // Left
            	joypad |= 0x40;
            else if(button==BUTTON1_RIGHT) // Right
            	joypad |= 0x80;

    }

    /**
     *
     * <P>Method to release a button on the </P>
     *
     * @param button
     *        The button to be released.
     *
     */
    public final void buttonUp(int button) {

    	if(button==BUTTON1_A) // Button A is Up (X)
    		joypad &= 0xFE;
    	else if(button==BUTTON1_B) // Button B is Up (Z)
    		joypad &= 0xFD;
    	else if(button==BUTTON1_SELECT) // Select is Up
    		joypad &= 0xFB;
    	else if(button==BUTTON1_START) // Start is Up
    		joypad &= 0xF7;
    	else if(button==BUTTON1_UP) // Up
    		joypad &= 0xEF;
    	else if(button==BUTTON1_DOWN) // Down
            joypad &= 0xDF;
    	else if(button==BUTTON1_LEFT) // Left
    		joypad &= 0xBF;
    	else if(button==BUTTON1_RIGHT) // Right
    		joypad &= 0x7F;

    }

    /**
     *
     * <P>Method to return the status of the </P>
     *
     * @return Status value of the JoyPad
     *
     */
    public final char getStatus() {
        return (char) joypadCorrection();
    }

    /**
     *
     * <P>Method to correct for when Both Directions are Pressed Together.</P>
     *
     * @return Status value of the JoyPad
     *
     * Thanks to SmashManiac for this function, which allows better handling
     * of the JoyPad when both UP+DOWN or LEFT+RIGHT are held together
     *
     */
    private int joypadCorrection() {

        int correction = joypad;

        if ((correction & 0x30) == 0x30) // Up and Down are pressed
        {
            correction &= 0xCF;
        }

        if ((correction & 0xC0) == 0xC0) // Left and Right are pressed
        {
            correction &= 0x3F;
        }


        return correction;

    }

    /**
     *
     * <P>Read value from the JoyPad for the currently indexed button.</P>
     *
     * @return Returns 1 if current indexed button is pressed, else 0.
     *
     */
    public final int readJoyPadBit() {


        // Read the Indexed Bit

        int retVal = joypadCorrection() >> joypadBit;


        // Roll Index

        joypadBit = (joypadBit + 1) & 0x7;


        // Return Least Significant Bit

        return retVal & 0x1;
    }

    /**
     *
     * <P>Reset the JoyPad so that the next read is for button A.</P>
     *
     */
    public final void resetJoyPad() {
        joypadBit = 0;
    }

    /**
     *
     * <P>Set the value of the </P>
     *
     * @param value The desired value of the 
     *
     */
    public final void setStatus(int value) {
        joypad = value;
    }

	public void setButtons(int up, int down, int left, int right,
			int b, int a, int select, int start) {
		
		this.BUTTON1_A = a;
		this.BUTTON1_B = b;
		this.BUTTON1_SELECT = select;
		this.BUTTON1_START = start;
		this.BUTTON1_DOWN = down;
		this.BUTTON1_UP = up;
		this.BUTTON1_LEFT = left;
		this.BUTTON1_RIGHT = right;
	}

}
