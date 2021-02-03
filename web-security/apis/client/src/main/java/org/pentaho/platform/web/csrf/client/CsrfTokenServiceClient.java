/*!
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

package org.pentaho.platform.web.csrf.client;

import java.net.CookieHandler;
import java.net.URI;

/**
 * The `ICsrfClient` interface represents a CSRF service client.
 *
 * The CSRF token retrieval service is necessarily an HTTP service
 * that works over a stateful HTTP connection.
 * The state of the HTTP connection is accomplished through the use of cookies and,
 * specifically, using a `CookieHandler`.
 */
public interface CsrfTokenServiceClient {

  /**
   * Gets a CSRF token to access a given protected service, if one needs to be used.
   *
   * To maintain the state of the HTTP connection the following is performed:
   * - Cookies which are applicable to {@code tokenServiceUrl} and are present in the cookie handler
   *   are added to the HTTP request.
   * - Cookies which the server sets in the HTTP response are added to the cookie handler.
   *
   * When CSRF protection is disabled, or if the specified {@code protectedServiceUrl} is not CSRF protected,
   * {@code null} may be returned.
   *
   * @param tokenServiceUri - The URI of the CSRF token retrieval service.
   * @param cookieHandler - The cookie handler user to maintain HTTP connection state.
   * @param protectedServiceUri - The URI of the protected service for which a CSRF token is requested.
   *
   * @return The CSRF token to use to access the specified protected service;
   * `null`, if a CSRF token need not be sent.
   */
  CsrfToken getToken( URI tokenServiceUri, CookieHandler cookieHandler, URI protectedServiceUri );
}
