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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.hitachivantara.security.web.api.model.cors.CorsConfiguration;
import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@SuppressWarnings( "UnstableApiUsage" )
public class CorsConfigurationSourceAdapter implements org.springframework.web.cors.CorsConfigurationSource {

  @Nonnull
  private final CorsConfiguration configuration;

  // Not using a Map directly to avoid buildup of the cached Spring CorsConfiguration instances.
  // If the source CorsConfiguration would be "refreshed" and started returning different instances of
  // CorsRequestSetConfiguration for the same requests. An OSGi baked configuration can cause this.
  @Nonnull
  private final LoadingCache<CorsRequestSetConfiguration, org.springframework.web.cors.CorsConfiguration> configCache;

  public CorsConfigurationSourceAdapter( @Nonnull CorsConfiguration configuration ) {
    requireNonNull( configuration );
    this.configuration = configuration;

    this.configCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build( new CacheLoader<CorsRequestSetConfiguration, org.springframework.web.cors.CorsConfiguration>() {
        @Override
        public org.springframework.web.cors.CorsConfiguration load( @Nonnull CorsRequestSetConfiguration config ) {
          return computeSpringCorsConfiguration( config );
        }
      } );
  }

  @Override
  public org.springframework.web.cors.CorsConfiguration getCorsConfiguration( @Nonnull HttpServletRequest request ) {
    return configCache.getUnchecked( configuration.getRequestConfiguration( request ) );
  }

  @Nullable
  private static org.springframework.web.cors.CorsConfiguration computeSpringCorsConfiguration(
    @Nonnull CorsRequestSetConfiguration requestConfig ) {

    if ( !requestConfig.isEnabled() ) {
      return null;
    }

    org.springframework.web.cors.CorsConfiguration springCorsConfig =
      new org.springframework.web.cors.CorsConfiguration();

    springCorsConfig.setAllowCredentials( requestConfig.getAllowCredentials() );
    springCorsConfig.setMaxAge( requestConfig.getMaxAge() );
    springCorsConfig.setAllowedOrigins( convertSet( requestConfig.getAllowedOrigins() ) );
    springCorsConfig.setAllowedHeaders( convertSet( requestConfig.getAllowedHeaders() ) );
    springCorsConfig.setAllowedMethods( convertSet( requestConfig.getAllowedMethods() ) );
    springCorsConfig.setExposedHeaders( convertSet( requestConfig.getExposedHeaders() ) );

    return springCorsConfig;
  }

  @Nullable
  private static List<String> convertSet( @Nullable Set<String> set ) {
    return set != null ? new ArrayList<>( set ) : null;
  }
}
