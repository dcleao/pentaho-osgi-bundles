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

package org.hitachivantara.security.web.api.model.cors;

import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * This interface represents a CORS configuration which applies to a set of (CORS) requests, identified by a request
 * matcher, {@link #getRequestMatcher()}.
 * <p>
 * The actual CORS settings follow the design and semantics of the corresponding Spring API,
 * <a href="https://docs.spring.io/spring-framework/docs/4.3.22.RELEASE/javadoc-api/org/springframework/web/cors/CorsConfiguration.html">CorsConfiguration</a>.
 */
public interface CorsRequestSetConfiguration {

  /**
   * The name of the root CORS request set configuration, {@code "root"}.
   */
  String ROOT_NAME = "root";

  /**
   * Gets the name of the request set.
   * <p>
   * When defined, allows other request sets to specify this one as a parent.
   * <p>
   * Additionally, if two or more request sets have the same name, these are combined, while respecting their definition
   * order.
   * <p>
   * The root request set is named {@link #ROOT_NAME}.
   */
  @Nullable
  String getName();

  /**
   * Gets the name of the parent request set.
   * <p>
   * A root request set has a {@code null} parent request set. Non-root request sets have a parent request, which
   * defaults to the root request set.
   */
  @Nullable
  String getParentName();

  /**
   * Gets the request matcher that identifies requests in the set.
   * <p>
   * The request matcher of a root configuration is typically {@link RequestMatcher#ALL}.
   *
   * @return A request matcher.
   */
  @Nonnull
  RequestMatcher getRequestMatcher();

  /**
   * Gets a value that indicates if CORS requests are enabled for this request set.
   * <p>
   * A configuration is effectively enabled if it and all of its ancestor configurations are enabled.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  boolean isEnabled();

  /**
   * Gets the external origins allowed by the CORS requests of this set.
   * <p>
   * The effective set of allowed origins includes all of the local and inherited origins.
   *
   * @return The set of allowed origins.
   */
  @Nullable
  Set<String> getAllowedOrigins();

  /**
   * Gets the methods allowed by the CORS requests of this set.
   * <p>
   * Example HTTP methods: {@code "GET"}, {@code "POST"}, {@code "PUT"}, etc.
   * <p>
   * An additional value, {@code "*"}, is supported which is equivalent to all methods.
   * <p>
   * The effective set of allowed methods includes all of the local and inherited methods.
   * <p>
   * When the effective set of allowed methods is {@code null}, then the allowed methods are {@code "GET"} and {@code
   * "HEAD"}.
   *
   * @return The allowed request methods.
   */
  @Nullable
  Set<String> getAllowedMethods();

  /**
   * Gets the headers allowed by the CORS requests of this set.
   * <p>
   * The following headers are always allowed and do not need to be specified: {@code Cache-Control}, {@code
   * Content-Language}, {@code Expires}, {@code Last-Modified}, and {@code Pragma}.
   *
   * @return The set of allowed methods.
   */
  @Nullable
  Set<String> getAllowedHeaders();

  /**
   * Gets whether CORS requests are allowed to include credentials.
   * <p>
   * The effective value of this property is the most specific set value. If the effective value would be {@code null},
   * then it defaults to {@code false}.
   *
   * @return {@code true} if allowed; {@code false}, if not allowed; {@code null} if not set.
   */
  @Nullable
  Boolean getAllowCredentials();

  /**
   * Gets the maximum time, in seconds, that the response of a pre-flight request can be cached by the browser.
   * <p>
   * The effective value of this property is the most specific set value. If the effective value would be {@code null},
   * then it defaults to five seconds.
   *
   * @return The maximum age.
   */
  @Nullable
  Long getMaxAge();

  /**
   * Gets the set of headers of an actual response that can be read by the browser scripting environment.
   * <p>
   * For CORS requests with no credentials, the special {@code "*"} header value exposes all response headers.
   * <p>
   * The effective set of exposed response headers includes all of the local and inherited exposed headers.
   * <p>
   * If the effective value is {@code null}, then no response headers are exposed.
   *
   * @return The set of exposed headers.
   */
  @Nullable
  Set<String> getExposedHeaders();
}
