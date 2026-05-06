package energy.eddie.tutorial.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface MeterReadingRepository extends JpaRepository<MeterReading, MeterReading.Id> {
    @Query(value = """
            SELECT DISTINCT ON (permission_id) *
            FROM meter_reading
            WHERE user_id = :userId
            ORDER BY permission_id, timestamp DESC;
            """, nativeQuery = true)
    List<MeterReading> findLatestPerPermission(@Param("userId") String userId);
}