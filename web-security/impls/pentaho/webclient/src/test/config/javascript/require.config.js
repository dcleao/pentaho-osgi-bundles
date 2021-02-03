(function() {

  "use strict";

  /* globals requireCfg, depDir, depWebJars, basePath, baseTest */

  CONTEXT_PATH = "/";

  requireCfg.paths["@pentaho/csrf"] = "/base/target/classes/web";
  requireCfg.packages.push({
    name: "@pentaho/csrf",
    main: "service"
  });
})();
