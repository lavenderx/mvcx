package ${parentPackageName};

import com.google.inject.ImplementedBy;
import Biz;
import BizResult;
import PageQuery;
import ${parentPackageName}.impl.${doName}BizImpl;
import ${doClassName};

@Biz
@ImplementedBy(${doName}BizImpl.class)
public interface ${doName}Biz {

    BizResult detail(long id);

    BizResult list(PageQuery pageQuery);

    BizResult delete(long id);

    BizResult create(${doName}DO ${doNameLower}DO);

    BizResult update(${doName}DO ${doNameLower}DO);

}
