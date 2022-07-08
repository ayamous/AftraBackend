package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentBean;
import ma.itroad.aace.eth.coref.model.constants.Constants;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentPermissionType;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import ma.itroad.aace.eth.coref.model.mapper.ESafeDocumentMapper;
import ma.itroad.aace.eth.coref.model.mapper.custom.ESafeDocumenyCustomMapper;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import ma.itroad.aace.eth.coref.model.view.EsafeDocumentFilterPayload;
import ma.itroad.aace.eth.coref.repository.*;
import ma.itroad.aace.eth.coref.security.keycloak.utils.SecurityUtils;
import ma.itroad.aace.eth.coref.service.*;
import ma.itroad.aace.eth.coref.specification.ESafeDocumentSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ESafeDocumentServiceImpl extends BaseServiceImpl<ESafeDocument, ESafeDocumentBean> implements IESafeDocumentService {

    private final IUserAccountService userAccountService;
    private final ESafeDocumentRepository eSafeDocumentRepository;
    private final ESafeDocumenyCustomMapper eSafeDocumenyCustomMapper;
    private final ESafeDocumentMapper eSafeDocumentMapper;
    private final String uploadPath;
    private final ESafeDocumentPermissionService eSafeDocumentPermissionService;
    private String currentUserUplaodPath;

    private AgreementRepository agreementRepository ;
    private TechBarrierRefRepository techBarrierRefRepository ;
    private SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository ;
    private TaxationRepository taxationRepository ;
    private NationalProcedureRefRepository nationalProcedureRefRepository ;
    private RegulationRefRepository regulationRefRepository ;

    public ESafeDocumentServiceImpl(IUserAccountService userAccountService, ESafeDocumentRepository eSafeDocumentRepository,
                                    ESafeDocumenyCustomMapper eSafeDocumenyCustomMapper, ESafeDocumentMapper eSafeDocumentMapper,
                                    @Value("${upload.path}") String uploadPath, ESafeDocumentPermissionService eSafeDocumentPermissionService ,
                                    AgreementRepository agreementRepository,
                                    TechBarrierRefRepository techBarrierRefRepository,
                                    SanitaryPhytosanitaryMeasuresRefRepository sanitaryPhytosanitaryMeasuresRefRepository,
                                    TaxationRepository taxationRepository,
                                    NationalProcedureRefRepository nationalProcedureRefRepository

    ) {
        this.userAccountService = userAccountService;
        this.eSafeDocumentRepository = eSafeDocumentRepository;
        this.eSafeDocumenyCustomMapper = eSafeDocumenyCustomMapper;
        this.eSafeDocumentMapper = eSafeDocumentMapper;
        this.uploadPath = uploadPath;
        this.eSafeDocumentPermissionService = eSafeDocumentPermissionService;
        this.agreementRepository=agreementRepository ;
        this.sanitaryPhytosanitaryMeasuresRefRepository=sanitaryPhytosanitaryMeasuresRefRepository;
        this.techBarrierRefRepository=techBarrierRefRepository ;
        this.taxationRepository=taxationRepository ;
        this.nationalProcedureRefRepository=nationalProcedureRefRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public void initUserFolder() {
        try {
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                init();
            }
            Files.createDirectories(Paths.get(uploadPath + "/" + userAccountService.getCurrentUser().get().getLogin()));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    @Transactional
    public ESafeDocumentBean save(MultipartFile file, ESafeDocumentVM eSafeDocumentVM) {
        try {
            currentUserUplaodPath = uploadPath + "/" + userAccountService.getCurrentUser().get().getLogin();
            ESafeDocument eSafeDocument = this.eSafeDocumenyCustomMapper.toEntity(prepareESafeDocument(file, eSafeDocumentVM, false));
            StringBuilder sb = new StringBuilder(currentUserUplaodPath);
            addCurrentUser(eSafeDocument);
            Path root = Paths.get(sb.toString());
            if (!Files.exists(root)) {
                initUserFolder();
            }
            Path pathToFile = Paths.get(getFullPathToFileOrFolder(eSafeDocument));
            if (!Files.exists(pathToFile)) {
                Files.copy(file.getInputStream(), pathToFile);
                ESafeDocument result =eSafeDocumentRepository.save(eSafeDocument) ;
                ESafeDocumentBean resultBean = eSafeDocumentMapper.entityToBean(result);
                linkDocumentToEntity(eSafeDocumentVM, result);
                addPermissions(eSafeDocumentVM, eSafeDocument);
                return resultBean;
            } else
                throw new RuntimeException("Could not store the file. Error: file already exists ");

        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public void linkDocumentToEntity(ESafeDocumentVM eSafeDocumentVM, ESafeDocument result) {
        if (eSafeDocumentVM.getDocType() != null && eSafeDocumentVM.getIdEntity() != null) {
            if (eSafeDocumentVM.getDocType().equals(Constants.AGREEMENT)) {
                Agreement agreement = agreementRepository.findOneById(eSafeDocumentVM.getIdEntity());
                agreement.setESafeDocument(result);
                agreementRepository.save(agreement);
            }
            if (eSafeDocumentVM.getDocType().equals(Constants.MSP)) {
                SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = sanitaryPhytosanitaryMeasuresRefRepository.findOneById(eSafeDocumentVM.getIdEntity());
                sanitaryPhytosanitaryMeasuresRef.setESafeDocument(result);
                sanitaryPhytosanitaryMeasuresRefRepository.save(sanitaryPhytosanitaryMeasuresRef);
            }
            if (eSafeDocumentVM.getDocType().equals(Constants.TECHNICAL_BARRIER)) {
                TechBarrierRef techBarrierRef = techBarrierRefRepository.findOneById(eSafeDocumentVM.getIdEntity());
                techBarrierRef.setESafeDocument(result);
                techBarrierRefRepository.save(techBarrierRef);
            }
            if (eSafeDocumentVM.getDocType().equals(Constants.PROCEDURE)) {
                NationalProcedureRef nationalProcedureRef = nationalProcedureRefRepository.findOneById(eSafeDocumentVM.getIdEntity());
                nationalProcedureRef.setESafeDocument(result);
                nationalProcedureRefRepository.save(nationalProcedureRef);
            }
            if (eSafeDocumentVM.getDocType().equals(Constants.TAXATION)) {
                Taxation taxation = taxationRepository.findOneById(eSafeDocumentVM.getIdEntity());
                taxation.setESafeDocument(result);
                taxationRepository.save(taxation);
            }
            if (eSafeDocumentVM.getDocType().equals(Constants.REGULATION)) {
                RegulationRef regulationRef = regulationRefRepository.findOneById(eSafeDocumentVM.getIdEntity());
                regulationRef.setESafeDocument(result);
                regulationRefRepository.save(regulationRef);
            }
        }
    }





    public ESafeDocumentVM prepareESafeDocument(MultipartFile file, ESafeDocumentVM eSafeDocumentVM, Boolean isFolder) {
        eSafeDocumentVM.setEdmStorld(System.currentTimeMillis());
        eSafeDocumentVM.setIsFolder(isFolder);
        eSafeDocumentVM.setIsArchived(false);
        eSafeDocumentVM.setIsSharedDoc(false);
        eSafeDocumentVM.setIsStarred(false);
        eSafeDocumentVM.setIsSharedFolder(false);
        eSafeDocumentVM.setFileSize(file != null ? file.getSize() : 0);
        return eSafeDocumentVM;
    }

    @Transactional
    public ESafeDocumentBean newFolder(ESafeDocumentVM eSafeDocumentVM) {
        try {
            currentUserUplaodPath = uploadPath + "/" + userAccountService.getCurrentUser().get().getLogin();
            ESafeDocument eSafeDocument = this.eSafeDocumenyCustomMapper.toEntity(prepareESafeDocument(null, eSafeDocumentVM, true));
            StringBuilder sb = new StringBuilder(currentUserUplaodPath);
            addCurrentUser(eSafeDocument);
            Path root = Paths.get(sb.toString());
            if (!Files.exists(root)) {
                initUserFolder();
            }
            Path pathToFile = Paths.get(getFullPathToFileOrFolder(eSafeDocument));
            if (!Files.exists(pathToFile)) {
                File dossier = new File(getFullPathToFileOrFolder(eSafeDocument));
                if (dossier.mkdir()) {
                    ESafeDocumentBean result = eSafeDocumentMapper.entityToBean(eSafeDocumentRepository.save(eSafeDocument));
                    // TODO merge permission with same organization
                    addPermissions(eSafeDocumentVM, eSafeDocument);
                    return result;
                }
            } else throw new RuntimeException("Could not create the folder : folder already exists ");

        } catch (Exception e) {
            logger.error("Error while creating new folder", e);
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    private void addPermissions(ESafeDocumentVM eSafeDocumentVM, ESafeDocument eSafeDocument) {
        if (eSafeDocumentVM.isSharedWithAll()) {
            eSafeDocumentPermissionService.addPermissionForAll(eSafeDocument, ESafeDocumentPermissionType.SHARE);
        } else {
            eSafeDocumentPermissionService.addPermission(eSafeDocument, eSafeDocumentVM.getSharingPermission(), ESafeDocumentPermissionType.SHARE);
        }
        if (eSafeDocumentVM.isVisibleToAll()) {
            eSafeDocumentPermissionService.addPermissionForAll(eSafeDocument, ESafeDocumentPermissionType.VIEW);
        } else {
            eSafeDocumentPermissionService.addPermission(eSafeDocument, eSafeDocumentVM.getVisibilityPermission(), ESafeDocumentPermissionType.VIEW);
        }
    }

    public ByteArrayResource download(Long edmStorld) {
        String compressedFolderStringPath = uploadPath + "\\compressedFolder.zip";
        ESafeDocument eSafeDocument = eSafeDocumentRepository.findByEdmStorld(edmStorld);
        String stringPath = getFullPathToFileOrFolder(eSafeDocument);
        try {
            if (eSafeDocument == null) throw new MalformedURLException();
            else {
                if (!eSafeDocument.getIsFolder()) {
                    Path path = Paths.get(stringPath);
                    byte[] data = Files.readAllBytes(path);
                    ByteArrayResource resource = new ByteArrayResource(data);
                    return resource;
                } else {
                    try {
                        FileOutputStream fos = new FileOutputStream(compressedFolderStringPath);
                        ZipOutputStream zipOut = new ZipOutputStream(fos);
                        File fileToZip = new File(stringPath);
                        zipFile(fileToZip, fileToZip.getName(), zipOut);
                        zipOut.close();
                        fos.close();
                        Path path = Paths.get(compressedFolderStringPath);
                        byte[] data = Files.readAllBytes(path);
                        ByteArrayResource resource = new ByteArrayResource(data);
                        return resource;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    public String getFullPathToFileOrFolder(ESafeDocument eSafeDocument) {
        currentUserUplaodPath = uploadPath + "/" + eSafeDocument.getOwner().getLogin();
        String fullPath = eSafeDocument.getFileName();
        while (eSafeDocument.getParent() != null) {
            fullPath = eSafeDocument.getParent().getFileName() + "/" + fullPath;
            eSafeDocument = eSafeDocument.getParent();
        }
        fullPath = currentUserUplaodPath + "/" + fullPath;
        return fullPath;
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public Set<ESafeDocumentVM> findESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId) {
        if (parentEdemStoreId == -1) {
            return eSafeDocumenyCustomMapper.listToBean(eSafeDocumentRepository.findByParentNull());
        }
        return eSafeDocumenyCustomMapper.listToBean(eSafeDocumentRepository.findByParentEdmStorld(parentEdemStoreId));
    }

    public Page<ESafeDocumentBean> findDetailedESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId, Pageable pageable) {
        if (userAccountService.getCurrentUser().isPresent()) {
            if (parentEdemStoreId == -1) {
                return eSafeDocumentRepository.findByParentNullAndOwner_Id(userAccountService.getCurrentUser().get().getId(), pageable).map(element -> eSafeDocumentMapper.entityToBean(element));
            }
            return eSafeDocumentRepository.findByParentEdmStorldAndOwner_Id(parentEdemStoreId, userAccountService.getCurrentUser().get().getId(), pageable).map(element -> eSafeDocumentMapper.entityToBean(element));
        }
        return Page.empty(pageable);
    }

    public void deleteESafeDocumentsByEdmStoreId(Long edemStoreId) {
        File file = new File(getFullPathToFileOrFolder(eSafeDocumentRepository.findByEdmStorld(edemStoreId)));
        eSafeDocumentRepository.deleteById(eSafeDocumentRepository.findByEdmStorld(edemStoreId).getId());
        deleteFolderOrFilePhysicly(file);
    }

    public Set<ESafeDocumentVM> findAllESafeDocumentsByParentEdmStoreId(Long parentEdemStoreId) {
        Set<ESafeDocumentVM> eSafeDocumentVMSToReturn = new HashSet<ESafeDocumentVM>();
        Set<ESafeDocumentVM> eSafeDocumentVMS = eSafeDocumenyCustomMapper.listToBean(eSafeDocumentRepository.findByParentEdmStorld(parentEdemStoreId));
        eSafeDocumentVMSToReturn.addAll(eSafeDocumentVMS);
        eSafeDocumentVMS.forEach(eSafeDocumentVM -> {
            if (eSafeDocumentVM.getIsFolder()) {
                eSafeDocumentVMSToReturn.addAll(findAllESafeDocumentsByParentEdmStoreId(eSafeDocumentVM.getEdmStorld()));
            }
        });
        return eSafeDocumentVMSToReturn;
    }

    public List<ESafeDocumentBean> findAllESafeDocumentBeansByParentEdmStoreId(Long parentEdemStoreId) {
        List<ESafeDocumentBean> eSafeDocumentBeansToReturn = new ArrayList<>();
        List<ESafeDocumentBean> eSafeDocumentBeans = eSafeDocumentMapper.toBeanList(new ArrayList<>(eSafeDocumentRepository.findByParentEdmStorld(parentEdemStoreId)));
        eSafeDocumentBeansToReturn.addAll(eSafeDocumentBeans);
        eSafeDocumentBeans.forEach(eSafeDocumentBean -> {
            if (eSafeDocumentBean.getIsFolder()) {
                eSafeDocumentBeansToReturn.addAll(findAllESafeDocumentBeansByParentEdmStoreId(eSafeDocumentBean.getEdmStorld()));
            }
        });
        return eSafeDocumentBeansToReturn;
    }

    private void deleteFolderOrFilePhysicly(File file) {
        if (file.isDirectory()) {
            String[] fileList = file.list();
            if (fileList.length == 0) {
                file.delete();
            } else {
                int size = fileList.length;
                for (int i = 0; i < size; i++) {
                    String fileName = fileList[i];
                    String fullPath = file.getPath() + "/" + fileName;
                    File fileOrFolder = new File(fullPath);
                    deleteFolderOrFilePhysicly(fileOrFolder);
                }
                file.delete();
            }
        } else {
            file.delete();
        }
    }

    @Transactional
    public void modifyFile(MultipartFile file, ESafeDocumentVM eSafeDocumentVM) {
        if (file != null) {
            try {
                currentUserUplaodPath = uploadPath + "/" + userAccountService.getCurrentUser().get().getLogin();
                deleteFolderOrFilePhysicly(Paths.get(getFullPathToFileOrFolder(eSafeDocumentRepository.findOneById(eSafeDocumentVM.getId()))).toFile());
                ESafeDocument eSafeDocument = this.eSafeDocumenyCustomMapper.patchEntityByVM(eSafeDocumentVM, eSafeDocumentRepository.findOneById(eSafeDocumentVM.getId()));
                Path root = Paths.get(currentUserUplaodPath);
                if (!Files.exists(root)) {
                    initUserFolder();
                }
                Files.copy(file.getInputStream(), Paths.get(getFullPathToFileOrFolder(eSafeDocument)));
                ESafeDocument result =eSafeDocumentRepository.save(eSafeDocument);
                linkDocumentToEntity(eSafeDocumentVM, result);
                addPermissions(eSafeDocumentVM, eSafeDocument);
            } catch (Exception e) {
                throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
            }
        } else {
            modify(eSafeDocumentVM);
        }
    }

    @Transactional
    public void modify(ESafeDocumentVM eSafeDocumentVM) {
        ESafeDocument oldESafeDocument = eSafeDocumentRepository.findOneById(eSafeDocumentVM.getId());
        String oldPath = "";
        Path returnedPath = null;
        if (oldESafeDocument != null) oldPath = getFullPathToFileOrFolder(oldESafeDocument);
        if (!eSafeDocumentVM.getFileName().equals(oldESafeDocument.getFileName())) {
            returnedPath = renameFile(oldPath, getFullPathToFileOrFolder(eSafeDocumenyCustomMapper.toEntity(eSafeDocumentVM)));
        }
        ESafeDocument eSafeDocument = eSafeDocumenyCustomMapper.patchEntityByVM(eSafeDocumentVM, oldESafeDocument);
        if (returnedPath != null) {
            eSafeDocumentRepository.save(eSafeDocument);
        }
        addPermissions(eSafeDocumentVM, eSafeDocument);
    }

    public Path renameFile(String oldPath, String newPath) {
        Path f = Paths.get(oldPath);
        Path rF = Paths.get(newPath);
        Path returnedPath = null;
        try {
            returnedPath = Files.move(f, rF, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnedPath;
    }

    @Override
    public Page<ESafeDocumentBean> findAllOwnedByCurrentUser(Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            return dbUser.map(userAccount -> eSafeDocumentRepository.findAll(
                    ESafeDocumentSpecification.hasOwner(
                            userAccount.getId(), userAccount.getReference()), pageable)
                    .map(eSafeDocumentMapper::entityToBean)).orElseGet(() -> eSafeDocumentRepository.findAll(ESafeDocumentSpecification.hasOwner(currentUser.get().getReference()), pageable).map(eSafeDocumentMapper::entityToBean));
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> findAllSharedByCurrentUser(Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            return dbUser.map(userAccount -> eSafeDocumentRepository.findAll(ESafeDocumentSpecification.shardByCurrentUser(userAccount.getId(), userAccount.getReference()), pageable).
                    map(eSafeDocumentMapper::entityToBean)).orElseGet(() -> eSafeDocumentRepository.findAll(ESafeDocumentSpecification.shardByCurrentUser(currentUser.get().getReference()), pageable).map(eSafeDocumentMapper::entityToBean));
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> findAllSharedWithCurrentUser(Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.shardWithCurrentUser(dbUser.get().getId()), pageable).map(eSafeDocumentMapper::entityToBean);
            }
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> findAllOwnedByAndSharedWithCurrentUser(Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            return dbUser.map(userAccount -> eSafeDocumentRepository.findAll(ESafeDocumentSpecification.hasOwner(userAccount.getId(), userAccount.getReference()).or(ESafeDocumentSpecification.shardWithCurrentUser(userAccount.getId())), pageable).
                    map(eSafeDocumentMapper::entityToBean)).orElseGet(() -> eSafeDocumentRepository.findAll(ESafeDocumentSpecification.hasOwner(currentUser.get().getReference()), pageable).map(eSafeDocumentMapper::entityToBean));
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> findAllVisibleWithCurrentUser(Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.visibleWithCurrentUser(dbUser.get().getId()), pageable).map(eSafeDocument -> {
                    ESafeDocumentBean bean = eSafeDocumentMapper.entityToBean(eSafeDocument);
                    if (!CollectionUtils.isEmpty(eSafeDocument.getPermissions())) {
                        bean.setSharable(eSafeDocument.getPermissions().stream().anyMatch(e -> e.getDocument().getId().equals(eSafeDocument.getId())
                                && e.getUserAccount().getId().equals(dbUser.get().getId()) && e.isSharable()));
                    }
                    return bean;
                });
            }
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> ownedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.ownedByCurrentUserFilter(esafeDocumentFilterPayload,dbUser.get().getId()), pageable).map(eSafeDocument -> {
                    ESafeDocumentBean bean = eSafeDocumentMapper.entityToBean(eSafeDocument);
                    return bean;
                });
            }
        }
        return Page.empty(pageable);
    }



    @Override
    public Page<ESafeDocumentBean> visibleWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.visibleWithCurrentUserFilter(esafeDocumentFilterPayload,dbUser.get().getId()), pageable).map(eSafeDocument -> {
                    ESafeDocumentBean bean = eSafeDocumentMapper.entityToBean(eSafeDocument);
                    return bean;
                });
            }
        }
        return Page.empty(pageable);
    }
    @Override
    public Page<ESafeDocumentBean> sharedWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.sharedWithCurrentUserFilter(esafeDocumentFilterPayload,dbUser.get().getId()), pageable).map(eSafeDocument -> {
                    ESafeDocumentBean bean = eSafeDocumentMapper.entityToBean(eSafeDocument);
                    return bean;
                });
            }
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<ESafeDocumentBean> sharedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        Optional<UserAccount> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<UserAccount> dbUser = userAccountService.findUserByReference(currentUser.get().getReference());
            if (dbUser.isPresent()) {
                return eSafeDocumentRepository.findAll(ESafeDocumentSpecification.sharedByCurrentUserFilter(esafeDocumentFilterPayload,dbUser.get().getId(),dbUser.get().getCreatedBy()), pageable).map(eSafeDocument -> {
                    ESafeDocumentBean bean = eSafeDocumentMapper.entityToBean(eSafeDocument);
                    return bean;
                });
            }
        }
        return Page.empty(pageable);
    }

    private void addCurrentUser(ESafeDocument eSafeDocument) {
        Optional<UserAccount> currentUser = userAccountService.getCurrentUser();
        currentUser.ifPresent(userAccount ->
                eSafeDocument.setOwner(userAccount)
        );
        SecurityUtils.getCurrentUser().ifPresent(userAccount -> {
            if (eSafeDocument.isNew()) {
                eSafeDocument.setCreatedBy(userAccount.getReference());
            } else {
                eSafeDocument.setUpdatedBy(userAccount.getReference());
            }
        });
    }

    @Override
    public Set<ESafeDocumentVM> findAllByChannelType(ExchangeChannelType channelType) {
        return eSafeDocumentRepository.findAllByChannelType(channelType).stream().map(eSafeDocumenyCustomMapper::toBean).collect(Collectors.toSet());
    }


    @Override
    public ESafeDocumentBean update(MultipartFile file, ESafeDocumentVM eSafeDocumentVM, Long id) {


        try {

            ESafeDocument oldDocument = eSafeDocumentRepository.findOneById(id);
            currentUserUplaodPath = uploadPath + "/" + userAccountService.getCurrentUser().get().getLogin();
            ESafeDocument newDocument = this.eSafeDocumenyCustomMapper.toEntity(eSafeDocumentVM);
            newDocument.setId(id);
            newDocument.setEdmStorld(oldDocument.getEdmStorld());
            addCurrentUser(newDocument);
            if(file != null){
                File oldFile = new File(getFullPathToFileOrFolder(eSafeDocumentRepository.findByEdmStorld(oldDocument.getEdmStorld())));
                deleteFolderOrFilePhysicly(oldFile);

                Path pathToFile = Paths.get(getFullPathToFileOrFolder(newDocument));
                if (Files.exists(pathToFile)) {
                    deleteFolderOrFilePhysicly(new File(getFullPathToFileOrFolder(newDocument)));
                }
                Files.copy(file.getInputStream(), pathToFile);
                StringBuilder sb = new StringBuilder(currentUserUplaodPath);

                Path root = Paths.get(sb.toString());
                if (!Files.exists(root)) {
                    initUserFolder();
                }

            }

            ESafeDocument result =eSafeDocumentRepository.save(newDocument) ;
            ESafeDocumentBean resultBean = eSafeDocumentMapper.entityToBean(result);
            linkDocumentToEntity(eSafeDocumentVM, result);
            addPermissions(eSafeDocumentVM, newDocument);
            return resultBean;

        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

    }

    @Override
    public ESafeDocumentBean getFileById(Long id) {
        ESafeDocument document = eSafeDocumentRepository.findById(id)
                .orElseThrow(
                ()-> new NotFoundException("Item with id not found : "+id));
        return eSafeDocumentMapper.entityToBean(document);
    }
}
