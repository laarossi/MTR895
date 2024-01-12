package com.projet.mtr895.app.engine.executor;

import com.projet.mtr895.app.entities.exec.ExecConfig;
import com.projet.mtr895.app.entities.TestCase;

import java.io.IOException;
import java.io.InputStream;

public interface Executor {


    default InputStream run(TestCase testCase) throws IOException {
        return null;
    }

}
