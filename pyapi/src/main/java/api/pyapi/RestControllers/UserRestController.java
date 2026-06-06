// package api.pyapi.RestControllers;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import api.pyapi.Entities.UserEntity;
// import api.pyapi.Repository.UserRepository;

// @RestController
// @RequestMapping("/user")
// public class UserRestController {

//     private final UserRepository userRepository;

//     public UserRestController(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//      @GetMapping("/list")
//     public ResponseEntity<?> getMethodName() {
//         return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
//     }

//     @GetMapping("/read/{id}")
//     public ResponseEntity<?> read(@PathVariable Long id) {
//         UserEntity userEntity = userRepository.findById(id).orElse(null);
//         if (userEntity == null) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
//         }
//         return new ResponseEntity<>(userEntity, HttpStatus.OK); 
//     }

//     @PostMapping
//     public ResponseEntity<?> create(@RequestBody UserEntity userEntity) {
//         if (userRepository.save(userEntity) != null) {
//             return ResponseEntity.ok().body("Successfull");
//         }
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
//     }

    
//     @PutMapping
//     public ResponseEntity<?> update(@RequestBody UserEntity userEntity) {
//         if (userRepository.findById(userEntity.getId()).orElse(null) != null && userRepository.save(userEntity) != null) {
//             return ResponseEntity.ok().body("Successfull");    
//         }
//         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
//     }

//     @DeleteMapping("/delete/{id}")
//     public ResponseEntity<?> delete(@PathVariable Long id) {
//         if (userRepository.existsById(id)) {
//             userRepository.deleteById(id);
//             return ResponseEntity.ok().body("Successfull");
//         }
//         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
//     }
// }
