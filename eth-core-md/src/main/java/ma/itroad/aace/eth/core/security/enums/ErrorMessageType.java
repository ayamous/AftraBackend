package ma.itroad.aace.eth.core.security.enums;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;

@Getter
public enum ErrorMessageType {

    MAIL_SENDING_ERROR(HttpServletResponse.SC_BAD_REQUEST, 53, "MAIL", "Mail Non Envoyé ! merci de réessayer plutard"),
    USER_ID_NOT_FOUND(HttpServletResponse.SC_BAD_REQUEST, 330, "circuitDerogation", "Aucun Utilisateur trouvé avec l'identifiant mentionné"),
    //User
    RECORD_NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, 404, "Suppression", "L'objet à supprimer n'existe pas"),
    UTILISATEUR_MISSING_REQUIRED_VALUES(HttpServletResponse.SC_BAD_REQUEST, 332, "utilisateur", "Un ou plusieurs champs obligatoires sont manquants"),
    DELETE_ERROR(HttpServletResponse.SC_CONFLICT, 409, "Suppression", "Impossible de supprimer cet objet car il est associé à un ou plusieurs autres objets, veuillez supprimer ces associations puis réessayer"),
    DELETE_SUCESS(HttpServletResponse.SC_OK, 200, "suppression", "delete done with success");
    private final int httpResponseStatus;
    private final int code;
    private final String fieldName;
    private final String messagePattern;

    private ErrorMessageType(int httpResponseStatus, int code, String fieldName, String messagePattern) {
        this.fieldName = fieldName;
        this.code = code;
        this.messagePattern = messagePattern;
        this.httpResponseStatus = httpResponseStatus;
    }

    public String getMessage(String msgArg) {
        return String.format(messagePattern, msgArg);
    }

    public String getMessage(String msgArg1, String msgArg2) {
        return String.format(messagePattern, msgArg1, msgArg2);
    }

    public String getMessage(Object... msgArgs) {
        return String.format(messagePattern, msgArgs);
    }

}
