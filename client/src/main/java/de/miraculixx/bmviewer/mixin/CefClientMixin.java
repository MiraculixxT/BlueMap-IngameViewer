package de.miraculixx.bmviewer.mixin;

import de.miraculixx.bmviewer.util.BrowserScreenHelper;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(CefClient.class)
public class CefClientMixin {
    @Inject(at = @At("HEAD"), method = "onAddressChange", remap = false)
    public void onAddressChange(CefBrowser browser, CefFrame frame, String url, CallbackInfo ci) {
        BrowserScreenHelper.INSTANCE.setCurrentUrl(url);
    }

    @Inject(at = @At("HEAD"), method = "onTooltip", remap = false)
    public void onTooltip(CefBrowser browser, String text, CallbackInfoReturnable<Boolean> cir) {
        BrowserScreenHelper.INSTANCE.setTooltipText(text);
    }

    @Inject(at = @At("HEAD"), method = "onLoadingStateChange", remap = false)
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward, CallbackInfo ci) {
        // Track if loading was success or not
        // Update #1 -> Checked availability before connecting
    }

}
