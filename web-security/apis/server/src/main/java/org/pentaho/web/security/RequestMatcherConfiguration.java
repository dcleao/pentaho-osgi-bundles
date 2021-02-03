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

package org.pentaho.web.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

/**
 * This class represents a request matcher that identifies one or more endpoints
 * being used using one or more HTTP methods.
 */
public class RequestMatcherConfiguration {

  @Nonnull
  private final String type;

  @Nonnull
  private final String pattern;

  @Nullable
  private final Collection<String> methods;

  public RequestMatcherConfiguration( @Nonnull String type, @Nonnull String pattern ) {
    this( type, pattern, null );
  }

  public RequestMatcherConfiguration( @Nonnull String type, @Nonnull String pattern,
                                      @Nullable Collection<String> methods ) {
    this.type = requireNonNull( type );
    this.pattern = requireNonNull( pattern );
    this.methods = methods != null ? Collections.unmodifiableCollection( methods ) : null;
  }

  /**
   * Gets the type of request matcher.
   * <p>
   * The only currently supported value is "regex", which performs a case-sensitive regular expression match.
   * <p>
   * The default value is "regex".
   *
   * @return the type of request matcher.
   */
  @Nonnull
  public String getType() {
    return this.type;
  }

  /**
   * Gets the matching request pattern.
   */
  @Nonnull
  public String getPattern() {
    return this.pattern;
  }

  /**
   * Gets the matching request methods.
   * <p>
   * When {@code null}, then all request methods match.
   *
   * @return the matching request methods.
   */
  public Collection<String> getMethods() {
    return this.methods;
  }
}
