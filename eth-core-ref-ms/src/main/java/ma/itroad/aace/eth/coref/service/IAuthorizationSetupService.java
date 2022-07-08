package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.model.bean.AuthorizationSetupBean;
import ma.itroad.aace.eth.coref.model.entity.AuthorizationSetup;
 
public interface IAuthorizationSetupService extends
        IBaseService<AuthorizationSetup,AuthorizationSetupBean>,
        IRsqlService<AuthorizationSetup,AuthorizationSetupBean> {
}