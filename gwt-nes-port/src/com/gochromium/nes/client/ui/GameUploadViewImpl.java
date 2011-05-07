package com.gochromium.nes.client.ui;

import com.gochromium.nes.client.html5.File;
import com.gochromium.nes.client.html5.FileList;
import com.gochromium.nes.client.html5.FileReader;
import com.gochromium.nes.client.html5.ProgressCallback;
import com.gochromium.nes.client.html5.ProgressEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class GameUploadViewImpl extends Composite implements GameUploadView {

	private static GameUploadViewImplUiBinder uiBinder = GWT
			.create(GameUploadViewImplUiBinder.class);

	interface GameUploadViewImplUiBinder extends
			UiBinder<Widget, GameUploadViewImpl> {
	}

	
	private Presenter listener;
	@UiField FileUpload fileUpload;
	@UiField FlowPanel fileDragUpload;
	
	public GameUploadViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		createDragHandlers(fileDragUpload.getElement(),this);
	}

	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

	@Override
	public void alert(String msg) {
		Window.alert(msg);
	}

	@UiHandler("fileUpload")
	void onFileUploadChangeEvent(ChangeEvent e) {
		FileList fileList  = FileList.fromEvent(e.getNativeEvent());
		processFiles(fileList);
	}


	@SuppressWarnings("unused")
	void processFiles(FileList fileList) {


		FileReader reader = FileReader.create();
		File file = fileList.get(0);
		
		if(file==null) {
			return;
		}
		
		final String fileName = file.getName();
		final String fileType = file.getType();
		
		reader.readAsBinaryString(file, new ProgressCallback(){

			@Override
			public void onError(ProgressEvent e) {
				listener.onLoadError("");
			}

			@Override
			public void onLoad(ProgressEvent e) {
				byte[] bytes = new byte[e.getTotal()];
				String result = e.getResult();
				for(int i=0;i<bytes.length;i++) {
					bytes[i] = (byte) result.charAt(i);
				}
				listener.onLoadFile(fileName, bytes);
			}
		});
	}
	
	

	
	static final native String createDragHandlers(Element elem, GameUploadViewImpl view) /*-{
		elem.addEventListener('dragover',function(evt) {
			evt.stopPropagation();
			evt.preventDefault();
		},false);
		elem.addEventListener('drop',function(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			view.@com.gochromium.nes.client.ui.GameUploadViewImpl::processFiles(Lcom/gochromium/nes/client/html5/FileList;)(evt.dataTransfer.files);
	
		},false);
	}-*/;

	static final native FileList fromEvent(NativeEvent event) /*-{
		return evt.dataTransfer.files;
	}-*/;
}
