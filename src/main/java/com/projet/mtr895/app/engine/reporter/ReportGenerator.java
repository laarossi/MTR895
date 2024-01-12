package com.projet.mtr895.app.engine.reporter;


public class ReportGenerator {

    private static ReportGenerator instance = null;

    ReportGenerator(){
    }

    public static ReportGenerator getInstance() {
        if(instance == null) instance = new ReportGenerator();
        return instance;
    }
    
}
