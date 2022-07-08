package ma.itroad.aace.eth.coref.model.enums;

public enum RegimType {

    IMPORT("IMPORT"),
    EXPORT("EXPORT"),
    TRANSIT("TRANSIT");


    private String text;

    RegimType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static RegimType fromString(String text) {
        for (RegimType b : RegimType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
