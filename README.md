# PayID

## Specs

- [You can find the full list of RFCs here](https://github.com/payid-org/rfcs/tree/master/dist/spec)

### List of RFCs

- [PayID Whitepaper](https://payid.org/whitepaper.pdf)
- [The 'payid' URI Scheme](https://github.com/payid-org/rfcs/blob/master/dist/spec/payid-uri.txt)
- [PayID Discovery](https://github.com/payid-org/rfcs/blob/master/dist/spec/payid-discovery.txt)
- [PayID Protocol](https://github.com/payid-org/rfcs/blob/master/dist/spec/payid-protocol.txt)
- [Verifiable PayID Protocol](https://github.com/payid-org/rfcs/blob/master/dist/spec/verifiable-payid-protocol.txt)
-- [Self-Sovereign Verifiable PayID Protocol](https://github.com/payid-org/rfcs/blob/master/dist/spec/self-sov-verifiable-payid-protocol.txt)

The source code for each spec is in [src/spec](https://github.com/payid-org/rfcs/tree/master/src/spec).

## Implementations

Known implementations of PayID and PayID Discovery:

- [TypeScript](https://github.com/payid-org/payid)
- Java: TBD.
- Swift: TBD.

If you would like to update this list, please feel free to open a pull request against this repository.

## Generating the Spec

From the root directory of the repo run:

```sh
    # Install IETF RFC tools
    gem install kramdown-rfc2629
    pip3 install xml2rfc

    # Install PayID RFC dependencies
    npm install

    # Generate the spec
    npm run spec
```

This generates the RFC output files in the `dist` folder using [kramdown-rfc2629](https://github.com/cabo/kramdown-rfc2629/), [xml2rfc](http://xml2rfc.ietf.org/) and [Grunt](http://gruntjs.com/) with the [Grunt kramdown_rfc2629 task](https://github.com/hildjj/grunt-kramdown-rfc2629/)

To watch edits to RFC source files and auto-generate output when changes are saved run `npm run watch`.

## Authoring a new RFC

First, write a Pull Request that adds the markdown file for the spec in the [src/spec](https://github.com/payid-org/rfcs/tree/master/src/spec) folder.

Then, add that file to the [Gruntfile](https://github.com/payid-org/rfcs/tree/master/Gruntfile.js) list of RFCs. That way the spec output can be generated for your proposal.
