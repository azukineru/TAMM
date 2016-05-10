package com.minemeander.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import com.minemeander.engine.ActionResolver;
import com.minemeander.objects.Jack;
import com.minemeander.Art;
import com.minemeander.Constant;

public class MainMenu extends AbstractScreen{

	private SpriteBatch spriteBatch = new SpriteBatch();
	private Stage stage = new Stage();
	private Skin skin = new Skin();
	private Texture titleImage;
	private Sprite sprite;
	
	
	public MainMenu(){
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));		
		skin.add("default", Art.bitmapFont);
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);		
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		skin.add("default", textButtonStyle);
		Table table = new Table();
		table.padTop(100);
		table.setFillParent(true);
		stage.addActor(table);
	
		titleImage = new Texture(Gdx.files.internal("data/Title.png"));
		
		TextButton button = new TextButton("START", skin);
		button.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				if (Gdx.app instanceof ActionResolver) {
					ActionResolver app = ((ActionResolver)(Gdx.app));
					if (!app.getSignedInGPGS()) {
						app.loginGPGS();
					}
				}
				MainMenu.this.transitionTo(new LevelSelectScreen());
			}
		});
		table.add(button).pad(20);
		
		table.row();
		TextButton button3 = new TextButton("EXIT", skin);
		button3.addListener(new ChangeListener() {			
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Gdx.app.exit();
			}
		});
		
		table.add(button3).pad(20);
		
		table.layout();

		
		Gdx.input.setInputProcessor(stage);
		
		fadeIn();
		
	}
	
	
	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
		spriteBatch.draw(titleImage, (Gdx.graphics.getWidth() - titleImage.getWidth()) / 2 , Gdx.graphics.getHeight() - 300);		
		spriteBatch.end();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		
		super.renderCurtain();
	}
	
		
}
