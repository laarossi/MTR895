package com.projet.mtr895.runtime;

import com.projet.mtr895.api.ApiApplication;

public class ApiRuntime implements RuntimeWrapper{
    @Override
    public void run(String... args) throws Exception {
        ApiApplication apiApplication = new ApiApplication();
        apiApplication.run();
    }
}
