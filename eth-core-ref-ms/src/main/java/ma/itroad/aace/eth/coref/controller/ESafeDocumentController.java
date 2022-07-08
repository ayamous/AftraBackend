package ma.itroad.aace.eth.coref.controller;

import com.google.gson.Gson;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.core.common.EthClient;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentBean;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingRequest;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentSharingResponse;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import ma.itroad.aace.eth.coref.model.view.EsafeDocumentFilterPayload;
import ma.itroad.aace.eth.coref.repository.ESafeDocumentRepository;
import ma.itroad.aace.eth.coref.service.ESafeDocumentSharingService;
import ma.itroad.aace.eth.coref.service.IESafeDocumentService;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/eSafeDocuments")
public class ESafeDocumentController {

    private final IESafeDocumentService service;
    private final ESafeDocumentSharingService eSafeDocumentSharingService;
    private final ESafeDocumentRepository eSafeDocumentRepository;
    private final EthClient ethClient;

    @GetMapping
    public ResponseEntity<Page<ESafeDocumentBean>> findAllOwnedByCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(service.findAllOwnedByCurrentUser(pageable));
    }

    @GetMapping("all")
    public ResponseEntity<Page<ESafeDocumentBean>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAllOwnedByAndSharedWithCurrentUser(pageable));
    }

    @GetMapping("sharedBy")
    public ResponseEntity<Page<ESafeDocumentBean>> findAllSharedByCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(service.findAllSharedByCurrentUser(pageable));
    }

    @GetMapping("sharedWith")
    public ResponseEntity<Page<ESafeDocumentBean>> findAllSharedWithCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(service.findAllSharedWithCurrentUser(pageable));
    }

    @PostMapping("share")
    public ResponseEntity<ESafeDocumentSharingResponse> share(@RequestBody @Valid ESafeDocumentSharingRequest request) {
        return ResponseEntity.ok(eSafeDocumentSharingService.share(request));
    }

    @GetMapping("visible")
    public ResponseEntity<Page<ESafeDocumentBean>> findAllVisibleWithCurrentUser(Pageable pageable) {
        return ResponseEntity.ok(service.findAllVisibleWithCurrentUser(pageable));
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("eSafeDocument") String s) {
        try {
            Gson gson = new Gson();
            ESafeDocumentVM eSafeDocumentVM = gson.fromJson(s, ESafeDocumentVM.class);
            return ResponseEntity.status(HttpStatus.OK) .body( service.save(file, eSafeDocumentVM));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED) .body("Could not upload the file: " + file.getOriginalFilename() + "!");
        }
    }

    @RequestMapping(value = "/modifyFile", method = RequestMethod.PATCH, consumes = {"multipart/form-data"})
    public ResponseEntity<String> modifyFile(@RequestParam("file") MultipartFile file, @RequestParam("eSafeDocument") String s) {
        try {
            Gson gson = new Gson();
            ESafeDocumentVM eSafeDocumentVM = gson.fromJson(s, ESafeDocumentVM.class);
            service.modifyFile(file, eSafeDocumentVM);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Uploaded the file successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Could not upload the file: " + file.getOriginalFilename() + "!");
        }
    }

    @RequestMapping(value = "/newFolder", method = RequestMethod.POST, consumes = {"application/json"})
    public ResponseEntity<Object> newFolder(@RequestBody ESafeDocumentVM eSafeDocumentVM) {
        try {
            return ResponseEntity.status(HttpStatus.OK) .body(service.newFolder(eSafeDocumentVM));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @RequestMapping(value = "/modifyFolder", method = RequestMethod.PATCH, consumes = {"application/json"})
    public ResponseEntity<String> modify(@RequestBody ESafeDocumentVM eSafeDocumentVM) {
        try {
            service.modify(eSafeDocumentVM);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("File/Folder modified successfully: ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body("Could not modify the File/Folder: " + "!");
        }
    }

    @GetMapping("/download/{fileEdmStorld}")
    public ResponseEntity<Resource> getFile(@PathVariable Long fileEdmStorld) throws IOException {
        ByteArrayResource resource = service.download(fileEdmStorld);
        ESafeDocument eSafeDocument = eSafeDocumentRepository.findByEdmStorld(fileEdmStorld);
        return ResponseEntity.ok()
//                .contentType(!eSafeDocument.getIsFolder() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType("application/zip"))
                .contentType(!eSafeDocument.getIsFolder() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (!eSafeDocument.getIsFolder() ? eSafeDocument.getFileName() : eSafeDocument.getFileName() + ".zip") + "\"")
                .body(resource);
    }

    @GetMapping("/listByParent/{parentEdmStorld}")
    public ResponseEntity<Set<ESafeDocumentVM>> listByParent(@PathVariable Long parentEdmStorld) {
        return ResponseEntity.ok().body(service.findESafeDocumentsByParentEdmStoreId(parentEdmStorld));
    }

    @GetMapping("/listByParent/detailed/{parentEdmStorld}")
    public ResponseEntity<Page<ESafeDocumentBean>> listByParentDevelopped(@PathVariable Long parentEdmStorld ,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ) {
        return ResponseEntity.ok().body(service.findDetailedESafeDocumentsByParentEdmStoreId(parentEdmStorld , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"))));
    }

    @GetMapping("/listAllUnderParent/{parentEdmStorld}")
    public ResponseEntity<Set<ESafeDocumentVM>> listAllUnderParent(@PathVariable Long parentEdmStorld) {
        return ResponseEntity.ok().body(service.findAllESafeDocumentsByParentEdmStoreId(parentEdmStorld));
    }

    @DeleteMapping("delete/{edmStorld}")
    public ResponseEntity<Void> deleteByEdmStorld(@PathVariable Long edmStorld) {
        service.deleteESafeDocumentsByEdmStoreId(edmStorld);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("ownedFilter")
    public ResponseEntity<Page<ESafeDocumentBean>> ownedByCurrentUserFilter(@RequestBody EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        return ResponseEntity.ok(service.ownedByCurrentUserFilter(esafeDocumentFilterPayload,pageable));
    }
    @PostMapping("visibleFilter")
    public ResponseEntity<Page<ESafeDocumentBean>> visibleWithCurrentUserFilter(@RequestBody EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        return ResponseEntity.ok(service.visibleWithCurrentUserFilter(esafeDocumentFilterPayload,pageable));
    }

    @PostMapping("sharedWithFilter")
    public ResponseEntity<Page<ESafeDocumentBean>> sharedWithCurrentUserFilter(@RequestBody EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        return ResponseEntity.ok(service.sharedWithCurrentUserFilter(esafeDocumentFilterPayload,pageable));
    }

    @PostMapping("sharedByFilter")
    public ResponseEntity<Page<ESafeDocumentBean>> sharedByCurrentUserFilter(@RequestBody EsafeDocumentFilterPayload esafeDocumentFilterPayload, Pageable pageable) {
        return ResponseEntity.ok(service.sharedWithCurrentUserFilter(esafeDocumentFilterPayload,pageable));
    }

    @RequestMapping(value = "/file/{id}", method = RequestMethod.PATCH, consumes = {"multipart/form-data"})
    public ResponseEntity<Object> updateFile(@RequestParam(value = "file",required = false) MultipartFile file, @RequestParam("eSafeDocument") String s,@PathVariable("id") Long id) {
        try {
            Gson gson = new Gson();
            ESafeDocumentVM eSafeDocumentVM = gson.fromJson(s, ESafeDocumentVM.class);
            return ResponseEntity.status(HttpStatus.OK) .body( service.update(file, eSafeDocumentVM,id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED) .body("Could not upload the file: " + file.getOriginalFilename() + "!");
        }
    }


    @GetMapping("file/{id}")
    public ResponseEntity<ESafeDocumentBean> getFileById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getFileById(id));
    }

}
