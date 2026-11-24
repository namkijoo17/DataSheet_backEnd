package com.datasheet.backend.datasheet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatasheetRepository extends JpaRepository<Datasheet, Long> {

	List<Datasheet> findByActiveTrueOrderByCodeAscRevisionDesc();

	@Query("""
			select distinct d from Datasheet d
			left join fetch d.items i
			left join fetch i.slots
			where d.id = :id
			""")
	Optional<Datasheet> findByIdWithItems(@Param("id") Long id);

	boolean existsByCodeAndRevision(String code, int revision);
}
