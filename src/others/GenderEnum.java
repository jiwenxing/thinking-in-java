package others;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum GenderEnum {
    MALE(1, "男"), FEMALE(2, "女");

    private String name;
    private int code;

    private static final Map<Integer, GenderEnum> GENDER_ENUM_MAP = new HashMap<>();

    static {
        Arrays.stream(GenderEnum.values()).forEach(genderEnum -> GENDER_ENUM_MAP.put(genderEnum.code, genderEnum));
    }

    GenderEnum(int code, String name) {
        this.name = name;
        this.code = code;
    }

    public static GenderEnum getByCode(int code) {
        return GENDER_ENUM_MAP.get(code);
    }

    public static String getNameByCode(int code) {
        return GENDER_ENUM_MAP.containsKey(code) ? GENDER_ENUM_MAP.get(code).name : null;
    }
}
