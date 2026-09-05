package com.example.coresto.repository;

import com.example.coresto.domain.identity.MemberBranchAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MemberBranchAccessRepository extends JpaRepository<MemberBranchAccess, UUID> {
    List<MemberBranchAccess> findByMemberId(UUID memberId);
    boolean existsByMember_User_IdAndBranch_Id(UUID userId, UUID branchId);
}
