module.exports = function(grunt) {
  // Add new RFC proposals to this list:
  const rfcs = [
    "src/spec/payid-uri.md",
    "src/spec/payid-discovery.md",
    "src/spec/payid-protocol.md",
    "src/spec/verifiable-payid-protocol.md"
  ]

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),

    // The command that generates specs
    kramdown_rfc2629: {
      options: {
        outputs: ["text", "html"],
        outputDir: "dist/spec",
        removeXML: false
      },
      your_target: {
        src: rfcs,
      }
    },

    // The "watch" command which will watch specs for file changes,
    // and automatically regenerate the RFC outputs.
    watch: {
      scripts: {
        files: rfcs,
        tasks: ["kramdown_rfc2629"],
        options: {
          spawn: false
        }
      }
    },

  });

  grunt.loadNpmTasks("grunt-contrib-watch");
  grunt.loadNpmTasks("grunt-kramdown-rfc2629");
};
