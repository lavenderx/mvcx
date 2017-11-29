package io.quasar.demo.action;

import io.quasar.core.annotation.action.Action;
import io.quasar.core.annotation.action.Req;

/**
 * Created by well on 2017/3/27.
 */
@Action
public class TestAction {

    @Req("test.*")
    public String test(){
        return "test.ftl";
    }

    @Req("abc.*")
    public String testabc(){
        throw new RuntimeException("abc");

    }
}
