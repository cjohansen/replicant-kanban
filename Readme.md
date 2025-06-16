# Kanban with Replicant

This is a demo application that partially implements a Kanban board with
[Replicant](https://replicant.fun). It was written as the driving example of my
2025 [re:Clojure](https://www.reclojure.org) talk about simpler frontend
development with Replicant.

[Watch "UI, Pure and Simple" on YouTube](https://www.youtube.com/watch?v=AGTDfXKGvNI&ab_channel=LondonClojurians).

## Run it

```sh
make tailwind
make shadow
```

This will start Portfolio on
[http://localhost:7070/portfolio.html](http://localhost:7070/portfolio.html)
and the app on [http://localhost:7070/](http://localhost:7070/).

## Data sources

The app uses client-side only sample data by default. To try the server version
of the app, start a Clojure REPL (I use `cider-jack-in` in Emacs) and start the
server from the comment block at the end of
[kanban.server](src/kanban/server.clj).

Load the app from [http://localhost:8088/](http://localhost:8088/). The app
boots from [kanban.dev](dev/kanban/dev.cljs). In this namespace you can disable
the local sample data and instead fetch data from the server.

If you install [dataspex](https://github.com/cjohansen/dataspex) you can browse
the app state from devtools. This will be particularly interesting when running
with the server, as you'll be able to look at the query and command logs.

## Tests

There are some tests. Please note that this app was written specifically to
support my talk, and is incomplete in various ways - it has enough features to
fill my talk with content.

```sh
bin/kaocha
```
