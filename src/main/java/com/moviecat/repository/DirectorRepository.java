package com.moviecat.repository;

import com.moviecat.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {
    boolean existsByLastNameAndFirstNameAndMiddleName(String lastName, String firstName, String middleName);

    boolean existsByLastNameAndFirstNameAndMiddleNameAndIdNot(
            String lastName, String firstName, String middleName, Long id);
}
