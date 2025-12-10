package com.bamboo.core.dao.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    private String contractId;
    private String startDate;
    private String startTime;
    private String endTime;
    private String mentorId;
    private String status;
    private String userId;
}
