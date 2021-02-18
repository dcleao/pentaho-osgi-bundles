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

import org.hitachivantara.security.web.impl.model.matcher.RegexRequestMatcher;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

public class RegexRequestMatcherParser extends AbstractBeanDefinitionParser {
  @Override
  protected AbstractBeanDefinition parseInternal( Element element, ParserContext parserContext ) {

    String pattern = element.getAttribute( "pattern" );
    String methods = element.getAttribute( "methods" );

    // Defaults to false, if not present.
    boolean isCaseInsensitive = Boolean.parseBoolean( element.getAttribute( "insensitive" ) );

    if ( pattern.equals( "" ) ) {
      // throws
      parserContext.getReaderContext().fatal(
        "'pattern' attribute is empty or unspecified.",
        element );
    }

    List<String> methodsList = null;
    if ( !methods.equals( "" ) ) {
      methodsList = Arrays.asList( methods.split( "\\s+" ) );
    }

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( RegexRequestMatcher.class );
    builder.addConstructorArgValue( pattern );
    builder.addConstructorArgValue( methodsList );
    builder.addConstructorArgValue( isCaseInsensitive );

    return builder.getBeanDefinition();
  }
}
