package com.wl4g.devops.dts.codegen.service;

import com.wl4g.components.data.page.PageModel;
import com.wl4g.devops.dts.codegen.bean.GenProject;

/**
* GenProjectService
*
* @author heweijie
* @Date 2020-09-11
*/
public interface GenProjectService {

    PageModel page(PageModel pm, String name);

    void save(GenProject genProject);

    GenProject detail(Integer id);

    void del(Integer id);
}
