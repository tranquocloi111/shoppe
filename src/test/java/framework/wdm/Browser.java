package framework.wdm;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Browser enum
 */
public enum Browser {
    CHROME("gc", "chrome"),
    FIREFOX("ff", "firefox"),
    IE("ie"),
    REMOTE("remote", "rm");

    /**
     * List of browser names supported in this Enum
     */
    private List<String> browserNames;

    Browser(final String... args) {
        this.browserNames = Arrays.asList(args);
    }

    public List<String> getBrowserNames() {
        return browserNames;
    }

    /**
     * Return Browser enum by provided name
     *
     * @param browserName
     * @return
     */
    public static Browser getBrowserByName(final String browserName) {
        Browser browser = CHROME;
        for (final Browser val : Browser.values()) {
            if (val.getBrowserNames().contains(browserName.toLowerCase(Locale.ENGLISH).trim())) {
                browser = val;
            }
        }
        return browser;
    }
}
