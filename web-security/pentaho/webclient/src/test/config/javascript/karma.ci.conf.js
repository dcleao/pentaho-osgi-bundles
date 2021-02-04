var baseConfig = require("./karma.conf.js");

module.exports = function(config) {
  baseConfig(config);

  config.set({

    browserNoActivityTimeout: 20000,

    basePath: "${basedir}",

    frameworks: ["jasmine", "requirejs"],

    plugins: [
      "karma-jasmine",
      "karma-requirejs",
      "karma-junit-reporter",
      "karma-coverage",
      "karma-phantomjs-launcher"
    ],

    reporters: ["progress", "junit", "coverage"],

    preprocessors: {
      "${build.outputDirectory}/web/*.js": ["coverage"]
    },

    junitReporter: {
      useBrowserName: false,
      outputFile: "${build.javascriptReportDirectory}/test-results.xml",
      suite: "unit"
    },

    coverageReporter: {
      useBrowserName: false,
      reporters: [
        {
          type: "html",
          dir: "${build.javascriptReportDirectory}/jscoverage/html/"
        },
        {
          type: "cobertura",
          dir: "${build.javascriptReportDirectory}/cobertura/xml/"
        }
      ],
      dir: "${build.javascriptReportDirectory}"
    },

    logLevel: config.LOG_INFO,

    autoWatch: false,

    browsers: ["PhantomJS"],

    singleRun: true
  });
};
