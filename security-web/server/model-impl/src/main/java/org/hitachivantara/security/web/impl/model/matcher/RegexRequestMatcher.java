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
import org.springframework.http.HttpMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexRequestMatcher implements RequestMatcher {
  @Nonnull
  private final Pattern pattern;
  @Nullable
  private final Set<HttpMethod> httpMethods;

  public RegexRequestMatcher( @Nonnull String pattern ) {
    this( pattern, null );
  }

  public RegexRequestMatcher( @Nonnull String pattern, @Nullable Collection<String> methods ) {
    this( pattern, methods, false );
  }

  public RegexRequestMatcher( @Nonnull String pattern, @Nullable Collection<String> methods,
                              boolean isCaseInsensitive ) {

    Objects.requireNonNull( pattern );
    if ( pattern.equals( "" ) ) {
      throw new IllegalArgumentException( "The argument 'pattern' is empty." );
    }

    this.pattern = isCaseInsensitive
      ? Pattern.compile( pattern, Pattern.CASE_INSENSITIVE )
      : Pattern.compile( pattern );

    this.httpMethods = parseHttpMethods( methods );
  }

  @Nullable
  private static Set<HttpMethod> parseHttpMethods( @Nullable Collection<String> methods ) {
    if ( methods == null ) {
      return null;
    }

    Set<HttpMethod> httpMethods = methods.stream()
      .map( HttpMethod::resolve )
      .filter( Objects::nonNull )
      .collect( Collectors.toSet() );

    return httpMethods.isEmpty() ? null : httpMethods;
  }

  @Override
  public boolean test( @Nonnull HttpServletRequest request ) {
    return matchesHttpMethod( request ) && matchesUrl( request );
  }

  private boolean matchesHttpMethod( @Nonnull HttpServletRequest request ) {
    if ( httpMethods != null ) {
      String method = request.getMethod();
      if ( method != null ) {
        // Return false if parse error or not in the methods set.
        HttpMethod httpMethod = HttpMethod.resolve( method );
        return httpMethod != null && httpMethods.contains( httpMethod );
      }
    }

    return true;
  }

  private boolean matchesUrl( @Nonnull HttpServletRequest request ) {
    return pattern.matcher( getMatchUrl( request ) ).matches();
  }

  private static String getMatchUrl( @Nonnull HttpServletRequest request ) {

    String url = request.getServletPath();

    String pathInfo = request.getPathInfo();
    String queryString = request.getQueryString();
    if ( pathInfo == null && queryString == null ) {
      return url;
    }

    StringBuilder sb = new StringBuilder( url );
    if ( pathInfo != null ) {
      sb.append( pathInfo );
    }

    if ( queryString != null ) {
      sb.append( '?' ).append( queryString );
    }

    return sb.toString();
  }
}
