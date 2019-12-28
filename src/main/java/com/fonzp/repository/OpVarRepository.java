package com.fonzp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonzp.model.entity.OpVar;

@Repository
public interface OpVarRepository extends JpaRepository<OpVar, Integer>
{
	@Query("SELECT a FROM OpVar a WHERE a.showForChart = 1")
	public List<OpVar> findAllForChart(); 
}