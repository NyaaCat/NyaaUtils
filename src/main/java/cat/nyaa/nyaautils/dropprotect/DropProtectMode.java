package cat.nyaa.nyaautils.dropprotect;

import org.librazy.nclangchecker.LangKey;
import org.librazy.nclangchecker.LangKeyType;

@LangKey(type = LangKeyType.SUFFIX)
public enum DropProtectMode {
    OFF,
    ON
}
