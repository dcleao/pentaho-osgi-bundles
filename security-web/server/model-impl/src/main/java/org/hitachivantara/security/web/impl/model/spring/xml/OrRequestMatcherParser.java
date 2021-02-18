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

import org.hitachivantara.security.web.impl.model.matcher.OrRequestMatcher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OrRequestMatcherParser extends AbstractBeanDefinitionParser {
  @Override
  protected AbstractBeanDefinition parseInternal( Element element, ParserContext parserContext ) {

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( OrRequestMatcher.class );
    AbstractBeanDefinition beanDef = builder.getRawBeanDefinition();

    BeanDefinitionParserDelegate parserDelegate = parserContext.getDelegate();
    NodeList childNodes = element.getChildNodes();
    int childCount = childNodes.getLength();

    ManagedList<BeanDefinition> childBeanDefs = new ManagedList<>( childCount );

    for ( int i = 0; i < childCount; i++ ) {
      Node node = childNodes.item( i );
      if ( node instanceof Element ) {
        BeanDefinition childBeanDef = parserDelegate.parseCustomElement( (Element) node, beanDef );
        if ( childBeanDef != null ) {
          childBeanDefs.add( childBeanDef );
        }
      }
    }

    builder.addConstructorArgValue( childBeanDefs );

    beanDef.validate();

    return beanDef;
  }
}
