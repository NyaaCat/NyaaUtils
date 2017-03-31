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
public enum I16rLang {

    EN_US("en_us", new HashMap<>()),
    ZH_CN("zh_cn", new HashMap<>());


    private static final Map<String, I16rLang> lookup = new HashMap<>();

    static {
        for (I16rLang lang : EnumSet.allOf(I16rLang.class))
            lookup.put(lang.getLocale(), lang);
    }

    private final String locale;
    private final Map<String, String> map;

    /**
     * Create an index of lang file.
     */
    I16rLang(String locale, Map<String, String> map) {
        this.locale = locale;
        this.map = map;
    }

    /**
     * @param locale The locale of the language
     * @return The index of a lang file based on locale.
     */
    public static I16rLang get(String locale) {
        I16rLang result = lookup.get(locale);
        return result == null ? EN_US : result;
    }

    /**
     * Initialize this class, load all the languages to the corresponding HashMap.
     */
    public static void init() {
        for (I16rLang i16rLang : I16rLang.values()) {
            try {
                readFile(i16rLang, new BufferedReader(new InputStreamReader(I16rLang.class.getResourceAsStream(NyaaUtils.instance.cfg.langFileDir + i16rLang.locale + ".lang"), Charset.forName("UTF-8"))));
                NyaaUtils.instance.getLogger().info(i16rLang.getLocale() + " has been loaded.");
            } catch (Exception e) {
                NyaaUtils.instance.getLogger().warning("Failed to load language file " + i16rLang.locale);
                e.printStackTrace();
            }
        }
    }

    public static void readFile(I16rLang i16rLang, BufferedReader reader) throws IOException {
        String temp;
        String[] tempStringArr;
        try {
            temp = reader.readLine();
            while (temp != null) {
                if (temp.startsWith("#")) continue;
                if (temp.contains("=")) {
                    tempStringArr = temp.split("=");
                    i16rLang.map.put(tempStringArr[0], tempStringArr.length > 1 ? tempStringArr[1] : "");
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
