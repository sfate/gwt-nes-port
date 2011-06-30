package com.googlecode.gwtnes.client.halfnes;

public class ControllerWebImpl implements ControllerInterface {

    private final int keyUp, keyDown, keyLeft, keyRight, keyA, keyB, keySelect, keyStart;
    private int controllerbyte = 0x00;
    


   protected static final int BUTTON1_A = 88;//90;//'X';
   protected static final int BUTTON1_B = 90;//88;//'Z';
   protected static final int BUTTON1_START = 13;//'\n';
   protected static final int BUTTON1_SELECT = 17;//0x20;
   protected static final int BUTTON1_UP = 38;
   protected static final int BUTTON1_DOWN = 40;
   protected static final int BUTTON1_LEFT = 37;
   protected static final int BUTTON1_RIGHT = 39;

    public ControllerWebImpl(final int controllernum) {
    	handleWindowKeyEvents(this);
        switch (controllernum) {
            case 0:
            default:
                keyUp = BUTTON1_UP;
                keyDown = BUTTON1_DOWN;
                keyLeft = BUTTON1_LEFT;
                keyRight = BUTTON1_RIGHT;
                keyA = BUTTON1_A;
                keyB = BUTTON1_B;
                keySelect = BUTTON1_SELECT;
                keyStart = BUTTON1_START;
                break;
//            case 1:
//            default:
//                keyUp = prefs.getInt("keyUp2", KeyEvent.VK_W);
//                keyDown = prefs.getInt("keyDown2", KeyEvent.VK_S);
//                keyLeft = prefs.getInt("keyLeft2", KeyEvent.VK_A);
//                keyRight = prefs.getInt("keyRight2", KeyEvent.VK_D);
//                keyA = prefs.getInt("keyA2", KeyEvent.VK_G);
//                keyB = prefs.getInt("keyB2", KeyEvent.VK_F);
//                keySelect = prefs.getInt("keySelect2", KeyEvent.VK_R);
//                keyStart = prefs.getInt("keyStart2", KeyEvent.VK_T);
//                break;
        }
    }


    public void keyPressed(int kepressed) {

        if (kepressed == keyA) {
            // A
            controllerbyte |=  0x01;
        } else if (kepressed == keyB) {
            controllerbyte |=  0x02;
            // B
        } else if (kepressed == keySelect) {
            controllerbyte |=  0x04;
            // Sel
        } else if (kepressed == keyStart) {
            controllerbyte |=  0x08;
            // Start
        } else if (kepressed == keyUp) {
            controllerbyte |=  0x10;
            // Up
            controllerbyte &=  (0xff - 0x20);
            //turn down off when up pressed
        } else if (kepressed == keyDown) {
            controllerbyte |=  0x20;
            // Down
            controllerbyte &=  (0xff - 0x10);
        } else if (kepressed == keyLeft) {
            controllerbyte |=  0x40;
            // Left
            controllerbyte &=  (0xff - 0x80);
        } else if (kepressed == keyRight) {
            controllerbyte |=  0x80;
            // Right
            controllerbyte &=  (0xff - 0x40);
        }
    }


    public void keyReleased(int kepressed) {

        if (kepressed == keyA) {
            // A
            controllerbyte &=  0xFE;
        }
        if (kepressed == keyB) {
            controllerbyte &=  (0xff - 0x02);
            // B
        }
        if (kepressed == keySelect) {
            controllerbyte &=  (0xff - 0x04);
            // Sel
        }
        if (kepressed == keyStart) {
            controllerbyte &=  (0xff - 0x08);
            // Start
        }
        if (kepressed == keyUp) {
            controllerbyte &=  (0xff - 0x10);
            // Up
        }
        if (kepressed == keyDown) {
            controllerbyte &=  (0xff - 0x20);
            // Down
        }
        if (kepressed == keyLeft) {
            controllerbyte &=  (0xff - 0x40);
            // Left
        }
        if (kepressed == keyRight) {
            controllerbyte &=  (0xff - 0x80);
            // Right
        }
    }

    public int getbyte() {
        return controllerbyte;
    }

	private native void handleWindowKeyEvents(final ControllerWebImpl c) /*-{
		$wnd.onkeydown = function(event) {
		   c.@com.googlecode.gwtnes.client.halfnes.ControllerWebImpl::keyPressed(I)(event.which);
		};
		$wnd.onkeyup = function(event) {
		   c.@com.googlecode.gwtnes.client.halfnes.ControllerWebImpl::keyReleased(I)(event.which);
		};
	}-*/;

}
