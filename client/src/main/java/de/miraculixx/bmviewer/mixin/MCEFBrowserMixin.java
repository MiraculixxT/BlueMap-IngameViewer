package de.miraculixx.bmviewer.mixin;

import com.cinemamod.mcef.MCEFBrowser;
import de.miraculixx.bmviewer.util.BrowserScreenHelper;
import org.cef.browser.CefBrowser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.nio.ByteBuffer;

@Mixin(MCEFBrowser.class)
public class MCEFBrowserMixin {
    @Inject(at = @At("HEAD"), method = "onPaint", remap = false, cancellable = true)
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, CallbackInfo ci) {
        if (!BrowserScreenHelper.INSTANCE.isOpen()) ci.cancel();
    }
}
