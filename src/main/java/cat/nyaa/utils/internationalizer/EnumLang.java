/*
 * Copyright (c) 2015 Jerrell Fang
 *
 * This project is Open Source and distributed under The MIT License (MIT)
 * (http://opensource.org/licenses/MIT)
 *
 * You should have received a copy of the The MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 */

package cat.nyaa.utils.internationalizer;

import cat.nyaa.nyaautils.NyaaUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meow J on 6/20/2015.
 * <p>
 * Unlocalized Name to Localized Name
 *
 * @author Meow J
 */
public enum EnumLang {

    EN_US("en_us", new HashMap<>()),
    ZH_CN("zh_cn", new HashMap<>());


    private static final Map<String, EnumLang> lookup = new HashMap<>();

    static {
        for (EnumLang lang : EnumSet.allOf(EnumLang.class))
            lookup.put(lang.getLocale(), lang);
    }

    private final String locale;
    private final Map<String, String> map;

    /**
     * Create an index of lang file.
     */
    EnumLang(String locale, Map<String, String> map) {
        this.locale = locale;
        this.map = map;
    }

    /**
     * @param locale The locale of the language
     * @return The index of a lang file based on locale.
     */
    public static EnumLang get(String locale) {
        EnumLang result = lookup.get(locale);
        return result == null ? EN_US : result;
    }

    /**
     * Initialize this class, load all the languages to the corresponding HashMap.
     */
    public static void init() {
        for (EnumLang enumLang : EnumLang.values()) {
            try {
                readFile(enumLang, new BufferedReader(new InputStreamReader(EnumLang.class.getResourceAsStream(NyaaUtils.instance.cfg.langFileDir + enumLang.locale + ".lang"), Charset.forName("UTF-8"))));
                NyaaUtils.instance.getLogger().info(enumLang.getLocale() + " has been loaded.");
            } catch (Exception e) {
                NyaaUtils.instance.getLogger().warning("Failed to load language file " + enumLang.locale);
                e.printStackTrace();
            }
        }
    }

    public static void readFile(EnumLang enumLang, BufferedReader reader) throws IOException {
        String temp;
        String[] tempStringArr;
        try {
            temp = reader.readLine();
            while (temp != null) {
                if (temp.startsWith("#")) continue;
                if (temp.contains("=")) {
                    tempStringArr = temp.split("=");
                    enumLang.map.put(tempStringArr[0], tempStringArr.length > 1 ? tempStringArr[1] : "");
                }
                temp = reader.readLine();
            }
        } finally {
            reader.close();
        }
    }

    /**
     * @return The locale of the language
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @return The HashMap of the language.
     */
    public Map<String, String> getMap() {
        return map;
    }
}
