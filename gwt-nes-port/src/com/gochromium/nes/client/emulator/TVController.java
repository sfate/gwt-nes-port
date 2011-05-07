package com.gochromium.nes.client.emulator;



import java.util.Date;

import com.gochromium.nes.client.html5.CanvasElement;
import com.gochromium.nes.client.html5.CanvasPixelArray;
import com.gochromium.nes.client.html5.CanvasRenderingContext2D;
import com.gochromium.nes.client.html5.ImageData;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;



/** 
 *
 * Class for the TV Controller used by the NESCafe NES Emulator.
 *
 * @author   David de Niese
 * @version  0.56f
 * @final    TRUE
 *
 */
public final class TVController extends FlowPanel {


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
	CanvasRenderingContext2D bufferContext;
	ImageData bufferImageData;
	CanvasPixelArray bufferPixelArray;
	
	final TextBox log = new TextBox();
	
    public TVController(final NES nes) {
        
    	this.nes = nes;
    	SimplePanel logPanel = new SimplePanel();
    	logPanel.getElement().getStyle().setProperty("textAlign", "center");
    	this.add(logPanel);
    	
    	log.getElement().getStyle().setBorderColor("transparent");
    	log.getElement().getStyle().setBackgroundColor("transparent");
    	log.getElement().getStyle().setColor("#FFF");
    	log.getElement().getStyle().setOpacity(.2);
    	log.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    	log.getElement().getStyle().setProperty("textAlign", "center");
    	logPanel.add(log);
    	

        buffer = (CanvasElement) Document.get().createElement("canvas");
        buffer.setWidth(256);
        buffer.setHeight(240);
        
        this.getElement().appendChild(buffer);

        
    	bufferContext = buffer.getContext2D();
        bufferImageData = bufferContext.createImageData(256, 240);
        bufferPixelArray = bufferImageData.getData();
        


//        buffer.getStyle().setDisplay(Display.NONE);
        
//        log.setWidth("300px");
//        RootPanel.get().add(log);
        
        
        
        
        Event.addNativePreviewHandler(new NativePreviewHandler() 
        { 
            @Override 
            public void onPreviewNativeEvent(NativePreviewEvent event) 
            { 
                NativeEvent ne = event.getNativeEvent(); 
                if (KeyDownEvent.getType().getName().equals(ne.getType())) { 
                	nes.joyPad1.buttonDown(ne.getKeyCode());
                } else if(KeyUpEvent.getType().getName().equals(ne.getType())) { 
                	nes.joyPad1.buttonUp(ne.getKeyCode());
                }
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

    public final void drawScreen(boolean force) {

        //PAINT HERE!!
    	boolean sleeping = true;

    	while (sleeping) {
            sleeping = System.currentTimeMillis() - lastRedraw < 16;

        }
    	
    	frameCount++;
    	long currTime = System.currentTimeMillis();
    	

    	
    	if(currTime-lastFrameCalc > 1000) {
    		double seconds = ((currTime-lastFrameCalc)/1000);
    		double fpscalc = frameCount / seconds;
    		lastFrameCalc = currTime;
    		log.setText(fpscalc + " FPS");
//    		
    		frameCount = 0;
    	}

    	
        bufferContext.putImageData(bufferImageData, 0, 0);
        lastRedraw = System.currentTimeMillis();
    }
    
    
    int frameCount = 0;
    Long lastRedraw = System.currentTimeMillis();
    Long lastFrameCalc = System.currentTimeMillis(); 

    
    public void zoomIn() {
    	buffer.getStyle().setWidth(256*2, Unit.PX);
    	buffer.getStyle().setHeight(240*2, Unit.PX);
    }
    
    public void zoomOut() {
    	buffer.getStyle().setWidth(256, Unit.PX);
    	buffer.getStyle().setHeight(240, Unit.PX);
    }
}
