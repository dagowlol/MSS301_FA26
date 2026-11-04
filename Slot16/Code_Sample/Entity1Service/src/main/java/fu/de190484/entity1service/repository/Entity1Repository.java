package fu.de190484.entity1service.repository;

import fu.de190484.entity1service.entity.Entity1;
import fu.de190484.entity1service.entity.Entity1Status;
import fu.de190484.entity1service.entity.Entity1Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface Entity1Repository extends JpaRepository<Entity1, Long> {

    // Kiểm tra trùng mã / unique identifier (code, username, room_number...)
    boolean existsByCode(String code);

    // Kiểm tra trùng mã khi update (bỏ qua entity hiện tại)
    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Entity1> findByCode(String code);

    @Query("SELECT e FROM Entity1 e WHERE (:type IS NULL OR e.type = :type) AND (:status IS NULL OR e.status = :status)")
    Page<Entity1> search(@Param("type") Entity1Type type,
                         @Param("status") Entity1Status status,
                         Pageable pageable);
}
