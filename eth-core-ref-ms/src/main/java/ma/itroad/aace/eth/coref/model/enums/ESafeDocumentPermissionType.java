package ma.itroad.aace.eth.coref.model.enums;

public enum ESafeDocumentPermissionType {
    SHARE,
    VIEW,
    ALL;

    public boolean isSharing() {
        return this.equals(SHARE) || this.equals(ALL);
    }

    public boolean isView() {
        return this.equals(VIEW) || this.equals(ALL);
    }
}
