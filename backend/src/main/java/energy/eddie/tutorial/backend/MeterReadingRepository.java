package energy.eddie.tutorial.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

interface MeterReadingRepository extends JpaRepository<MeterReading, MeterReading.Id> {
    @Query(value = """
            SELECT DISTINCT ON (permission_id) *
            FROM meter_reading
            WHERE user_id = :userId
            ORDER BY permission_id, timestamp DESC;
            """, nativeQuery = true)
    List<MeterReading> findLatestPerPermission(@Param("userId") String userId);

    @Query(value = """
            WITH
            filtered AS (
              SELECT
                mr.permission_id,
                mr.timestamp,
                mr.quantity
              FROM meter_reading mr
              WHERE mr.user_id = :userId
                AND (CAST(:from AS timestamp) IS NULL OR mr.timestamp >= :from)
                AND (CAST(:to   AS timestamp) IS NULL OR mr.timestamp <  :to)
            ),
            bucketed AS (
              SELECT
                permission_id,
                date_bin(
                  CAST(:interval as interval),
                  (timestamp AT TIME ZONE 'UTC'),
                  TIMESTAMP '1970-01-01 00:00:00'
                ) AS bucket,
                quantity
              FROM filtered
            ),
            aggregated AS (
              SELECT
                permission_id,
                bucket,
                SUM(quantity) AS total
              FROM bucketed
              GROUP BY permission_id, bucket
            ),
            grouped AS (
              SELECT
                permission_id,
                jsonb_build_object(
                  'name', permission_id,
                  'data', jsonb_agg(
                    jsonb_build_array(bucket::text || 'Z', total)
                    ORDER BY bucket
                  )
                ) AS series
              FROM aggregated
              GROUP BY permission_id
            )
            SELECT COALESCE(jsonb_agg(series ORDER BY (series ->> 'name')), '[]'::jsonb)::text AS series
            FROM grouped
            """, nativeQuery = true)
    String findByUserId(
            @Param("userId") String userId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("interval") String interval);
}