package api.pyapi.RestControllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.pyapi.Entities.GummyEntity;
import api.pyapi.Entities.UserEntity;

import api.pyapi.Repository.GummyRepository;
import api.pyapi.Repository.UserRepository;

@RestController
@RequestMapping("/gummy")
public class GummyRestController {

    private final GummyRepository gummyRepository;
    private final UserRepository userRepository;

    public GummyRestController(GummyRepository gummyRepository, UserRepository userRepository) {
        this.gummyRepository = gummyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMethodName() {
        return new ResponseEntity<>(gummyRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        GummyEntity gummyEntity = gummyRepository.findById(id).orElse(null);
        if (gummyEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        return new ResponseEntity<>(gummyEntity, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> create(@RequestBody GummyEntity gummyEntity, @PathVariable long id) {
        UserEntity entryOperator = userRepository.findById(id).orElse(null);
        if (entryOperator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        gummyEntity.setEntryOperator(entryOperator);
        if (gummyRepository.save(gummyEntity) != null) {
            return ResponseEntity.ok().body("Successfull");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody GummyEntity gummyEntity, @PathVariable long id) {
        UserEntity entryOperator = userRepository.findById(id).orElse(null);
        GummyEntity existingGummy = gummyRepository.findById(gummyEntity.getId()).orElse(null);
        if (existingGummy != null) {
            if (entryOperator == null
                    || entryOperator.getId() != existingGummy.getEntryOperator().getId()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user");
            }
            gummyEntity.setEntryOperator(entryOperator);
            if (gummyRepository.save(gummyEntity) != null) {
                return ResponseEntity.ok().body("Successfull");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (gummyRepository.existsById(id)) {
            gummyRepository.deleteById(id);
            return ResponseEntity.ok().body("Successfull");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }
}
