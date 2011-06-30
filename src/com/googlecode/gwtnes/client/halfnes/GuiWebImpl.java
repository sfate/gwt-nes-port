package com.googlecode.gwtnes.client.halfnes;

import java.util.Date;

import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

public class GuiWebImpl  extends FlowPanel implements GUIInterface {

    CanvasElement buffer;
    Context2d bufferContext;
    ImageData bufferImageData;
    CanvasPixelArray bufferPixelArray;
    final TextBox log = new TextBox();
    
	public GuiWebImpl() {
		

        SimplePanel logPanel = new SimplePanel();
        logPanel.getElement().getStyle().setProperty("textAlign", "center");
        this.add(logPanel);
        logPanel.add(log);
        
        

        buffer = (CanvasElement) Document.get().createElement("canvas");
        buffer.setWidth(256);
        buffer.setHeight(240);
        
        this.getElement().appendChild(buffer);

        
        bufferContext = buffer.getContext2d();
        bufferImageData = bufferContext.createImageData(256, 240);
        bufferPixelArray = bufferImageData.getData();
        


//        buffer.getStyle().setDisplay(Display.NONE);
        
//        log.setWidth("300px");
//        RootPanel.get().add(log);
        
        
        
        
//        Event.addNativePreviewHandler(new NativePreviewHandler() 
//        { 
//            @Override 
//            public void onPreviewNativeEvent(NativePreviewEvent event) 
//            { 
//                NativeEvent ne = event.getNativeEvent(); 
//                if (KeyDownEvent.getType().getName().equals(ne.getType())) { 
//                      nes.joyPad1.buttonDown(ne.getKeyCode());
//                } else if(KeyUpEvent.getType().getName().equals(ne.getType())) { 
//                      nes.joyPad1.buttonUp(ne.getKeyCode());
//                }
//            } 
//        }); 
        
//        handleWindowKeyEvents(this);
    }

//    private native void handleWindowKeyEvents(final GuiWebImpl c) /*-{
//                $wnd.onkeydown = function(event) {
//                 //$wnd.alert("keydown event detected!"+event.which);
//                 //nes.joyPad1.buttonDown(event.getNativeKeyCode());
//                 c.@com.googlecode.gwtnes.client.halfnes.GuiWebImpl::onKeyDown(I)(event.which);
//                };
//                $wnd.onkeyup = function(event) {
//                 //$wnd.alert("keyup event detected!"+event.which);
//                 //nes.joyPad1.buttonUp(event.getNativeKeyCode());
//                 c.@com.googlecode.gwtnes.client.halfnes.GuiWebImpl::onKeyUp(I)(event.which);
//                };
//        }-*/;
//	}
	
	
	
	int f = 0;
	long lastRedraw = new Date().getTime();;
	@Override
	public void setFrame(int[] frame) {
		
		// TODO Auto-generated method stub
//		System.out.println("  draw @ "+ new Date() + "  pixels: "+frame.length);
		int i=0;
		int r=0;
		int g=0;
		int b=0;
		int a=0;
		int pxl = 0;
		for(;i<frame.length;) {
			pxl = frame[i];
			a = 0xff;
			r = (pxl >>> 16) & 0xff;
			g = (pxl >>> 8) & 0xff;
			b =  pxl & 0xff;
			int p = i*4;
            bufferPixelArray.set(p, r);
            bufferPixelArray.set(p+1, g);
            bufferPixelArray.set(p+2, b);
            bufferPixelArray.set(p+3, a);
            
            i++;
            

		}
		if(f==63) {
			f = 0;
		    double fps = 63d/((System.currentTimeMillis()-lastRedraw)/1000d);
		    lastRedraw = new Date().getTime();
			log.setText("fps: " + fps);
		}else {
			f++;
		}
		bufferContext.putImageData(bufferImageData, 0, 0);
//		if(f%5==0)
//			System.out.println(Arrays.toString(Arrays.copyOf(frame));
	}

	@Override
	public void messageBox(String message) {
		Window.alert(message);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
