package com.fonzp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonzp.model.entity.OpCategory;

@Repository
public interface OpLogRepository extends JpaRepository<OpCategory, Integer>
{
	@Query("SELECT a FROM OpCategory a GROUP BY a.program, a.position")
	public List<OpCategory> findOpCategories(); 
}