package fu.de190484.entity2service.repository;

import fu.de190484.entity2service.entity.Entity2;
import fu.de190484.entity2service.entity.Entity2Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Entity2Repository extends JpaRepository<Entity2, Long> {

    @Query("SELECT e FROM Entity2 e WHERE (:clientName IS NULL OR LOWER(e.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))) AND (:status IS NULL OR e.status = :status)")
    Page<Entity2> search(@Param("clientName") String clientName,
                         @Param("status") Entity2Status status,
                         Pageable pageable);
}
