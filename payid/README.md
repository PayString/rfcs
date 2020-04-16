# PayID

## Spec

You can find the spec here: https://tools.ietf.org/html/TBD

The source code for the spec is in `src/spec/payid.md`

## Implementations

Known implementations of Crypto Conditions:

* JavaScript: https://github.com/xpring-eng/Xpring-JS
* Java: https://github.com/xpring-eng/Xpring4j
* Swift: https://github.com/xpring-eng/XpringKit

If you would like to update this list, please feel free to open a pull request against this repository.

## Test Vectors
TODO

## Test Vectors Source

The test vectors themselves are generated from example data in the `src/test-vectors/` folder. You should never have to worry about that unless you are adding or editing test vectors.

## Generating Tests
TODO

## Generating the Spec

Uses [kramdown-rfc2629](https://github.com/cabo/kramdown-rfc2629/), [xml2rfc](http://xml2rfc.ietf.org/) and [Grunt](http://gruntjs.com/) with [Grunt kramdown_rfc2629 task](https://github.com/hildjj/grunt-kramdown-rfc2629/)

From root directory of the repo run:

    npm install
    grunt kramdown_rfc2629
   
To watch edits to `payid.md` and auto-generate output when changes are saved run:

    grunt watch
    
