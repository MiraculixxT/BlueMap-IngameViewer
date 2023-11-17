package de.miraculixx.bmviewer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "bmviewer")
public class BrowserAutoConfig implements ConfigData {
        public int scale = 90;
        public String currentUrl = "Not Connected";
        public boolean saveProtocol = true;
}
