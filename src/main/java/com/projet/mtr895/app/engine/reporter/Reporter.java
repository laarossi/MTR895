package com.projet.mtr895.app.engine.reporter;

import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.report.Report;

import java.io.File;
import java.io.InputStream;

public interface Reporter {
    default void report(TestCase testCase) throws Exception {
        throw new Exception("Report does not support HTML format");
    }

}
