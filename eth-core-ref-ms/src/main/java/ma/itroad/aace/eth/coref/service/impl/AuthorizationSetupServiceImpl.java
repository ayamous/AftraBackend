package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.coref.model.bean.AuthorizationSetupBean;
import ma.itroad.aace.eth.coref.model.entity.AuthorizationSetup;
import ma.itroad.aace.eth.coref.service.IAuthorizationSetupService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationSetupServiceImpl extends BaseServiceImpl<AuthorizationSetup,AuthorizationSetupBean> implements IAuthorizationSetupService {
}