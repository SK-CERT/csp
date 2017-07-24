package com.instrasoft.csp.ccs.repository;

import com.instrasoft.csp.ccs.domain.postgresql.CspManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CspManagementRepository extends JpaRepository<CspManagement, Long> {

    public List<CspManagement> findByCspId(String cspId);

    List<CspManagement> findByCspIdAndModuleId(String cspId, Long moduleId);

    List<CspManagement> findByModuleIdAndModuleVersionId(Long moduleId, Long moduleVersionId);

    @Transactional
    List<CspManagement> removeByCspId(String cspId);

    @Transactional
    List<CspManagement> removeByCspIdAndModuleId(String cspId, Long moduleId);

}