package com.xq.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CardRcDetailVO {
    private Long id;
    private Long cardId;
    private Long roleId;          // 发起人角色ID
    private String roleName;      // 发起人角色名
    private String roleAvatar;    // 发起人头像（可选）
    private Integer episodeNo;
    private String title;
    private LocalDateTime createdAt;
    private List<DialogueVersionVO> versions;
}