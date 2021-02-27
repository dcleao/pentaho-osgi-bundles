/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.model.spring.xml;

import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

public class WebSecurityModelNamespaceHandler extends NamespaceHandlerSupport {

  @Override
  public void init() {
    registerBeanDefinitionParser( "regex-request-matcher", new RegexRequestMatcherParser() );
    registerBeanDefinitionParser( "or-request-matcher", new OrRequestMatcherParser() );
    registerBeanDefinitionParser( "all-request-matcher", new AllRequestMatcherParser() );
    registerBeanDefinitionParser( "none-request-matcher", new NoneRequestMatcherParser() );
  }

  private static class AllRequestMatcherParser extends AbstractSimpleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(  Element element ) {
      return AllRequestMatcherFactoryBean.class;
    }
  }

  private static class AllRequestMatcherFactoryBean implements FactoryBean<RequestMatcher> {

    @Override
    public RequestMatcher getObject() throws Exception {
      return RequestMatcher.ALL;
    }

    @Override
    public Class<?> getObjectType() {
      return RequestMatcher.class;
    }

    @Override
    public boolean isSingleton() {
      return true;
    }
  }

  private static class NoneRequestMatcherParser extends AbstractSimpleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(  Element element ) {
      return NoneRequestMatcherFactoryBean.class;
    }
  }

  private static class NoneRequestMatcherFactoryBean implements FactoryBean<RequestMatcher> {

    @Override
    public RequestMatcher getObject() throws Exception {
      return RequestMatcher.NONE;
    }

    @Override
    public Class<?> getObjectType() {
      return RequestMatcher.class;
    }

    @Override
    public boolean isSingleton() {
      return true;
    }
  }
}
