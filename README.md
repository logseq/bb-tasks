## Description

Library of reusable [babashka](https://github.com/babashka/babashka) tasks
and [bbin](https://github.com/babashka/bbin) CLIs

## Setup

Add a git dependency to your `bb.edn`:

```clojure
:deps
{logseq/bb-tasks
 {:git/url "https://github.com/logseq/bb-tasks"
  :git/sha "<LATEST SHA>"}}
```

Define the tasks to import from this library in `bb.edn` e.g.

```clojure
:tasks
{nbb:watch
 logseq.bb-tasks.nbb.watch/watch}
```

Some tasks require bb pods. Babashka doesn't yet support providing them through
a gitlib dependency. This means that you will need to add the necessary `:pods`
to your `bb.edn`. See `bb.edn` for pods and versions that are known to work.

## Usage

Tasks are described by namespace.

### `logseq.bb-tasks.lint.carve`

#### `logseq.bb-tasks.lint.carve/-main`

Finds unused vars with https://github.com/borkdude/carve. Also provides more
friendly commandline interface as the default config is preserved when
additional options are given.

### `logseq.bb-tasks.lint.datalog`

#### `logseq.bb-tasks.lint.datalog/lint-rules`

Lints given datalog rules for valid parse-ability and unbound variables.

### `logseq.bb-tasks.lint.large-vars`

#### `logseq.bb-tasks.lint.large-vars/-main`

Lints codebases for large vars. Large vars make it difficult for teams to
maintain and understand codebases.

### `logseq.bb-tasks.nbb`

Collection of nbb tasks located
[here](https://github.com/logseq/bb-tasks/tree/main/src/logseq/bb_tasks/nbb) for use with `nbb-logseq`. Most of them are also be usable with `nbb`.

#### `logseq.bb-tasks.nbb.test`

##### `logseq.bb-tasks.nbb.test/load-all-namespaces`

Verify that all namespaces in a directory can be required by nbb-logseq. Useful
for ensuring cljs code continues to be compatible with nbb(-logseq).

#### `logseq.bb-tasks.nbb.cached-db`

Enables bb tasks to call nbb scripts with an up-to-date cached db
of the current graph directory. The underlying bb task manages a cache like the
one Logseq editor builds in `~/.logseq/graphs/`. If a user git commits the
cached db, nbb CI processes also benefit from the speed up.

To set this up in your graph:
- Create a `script/` directory and copy all the files in [this example's script/](https://github.com/logseq/docs/tree/master/script) to it.
- `cd script && yarn install && cd -`
- Copy [this example's bb.edn](https://github.com/logseq/docs/blob/master/bb.edn) to bb.edn
- Change the implementation of `script/query.cljs` to queries specific to your graph. Some notes:
  - `(cached-db/read-db)` is necessary to read the cached db.
  - `query.cljs` is just an example nbb script. _Any_ script works with the cached db.
- Run `bb query properties` to call the nbb script to return properties in your graph.
  - Note the first time takes time as it builds the cache. Subsequent calls are pretty quick.

##### `logseq.bb-tasks.nbb.cached-db/ensure-latest-cached-db`

This task should be used in the `:depends` of a task calling a nbb script. The
task ensures that the transit db is rebuilt if any graph file is detected to be
outdated by their timestamp. Cache building is slow the first time as all files
need to be parsed. Subsequent updates are quick because only files modified
since the last cache are re-parsed.

#### `logseq.bb-tasks.nbb.watch`

These tasks demonstrate that nbb-logseq scripts can run when a graph is saved to
provide useful information to a Logseq user. When these scripts use the graph-parser, these scripts
start to behave like intelligent assistants as they understand the data in your file as well as the Logseq app does.

Tasks in this namespace require installing `nbb-logseq` e.g. `npm install -g
@logseq/nbb-logseq`. For the examples in this ns, there is a one time setup of
`cd examples && yarn install && cd -`.

##### `logseq.bb-tasks.nbb.watch/watch`

Given a graph directory and an nbb script, the nbb script runs when either the
script or a graph file is saved. The run on script save provides a live-editing
experience as different queries can be run without restarting this task.

For an example, run the following:

```
$ bb nbb:watch /path/to/graph examples/analyze_file.cljs
Watching /path/to/graph ...
```

See [this demo
clip](https://www.loom.com/share/20debb49fdd64e77ae83056289750b0f) to see it in
action.

##### `logseq.bb-tasks.nbb.watch/portal-watch`

Given a graph directory and an nbb script,
[portal](https://github.com/djblue/portal) renders the edn produced by the nbb
script when either the script or a graph file is saved.

For an example, run the following:

```
$ bb nbb:portal-watch /path/to/graph examples/print_file_query.cljs
Watching /path/to/graph ...
```

### CLIs

To use the CLIs, first install [bbin](https://github.com/babashka/bbin). Then install
scripts in this repo:

```sh
bbin install https://raw.githubusercontent.com/logseq/bb-tasks/main/src/logseq/bb_tasks/bin/logseq_import.clj && \
bbin install https://raw.githubusercontent.com/logseq/bb-tasks/main/src/logseq/bb_tasks/bin/logseq_export.clj
```

To use these scripts:

```sh
# From any graph directory, export any text files e.g.
$ logseq-export journals/2022_03_22.md pages/Abraham\ Lincoln.md > export.edn

# cd to another graph and then import the export.edn
$ logseq-import ../previous-graph/export.edn
Imported journals/2022_03_22.md
Imported pages/Abraham Lincoln.md
```

## Contributing

Bug reports are welcome. For feature contributions, please discuss
them first as this library primarily serves https://github.com/logseq/logseq.

## License

See LICENSE.md

## Development

Tasks are linted with https://github.com/clj-kondo/clj-kondo using `clojure
-M:clj-kondo --lint src`. Dependencies are kept up to date with
https://github.com/liquidz/antq using `clojure -M:outdated`.

## Additional Links
* https://github.com/logseq/logseq/blob/master/bb.edn uses this library
