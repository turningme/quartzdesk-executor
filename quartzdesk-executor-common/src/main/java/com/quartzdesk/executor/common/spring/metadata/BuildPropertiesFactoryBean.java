/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.spring.metadata;

import com.quartzdesk.executor.common.io.IOUtils;
import com.quartzdesk.executor.common.text.UTF8Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

/**
 * Spring factory bean that reads the build properties generated by the Maven
 * maven-buildmetadata-plugin plugin. Unless specified otherwise, this factory
 * reads the build properties resource from the {@code META-INF/build.properties}
 * location using the current thread's context class loader.
 * <p>
 * If the build properties resource is not available at the specified location, then
 * this factory throws an exception.
 * </p>
 * <p>
 * Because there can be multiple build properties resources on the classpath, this
 * factory uses the configured {@code moduleName} that is used as a filter to
 * choose the intended build properties resource.
 * </p>
 */
public class BuildPropertiesFactoryBean
    extends AbstractFactoryBean<Map<String, String>>
{
  private static final Logger log = LoggerFactory.getLogger( BuildPropertiesFactoryBean.class );

  private String propertiesPath = "META-INF/build.properties";

  private String moduleName;

  public String getPropertiesPath()
  {
    return propertiesPath;
  }

  public void setPropertiesPath( String propertiesPath )
  {
    this.propertiesPath = propertiesPath;
  }

  public String getModuleName()
  {
    return moduleName;
  }

  @Required
  public void setModuleName( String moduleName )
  {
    this.moduleName = moduleName;
  }

  @Override
  @SuppressWarnings( "rawtypes" )
  public Class<Map> getObjectType()
  {
    return Map.class;
  }

  @Override
  protected Map<String, String> createInstance()
      throws Exception
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    Enumeration<URL> resourceUrls = classLoader.getResources( propertiesPath );
    while ( resourceUrls.hasMoreElements() )
    {
      URL resourceUrl = resourceUrls.nextElement();
      if ( resourceUrl.toString().contains( moduleName ) )
      {
        InputStream ins = null;
        try
        {
          ins = resourceUrl.openStream();
          return new UTF8Map( ins );
        }
        finally
        {
          IOUtils.close( ins );
        }
      }
    }

    throw new IllegalStateException(
        "Build properties resource not found at: " + propertiesPath + ". Used module name: " + moduleName );
  }
}
