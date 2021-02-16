package com.ssafy.sns.domain.notice;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Entity
@NoArgsConstructor
public class NoticeGroup extends Notice{

    @Column(name = "group_id")
    private Long group_id;

    @Builder
    public NoticeGroup(Long group_id) {
        this.group_id = group_id;
    }


}
