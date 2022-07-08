package ma.itroad.aace.eth.coref.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;

public interface AdminApi<T, ID extends Serializable> {

    @PostMapping
    ResponseEntity<T> add(@RequestBody @Valid T domain);

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteById(@PathVariable("id") ID id);

    @GetMapping
    ResponseEntity<Iterable<T>> getAll();

    @GetMapping("{id}")
    ResponseEntity<T> getOneById(@PathVariable("id") ID id);

    @PutMapping("{id}")
    ResponseEntity<Void> updateById(@PathVariable("id") ID id, @RequestBody @Valid T domain);
}
