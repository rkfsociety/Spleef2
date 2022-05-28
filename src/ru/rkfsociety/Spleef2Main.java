package ru.rkfsociety;

import org.bukkit.plugin.java.JavaPlugin;

import com.worldbiomusic.minigameworld.api.MiniGameWorld;

public class Spleef2Main extends JavaPlugin{
	   @Override
	   public void onEnable() {
	       super.onEnable();

	       MiniGameWorld mw = MiniGameWorld.create("0.8.0"); // now (2022-05-24)
	       mw.registerMiniGame(new Spleef2());
	   }
}
