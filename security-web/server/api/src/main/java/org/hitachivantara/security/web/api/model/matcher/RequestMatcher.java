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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.api.model.matcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This interface represents an HttpRequest predicate.
 * <p>
 * Matching can be performed based on multiple characteristics of a request.
 */
public interface RequestMatcher extends Predicate<HttpServletRequest> {
  RequestMatcher ALL = r -> true;
  RequestMatcher NONE = r -> false;

  /**
   * Creates an OR request matcher from a list of request matchers.
   *
   * @param requestMatchers A list of request matchers.
   * @return A request matcher which matches if any of the specified request matchers matches.
   */
  @Nonnull
  static RequestMatcher createOr( @Nonnull List<RequestMatcher> requestMatchers ) {
    Objects.requireNonNull( requestMatchers );

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

    return request -> {
      for ( RequestMatcher requestMatcher : filtered ) {
        if ( requestMatcher.test( request ) ) {
          return true;
        }
      }

      return false;
    };
  }
}
