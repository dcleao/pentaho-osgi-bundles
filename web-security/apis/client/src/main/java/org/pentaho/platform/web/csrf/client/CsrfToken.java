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

/**
 * The `ICsrfToken` interface represents a CSRF token which needs to be sent
 * in an HTTP request when requesting a CSRF protected HTTP service.
 *
 * The CSRF token can be sent in an HTTP request either in a header or in a parameter,
 * the former method being preferred.
 */
public interface CsrfToken {
  /**
   * Gets the name of the HTTP request header in which the CSRF token value can be specified.
   */
  String getHeader();

  /**
   * Gets the name of the HTTP request parameter in which the CSRF token value can be specified.
   */
  String getParameter();

  /**
   * Gets the value of the CSRF token itself.
   */
  String getToken();
}
