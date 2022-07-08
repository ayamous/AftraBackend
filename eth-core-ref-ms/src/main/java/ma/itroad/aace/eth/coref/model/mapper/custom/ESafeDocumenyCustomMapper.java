package ma.itroad.aace.eth.coref.model.mapper.custom;

import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.mapper.ESafeDocumentMapper;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import ma.itroad.aace.eth.coref.repository.EDocumentTypeRepository;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentRepository;
import ma.itroad.aace.eth.coref.repository.EconomicOperatorRepository;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ESafeDocumenyCustomMapper {

    @Autowired
    ESafeDocumentMapper eSafeDocumentMapper ;

    @Autowired
    EconomicOperatorRepository economicOperatorRepository ;

    @Autowired
    ESafeDocumentRepository eSafeDocumentRepository ;

    @Autowired
    OrganizationRepository organizationRepository ;

    @Autowired
    EDocumentTypeRepository eDocumentTypeRepository ;

    public ESafeDocument toEntity(ESafeDocumentVM eSafeDocumentVM){
        ESafeDocument eSafeDocument =new ESafeDocument() ;
        eSafeDocument.setId(eSafeDocumentVM.getId());
        eSafeDocument.setChannelType(eSafeDocumentVM.getChannelType());
        if (eSafeDocumentVM.getFileSize()!=null) eSafeDocument.setFileSize(eSafeDocumentVM.getFileSize());
        if (eSafeDocumentVM.getFileName()!=null)eSafeDocument.setFileName(eSafeDocumentVM.getFileName());
        if (eSafeDocumentVM.getIsFolder()!=null)eSafeDocument.setIsFolder(eSafeDocumentVM.getIsFolder());
        if (eSafeDocumentVM.getIsStarred()!=null) eSafeDocument.setIsStarred(eSafeDocumentVM.getIsStarred());
        if (eSafeDocumentVM.getIsSharedDoc()!=null) eSafeDocument.setIsSharedDoc(eSafeDocumentVM.getIsSharedDoc());
        if (eSafeDocumentVM.getIsSharedFolder()!=null)   eSafeDocument.setIsSharedFolder(eSafeDocumentVM.getIsSharedFolder());
        if (eSafeDocumentVM.getIsArchived()!=null) eSafeDocument.setIsArchived(eSafeDocumentVM.getIsArchived());
        if (eSafeDocumentVM.getExpirationDate()!=null) eSafeDocument.setExpirationDate(eSafeDocumentVM.getExpirationDate());
        if (eSafeDocumentVM.getEdmStorld()!=null)  eSafeDocument.setEdmStorld(eSafeDocumentVM.getEdmStorld());
        if (eSafeDocumentVM.getVersion()!=null)  eSafeDocument.setVersion(eSafeDocumentVM.getVersion());
        if (eSafeDocumentVM.getDocType()!=null)   eSafeDocument.setDocumentType(eDocumentTypeRepository.findByCode(eSafeDocumentVM.getDocType()));
        if(eSafeDocumentVM.getEconomicOperatorCode()!=null) eSafeDocument.setEconomicOperator(economicOperatorRepository.findByCode(eSafeDocumentVM.getEconomicOperatorCode()));
        if(eSafeDocumentVM.getParentEdmStorld()!=null) eSafeDocument.setParent(eSafeDocumentRepository.findByEdmStorld(eSafeDocumentVM.getParentEdmStorld()));
        if(eSafeDocumentVM.getOrganizationReference()!=null) eSafeDocument.setOrganization(organizationRepository.findByReference(eSafeDocumentVM.getOrganizationReference()));
        if (eSafeDocumentVM.getAuteur()!=null) eSafeDocument.setAuteur(eSafeDocumentVM.getAuteur());

        return  eSafeDocument ;
    } ;

    public ESafeDocument patchEntityByVM(ESafeDocumentVM eSafeDocumentVM ,ESafeDocument eSafeDocument){
        if (eSafeDocumentVM.getFileSize()!=null) eSafeDocument.setFileSize(eSafeDocumentVM.getFileSize());
        if (eSafeDocumentVM.getFileName()!=null)eSafeDocument.setFileName(eSafeDocumentVM.getFileName());
        if (eSafeDocumentVM.getIsFolder()!=null)eSafeDocument.setIsFolder(eSafeDocumentVM.getIsFolder());
        if (eSafeDocumentVM.getIsStarred()!=null) eSafeDocument.setIsStarred(eSafeDocumentVM.getIsStarred());
        if (eSafeDocumentVM.getIsSharedDoc()!=null) eSafeDocument.setIsSharedDoc(eSafeDocumentVM.getIsSharedDoc());
        if (eSafeDocumentVM.getIsSharedFolder()!=null)   eSafeDocument.setIsSharedFolder(eSafeDocumentVM.getIsSharedFolder());
        if (eSafeDocumentVM.getIsArchived()!=null) eSafeDocument.setIsArchived(eSafeDocumentVM.getIsArchived());
        if (eSafeDocumentVM.getExpirationDate()!=null) eSafeDocument.setExpirationDate(eSafeDocumentVM.getExpirationDate());
        if (eSafeDocumentVM.getEdmStorld()!=null)  eSafeDocument.setEdmStorld(eSafeDocumentVM.getEdmStorld());
        if (eSafeDocumentVM.getVersion()!=null)  eSafeDocument.setVersion(eSafeDocumentVM.getVersion());
        if (eSafeDocumentVM.getDocType()!=null)   eSafeDocument.setDocumentType(eDocumentTypeRepository.findByCode(eSafeDocumentVM.getDocType()));
        if(eSafeDocumentVM.getEconomicOperatorCode()!=null) eSafeDocument.setEconomicOperator(economicOperatorRepository.findByCode(eSafeDocumentVM.getEconomicOperatorCode()));
        if(eSafeDocumentVM.getParentEdmStorld()!=null) eSafeDocument.setParent(eSafeDocumentRepository.findByEdmStorld(eSafeDocumentVM.getParentEdmStorld()));
        if(eSafeDocumentVM.getOrganizationReference()!=null) eSafeDocument.setOrganization(organizationRepository.findByReference(eSafeDocumentVM.getOrganizationReference()));

        return  eSafeDocument ;
    } ;

    public ESafeDocumentVM toBean(ESafeDocument eSafeDocument) {
        ESafeDocumentVM  vm =new ESafeDocumentVM() ;
        vm.setId(eSafeDocument.getId());
        vm.setFileSize(eSafeDocument.getFileSize());
        vm.setFileName(eSafeDocument.getFileName());
        vm.setIsFolder(eSafeDocument.getIsFolder());
        vm.setIsStarred(eSafeDocument.getIsStarred());
        vm.setIsSharedDoc(eSafeDocument.getIsSharedDoc());
        vm.setIsSharedFolder(eSafeDocument.getIsSharedFolder());
        vm.setIsArchived(eSafeDocument.getIsArchived());
        vm.setExpirationDate(eSafeDocument.getExpirationDate());
        vm.setEdmStorld(eSafeDocument.getEdmStorld());
        vm.setVersion(eSafeDocument.getVersion());
        vm.setDocType(eSafeDocument.getDocumentType().getCode());
        vm.setChannelType(eSafeDocument.getChannelType());
        if(eSafeDocument.getEconomicOperator()!=null) vm.setEconomicOperatorCode(eSafeDocument.getEconomicOperator().getCode());
        if(eSafeDocument.getParent()!=null) vm.setParentEdmStorld( eSafeDocument.getParent().getEdmStorld());
        if(eSafeDocument.getOrganization()!=null) vm.setOrganizationReference(eSafeDocument.getOrganization().getReference());
        return vm ;
    }

    public Set<ESafeDocumentVM> listToBean(Set<ESafeDocument> eSafeDocuments){
        return eSafeDocuments.stream().map(doc ->   toBean(doc) ) .collect(Collectors.toSet());
   }

    public  Set<ESafeDocument> listToEntity(Set<ESafeDocumentVM> eSafeDocuments){
        return eSafeDocuments.stream().map(doc ->   toEntity(doc) ) .collect(Collectors.toSet());
    }
}
