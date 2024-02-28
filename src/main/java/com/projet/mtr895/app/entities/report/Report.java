package com.projet.mtr895.app.entities.report;

import com.projet.mtr895.app.entities.TestCase;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
public class Report {

    private TestCase testCase;
    private InputStream inputStream;
    private String outputType;

}
