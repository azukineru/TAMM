package com.minemeander.desktop;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import com.minemeander.engine.ActionResolver;
import com.minemeander.MyMineMeander;
import com.minemeander.Constant;

public class DesktopLauncher {
	public static void main (String[] arg) throws IOException {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Mine Meander";
		config.width = 1280;
		config.height = 800;
		Constant.ZOOM_FACTOR = 0.5f;
		
		deleteFiles();
		copyTiledMaps();
		processSprites();
		
		new LwjglApplication(new MyMineMeander(new ActionResolverDekstop()), config);
	}
	
	public static void deleteFiles() {
		File outputDir = new File("../core/data/output");
		File[] listFiles = outputDir.listFiles();
		if (listFiles != null && listFiles.length > 0) {				
			for (File file : listFiles) {
				file.delete();
			}
		}
	}
	
	public static void processSprites(){
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.maxWidth = 512;
		settings.maxHeight = 512;
		TexturePacker.processIfModified(settings, "../core/data/input/sprites", "../core/data/output", "pack");	
	}
	
	public static void copyTiledMaps() throws IOException {
		File inputDir = new File("../core/data/input");
		File outputDir = new File("../core/data/output");

		System.out.println("Copying tiled map to output...");
		File[] listFiles = inputDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("tsx") || pathname.getName().endsWith("tmx") || pathname.getName().endsWith("png"); 
			}
		});
		for (File file : listFiles) {
			Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(outputDir.getAbsolutePath()+"/"+file.getName()), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
}
