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
