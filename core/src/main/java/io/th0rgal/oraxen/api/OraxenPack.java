package io.th0rgal.oraxen.api;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.font.FontManager;
import io.th0rgal.oraxen.pack.upload.UploadManager;
import io.th0rgal.oraxen.sound.SoundManager;

import java.io.File;

public class OraxenPack {
    public static File getPack() {
        return OraxenPlugin.get().getResourcePack().getFile();
    }

    public static void uploadPack() {
        UploadManager uploadManager = new UploadManager(OraxenPlugin.get());
        OraxenPlugin.get().setUploadManager(uploadManager);
        uploadManager.uploadAsyncAndSendToPlayers(OraxenPlugin.get().getResourcePack(), true, true);
    }

    public static void reloadPack() {
        OraxenPlugin oraxen = OraxenPlugin.get();
        oraxen.setFontManager(new FontManager(oraxen.getConfigsManager()));
        oraxen.setSoundManager(new SoundManager(oraxen.getConfigsManager().getSound()));
        oraxen.getResourcePack().generate();
        oraxen.getUploadManager().uploadAsyncAndSendToPlayers(oraxen.getResourcePack(), true, true);
    }
}
