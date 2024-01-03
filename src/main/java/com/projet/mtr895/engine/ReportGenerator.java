package com.projet.mtr895.engine;


public class ReportGenerator {

    private static ReportGenerator instance = null;

    private ReportGenerator(){
    }

    public static ReportGenerator getInstance() {
        if(instance == null) instance = new ReportGenerator();
        return instance;
    }
    
}
