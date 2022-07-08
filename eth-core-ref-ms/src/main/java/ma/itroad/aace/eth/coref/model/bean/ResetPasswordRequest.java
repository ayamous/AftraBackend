package ma.itroad.aace.eth.coref.model.bean;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    String newPassword;
    String currentPassword;
}
