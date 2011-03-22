package com.googlecode.gwtnes.client;

import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;




/** 
 *
 * Class for the TV Controller used by the NESCafe NES Emulator.
 *
 * @author David de Niese
 * @author Brad Rydzewski
 * @version  0.56f
 * @final    TRUE
 *
 */
public final class TVController extends FocusPanel {

    /**
     *
     * <P>The current scanline number.</P>
     *
     */
    protected int scanLine = 0;

    /**
     *
     * <P>The Screen Palette.</P>
     *
     */
    protected int palette[];

    /**
     *
     * <P>The current NES Engine.</P>
     *
     */
    private final NES nes;

    /**
     *
     * <P>The Percentage Tint for the Palette</P>
     *
     */
    protected float tint = 128.0f;

    /**
     *
     * <P>The Percentage Hue for the Palette</P>
     *
     */
    protected float hue = 128.0f;


	CanvasElement buffer;
	Context2d bufferContext;
	ImageData bufferImageData;
	CanvasPixelArray bufferPixelArray;
	
	final TextBox log = new TextBox();
	
    public TVController(final NES nes) {
        
    	this.nes = nes;
    	
        buffer = (CanvasElement) Document.get().createElement("canvas");
        buffer.setWidth(256);
        buffer.setHeight(240);
        buffer.getStyle().setBackgroundColor("#000000");
        
        this.getElement().appendChild(buffer);

    	bufferContext = buffer.getContext2d();
        bufferImageData = bufferContext.createImageData(256, 240);
        bufferPixelArray = bufferImageData.getData();
  
        log.setWidth("300px");
        RootPanel.get().add(log);
        
//        buffer.getStyle().setWidth(512, Unit.PX);
        

    	this.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				TVController.this.setFocus(true);
			}
    	});
    	this.addKeyDownHandler(new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				nes.joyPad1.buttonDown(event.getNativeKeyCode());
			}
    	});
    	this.addKeyUpHandler(new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
	            ////nes.joyPad1.buttonDown(userPressed);
	            //nes.joyPad2.buttonDown(userPressed);
	         // Inform the JoyPad

	            nes.joyPad1.buttonUp(event.getNativeKeyCode());
//	            nes.joyPad2.buttonUp(keyevent.getKeyCode());
			}
    	});
    }

	public void updatePalette() {
        palette = nes.palette.palette;
	}

	/**
     *
     * <P>Set the Scanline Manually</P>
     *
     */
    public final boolean setScanLineNum(int sl) {
        // Set the Scanline
        scanLine = sl;

        return false;
    }

    /**
     *
     * <P>Sets an Array of Pixels representing the current Scanline.</P>
     *
     */
    public final void setPixels(int[] palEntries) {
    	//we want a top / bottom border
    	if(scanLine<9||scanLine>232)return;
    	//0,256
        for (int x = 8; x < 248; x++) {
        	
        	int pos = (scanLine << 8) | x;
        	int p = pos*4;
        	int pxl = palette[palEntries[x]];

        	int r = (pxl >>> 16) & 0xff;
        	int g = (pxl >>> 8) & 0xff;
        	int b = pxl & 0xff;
    		bufferPixelArray.set(p+0, r);
    		bufferPixelArray.set(p+1, g);
    		bufferPixelArray.set(p+2, b);
    		bufferPixelArray.set(p+3, 0xFF);
        }
    }

    int frameCount = 0;
    Long lastRedraw = System.currentTimeMillis();
    
    public final void drawScreen(boolean force) {

    	frameCount++;
    	if(System.currentTimeMillis()-lastRedraw > 1000) {
    		double seconds = ((System.currentTimeMillis()-lastRedraw)/1000);
    		double fpscalc = frameCount / seconds;
    		lastRedraw = System.currentTimeMillis();
    		log.setText("fps rate of  {" + fpscalc + "} calculated at "+String.valueOf(lastRedraw));
    		
    		frameCount = 0;
    	}

        bufferContext.putImageData(bufferImageData, 0, 0);
    }

}
