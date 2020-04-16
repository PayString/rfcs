module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    watch: {
      scripts: {
        files: ['src/spec/payid-uri.md', 'src/spec/payid-discovery.md'],
        tasks: ['kramdown_rfc2629'],
        options: {
          spawn: false,
        },
      },
    },
    kramdown_rfc2629: {
      options: {
        outputs: ['text', 'html'],
        outputDir: 'dist/spec',
        removeXML: false
      },
      your_target: {
        src: ['src/spec/payid-uri.md', 'src/spec/payid-discovery.md']
      },
    },
  });

  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-kramdown-rfc2629');

};
