package com.googlecode.gwtnes.client;

import java.io.ByteArrayInputStream;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.gwtnes.client.html5.File;
import com.googlecode.gwtnes.client.html5.FileList;
import com.googlecode.gwtnes.client.html5.FileReader;
import com.googlecode.gwtnes.client.html5.ProgressCallback;
import com.googlecode.gwtnes.client.html5.ProgressEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NESEntryPoint implements EntryPoint {

	public void onModuleLoad() {
		//create the file upload component
		final FileUpload fileUpload = new FileUpload();
		
		RootPanel.get().add(fileUpload);

		//create the callback for reading files, using HTML5 file api
		final ProgressCallback binaryReaderCallback = new ProgressCallback() {
			@Override
			public void onError(ProgressEvent e) {
				
			}
			@Override
			public void onLoad(ProgressEvent e) {
				byte[] bytes = new byte[e.getTotal()];
				
				//convert the results to a byte array
				String result = e.getResult();
				for(int i=0;i<bytes.length;i++) {
					bytes[i] = (byte) result.charAt(i);
				}

				//hide the file upload
				fileUpload.setVisible(false);
				
				//create and initialize the emulator
				NES nes = new NES();
				TVController gui = new TVController(nes);
				nes.init(gui);	

				//load the cartridge
				nes.cartLoad(new ByteArrayInputStream(bytes));

				//add the game canvas to the page
				RootPanel.get().add(gui);
			}
		};
		
		//create the callback for when a file is selected
		fileUpload.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				
				FileList fileList  = FileList.fromEvent(event.getNativeEvent());
				FileReader reader = FileReader.create();
				File file = fileList.get(0);
				
				if(file==null) {
					return;
				}

				try {
					reader.readAsBinaryString(file, binaryReaderCallback);
				} catch (Exception ex) {
					Window.alert(ex.toString());
				}
			}
		});
	}
}
