package com.be.server.infrastructure.job.identity.model;

import lombok.Data;

import java.util.Date;

@Data
public class IdCardData {
    private String fullName;
    private String idNumber;
    private Date dateOfBirth;
    private String gender;
    private String address;
    private String province;
    private Date dateOfIssue;
    private String placeOfIssue;

}
