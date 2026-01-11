package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Bill {
    // Getters and setters
//    private int id;
    private Integer sid;
    private Integer maintenance_contribution;
    private Integer housing_board_contribution;
    private Integer property_tax_contribution;
    private Integer sinking_fund;
    private Integer reserve_mhada_service_charge;
    private Integer sub_charge;
    private Integer fine;
    private Integer building_dev_fund;
    private Integer other;
    private String due_date;
}