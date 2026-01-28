package net.sydneyclient.phobos.entrypoints;

import net.fabricmc.api.ModInitializer;
import net.sydneyclient.phobos.AuthHttpServer;
import net.sydneyclient.phobos.AuthSocket;

public class FabricEntryPoint implements ModInitializer {
    @Override
    public void onInitialize() {
        AuthHttpServer.initialize();
        AuthSocket.initialize();
    }
}
