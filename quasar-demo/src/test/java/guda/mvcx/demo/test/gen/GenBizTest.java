package guda.mvcx.demo.test.gen;

import io.quasar.gen.GenBiz;
import io.quasar.gen.GenContext;

import java.io.File;

/**
 * Created by well on 2017/3/24.
 */
public class GenBizTest {

    public static void main(String[] args) throws Exception {
        String baseDir = System.getProperties().get("user.dir") + File.separator + "demo";

        GenContext genContext = new GenContext(baseDir, "io.quasar.demo.dao.model.AbcDescDO", "io.quasar.demo.biz");

        System.out.println(baseDir);
        GenBiz genBiz = new GenBiz(genContext);

        genBiz.gen();
    }
}
