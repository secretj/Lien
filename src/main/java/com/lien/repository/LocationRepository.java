package com.lien.repository;

import com.lien.entity.Location;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    
    @Query("SELECT l FROM Location l WHERE " +
           "(l.isPublic = true OR l.user = :user) " +
           "AND (:category IS NULL OR l.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Location> findByUserOrPublicWithFilters(
        @Param("user") User user,
        @Param("category") LocationCategory category,
        @Param("keyword") String keyword
    );
    
    Optional<Location> findByIdAndUser(Long id, User user);
    
    List<Location> findByUser(User user);
}