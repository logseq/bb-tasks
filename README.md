## Description

Library of reusable [babashka](https://github.com/babashka/babashka) tasks

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

## Usage

Tasks are described by namespace.

### `logseq.bb-tasks.nbb.watch`

These tasks demonstrate that nbb-logseq scripts can run when a graph is saved to
provide useful information to a Logseq user. When these scripts use the graph-parser, these scripts
start to behave like intelligent assistants as they understand the data in your file as well as the Logseq app does.

Tasks in this namespace require installing `nbb-logseq` e.g. `npm install -g @logseq/nbb-logseq`.
They also require the fswatcher pod to be specified in bb.edn's `:pods` e.g. `org.babashka/fswatcher {:version "0.0.3"}`. For the examples in this ns, there is a one time setup of `cd examples && yarn install && cd -`.

#### `logseq.bb-tasks.nbb.watch/watch`

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

#### `logseq.bb-tasks.nbb.watch/portal-watch`

Given a graph directory and an nbb script,
[portal](https://github.com/djblue/portal) renders the edn produced by the nbb
script when either the script or a graph file is saved.

For an example, run the following:

```
$ bb nbb:portal-watch /path/to/graph examples/print_file_query.cljs
Watching /path/to/graph ...
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
