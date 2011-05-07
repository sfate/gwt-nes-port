package com.gochromium.nes.client.ui;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.gochromium.nes.client.emulator.NES;
import com.gochromium.nes.client.emulator.TVController;
import com.gochromium.nes.client.model.Game;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class GameListViewImpl extends Composite implements GameListView {

	private static GameListViewImplUiBinder uiBinder = GWT
			.create(GameListViewImplUiBinder.class);

	interface GameListViewImplUiBinder extends
			UiBinder<Widget, GameListViewImpl> {
	}
	
	private static Template template = GWT.create(Template.class);
	
	
	
	interface Template extends SafeHtmlTemplates {
		@Template("<div onclick='' __idx='{0}' class='tile'><img src='{1}' /><div class='title'>{2}</div></div>")
		SafeHtml div(String idx, String url, String cellContents);

		@Template("<div onclick='' __idx='{0}' class='tile'><img src='no-image.png' style='border:1px solid #CBCBCB !IMPORTANT;' /><div class='title'>{1}</div></div>")
		SafeHtml divNoImg(String idx, String cellContents);
	}
	
	public class ClickableHTMLPanel extends HTMLPanel {

		public ClickableHTMLPanel(SafeHtml safeHtml) {
			this(safeHtml.asString());
		}

		public ClickableHTMLPanel(String html) {
			super(html);
			this.sinkEvents(Event.ONCLICK);
		}

		@Override
		public void onBrowserEvent(Event event) {
			GameListViewImpl.this.onBrowserEvent2(event);
		}
	}

	private Presenter listener;
	@UiField(provided=true) HTMLPanel tiles =
		new ClickableHTMLPanel("");
	@UiField HTMLPanel gamePopup;
	@UiField FlowPanel gamePanel;
	@UiField Button zoomInButton;
	@UiField Button zoomOutButton;

	public GameListViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void setPresenter(Presenter listener) {
		this.listener = listener;
	}

	@Override
	public void renderGameList(List<Game> gameList) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();

		for(Game game : gameList) {
			
			if(game.getImage()==null || game.getImage().isEmpty()) {
				sb.append(template.divNoImg(game.getId(), game.getName()));
			} else {
				sb.append(template.div(game.getId(), game.getImage(), game.getName()));
			}
			
			
		}
		tiles.getElement().setInnerHTML(sb.toSafeHtml().asString());
	}


	private void onBrowserEvent2(Event event) {
	    // Get the event target.
	    EventTarget eventTarget = event.getEventTarget();
	    if (!Element.is(eventTarget)) {
	      return;
	    }
	    Element target = event.getEventTarget().cast();

	    // Forward the event to the cell.
	    String idxString = "";
	    while ((target != null)
	        && ((idxString = target.getAttribute("__idx")).length() == 0)) {
	      target = target.getParentElement();
	    }
	    if (idxString.length() > 0) {
	    	listener.onGameClicked(idxString);
	    }
	}

	private TVController gui = null;
	private NES nes = null;
	
	@Override
	public void loadGameCartridge(byte[] game) {

		System.gc();
		
		if(nes == null) {
			nes = new NES();
			gui = new TVController(nes);
			nes.init(gui);
			gamePanel.add(gui);
		}

		// Load the Cartridge		
		nes.cartLoad(new ByteArrayInputStream(game));

		gamePopup.setStyleName("gameVisible");
	}

	@UiHandler("shutdownButton")
	void onShutdownClick(ClickEvent e) {

		gamePopup.setStyleName("gameHidden");
		System.gc();
	}

	@UiHandler("zoomInButton")
	void onZoomInClick(ClickEvent e) {

		zoomInButton.setVisible(false);
		gui.zoomIn();
		zoomOutButton.setVisible(true);
		System.gc();
	}

	@UiHandler("zoomOutButton")
	void onZoomOutClick(ClickEvent e) {

		zoomOutButton.setVisible(false);
		gui.zoomOut();
		zoomInButton.setVisible(true);
		
		System.gc();
	}
}
