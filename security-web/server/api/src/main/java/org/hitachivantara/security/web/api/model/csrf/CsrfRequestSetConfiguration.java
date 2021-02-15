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

package org.hitachivantara.security.web.api.model.csrf;

import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;

/**
 * This interface represents a set of requests which should be protected against CSRF attacks.
 * <p>
 * The requests which belong to the set are identified by the request matcher {@link #getRequestMatcher()}.
 */
public interface CsrfRequestSetConfiguration {

  /**
   * Gets the request matcher that identifies requests in the set.
   *
   * @return A request matcher.
   */
  @Nonnull
  RequestMatcher getRequestMatcher();
}
