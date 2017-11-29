package io.quasar.demo.biz;

import com.google.inject.ImplementedBy;
import io.quasar.core.annotation.biz.Biz;
import io.quasar.core.helper.BizResult;
import io.quasar.core.helper.PageQuery;
import io.quasar.demo.biz.impl.BlogBizImpl;
import io.quasar.demo.dao.model.BlogDO;

@Biz
@ImplementedBy(BlogBizImpl.class)
public interface BlogBiz {

    BizResult detail(long id);

    BizResult list(PageQuery pageQuery);

    BizResult delete(long id);

    BizResult create(BlogDO blogDO);

    BizResult update(BlogDO blogDO);
}
