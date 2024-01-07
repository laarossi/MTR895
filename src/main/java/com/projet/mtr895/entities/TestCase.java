package com.projet.mtr895.entities;

import com.projet.mtr895.entities.exec.ExecConfig;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class TestCase {

    protected int id;
    protected Request request;
    protected ExecConfig execConfig;

}
