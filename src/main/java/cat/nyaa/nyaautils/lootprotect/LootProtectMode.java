package cat.nyaa.nyaautils.lootprotect;

import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

@LangKey(type = LangKeyType.SUFFIX)
public enum LootProtectMode {
    OFF,
    MAX_DAMAGE,
    FINAL_DAMAGE;
}
