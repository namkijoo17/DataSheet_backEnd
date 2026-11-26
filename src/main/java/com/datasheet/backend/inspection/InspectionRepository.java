package com.datasheet.backend.inspection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

	@Query("""
			select distinct i from Inspection i
			join fetch i.datasheet
			left join fetch i.answers
			where i.id = :id and i.userId = :userId
			""")
	Optional<Inspection> findOwnedWithAnswers(@Param("id") Long id, @Param("userId") Long userId);

	@Query("""
			select distinct i from Inspection i
			join fetch i.datasheet
			left join fetch i.answers
			where i.userId = :userId and i.status = :status
			order by i.updatedAt desc
			""")
	List<Inspection> findByUserAndStatus(
			@Param("userId") Long userId,
			@Param("status") InspectionStatus status
	);

	boolean existsByDatasheetId(Long datasheetId);
}
