package com.projet.mtr895.engine.executor;

import com.projet.mtr895.entities.TestCase;
import com.projet.mtr895.entities.exec.ExecConfig;

import java.io.InputStream;

public interface Executor {
    Executor parse(TestCase testCase);
    default InputStream run(ExecConfig execConfig){
        return null;
    }
}
