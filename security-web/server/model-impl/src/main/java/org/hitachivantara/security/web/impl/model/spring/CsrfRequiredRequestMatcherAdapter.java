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

package org.hitachivantara.security.web.impl.model.spring;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.requireNonNull;

/**
 * A Spring {@link RequestMatcher} implementation that adapts a given {@link CsrfConfiguration} to a protection required
 * request matcher.
 */
public class CsrfRequiredRequestMatcherAdapter implements RequestMatcher {

  @Nonnull
  private final CsrfConfiguration configuration;

  public CsrfRequiredRequestMatcherAdapter( @Nonnull CsrfConfiguration configuration ) {

    requireNonNull( configuration );

    this.configuration = configuration;
  }

  @Override
  public boolean matches( HttpServletRequest request ) {
    return configuration.isEnabled( request );
  }
}
