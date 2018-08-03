package cat.nyaa.nyaautils.lootprotect;

import org.librazy.nclangchecker.LangKey;
import org.librazy.nclangchecker.LangKeyType;

@LangKey(type = LangKeyType.SUFFIX)
public enum LootProtectMode {
    OFF,
    MAX_DAMAGE,
    FINAL_DAMAGE;
}
