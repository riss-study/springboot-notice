package dev.riss.notice.domain.notice;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByEndAtAfter(@Param("endAt")LocalDateTime endAt, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.views = n.views + :count WHERE n.uid = :noticeUid")
    void incrementViewCount(@Param("noticeUid") Long noticeUid, @Param("count") Long count);
}
