package com.moviecat.repository;

import com.moviecat.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing director data.
 */
@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {
    boolean existsByFullName(String fullName);
    boolean existsByFullNameAndIdNot(String fullName, Long id);
}
