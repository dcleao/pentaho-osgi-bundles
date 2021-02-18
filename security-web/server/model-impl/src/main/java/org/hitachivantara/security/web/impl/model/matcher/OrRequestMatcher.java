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

package org.hitachivantara.security.web.impl.model.matcher;

import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OrRequestMatcher implements RequestMatcher {

  @Nonnull
  private final List<RequestMatcher> matchers;

  private OrRequestMatcher( @Nonnull List<RequestMatcher> matchers ) {
    this.matchers = matchers;
  }

  @Override
  public boolean test( HttpServletRequest request ) {
    for ( RequestMatcher requestMatcher : matchers ) {
      if ( requestMatcher.test( request ) ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates an OR request matcher from a list of request matchers.
   *
   * @param requestMatchers A list of request matchers.
   * @return A request matcher which matches if any of the specified request matchers matches.
   */
  @Nonnull
  public static RequestMatcher create( @Nonnull List<RequestMatcher> requestMatchers ) {

    requireNonNull( requestMatchers );

    List<RequestMatcher> filtered = new ArrayList<>();

    for ( RequestMatcher requestMatcher : requestMatchers ) {
      if ( requestMatcher == RequestMatcher.ALL ) {
        return RequestMatcher.ALL;
      }

      if ( requestMatcher != RequestMatcher.NONE ) {
        filtered.add( requestMatcher );
      }
    }

    if ( filtered.isEmpty() ) {
      return RequestMatcher.NONE;
    }

    if ( filtered.size() == 1 ) {
      return filtered.get( 0 );
    }

    return new OrRequestMatcher( filtered );
  }
}
