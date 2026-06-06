package api.pyapi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.pyapi.Entities.GummyEntity;

public interface GummyRepository extends JpaRepository<GummyEntity, Long> {
    
}