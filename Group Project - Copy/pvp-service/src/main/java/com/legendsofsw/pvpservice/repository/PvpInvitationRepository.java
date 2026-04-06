package com.legendsofsw.pvpservice.repository;

import com.legendsofsw.pvpservice.model.InvitationStatus;
import com.legendsofsw.pvpservice.model.PvpInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PvpInvitationRepository extends JpaRepository<PvpInvitation, Long> {

    List<PvpInvitation> findByReceiverIdAndStatus(Long receiverId, InvitationStatus status);

    List<PvpInvitation> findBySenderIdAndStatus(Long senderId, InvitationStatus status);
}
