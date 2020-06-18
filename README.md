# PayID

## Specs

You can find the specs here: https://github.com/xpring-eng/rfcs/tree/master/dist/spec

* [The 'payid' URI Scheme](https://github.com/xpring-eng/rfcs/blob/master/dist/spec/payid-uri.txt)
* [PayID Discovery](https://github.com/xpring-eng/rfcs/blob/master/dist/spec/payid-discovery.txt)
* [PayID Protocol](https://github.com/payid-org/rfcs/blob/master/dist/spec/payid-protocol.txt)
* [Verifiable PayID Protocol](https://github.com/payid-org/rfcs/blob/master/dist/spec/verifiable-payid-protocol.txt)

The source code for each spec is in [src/spec](https://github.com/xpring-eng/rfcs/tree/master/src/spec).

## Implementations

Known implementations of PayID and PayID Discovery:

* JavaScript: TBD.
* Java: TBD.
* Swift: TBD.

If you would like to update this list, please feel free to open a pull request against this repository.

## Generating the Spec

Uses [kramdown-rfc2629](https://github.com/cabo/kramdown-rfc2629/), [xml2rfc](http://xml2rfc.ietf.org/) and [Grunt](http://gruntjs.com/) with [Grunt kramdown_rfc2629 task](https://github.com/hildjj/grunt-kramdown-rfc2629/)

From root directory of the repo run:

    npm install
    grunt kramdown_rfc2629

To watch edits to `payid-uri.md` or `payid-discovery.md` and auto-generate output when changes are saved run:

    grunt watch
