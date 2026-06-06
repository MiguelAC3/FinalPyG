package api.pyapi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.pyapi.Entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
}
