package com.example.societyhub.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Bill {
    // Getters and setters
//    private int id;
    private int sid;
    private int maintenance_contribution;
    private int housing_board_contribution;
    private int property_tax_contribution;
    private int sinking_fund;
    private int reserve_mhada_service_charge;
    private int sub_charge;
    private int fine;
    private int building_dev_fund;
    private int other;
    private String due_date;
}