package com.projet.mtr895.app.entities;

import com.projet.mtr895.app.entities.exec.ExecConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class TestCase {

    protected int id;
    protected Request request;
    protected ExecConfig execConfig;
    protected Map<String, Object> execConfigDataMap;

}
