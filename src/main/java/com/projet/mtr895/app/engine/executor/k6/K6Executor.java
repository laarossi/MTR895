package com.projet.mtr895.app.engine.executor.k6;
import com.projet.mtr895.app.engine.executor.Executor;
import com.projet.mtr895.app.entities.TestCase;
import com.projet.mtr895.app.entities.api.APITestCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K6Executor implements Executor {


    public Executor parse(TestCase testCase){
        if(!(testCase instanceof APITestCase apiTestCase)) {
            return null;
        }
        if(apiTestCase.getApiTestType().equals("api.smoke")) return new K6SmokeTestExecutor();
        return null;
    }

}
