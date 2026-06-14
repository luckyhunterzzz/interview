package com.interview.repository;

import com.interview.domain.entity.OutboxEvent;
import com.interview.domain.enums.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusInOrderByCreatedAtAsc(Collection<OutboxEventStatus> statuses, Pageable pageable);
}
