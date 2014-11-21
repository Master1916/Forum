/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * This file creation date: 27/08/2004 - 18:22:10
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum;

import java.io.File;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.jforum.exceptions.ForumStartupException;
import net.jforum.repository.BBCodeRepository;
import net.jforum.repository.ModulesRepository;
import net.jforum.repository.Tpl;
import net.jforum.util.I18n;
import net.jforum.util.bbcode.BBCodeHandler;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * @author Rafael Steil
 * @version $Id: JForumBaseServlet.java,v 1.24 2007/09/21 15:54:31 rafaelsteil Exp $
 */
public class JForumBaseServlet extends HttpServlet
{
	private static Logger logger = Logger.getLogger(JForumBaseServlet.class);

	protected boolean debug;

	protected void startApplication()
	{
		try {
			//sql.queries.generic = ${config.dir}/database/generic/generic_queries.sql
			SystemGlobals.loadQueries(SystemGlobals.getValue(ConfigKeys.SQL_QUERIES_GENERIC));
			//sql.queries.driver = ${config.dir}/database/${database.driver.name}/${database.driver.name}.sql
			SystemGlobals.loadQueries(SystemGlobals.getValue(ConfigKeys.SQL_QUERIES_DRIVER));
			//quartz.config = ${config.dir}/quartz-jforum.properties
			String filename = SystemGlobals.getValue(ConfigKeys.QUARTZ_CONFIG);
			
			SystemGlobals.loadAdditionalDefaults(filename);
			//获取登陆需要验证的信息
			ConfigLoader.createLoginAuthenticator();
			//load dao 和设置driver
			ConfigLoader.loadDaoImplementation();
			//监听文件是否配置文件是否改变如改变重新加载
			ConfigLoader.listenForChanges();
			//
			ConfigLoader.startSearchIndexer();
			ConfigLoader.startSummaryJob();
		}
		catch (Exception e) {
			throw new ForumStartupException("Error while starting JForum", e);
		}
	}

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		try {
			//获得项目的根目录
			String appPath = config.getServletContext().getRealPath("");
			debug = "true".equals(config.getInitParameter("development"));
			//加载logger配置文件
			DOMConfigurator.configure(appPath + "/WEB-INF/log4j.xml");

			logger.info("Starting JForum. Debug mode is " + debug);
			//加载配置文件
			ConfigLoader.startSystemglobals(appPath);
			//开启缓存
			ConfigLoader.startCacheEngine();

			
			
			// Configure the template engine
			//configuration是可以缓存模板的 clearTemplateCache()方法可以释放缓存的模板数据
			Configuration templateCfg = new Configuration();
			//配置模板检查更新时间为2秒
			templateCfg.setTemplateUpdateDelay(2);
			//设置数字格式化的样式
			templateCfg.setSetting("number_format", "#");
			//设置共享变量所有的模板都可以使用这个参数
			templateCfg.setSharedVariable("startupTime", new Long(new Date().getTime()));
			// Create the default template loader
			String defaultPath = SystemGlobals.getApplicationPath() + "/templates";
			//文件加载的位置
			FileTemplateLoader defaultLoader = new FileTemplateLoader(new File(defaultPath));
			//获得外部freemarker 模板路径 
			String extraTemplatePath = SystemGlobals.getValue(ConfigKeys.FREEMARKER_EXTRA_TEMPLATE_PATH);
			//如果存在用户之定义的模板则执行下面的if
			if (StringUtils.isNotBlank(extraTemplatePath)) {
				// An extra template path is configured, we need a MultiTemplateLoader
				FileTemplateLoader extraLoader = new FileTemplateLoader(new File(extraTemplatePath));
				//在需要加载多个位置的freemarker的模板时要使用TemplateLoader进行一个整合 并且使用MultiTemplateLoader进行加载
				TemplateLoader[] loaders = new TemplateLoader[] { extraLoader, defaultLoader };
				MultiTemplateLoader multiLoader = new MultiTemplateLoader(loaders);
				templateCfg.setTemplateLoader(multiLoader);
			} 
			else {
				// An extra template path is not configured, we only need the default loader
				templateCfg.setTemplateLoader(defaultLoader);
			}
 
			
		 
			//config.dir = ${application.path}/WEB-INF/config
			//ModulesRepository.getModuleClass(ClassName);
			ModulesRepository.init(SystemGlobals.getValue(ConfigKeys.CONFIG_DIR));

			this.loadConfigStuff();

			if (!this.debug) {
				templateCfg.setTemplateUpdateDelay(3600);
			}
			JForumExecutionContext.setTemplateConfig(templateCfg);
		}
		catch (Exception e) {
			throw new ForumStartupException("Error while starting JForum", e);
		}
	}
	//加载资源
	protected void loadConfigStuff()
	{
		//加载urlPattern.properities资源
		ConfigLoader.loadUrlPatterns();
		//加载语言配置
		I18n.load();
		//加载htm模板
		Tpl.load(SystemGlobals.getValue(ConfigKeys.TEMPLATES_MAPPING));
		// BB Code
		BBCodeRepository.setBBCollection(new BBCodeHandler().parse());
	}
}
