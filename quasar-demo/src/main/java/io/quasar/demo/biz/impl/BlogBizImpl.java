package io.quasar.demo.biz.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.quasar.core.helper.BizResult;
import io.quasar.core.helper.PageQuery;
import io.quasar.demo.biz.BlogBiz;
import io.quasar.demo.dao.BlogDAO;
import io.quasar.demo.dao.model.BlogDO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Singleton
public class BlogBizImpl implements BlogBiz {

    @Inject
    private BlogDAO blogDAO;

    @Override
    public BizResult detail(long id) {
        BizResult bizResult = new BizResult();
        try {
            BlogDO blogDO = blogDAO.selectById(id);
            bizResult.data.put("blogDO", blogDO);
            bizResult.success = true;
        } catch (Exception e) {
            log.error("query blog error", e);
        }
        return bizResult;
    }

    @Override
    public BizResult list(PageQuery pageQuery) {
        BizResult bizResult = new BizResult();
        try {
            int totalCount = blogDAO.countForPage(pageQuery);
            pageQuery.setTotalCount(totalCount);
            List<BlogDO> blogList = blogDAO.selectForPage(pageQuery);
            bizResult.data.put("blogList", blogList);
            bizResult.data.put("query", pageQuery);
            bizResult.success = true;
        } catch (Exception e) {
            log.error("view blog list error", e);
        }
        return bizResult;
    }

    @Override
    public BizResult delete(long id) {
        BizResult bizResult = new BizResult();
        try {
            blogDAO.delById(id);
            bizResult.success = true;
        } catch (Exception e) {
            log.error("delete blog error", e);
        }
        return bizResult;
    }

    @Override
    public BizResult create(BlogDO blogDO) {
        BizResult bizResult = new BizResult();
        try {
            long id = blogDAO.insert(blogDO);
            bizResult.data.put("id", id);
            bizResult.success = true;
        } catch (Exception e) {
            log.error("create blog error", e);
        }
        return bizResult;
    }

    @Override
    public BizResult update(BlogDO blogDO) {
        BizResult bizResult = new BizResult();
        try {
            blogDAO.updateByIdSelective(blogDO);
            bizResult.success = true;
        } catch (Exception e) {
            log.error("update blog error", e);
        }
        return bizResult;
    }
}
