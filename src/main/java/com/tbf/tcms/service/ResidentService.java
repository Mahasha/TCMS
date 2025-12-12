package com.tbf.tcms.service;

public interface ResidentService {

    /**
     * Generates a simple Proof of Residence statement for the given resident.
     * If the family's levy is not up to date for the current year, throws an exception.
     */
    String generateProofOfResidence(Long residentId);
}
