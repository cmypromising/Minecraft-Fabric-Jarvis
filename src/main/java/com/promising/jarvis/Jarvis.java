package com.promising.jarvis;

import com.promising.jarvis.core.register.NLRegister;
import com.promising.jarvis.core.register.NLRegisterFactory;
import com.promising.jarvis.core.JarvisRuntime;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jarvis implements ModInitializer {
	public static final String MOD_ID = "jarvis";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModConfig.init();
		JarvisRuntime.initialize();
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> JarvisRuntime.shutdown());
		ServerTickEvents.END_SERVER_TICK.register(server -> JarvisRuntime.proactiveCompanion().tick(server));

		for(NLRegister register : NLRegisterFactory.createRegisters()){
			register.register();
		}
		LOGGER.info("Hello Fabric world!");
	}

}
